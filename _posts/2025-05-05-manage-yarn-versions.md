# Manage Yarn Versions

There are several ways to manage the version of Yarn you are using, both globally and within specific projects. Here's a breakdown of the common methods:

### 1. Using `yarn set version` (Recommended for project-specific versions)

This is the preferred method for ensuring consistency across your project team. It allows you to specify the exact Yarn version that should be used within a project.

```bash
yarn set version <version>
```

Replace `<version>` with the desired Yarn version. Here are some examples:

* **Latest stable Yarn (Berry, >= 2.0.0):**
    ```bash
    yarn set version latest
    # or
    yarn set version berry
    # or
    yarn set version stable
    ```
* **Latest canary Yarn (release candidate of Berry):**
    ```bash
    yarn set version canary
    ```
* **Latest classic Yarn ( ^0.x || ^1.x ):**
    ```bash
    yarn set version classic
    ```
* **Specific Yarn version:**
    ```bash
    yarn set version 3.6.4
    yarn set version 2.0.0-rc.30
    yarn set version 1.22.1
    ```
* **From a local file:**
    ```bash
    yarn set version ./path/to/yarn.cjs
    ```
* **From a URL:**
    ```bash
    yarn set version https://repo.yarnpkg.com/3.1.0/packages/yarnpkg-cli/bin/yarn.js
    ```

When you run this command, Yarn will:

* Update the `packageManager` field in your `package.json` file.
* Download the specified Yarn version into your project's `.yarn/releases` folder.
* Update the `.yarnrc.yml` file to point to this specific version.

This ensures that anyone working on the project will use the exact same Yarn version, regardless of their global installation.

### 2. Using Corepack (Requires Node.js >= 14.19.0)

Corepack is a tool included with Node.js that allows you to manage package manager versions (npm, pnpm, and Yarn) on a per-project basis, based on the `packageManager` field in `package.json`.

1.  **Enable Corepack globally (if you haven't already):**
    ```bash
    corepack enable
    ```

2.  **In your project's `package.json`, add or modify the `packageManager` field:**
    ```json
    {
      "name": "my-project",
      "version": "1.0.0",
      "packageManager": "yarn@3.6.4"
    }
    ```
    Replace `"yarn@3.6.4"` with the desired Yarn version.

Now, when anyone runs a Yarn command within that project directory, Corepack will automatically ensure that the specified version of Yarn is used. If that version isn't already installed, Corepack will download and use it.

### 3. Global Installation (Less recommended for project consistency)

You can install or update Yarn globally on your system. However, this doesn't guarantee that all your projects will use the same version.

* **Using npm:**
    ```bash
    npm install --global yarn@<version>
    ```
    Replace `<version>` with the desired version (e.g., `yarn@1.22.19` or just `yarn@latest`).

* **Using the installation script:**
    ```bash
    curl -o- -L https://yarnpkg.com/install.sh | bash -s -- --version <version>
    ```
    Again, replace `<version>` with the specific version you want.

* **Using Homebrew (macOS):**
    ```bash
    brew upgrade yarn # To get the latest version
    brew install yarn@<version> # To install a specific version (you might need to unlink the current version first: brew unlink yarn)
    brew switch yarn <version> # To switch between installed versions (note: brew switch is deprecated in newer Homebrew versions)
    ```

### 4. Using Yarn Version Manager (yvm) - Deprecated

`yvm` was a tool to manage multiple Yarn versions, similar to `nvm` for Node.js. However, it is now deprecated in favor of Corepack. While you might still find it in older tutorials, it's recommended to use Corepack for managing Yarn versions.

### Checking Your Yarn Version

You can always check the currently active Yarn version by running:

```bash
yarn --version
# or
yarn -v
```

**In summary, for managing Yarn versions effectively, especially for ensuring consistency within projects, the `yarn set version` command or using Corepack are the recommended approaches.** Global installations can be useful for your personal development environment but don't enforce version consistency across projects.