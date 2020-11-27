import utils
import os, glob
ROOT_DIR = os.getcwd()

f = open("list", 'r')
target_repos = [repo_str.rstrip("\n") for repo_str in f.readlines()]
all_repos = [
    os.path.basename(repo_dir) for repo_dir in glob.glob("generated_bugs/*")
]
to_removes = list(set(all_repos) - set(target_repos))
print(to_removes)
for repo in to_removes:
    #    utils.execute(f"rm -rf benchmarks/{repo}", dir=ROOT_DIR, verbosity=1)
    utils.execute(f"rm -rf generated_bugs/{repo}", dir=ROOT_DIR, verbosity=1)
