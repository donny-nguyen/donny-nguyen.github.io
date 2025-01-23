# Installing Redis

Installing Redis depends on your operating system. Here are the steps for Ubuntu (Linux), macOS, and Windows:

### Ubuntu (Linux)
1. **Update your system:**
    ```bash
    sudo apt update
    sudo apt upgrade -y
    ```
2. **Install Redis:**
    ```bash
    sudo apt install redis-server
    ```
3. **Start Redis:**
    ```bash
    sudo systemctl start redis
    sudo systemctl enable redis
    ```
4. **Test Redis installation:**
    ```bash
    redis-cli ping
    ```
   You should see a response: `PONG`.

### macOS
1. **Install Homebrew** if you haven’t already:
    ```bash
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    ```
2. **Install Redis using Homebrew:**
    ```bash
    brew install redis
    ```
3. **Start Redis:**
    ```bash
    brew services start redis
    ```
4. **Test Redis installation:**
    ```bash
    redis-cli ping
    ```
   You should see a response: `PONG`.

### Windows
1. **Enable WSL2 (Windows Subsystem for Linux):**
    Follow [Microsoft's instructions](https://docs.microsoft.com/en-us/windows/wsl/install) to enable WSL2, choosing Ubuntu as the distribution.
2. **Install Redis in the WSL Ubuntu environment:**
    ```bash
    sudo apt update
    sudo apt install redis-server
    ```
3. **Start Redis:**
    ```bash
    sudo systemctl start redis
    sudo systemctl enable redis
    ```
4. **Test Redis installation:**
    ```bash
    redis-cli ping
    ```
   You should see a response: `PONG`.

These steps will get you up and running with Redis on your preferred operating system.