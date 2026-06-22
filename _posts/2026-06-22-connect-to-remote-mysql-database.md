# Connect to a Remote MySQL Database

Connecting to a remote MySQL database means your MySQL client or application connects to a MySQL server running on another machine. To make it work, the MySQL server must accept network connections, the user account must be allowed to connect remotely, and the network must allow traffic to MySQL's port.

By default, many MySQL installations only accept local connections for security reasons. Remote access should be enabled carefully, especially on production servers.

## Prerequisites

Before connecting, collect these details:

- Hostname or IP address of the MySQL server
- MySQL port, usually `3306`
- Database name
- Username and password
- Your client machine's public IP address, if the server restricts access by IP

You also need a MySQL client installed locally. On Ubuntu, install it with:

```bash
sudo apt update
sudo apt install mysql-client
```

On macOS with Homebrew:

```bash
brew install mysql-client
```

## 1. Allow MySQL to Listen for Remote Connections

On the MySQL server, check the MySQL configuration file. Common locations include:

```bash
/etc/mysql/mysql.conf.d/mysqld.cnf
/etc/my.cnf
/etc/mysql/my.cnf
```

Open the file and find `bind-address`:

```ini
bind-address = 127.0.0.1
```

`127.0.0.1` means MySQL only listens on localhost. To allow remote connections, change it to the server's private IP address:

```ini
bind-address = 10.0.1.10
```

You can also use `0.0.0.0` to listen on all network interfaces:

```ini
bind-address = 0.0.0.0
```

Using the specific server IP is usually safer than listening on every interface.

Restart MySQL after changing the configuration:

```bash
sudo systemctl restart mysql
```

Verify MySQL is listening on port `3306`:

```bash
sudo ss -lntp | grep 3306
```

## 2. Create a User for Remote Access

Log in to MySQL on the server:

```bash
sudo mysql
```

Create a user that can connect from a specific client IP address:

```sql
CREATE USER 'app_user'@'203.0.113.25' IDENTIFIED BY 'StrongPassword123!';
```

Grant the user access to one database:

```sql
GRANT SELECT, INSERT, UPDATE, DELETE ON myapp.* TO 'app_user'@'203.0.113.25';
```

Apply the changes:

```sql
FLUSH PRIVILEGES;
```

Check the user's permissions:

```sql
SHOW GRANTS FOR 'app_user'@'203.0.113.25';
```

If the client IP changes often, you may see examples using `%`:

```sql
CREATE USER 'app_user'@'%' IDENTIFIED BY 'StrongPassword123!';
```

`%` allows the user to connect from any host that can reach the MySQL server. Avoid this for production unless you also have strong firewall rules, TLS, and a clear operational reason.

## 3. Open the Firewall or Security Group

MySQL uses port `3306` by default. The server firewall must allow traffic from your client machine or application server.

On Ubuntu with UFW, allow one trusted IP:

```bash
sudo ufw allow from 203.0.113.25 to any port 3306 proto tcp
```

Check the firewall status:

```bash
sudo ufw status
```

If the database is hosted in a cloud provider, update the security group, firewall rule, or network access list to allow inbound TCP traffic on port `3306` from the client IP.

Do not open MySQL to the entire internet with `0.0.0.0/0` unless the database is temporary and tightly controlled. For real systems, allow only known application servers, bastion hosts, VPN ranges, or office IP addresses.

## 4. Test Network Connectivity

From the client machine, test whether the MySQL port is reachable:

```bash
nc -vz mysql.example.com 3306
```

If `nc` is not installed, use `telnet`:

```bash
telnet mysql.example.com 3306
```

A successful connection means the network path is open. It does not mean the username and password are correct yet.

## 5. Connect with the MySQL CLI

Use the MySQL client from your local machine:

```bash
mysql -h mysql.example.com -P 3306 -u app_user -p
```

After entering the password, select the database:

```sql
USE myapp;
SHOW TABLES;
```

You can also provide the database name directly:

```bash
mysql -h mysql.example.com -P 3306 -u app_user -p myapp
```

## 6. Connect with MySQL Workbench

In MySQL Workbench, create a new connection and enter:

- Connection Method: `Standard TCP/IP`
- Hostname: `mysql.example.com`
- Port: `3306`
- Username: `app_user`
- Password: store it in the vault or enter it when prompted
- Default Schema: `myapp`

Click `Test Connection`. If it fails, check the error message carefully. Authentication errors, network timeouts, and host permission errors usually point to different problems.

## 7. Connect from an Application

Application connection strings usually contain the host, port, database, username, and password.

Example JDBC URL:

```properties
spring.datasource.url=jdbc:mysql://mysql.example.com:3306/myapp
spring.datasource.username=app_user
spring.datasource.password=StrongPassword123!
```

Example Node.js configuration:

```javascript
const connection = await mysql.createConnection({
  host: 'mysql.example.com',
  port: 3306,
  user: 'app_user',
  password: process.env.DB_PASSWORD,
  database: 'myapp'
});
```

Store passwords in environment variables or a secrets manager instead of hardcoding them in source code.

## 8. Use an SSH Tunnel When Direct Access Is Not Allowed

For better security, many teams do not expose MySQL directly to the internet. Instead, they connect through a bastion server or SSH tunnel.

Create a tunnel from your local machine:

```bash
ssh -L 3307:127.0.0.1:3306 user@bastion.example.com
```

Then connect to MySQL through the local forwarded port:

```bash
mysql -h 127.0.0.1 -P 3307 -u app_user -p myapp
```

In this setup, your MySQL client connects to `127.0.0.1:3307`, SSH forwards the traffic to the remote network, and MySQL receives the connection on the server side.

## 9. Enable TLS for Remote Connections

For production systems, encrypt remote MySQL traffic with TLS. Without TLS, credentials and data may be exposed on the network.

Check whether the current connection uses SSL:

```sql
SHOW STATUS LIKE 'Ssl_cipher';
```

If the value is empty, the connection is not encrypted.

Connect with TLS from the CLI:

```bash
mysql -h mysql.example.com -P 3306 -u app_user -p --ssl-mode=REQUIRED
```

You can require SSL for a specific user:

```sql
ALTER USER 'app_user'@'203.0.113.25' REQUIRE SSL;
```

Managed database services often provide CA certificates. Download the provider's CA certificate and configure your client or application to verify the server certificate.

## Common Errors

### ERROR 2003: Can't connect to MySQL server

This usually means the client cannot reach the server. Check:

- The hostname or IP address
- The port number
- Server firewall rules
- Cloud security group rules
- Whether MySQL is running
- Whether MySQL is listening on a remote interface

### ERROR 1045: Access denied for user

This usually means the username, password, host, or privileges are wrong. Check:

```sql
SELECT user, host FROM mysql.user WHERE user = 'app_user';
SHOW GRANTS FOR 'app_user'@'203.0.113.25';
```

Remember that `'app_user'@'localhost'` and `'app_user'@'203.0.113.25'` are different MySQL accounts.

### Host is not allowed to connect

This means MySQL does not have an account that matches the username and client host. Create or update the user with the correct host value:

```sql
CREATE USER 'app_user'@'203.0.113.25' IDENTIFIED BY 'StrongPassword123!';
GRANT SELECT, INSERT, UPDATE, DELETE ON myapp.* TO 'app_user'@'203.0.113.25';
```

## Security Best Practices

- Allow remote access only from trusted IP addresses.
- Avoid using the MySQL `root` account for remote connections.
- Create separate users for applications, administrators, and reporting tools.
- Grant only the privileges each user needs.
- Use strong passwords and rotate them regularly.
- Use TLS for remote connections.
- Prefer private networks, VPNs, or SSH tunnels over public exposure.
- Monitor failed login attempts and unusual database activity.
- Keep MySQL and client drivers updated.

## Quick Checklist

To connect successfully to a remote MySQL database, confirm that:

- MySQL is running on the server.
- `bind-address` allows remote connections.
- The MySQL user is created with the correct host value.
- The user has privileges on the target database.
- Port `3306` is open only to trusted clients.
- The client uses the correct host, port, username, password, and database name.
- TLS, VPN, or SSH tunneling is used for sensitive environments.

Once these pieces line up, connecting remotely is usually straightforward.
