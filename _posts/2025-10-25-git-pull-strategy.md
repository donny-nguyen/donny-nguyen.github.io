# Git Pull Strategy

When trying to pull changes from a remote repository, you might encounter a situation where your local branch has diverged from the remote branch. In such cases, Git requires you to specify how to reconcile these differences:

```
> git pull --tags origin main
From github.com:donny-nguyen/donny-nguyen.github.io
 * branch            main       -> FETCH_HEAD
hint: You have divergent branches and need to specify how to reconcile them.
hint: You can do so by running one of the following commands sometime before
hint: your next pull:
hint: 
hint:   git config pull.rebase false  # merge
hint:   git config pull.rebase true   # rebase
hint:   git config pull.ff only       # fast-forward only
hint: 
hint: You can replace "git config" with "git config --global" to set a default
hint: preference for all repositories. You can also pass --rebase, --no-rebase,
hint: or --ff-only on the command line to override the configured default per
hint: invocation.
fatal: Need to specify how to reconcile divergent branches.
```

To resolve this, you have three main options:

1. **Merge (default behavior)**:

   This option creates a merge commit that combines the changes from both branches.
   ```bash
   git config pull.rebase false
   ```
   Or for a single pull:
   ```bash
   git pull --no-rebase
   ```

2. **Rebase**:

   This option re-applies your local changes on top of the fetched changes, creating a linear history.
   ```bash
   git config pull.rebase true
   ```
   Or for a single pull:
   ```bash
   git pull --rebase
   ```

3. **Fast-Forward Only**:

   This option only allows the pull to succeed if it can be fast-forwarded, meaning there are no divergent changes.
   ```bash
   git config pull.ff only
   ```
   Or for a single pull:
   ```bash
   git pull --ff-only
   ``` 

You can set your preferred strategy globally by adding the `--global` flag to the `git config` command. Choose the strategy that best fits your workflow and team practices.