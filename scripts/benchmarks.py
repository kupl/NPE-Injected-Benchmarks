import argparse
from benchmark_classes import *
from dataclasses import asdict, dataclass, field, fields, is_dataclass
from typing import List, Set, Dict, Tuple, Optional
from multiprocessing import Pool
from functools import wraps
import utils
from config import *
from time import sleep

ROOT_DIR = os.getcwd()
LEARNING_DIR = f"{ROOT_DIR}/generated_bugs"
BENCH_DIR = f"{ROOT_DIR}/benchmarks"


def get_original_dir(repo):
    return f"{BENCH_DIR}/{repo}"


def execute_no_fail(cmd: str, dir: str):
    ret = utils.execute(cmd, dir=dir)
    if ret.return_code != 0:
        print(f"{ERROR}: failed to execute {cmd}")
    return ret


def clean(repo):
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


def checkout(repo: str):
    repo_dir = f"{BENCH_DIR}/{repo}"
    while True:
        ret = utils.execute("git checkout -f -- .", repo_dir)
        if ret.return_code == 128:
            print(f"wait for git checkout at {repo_dir}")
            sleep(0.5)
            continue
        else:
            return ret


@dataclass
class Bug:
    repo: str
    bug_id: str
    npe_info: Npe
    build_info: Optional[Build] = None
    test_info: Optional[Test] = None
    patches: List[Patch] = field(default_factory=list)

    @classmethod
    def init_by_bug_dir(cls, repo, bug_dir):
        bug_id = os.path.basename(bug_dir)
        npe_info = Npe.from_json(f"{bug_dir}/npe.json")
        return cls(repo=repo,
                   bug_id=bug_id,
                   npe_info=npe_info,
                   build_info=None,
                   test_info=None)

    def execute_test_all(self):
        original_dir = f"{BENCH_DIR}/{self.repo}"
        return utils.execute(f"mvn test {MVN_OPTION}", dir=original_dir)

    def apply_bug(self):
        original_dir = f"{BENCH_DIR}/{self.repo}"
        buggy_java = f"{LEARNING_DIR}/{self.repo}/bugs/{self.bug_id}/buggy.java"
        checkout(self.repo)
        execute_no_fail(
            f"cp {buggy_java} {original_dir}/{self.npe_info.filepath}",
            original_dir)

    def build(self):
        compile_cmd = f"mvn test-compile {MVN_OPTION}"
        original_dir = f"{BENCH_DIR}/{self.repo}"

        self.apply_bug()
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

        checkout(self.repo)
        original_dir = f"{BENCH_DIR}/{self.repo}"
        ret_fixed_test = self.execute_test_all()
        testcases_fixed = TestCase.from_test_results(original_dir)

        self.apply_bug()
        ret_buggy_test = self.execute_test_all()
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
    def configure(cls, repo, bug_id):
        bug_dir = f"{LEARNING_DIR}/{repo}/bugs/{bug_id}"
        bug = cls.init_by_bug_dir(repo, bug_dir)
        bug.build()
        bug.find_test()
        return bug

    def compile(self):
        original_dir = f"{BENCH_DIR}/{self.repo}"
        compile_cmd = f"mvn test-compile {MVN_OPTION}"
        return utils.execute(compile_cmd, dir=original_dir)

    def test(self, verbosity=0, env=os.environ):
        original_dir = f"{BENCH_DIR}/{self.repo}"
        test_cmd = f'mvn test -DfailIfNoTests=false {MVN_OPTION}'
        for testcase in self.test_info.testcases:
            test_cmd = test_cmd + f" -Dtest={testcase.classname}#{testcase.method}"

        self.test_info.test_command = test_cmd
        return utils.execute(test_cmd, dir=dir, env=env, verbosity=verbosity)

    def modify_npe(self):
        data_dir = f"{LEARNING_DIR}/{self.repo}/bugs/{self.bug_id}"
        lines = open(f"{data_dir}/buggy.java", 'r')
        i = 1
        line_number = None
        for line in lines:
            if "NPEX_NULL_EXP" in line:
                npe_line_number = i + 1
                npe_json = utils.read_json_from_file(f"{data_dir}/npe.json")
                npe_json["line"] = npe_line_number
                utils.save_dict_to_jsonfile(f"{data_dir}/npe.json", npe_json)
                return True
            i = i + 1
        return False

    def generate_patches(self):
        original_dir = f"{BENCH_DIR}/{self.repo}"
        data_dir = f"{LEARNING_DIR}/{self.repo}/bugs/{self.bug_id}"
        ### Pre condition ###
        if self.build_info == None or self.build_info.compiled is False:
            self.patches = []
            return

        if self.test_info == None or self.test_info.testcases == []:
            self.patches = []
            return

        if os.path.isfile(f"{data_dir}/buggy.java") is False:
            print(f"{WARNING}: {self.bug_id} has no buggy.java")
            return

        if os.path.isdir(f"{original_dir}/patches"):
            execute_no_fail(f"rm -rf {original_dir}/patches", dir)

        if os.path.isdir(f"{data_dir}/patches"):
            execute_no_fail(f"rm -rf {data_dir}/patches", dir)

        ### TODO: remove it ###
        if self.modify_npe() is False:
            print(f"{FAIL}: failed to convert npe")
            return

        ### Generate patches ###
        self.apply_bug()
        utils.execute(
            f"java -cp {SYNTHESIZER} npex.synthesizer.Main -patch {original_dir} {data_dir}/npe.json",
            dir=original_dir)

        if os.path.isdir(f"{original_dir}/patches") is False:
            print(f"{SERIOUS}: no patches are generated for {self.bug_id}")
            return

        execute_no_fail(f"mv {original_dir}/patches {data_dir}/patches",
                        ROOT_DIR)
        patch_dirs = glob.glob(f"{data_dir}/patches/*")
        for patch_dir in patch_dirs:
            patch_id = os.path.basename(patch_dir)

            if os.path.isfile(f"{patch_dir}/patch.json") is False:
                execute_no_fail(f"rm -rf {patch_dir}", ROOT_DIR)
                print(f"{ERROR} {self.bug_id}-{patch_id} NOT IMPLEMENTED")
                continue

            original_filepath = utils.read_json_from_file(
                f"{patch_dir}/patch.json")["original_filepath"]
            patch = Patch(patch_id=patch_id,
                          patch_dir=patch_dir,
                          original_filepath=original_filepath)

            ## collect only compilable patches ##
            self.apply_patch(patch)
            if self.compile().return_code == 0:
                self.patches.append(patch)

        ### Print ###
        if self.patches != []:
            print(
                f"{PROGRESS}: {len(self.patches)} patches are generated for {self.bug_id}"
            )
        else:
            print(f"{FAIL}: no patches are generated for {self.bug_id}")

    def apply_patch(self, patch):
        original_dir = f"{BENCH_DIR}/{self.repo}"
        checkout(self.repo)
        execute_no_fail(
            f"cp \"{patch.patch_dir}/patch.java\" {original_dir}/{patch.original_filepath}",
            original_dir)

    def validate_patches(self):
        if self.patches == []:
            return

        print(f"{PROGRESS}: validating patches of {self.bug_id}")
        for patch in self.patches:
            if patch.compiled is False or patch.pass_testcase is not None:
                continue

            self.apply_patch(patch)
            patch.compiled = self.compile().return_code == 0
            if patch.compiled and self.test_info.testcases != []:
                patch.pass_testcase = self.test().return_code == 0

    def validate_first_patch(self):
        if self.patches == []:
            return

        patch = self.patches[0]
        print(
            f"{PROGRESS}: validating patch {patch.patch_id} of {self.bug_id}")
        self.apply_patch(patch)
        patch.compiled = self.compile().return_code == 0
        if patch.compiled and self.test_info.testcases != []:
            patch.pass_testcase = self.test().return_code == 0

        if patch.compiled is False:
            print(f"{FAIL}: failed to compile patch {patch.patch_id}")
        elif patch.pass_testcase:
            print(
                f"{SUCCESS}: {patch.patch_id} succeed to pass testcase for {self.bug_id}"
            )
        else:
            print(f"{FAIL}: failed to pass testcase {patch.patch.id}")


@dataclass
class Repo:
    repo: str
    bugs: List[Bug]
    repository_info: Optional[Repository] = None

    def to_json(self):
        utils.save_dict_to_jsonfile(f"{LEARNING_DIR}/{self.repo}/bugs.json",
                                    asdict(self))

    @classmethod
    def from_json(cls, jsonfile):
        return from_dict(cls, utils.read_json_from_file(jsonfile))

    def configure_bug(self, bug_id):
        self.bugs.append(Bug.configure(self.repo, bug_id))
        self.to_json()

    @classmethod
    def configure(cls, repo):
        bug_ids = [
            os.path.basename(dir)
            for dir in glob.glob(f"{LEARNING_DIR}/{repo}/bugs/*")
            if os.path.isfile(f"{dir}/npe.json")
        ]
        if os.path.isfile(f"{LEARNING_DIR}/{repo}/bugs.json"):
            repo_data = cls.from_json(f"{LEARNING_DIR}/{repo}/bugs.json")
            bug_ids_done = [bug.bug_id for bug in repo_data.bugs]
            bug_ids = list(set(bug_ids) - set(bug_ids_done))
        else:
            repo_data = cls(repo=repo, bugs=[], repository_info=None)

        for bug_id in bug_ids:
            repo_data.configure_bug(bug_id)
        return repo_data

    @classmethod
    def generate_and_validate_patches(cls, repo):
        repo_data = cls.from_json(f"{LEARNING_DIR}/{repo}/bugs.json")
        for bug in repo_data.bugs:
            bug.generate_patches()
            bug.validate_first_patch()
            repo_data.to_json()


def generate_bugs(repo):
    original_dir = get_original_dir(repo)
    spoon_cmd = f"java -cp {SYNTHESIZER} npex.synthesizer.Main -extract {original_dir}/ {LEARNING_DIR}"
    print(f"{PROGRESS}: extracting buggy java for {repo}")
    ret_spoon = utils.execute(spoon_cmd, dir=ROOT_DIR)


def to_metadata(repos):
    results = {}
    for repo in repos:
        if os.path.isfile(f"{LEARNING_DIR}/{repo}/bugs.json") is False:
            continue
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
    parser.add_argument("--generate_bugs",
                        action='store_true',
                        default=False,
                        help="generate_bugs")
    parser.add_argument("--build_and_test",
                        action='store_true',
                        default=False,
                        help="build_and_test")
    parser.add_argument("--metadata",
                        action='store_true',
                        default=False,
                        help="get metadata")
    parser.add_argument("--generate_and_validate_patches",
                        action='store_true',
                        default=False,
                        help="generate and validate patches")
    parser.add_argument("--n_cpus", default=20, help="number of cpus")
    args = parser.parse_args()

    repos = [
        os.path.basename(repo_dir) for repo_dir in glob.glob("benchmarks/*")
    ]
    # repos = [repo for repo in utils.read_json_from_file("metadata.json")]
    p = Pool(args.n_cpus)
    if args.generate_bugs:
        p.map(generate_bugs, repos)

    if args.build_and_test:
        p.map(Repo.configure, repos)

    if args.metadata:
        to_metadata(repos)

    if args.generate_and_validate_patches:
        repos = [
            repo for repo in repos
            if os.path.isfile(f"{LEARNING_DIR}/{repo}/bugs.json")
        ]
        p.map(Repo.generate_and_validate_patches, repos)

    p.close()
