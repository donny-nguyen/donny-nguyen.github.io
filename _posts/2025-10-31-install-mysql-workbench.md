# Install MySQL Workbench

**To install MySQL Workbench on Ubuntu, you can use either the APT repository or Snap package method. The APT method is recommended for full compatibility and updates.**

Here’s a step-by-step guide to installing MySQL Workbench using the APT repository:

---

### 🛠️ Method 1: Install via APT Repository (Recommended)

1. **Update your package list**:
   ```bash
   sudo apt update
   ```

2. **Install MySQL Workbench**:
   ```bash
   sudo apt install mysql-workbench
   ```

3. **Launch the application**:
   - You can start MySQL Workbench from the terminal:
     ```bash
     mysql-workbench
     ```
   - Or find it in your application menu.

> This method installs the version provided by Ubuntu’s default repositories. If you want the latest version, use the official MySQL APT repository.

---

### 📦 Method 2: Install via Snap Package

1. **Install Snap (if not already installed)**:
   ```bash
   sudo apt install snapd
   ```

2. **Install MySQL Workbench using Snap**:
   ```bash
   sudo snap install mysql-workbench-community
   ```

> Snap packages are sandboxed and may have limitations with system integration, but they’re easy to install and update.

---

### 🧰 Optional: Install MySQL Server (if not already installed)

MySQL Workbench requires a running MySQL server to connect to. To install it:

```bash
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

---

### 🧪 Verify Installation

- Open MySQL Workbench and try connecting to your local MySQL server.
- If you encounter issues, check for missing dependencies or run:
  ```bash
  sudo apt --fix-broken install
  ```