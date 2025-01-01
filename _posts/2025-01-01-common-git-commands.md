# Common Git Commands

Git is an essential tool for version control in software development. Here are some common Git commands that developers frequently use:

## Basic Commands

**Initializing and Cloning**
- `git init`: Initialize a new Git repository
- `git clone <repository-url>`: Create a local copy of a remote repository[1][2]

**Staging and Committing**
- `git add <file-name>`: Add a file to the staging area
- `git add -A`: Add all new and changed files to the staging area
- `git commit -m "<commit message>"`: Commit changes with a descriptive message[1][2]

**Checking Status and History**
- `git status`: Check the current status of your repository
- `git log`: View commit history
- `git log --oneline`: View commit history in a condensed format[1][2]

## Branch Management

**Creating and Switching Branches**
- `git branch`: List all local branches
- `git branch <branch-name>`: Create a new branch
- `git checkout <branch-name>`: Switch to a specific branch
- `git checkout -b <branch-name>`: Create a new branch and switch to it[1][2]

**Merging and Deleting Branches**
- `git merge <branch-name>`: Merge a branch into the active branch
- `git branch -d <branch-name>`: Delete a local branch
- `git push origin --delete <branch-name>`: Delete a remote branch[1][2]

## Remote Repository Operations

**Pushing and Pulling**
- `git push origin <branch-name>`: Push local changes to a remote repository
- `git pull`: Update local repository with changes from remote
- `git fetch`: Retrieve changes from remote without merging[1][2][3]

**Managing Remotes**
- `git remote add origin <repository-url>`: Add a remote repository
- `git remote -v`: View remote repositories[1][2]

## Advanced Commands

**Stashing Changes**
- `git stash`: Temporarily store uncommitted changes
- `git stash pop`: Apply the most recently stashed changes[1][2]

**Reviewing Changes**
- `git diff <source-branch> <target-branch>`: Preview changes before merging
- `git blame <file-name>`: Show who last modified each line of a file[1][3]

These commands form the foundation of Git usage and can help developers manage their codebase effectively. As you become more comfortable with these, you can explore more advanced Git features to enhance your workflow.