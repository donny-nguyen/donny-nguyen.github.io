# Manage Username and Email

**1. Setting Global Username and Email:**

* **Set globally:**
   ```bash
   git config --global user.name "Your Name"
   git config --global user.email "your.email@example.com"
   ```
* **Check global settings:**
   ```bash
   git config --global user.name
   git config --global user.email
   ```

**2. Setting Username and Email for a Specific Repository:**

* **Set for a repository:**
   ```bash
   git config user.name "Your Name"
   git config user.email "your.email@example.com"
   ```
* **Check repository settings:**
   ```bash
   git config user.name
   git config user.email
   ```

**3. Editing Configuration Files Directly:**

* **Edit global configuration file:**
   ```bash
   nano ~/.gitconfig
   ```
* **Edit repository configuration file:**
   ```bash
   nano .git/config
   ```

**Note:**

* The `--global` option sets the username and email for all repositories on our system.
* Without `--global`, the settings are specific to the current repository.
* We can use any text editor (like `nano`, `vim`, or `gedit`) to edit the configuration files.

**Additional Tips:**

* If we need to change our username or email frequently, consider using a Git alias to simplify the commands.
* For more advanced configuration options, refer to the Git documentation: [https://git-scm.com/docs/git-config](https://git-scm.com/docs/git-config)
