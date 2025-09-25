# Installing `pyenv` on Mac

Installing `pyenv` on a Mac is straightforward, especially if you're using **Homebrew**, the popular macOS package manager. Here's a step-by-step guide to get you up and running:

---

### 🛠️ Step-by-Step: Installing Pyenv on macOS

#### 1. **Install Homebrew (if you haven’t already)**
Open Terminal and run:
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

#### 2. **Install pyenv via Homebrew**
```bash
brew update
brew install pyenv
```

#### 3. **Configure your shell**
Add the following to your shell configuration file:

- For **Zsh** (`~/.zshrc`):
  ```bash
  echo 'export PYENV_ROOT="$HOME/.pyenv"' >> ~/.zshrc
  echo 'export PATH="$PYENV_ROOT/bin:$PATH"' >> ~/.zshrc
  echo 'eval "$(pyenv init --path)"' >> ~/.zshrc
  echo 'eval "$(pyenv init -)"' >> ~/.zshrc
  ```

- For **Bash** (`~/.bash_profile` or `~/.bashrc`):
  ```bash
  echo 'export PYENV_ROOT="$HOME/.pyenv"' >> ~/.bash_profile
  echo 'export PATH="$PYENV_ROOT/bin:$PATH"' >> ~/.bash_profile
  echo 'eval "$(pyenv init --path)"' >> ~/.bash_profile
  echo 'eval "$(pyenv init -)"' >> ~/.bash_profile
  ```

Then restart your terminal or run:
```bash
source ~/.zshrc   # or source ~/.bash_profile
```

#### 4. **Install Python versions**
```bash
pyenv install 3.11.6
pyenv install 3.10.13
```

#### 5. **Set global or local Python version**
- Global (default for all shells):
  ```bash
  pyenv global 3.11.6
  ```

- Local (per project):
  ```bash
  pyenv local 3.10.13
  ```

---

### ✅ Verify Installation
Run:
```bash
pyenv versions
python --version
```

If everything’s set up correctly, `python` should point to the version managed by `pyenv`.

---
