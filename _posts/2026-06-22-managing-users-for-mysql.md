# Managing Users for MySQL

MySQL user management controls who can connect to the database server, where they can connect from, and what they are allowed to do after connecting. A MySQL account is made of two parts: the username and the host. For example, `'app_user'@'localhost'` and `'app_user'@'%'` are different accounts, even though the username is the same.

Good user management usually follows the principle of least privilege: create separate accounts for separate purposes, grant only the permissions each account needs, and review those permissions regularly.

## Log in as an Administrative User

On many Linux installations, the MySQL root account can be accessed with `sudo`:

```bash
sudo mysql
```

If your root account uses password authentication, connect with:

```bash
mysql -u root -p
```

After logging in, you can run SQL statements to create users, grant privileges, inspect permissions, and remove accounts.

## Understand MySQL Account Names

MySQL account names use this format:

```sql
'username'@'host'
```

Common host values include:

- `'localhost'`: The user can connect only from the same machine.
- `'%'`: The user can connect from any host allowed by the server and network configuration.
- `'192.168.1.%'`: The user can connect from a matching IP range.
- `'app.example.com'`: The user can connect from a specific hostname.

For production systems, avoid using `'%'` unless the account truly needs broad remote access. A narrower host pattern reduces the blast radius if credentials are exposed.

## Create a User

Create a local user with a password:

```sql
CREATE USER 'app_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';
```

Create a user that can connect from a specific application server:

```sql
CREATE USER 'app_user'@'10.0.1.25' IDENTIFIED BY 'StrongPassword123!';
```

Create a user with the default MySQL 8 authentication plugin:

```sql
CREATE USER 'app_user'@'localhost'
IDENTIFIED WITH caching_sha2_password BY 'StrongPassword123!';
```

Some older clients may not support `caching_sha2_password`. If compatibility is required, you may see `mysql_native_password` used instead:

```sql
CREATE USER 'legacy_user'@'localhost'
IDENTIFIED WITH mysql_native_password BY 'StrongPassword123!';
```

Use `mysql_native_password` only when needed for older drivers or tools.

## Grant Privileges

Grant all privileges on one database to an application user:

```sql
GRANT ALL PRIVILEGES ON myapp.* TO 'app_user'@'localhost';
```

Grant only read access:

```sql
GRANT SELECT ON myapp.* TO 'report_user'@'localhost';
```

Grant read and write access without schema changes:

```sql
GRANT SELECT, INSERT, UPDATE, DELETE ON myapp.* TO 'app_user'@'localhost';
```

Grant privileges on a single table:

```sql
GRANT SELECT, UPDATE ON myapp.customers TO 'support_user'@'localhost';
```

Grant privileges on all databases:

```sql
GRANT SELECT ON *.* TO 'auditor'@'localhost';
```

Be careful with global privileges on `*.*`. Administrative privileges such as `SUPER`, `GRANT OPTION`, `CREATE USER`, `FILE`, and `SYSTEM_USER` should be limited to trusted administrative accounts.

## Apply Privilege Changes

For `CREATE USER`, `GRANT`, `REVOKE`, `ALTER USER`, and `DROP USER`, MySQL updates the grant tables automatically.

If you modify grant tables directly, which is uncommon and not recommended for normal administration, reload privileges manually:

```sql
FLUSH PRIVILEGES;
```

## View Users

List MySQL users and their allowed hosts:

```sql
SELECT user, host
FROM mysql.user
ORDER BY user, host;
```

View authentication plugins and account status:

```sql
SELECT user, host, plugin, account_locked
FROM mysql.user
ORDER BY user, host;
```

Check the current connected user:

```sql
SELECT CURRENT_USER();
```

`CURRENT_USER()` shows the authenticated MySQL account that determines privileges. This can differ from `USER()`, which shows the username and host provided by the client connection.

## Show User Privileges

Show grants for a specific user:

```sql
SHOW GRANTS FOR 'app_user'@'localhost';
```

Show grants for the current user:

```sql
SHOW GRANTS;
```

You can also query privilege tables, but `SHOW GRANTS` is usually the clearest way to review what an account can do.

## Change a Password

Change a user's password:

```sql
ALTER USER 'app_user'@'localhost' IDENTIFIED BY 'NewStrongPassword123!';
```

Expire a password so the user must change it at next login:

```sql
ALTER USER 'app_user'@'localhost' PASSWORD EXPIRE;
```

Set a password expiration interval:

```sql
ALTER USER 'app_user'@'localhost' PASSWORD EXPIRE INTERVAL 90 DAY;
```

Disable password expiration for a service account:

```sql
ALTER USER 'app_user'@'localhost' PASSWORD EXPIRE NEVER;
```

For service accounts, rotate passwords through your deployment or secrets management process instead of relying on interactive password changes.

## Lock and Unlock Users

Lock an account to prevent login without deleting it:

```sql
ALTER USER 'app_user'@'localhost' ACCOUNT LOCK;
```

Unlock the account:

```sql
ALTER USER 'app_user'@'localhost' ACCOUNT UNLOCK;
```

Locking is useful when an employee leaves, an application is retired, or suspicious activity is detected and you want to preserve the account for investigation.

## Rename a User

Rename a MySQL account:

```sql
RENAME USER 'old_user'@'localhost' TO 'new_user'@'localhost';
```

You can also change the host part:

```sql
RENAME USER 'app_user'@'%' TO 'app_user'@'10.0.1.25';
```

After renaming, review the account's grants:

```sql
SHOW GRANTS FOR 'app_user'@'10.0.1.25';
```

## Revoke Privileges

Revoke one privilege:

```sql
REVOKE DELETE ON myapp.* FROM 'app_user'@'localhost';
```

Revoke several privileges:

```sql
REVOKE INSERT, UPDATE, DELETE ON myapp.* FROM 'app_user'@'localhost';
```

Revoke all privileges and grant option:

```sql
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'app_user'@'localhost';
```

Always run `SHOW GRANTS` after revoking permissions to confirm the account has the intended access.

## Drop a User

Remove a user account:

```sql
DROP USER 'app_user'@'localhost';
```

Drop multiple accounts:

```sql
DROP USER 'app_user'@'localhost', 'report_user'@'localhost';
```

Dropping a user removes the account and its privileges, but it does not remove database objects created by that user.

## Manage Roles

Roles let you group privileges and assign them to users. This is useful when many users need the same permissions.

Create a role:

```sql
CREATE ROLE 'app_readonly';
```

Grant privileges to the role:

```sql
GRANT SELECT ON myapp.* TO 'app_readonly';
```

Grant the role to a user:

```sql
GRANT 'app_readonly' TO 'report_user'@'localhost';
```

Set the role as active by default:

```sql
SET DEFAULT ROLE 'app_readonly' TO 'report_user'@'localhost';
```

Show grants for the role:

```sql
SHOW GRANTS FOR 'app_readonly';
```

Revoke a role from a user:

```sql
REVOKE 'app_readonly' FROM 'report_user'@'localhost';
```

Drop a role:

```sql
DROP ROLE 'app_readonly';
```

## Create Common Account Types

### Application User

An application account usually needs access only to its own database:

```sql
CREATE USER 'app_user'@'10.0.1.25' IDENTIFIED BY 'StrongPassword123!';
GRANT SELECT, INSERT, UPDATE, DELETE ON myapp.* TO 'app_user'@'10.0.1.25';
```

### Readonly Reporting User

A reporting account should not modify data:

```sql
CREATE USER 'report_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT SELECT ON myapp.* TO 'report_user'@'localhost';
```

### Backup User

A backup user for `mysqldump` usually needs read access and a few additional privileges depending on the backup options and MySQL version:

```sql
CREATE USER 'backup_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT SELECT, SHOW VIEW, TRIGGER, EVENT ON myapp.* TO 'backup_user'@'localhost';
```

For InnoDB backups with `mysqldump --single-transaction`, avoid granting broad write or administrative privileges unless your backup process specifically requires them.

### Administrator User

Create named administrator accounts instead of sharing `root`:

```sql
CREATE USER 'db_admin'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT ALL PRIVILEGES ON *.* TO 'db_admin'@'localhost' WITH GRANT OPTION;
```

Use this type of account carefully. It can manage databases, privileges, and other users.

## Remote User Access

Creating `'user'@'%'` does not automatically make MySQL reachable from the network. Remote access may also require:

- MySQL listening on a network interface in the server configuration.
- Firewall rules allowing inbound traffic on port `3306`.
- Cloud security group or network ACL rules allowing the client host.
- A user account whose host matches the connecting client.

For example, a restricted remote application user is safer than a wildcard user:

```sql
CREATE USER 'app_user'@'10.0.1.25' IDENTIFIED BY 'StrongPassword123!';
GRANT SELECT, INSERT, UPDATE, DELETE ON myapp.* TO 'app_user'@'10.0.1.25';
```

If remote access is needed over an untrusted network, use TLS or connect through a private network, VPN, bastion host, or SSH tunnel.

## Best Practices

- Create one account per application, person, or automation process.
- Avoid sharing `root` or other administrative accounts.
- Use strong, unique passwords and store them in a secrets manager.
- Grant privileges at the database or table level instead of globally whenever possible.
- Prefer specific hosts over `'%'` for production accounts.
- Use roles to standardize common permission sets.
- Review users and grants regularly with `SELECT user, host FROM mysql.user;` and `SHOW GRANTS`.
- Lock accounts before dropping them if you need a review period.
- Remove unused users and revoke permissions that are no longer needed.
- Keep MySQL and client drivers updated so modern authentication methods work correctly.

## Quick Reference

```sql
-- Create user
CREATE USER 'user_name'@'localhost' IDENTIFIED BY 'StrongPassword123!';

-- Grant database privileges
GRANT SELECT, INSERT, UPDATE, DELETE ON database_name.* TO 'user_name'@'localhost';

-- Show privileges
SHOW GRANTS FOR 'user_name'@'localhost';

-- Change password
ALTER USER 'user_name'@'localhost' IDENTIFIED BY 'NewStrongPassword123!';

-- Lock and unlock account
ALTER USER 'user_name'@'localhost' ACCOUNT LOCK;
ALTER USER 'user_name'@'localhost' ACCOUNT UNLOCK;

-- Revoke privileges
REVOKE INSERT, UPDATE, DELETE ON database_name.* FROM 'user_name'@'localhost';

-- Drop user
DROP USER 'user_name'@'localhost';
```

Managing MySQL users well keeps applications isolated, limits accidental damage, and makes database access easier to audit. Start with narrow accounts, grant permissions intentionally, and revisit access whenever applications, teams, or infrastructure change.