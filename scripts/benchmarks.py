import argparse
from benchmark_classes import *
from typing import List, Set, Dict, Tuple, Optional
from multiprocessing import Pool
from functools import wraps
import utils
from config import *

ROOT_DIR = os.getcwd()
LEARNING_DIR = f"{ROOT_DIR}/generated_bugs"
BENCH_DIR = f"{ROOT_DIR}/benchmarks"

def get_original_dir(repo):
    return f"{BENCH_DIR}/{repo}"

def execute_no_fail (cmd, dir):
    ret = utils.execute(cmd, dir=dir)
    if ret.return_code != 0: 
        print(f"{ERROR}: failed to execute {cmd}")
    return ret

def clean (repo):
    repo_dir = f"{BENCH_DIR}/{repo}"
    execute_no_fail(f"mvn clean {MVN_OPTION}", repo_dir)
    if os.path.isdir(f"{repo_dir}/.git"):
        execute_no_fail(f"rm -rf .git", repo_dir)
    if os.path.isdir(f"{repo_dir}/infer-out"):
        execute_no_fail(f"rm -rf {repo_dir}/infer-out", repo_dir)
    # execute_no_fail(f"rm -rf .git", repo_dir)
    # execute_no_fail(f"git checkout -f HEAD", repo_dir)
    # execute_no_fail(f"git clean -df", repo_dir)
    # execute_no_fail(f"rm -rf .git", repo_dir)

@dataclass
class Bug:
    repo: str
    bug_id: str
    npe_info: Npe
    build_info: Optional[Build] = None
    test_info: Optional[Test] = None
    # patch_results: List[Patch] = field(default_factory=list)

    @classmethod
    def init_by_bug_dir(cls, repo, bug_dir):
        bug_id = os.path.basename(bug_dir)
        npe_info = Npe.from_json(f"{bug_dir}/npe.json")
        return cls(repo=repo,
                   bug_id=bug_id,
                   npe_info=npe_info,
                   build_info=None,
                   test_info=None)

    def execute_single_test(self, dir, verbosity=0, env=os.environ):
        test_cmd = f'mvn test -DfailIfNoTests=false {MVN_OPTION}' + f" -Dtest={self.test_info.testcases[0].classname}#{self.test_info.testcases[0].method}"
        env = utils.set_java_version(java_version=self.build_info.java_version)

        self.test_info.test_command = test_cmd
        return utils.execute(test_cmd, dir=dir, env=env, verbosity=verbosity)

    def execute_test_all(self, original_dir):
        return utils.execute(f"mvn test {MVN_OPTION}", dir=original_dir)



    def build(self):
        compile_cmd = f"mvn test-compile {MVN_OPTION}"
        original_dir = f"{BENCH_DIR}/{self.repo}"
        ret_compile = utils.execute(compile_cmd, dir=original_dir)
        if ret_compile.return_code == 0:
            self.build_info = Build(compiled=True,
                                    build_command=compile_cmd,
                                    java_version=8,
                                    time=ret_compile.time)
            print(f"{SUCCESS}: successfully compiled {self.bug_id}")
        else:
            print(f"{FAIL}: failed to compile {self.bug_id}")
            self.build_info = Build(compiled=False,
                                    error_message=utils.parse_error(
                                        ret_compile.stdout))

    def find_test(self):
        if self.build_info.compiled is False:
            # print(f"{WARNING}: {self.bug_id} is not compiled")
            return

        test_cmd = f"mvn test {MVN_OPTION}"
        original_dir = f"{BENCH_DIR}/{self.repo}"
        buggy_java = f"{LEARNING_DIR}/{self.repo}/bugs/{self.bug_id}/buggy.java"

        execute_no_fail(f"git checkout -- {original_dir}", original_dir)
        ret_fixed_test = self.execute_test_all(original_dir)
        testcases_fixed = TestCase.from_test_results(original_dir)

        execute_no_fail(f"cp {buggy_java} {original_dir}/{self.npe_info.filepath}", original_dir)
        ret_buggy_test = self.execute_test_all(original_dir)
        testcases_buggy = TestCase.from_test_results(original_dir)

        testcases = list(set(testcases_buggy) - set(testcases_fixed))

        if testcases != []:
            print(f"{SUCCESS}: found validating testcases for {self.bug_id}")
        else:
            print(
                f"{FAIL}: failed to find meaningful testcases for {self.bug_id}"
            )

        self.test_info = Test(test_command=test_cmd,
                              fail_buggy=ret_buggy_test.return_code == 1,
                              pass_fixed=ret_fixed_test.return_code == 0,
                              testcases=testcases)
        
    @classmethod
    def configure (cls, repo, bug_id):
      bug_dir = f"{LEARNING_DIR}/{repo}/bugs/{bug_id}"
      bug = cls.init_by_bug_dir(repo, bug_dir)
      bug.build()
      bug.find_test()
      return bug

@dataclass
class Repo:
    bugs: List[Bug]
    repository_info: Optional[Repository] = None

    def to_json(self):
        utils.save_dict_to_jsonfile(f"{LEARNING_DIR}/{self.repo}/bugs.json", asdict(self))
    
    @classmethod
    def from_json(cls, jsonfile):
        return from_dict(cls, utils.read_json_from_file(jsonfile))

    @classmethod
    def configure(cls, repo):
        bug_ids = [
            os.path.basename(dir) for dir in glob.glob(f"{LEARNING_DIR}/{repo}/bugs/*")
            if os.path.isfile(f"{dir}/npe.json")
        ]
        if os.path.isfile(f"{LEARNING_DIR}/{repo}/bugs.json"):
          repo_data = cls.from_json(f"{LEARNING_DIR}/{repo}/bugs.json")
          bug_ids_done = [bug.bug_id for bug in repo_data.bugs]
          bug_ids_new = list(set(bug_ids) - set(bug_ids_done))
          bugs_new = [Bug.configure(repo, bug_id) for bug_id in bug_ids_new]
          bugs = repo_data.bugs + bugs_new
        else:
          bugs = [Bug.configure(repo, bug_id) for bug_id in bug_ids]
          
        repo_data = cls(bugs=bugs, repository_info=None)
        repo_data.to_json()
        return repo_data

def generate_bugs(repo):
    original_dir = get_original_dir(repo)
    spoon_cmd = f"java -cp {SYNTHESIZER} npex.synthesizer.Main -extract {original_dir}/ {LEARNING_DIR}"
    print(f"{PROGRESS}: extracting buggy java for {repo}")
    ret_spoon = utils.execute(spoon_cmd, dir=ROOT_DIR)
 
def to_metadata (repos):
  results = {}
  for repo in repos:
    results[repo] = []
    repo_data = Repo.from_json(f"{LEARNING_DIR}/{repo}/bugs.json")
    for bug in repo_data.bugs:
      # if bug.test_info and any([MSG_NPE in testcase.exn_type for testcase in bug.test_info.testcases]):
      if bug.test_info and bug.test_info.testcases != []:
          results[repo].append(bug.bug_id)
    if len(results[repo]) < 1:
      del results[repo]
  utils.save_dict_to_jsonfile("metadata.json", results)



if __name__ == "__main__":
  parser = argparse.ArgumentParser()
  parser.add_argument("--generate_bugs", action='store_true', default=False, help="generate_bugs")
  parser.add_argument("--build_and_test", action='store_true', default=False, help="build_and_test")
  parser.add_argument("--metadata", action='store_true', default=False, help="get metadata")
  parser.add_argument("--n_cpus", default=20, help="number of cpus")
  args = parser.parse_args()
  
  repos = [repo for repo in utils.read_json_from_file("backup.json")]
  p = Pool(args.n_cpus)
  if args.generate_bugs:
    p.map(generate_bugs, repos)
    
  if args.build_and_test:
    p.map(Repo.configure, repos)
    
  if args.metadata:
    to_metadata(repos)

  for repo in repos:
      clean(repo)
 
  p.close() 
