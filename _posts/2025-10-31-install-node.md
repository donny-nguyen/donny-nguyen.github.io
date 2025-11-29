# Install Node.js on Ubuntu

**To install Node.js on Ubuntu, the most flexible and recommended method is using NVM (Node Version Manager). This lets you easily switch between Node.js versions.**

Here’s a breakdown of the three main installation methods:

---

### 🛠️ Method 1: Using NVM (Node Version Manager) — *Recommended*

This method is ideal if you want to manage multiple Node.js versions or avoid system-wide changes.

1. **Install NVM**:
   ```bash
   curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
   ```
   Then restart your terminal or run:
   ```bash
   source ~/.nvm/nvm.sh
   ```

2. **Install Node.js via NVM**:
   ```bash
   nvm install node        # Latest version
   nvm install 20          # Specific version (e.g., v20)
   nvm use 20              # Switch to version 20
   ```

3. **Verify installation**:
   ```bash
   node -v
   npm -v
   ```

---

### 📦 Method 2: Using Ubuntu’s APT Package Manager

This is the simplest method but may not give you the latest version.

1. **Update package index**:
   ```bash
   sudo apt update
   ```

2. **Install Node.js and npm**:
   ```bash
   sudo apt install nodejs npm
   ```

3. **Check versions**:
   ```bash
   node -v
   npm -v
   ```

---

### 📥 Method 3: Using NodeSource PPA (for specific versions)

This method is good for installing a specific version system-wide.

1. **Add NodeSource PPA**:
   ```bash
   curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
   ```

2. **Install Node.js**:
   ```bash
   sudo apt install -y nodejs
   ```

3. **Verify installation**:
   ```bash
   node -v
   npm -v
   ```

---

### 🔍 Bonus Tips

- Use `nvm ls` to list installed versions.
- Use `nvm alias default <version>` to set a default version.
- For production environments, stick to LTS versions like Node.js 20.

### Install Yarn
- If you already use NPM:
  ```bash
  npm install --global yarn
  ```
- Run this to find where NPM installed Yarn:
  ```bash
  which yarn
  ```
  You’ll get something like:
    ```
    /home/dung-nguyen/.nvm/versions/node/v24.11.0/bin/yarn
    ```
- Edit your shell config file (depending on your shell):

  For Bash:
  ```bash
  echo 'export PATH="$PATH:/home/dung-nguyen/.nvm/versions/node/v24.11.0/bin/yarn"' >> ~/.bashrc
  source ~/.bashrc
  ```

  For Zsh:
  ```bash
  echo 'export PATH="$PATH:/home/dung-nguyen/.nvm/versions/node/v24.11.0/bin/yarn"' >> ~/.zshrc
  source ~/.zshrc
  ```
- Verify Yarn is accessible:
  ```bash
  yarn --version
  ```