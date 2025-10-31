# Install MySQL on Ubuntu

**To install MySQL on Ubuntu, run `sudo apt install mysql-server`, then secure and verify the installation.**

Here’s a step-by-step guide to get MySQL up and running on Ubuntu (works for versions like 20.04, 22.04, and 24.04):

---

### 🛠️ Step-by-Step Installation

1. **Update your package index**
   ```bash
   sudo apt update
   ```

2. **Install MySQL Server**
   ```bash
   sudo apt install mysql-server
   ```
   - This installs the latest MySQL version available in Ubuntu’s repositories (typically MySQL 8.0).

3. **Start and enable MySQL service**
   ```bash
   sudo systemctl start mysql
   sudo systemctl enable mysql
   ```

4. **Secure the installation**
   ```bash
   sudo mysql_secure_installation
   ```
   - You’ll be prompted to set a root password, remove anonymous users, disallow remote root login, and remove test databases. Answer based on your security needs.

5. **Verify MySQL is running**
   ```bash
   sudo systemctl status mysql
   ```
   - You should see `active (running)` in the output.

6. **Log into MySQL**
   ```bash
   sudo mysql
   ```
   - This logs you in as root. You can now create databases, users, and manage your server.

---

### 🔐 Optional 1: Set or reset the MySQL root password
1. **Log into MySQL as root**
   ```bash
   sudo mysql
   ```

2. **Run the following SQL commands**
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'your_new_password';
   FLUSH PRIVILEGES;
   ```

3. **Exit MySQL**
   ```sql
   EXIT;
   ```

---

### 🔐 Optional 2: Create a MySQL User and Database
```sql
CREATE DATABASE myapp;
CREATE USER 'myuser'@'localhost' IDENTIFIED BY 'mypassword';
GRANT ALL PRIVILEGES ON myapp.* TO 'myuser'@'localhost';
FLUSH PRIVILEGES;
```

---

### 🧠 Tips & Best Practices
- *Use strong passwords* for MySQL users.
- *Restrict remote access* unless absolutely necessary.
- *Back up your databases* regularly.
- Consider using **DigitalOcean Managed Databases** or similar if you want automated backups and scaling.

Would you like help configuring MySQL for a specific app stack like LAMP or Node.js?