# Install Python

Before you can start writing Python programs, you need to install the Python interpreter on your machine. This guide covers how to install Python on Windows, macOS, and Linux, along with how to verify the installation.

### Check If Python Is Already Installed

Many systems come with Python pre-installed. Open a terminal or command prompt and run:

```bash
python --version
```

or

```bash
python3 --version
```

If a version number is printed (for example, `Python 3.12.4`), Python is already installed. Note that Python 2 is end-of-life, so make sure you have **Python 3**.

### Install on Windows

1. Go to the official downloads page at [python.org/downloads](https://www.python.org/downloads/).
2. Download the latest **Windows installer** (64-bit recommended).
3. Run the installer.
4. **Important**: Check the box **"Add Python to PATH"** at the bottom of the first screen.
5. Click **Install Now** and follow the prompts.

Verify the installation by opening Command Prompt or PowerShell:

```bash
python --version
pip --version
```

### Install on macOS

macOS includes an older system Python, but it is best to install your own version.

**Option 1 — Official Installer**

1. Download the macOS installer from [python.org/downloads](https://www.python.org/downloads/).
2. Open the `.pkg` file and follow the installation steps.

**Option 2 — Homebrew (recommended)**

If you have [Homebrew](https://brew.sh/) installed:

```bash
brew install python
```

Verify:

```bash
python3 --version
pip3 --version
```

### Install on Linux

Most Linux distributions ship with Python 3. If you need to install or update it, use your package manager.

**Debian / Ubuntu:**

```bash
sudo apt update
sudo apt install python3 python3-pip
```

**Fedora:**

```bash
sudo dnf install python3 python3-pip
```

**Arch Linux:**

```bash
sudo pacman -S python python-pip
```

Verify:

```bash
python3 --version
pip3 --version
```

### Using a Version Manager

If you need to work with multiple Python versions, a version manager like `pyenv` makes it easy to install and switch between them:

```bash
pyenv install 3.12.4
pyenv global 3.12.4
```

This is especially useful when different projects require different Python versions.

### Set Up a Virtual Environment

After installing Python, it is good practice to create isolated environments for your projects so dependencies do not conflict:

```bash
python3 -m venv myenv
source myenv/bin/activate   # On Windows: myenv\Scripts\activate
```

When activated, any packages you install with `pip` are contained within that environment.

### Conclusion

Installing Python is a quick first step toward learning the language. Choose the method that fits your operating system, confirm the installation with `python --version`, and consider using a version manager and virtual environments to keep your projects organized and your dependencies isolated.
