# Oracle Database User Management

### 1. Creating Users

Basic user creation

```
CREATE USER john_doe IDENTIFIED BY "StrongPassword123";
```

Create user with specific tablespace

```
CREATE USER jane_doe 
IDENTIFIED BY "StrongPassword123"
DEFAULT TABLESPACE users
TEMPORARY TABLESPACE temp
QUOTA 100M ON users;
```

### 2. Granting Basic Privileges

Minimum privileges for connection

```
GRANT CREATE SESSION TO john_doe;
GRANT CONNECT TO john_doe;
```

Grant resource role for basic schema operations

```
GRANT RESOURCE TO john_doe;
```

Grant unlimited tablespace usage

```
GRANT UNLIMITED TABLESPACE TO john_doe;
```

### 3. Common Role Assignments

Create and grant custom role

```
CREATE ROLE app_developer;
GRANT CREATE TABLE, CREATE VIEW, CREATE PROCEDURE TO app_developer;
GRANT app_developer TO john_doe;
```

Grant system privileges

```
GRANT CREATE ANY TABLE TO jane_doe;
GRANT SELECT ANY TABLE TO jane_doe;
```

### 4. Object-Level Privileges

Grant specific table privileges

```
GRANT SELECT, INSERT, UPDATE ON schema_name.table_name TO john_doe;
```

Grant with grant option (user can grant these privileges to others)

```
GRANT SELECT ON schema_name.table_name TO jane_doe WITH GRANT OPTION;
```

### 5. Modifying Users

Change password

```
ALTER USER john_doe IDENTIFIED BY "NewPassword123";
```

Lock account

```
ALTER USER john_doe ACCOUNT LOCK;
```

Unlock account

```
ALTER USER john_doe ACCOUNT UNLOCK;
```

Modify quotas

```
ALTER USER john_doe QUOTA UNLIMITED ON users;
ALTER USER john_doe QUOTA 200M ON users;
```

### 6. Revoking Privileges

Revoke specific privileges

```
REVOKE CREATE TABLE FROM john_doe;
REVOKE SELECT ON schema_name.table_name FROM john_doe;
```

Revoke role

```
REVOKE app_developer FROM john_doe;
```

### 7. Removing Users

Drop user

```
DROP USER john_doe;
```

Drop user and all objects

```
DROP USER john_doe CASCADE;
```

### 8. User Information Queries

List all users

```
SELECT username, account_status, default_tablespace, created
FROM dba_users
ORDER BY created DESC;
```

Check user privileges

```
SELECT privilege 
FROM dba_sys_privs 
WHERE grantee = 'JOHN_DOE';
```

Check role memberships

```
SELECT granted_role
FROM dba_role_privs
WHERE grantee = 'JOHN_DOE';
```

Check resource usage

```
SELECT username, default_tablespace, bytes/1024/1024 as "Space Used (MB)"
FROM dba_ts_quotas
WHERE username = 'JOHN_DOE';
```

### 9. Password Management

Set password life time

```
ALTER PROFILE DEFAULT LIMIT PASSWORD_LIFE_TIME 90;
```

Set password complexity

```
ALTER PROFILE DEFAULT LIMIT 
    FAILED_LOGIN_ATTEMPTS 3
    PASSWORD_LIFE_TIME 90
    PASSWORD_REUSE_TIME 365
    PASSWORD_REUSE_MAX 5
    PASSWORD_VERIFY_FUNCTION ora12c_verify_function;
```

### 10. Best Practices for Security

Create user with specific limitations

```
CREATE USER secure_user
IDENTIFIED BY "StrongPassword123"
DEFAULT TABLESPACE users
TEMPORARY TABLESPACE temp
QUOTA 50M ON users
PASSWORD EXPIRE
ACCOUNT LOCK;
```

Grant minimum necessary privileges

```
GRANT CREATE SESSION TO secure_user;
GRANT CONNECT TO secure_user;
GRANT SELECT ON specific_schema.specific_table TO secure_user;
```

Enable auditing for sensitive operations

```
AUDIT SELECT TABLE, UPDATE TABLE BY secure_user BY ACCESS;
```