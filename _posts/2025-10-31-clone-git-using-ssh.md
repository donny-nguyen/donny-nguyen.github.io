# Clone Git repository using SSH

To clone a Git repository using SSH, follow these steps:

### 🛠️ Prerequisites
- You must have an SSH key pair (public and private) set up on your machine.
- Your public key must be added to your Git hosting service (e.g., GitHub, GitLab, Bitbucket).

### 🔑 Step 1: Generate SSH Key (if not already done)
```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
```
This creates a key pair in `~/.ssh/id_ed25519` and `~/.ssh/id_ed25519.pub`.

### 📤 Step 2: Add SSH Key to Git Host
- Copy your public key:
  ```bash
  cat ~/.ssh/id_ed25519.pub
  ```
- Paste it into your Git host's SSH settings (e.g., GitHub → Settings → SSH and GPG keys → New SSH key).

### 📦 Step 3: Clone the Repository
Use the SSH URL provided by your Git host. It typically looks like:
```bash
git@github.com:username/repository.git
```

Run:
```bash
git clone git@github.com:username/repository.git
```

### 🧪 Step 4: Test SSH Connection (Optional)
```bash
ssh -T git@github.com
```
You should see a message like:
```
Hi username! You've successfully authenticated...
```