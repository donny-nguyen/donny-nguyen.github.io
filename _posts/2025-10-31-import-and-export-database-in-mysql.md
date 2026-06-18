# Import and Export Database in MySQL

MySQL databases can be exported to a `.sql` file for backup, migration, or sharing, and imported later into the same or another MySQL server. The most common tools are `mysqldump`, the `mysql` command line client, MySQL Workbench, and Docker commands when MySQL runs inside a container.

## Export a Database to a `.sql` File

### Method 1: Using `mysqldump`

Use `mysqldump` to export a database from the command line:

```bash
mysqldump -u username -p database_name > database_name.sql
```

- Replace `username` with your MySQL username.
- Replace `database_name` with the database you want to export.
- You will be prompted for your password.
- The exported file will be saved as `database_name.sql` in the current directory.

To export multiple databases:

```bash
mysqldump -u username -p --databases database_one database_two > databases.sql
```

To export all databases:

```bash
mysqldump -u username -p --all-databases > all_databases.sql
```

To export a database from a remote MySQL server, add the remote host and port:

```bash
mysqldump -h remote_host -P 3306 -u username -p database_name > database_name.sql
```

- Replace `remote_host` with the remote database hostname or IP address.
- Replace `3306` with the MySQL port if your server uses a different one.
- Make sure your local machine is allowed to connect to the remote MySQL server.

If the remote database is only accessible through SSH, create an SSH tunnel first:

```bash
ssh -L 3307:127.0.0.1:3306 ssh_user@remote_server
```

Then export through the tunnel from another terminal:

```bash
mysqldump -h 127.0.0.1 -P 3307 -u username -p database_name > database_name.sql
```

In this example, `3307` is the local port forwarded to the remote MySQL server's `3306` port.

#### Export Using a Readonly User

When exporting with a readonly user, `mysqldump` may fail because some default operations require extra privileges. For example, you might see warnings about GTIDs, missing `PROCESS` privileges for tablespaces, or missing `LOCK TABLES` privileges:

```text
Warning: A partial dump from a server that has GTIDs will by default include the GTIDs of all transactions, even those that changed suppressed parts of the database. If you don't want to restore GTIDs, pass --set-gtid-purged=OFF.
mysqldump: Error: 'Access denied; you need (at least one of) the PROCESS privilege(s) for this operation' when trying to dump tablespaces
mysqldump: Got error: 1044: Access denied for user 'readonly_user'@'%' to database 'database_name' when using LOCK TABLES
```

For an InnoDB database, use `--single-transaction`, disable table locks, skip tablespace metadata, and turn off GTID purging if you do not need to restore GTID information:

```bash
mysqldump -h remote_host -P 3306 -u readonly_user -p \
	--single-transaction \
	--skip-lock-tables \
	--no-tablespaces \
	--set-gtid-purged=OFF \
	database_name > database_name.sql
```

These options help because:

- `--single-transaction` creates a consistent snapshot for InnoDB tables without locking them.
- `--skip-lock-tables` avoids `LOCK TABLES`, which readonly users often do not have permission to run.
- `--no-tablespaces` avoids dumping tablespace metadata, which can require the `PROCESS` privilege.
- `--set-gtid-purged=OFF` prevents GTID statements from being written to the dump file when you only need the schema and data.

If the database contains MyISAM tables, `--single-transaction` cannot guarantee a fully consistent dump for those tables. In that case, ask a database administrator for a backup user with the required privileges or run the export during a maintenance window.

### Method 2: Using MySQL Workbench

1. Open MySQL Workbench and connect to your MySQL server.
2. Go to **Server > Data Export**.
3. Select the database schema you want to export.
4. Choose whether to export tables, views, stored procedures, or functions.
5. Select **Export to Self-Contained File** and choose a `.sql` file path.
6. Click **Start Export**.

### Method 3: Export from a Docker Container

If MySQL is running in Docker, run `mysqldump` inside the container and redirect the output to a file on your host machine:

```bash
docker exec mysql-container-name mysqldump -u username -p database_name > database_name.sql
```

If you want to avoid the password prompt, you can pass the password directly, but this may expose it in your shell history:

```bash
docker exec mysql-container-name mysqldump -u username -ppassword database_name > database_name.sql
```

## Import a Database from a `.sql` File

### Method 1: Using the MySQL Command Line

Use the `mysql` command line client to import a `.sql` file into an existing database:

```bash
mysql -u username -p database_name < path/to/file.sql
```

- Replace `username` with your MySQL username.
- Replace `database_name` with the database you want to import into.
- You will be prompted for your password.
- Make sure the target database already exists.

If the database does not exist, create it first:

```bash
mysql -u username -p -e "CREATE DATABASE database_name;"
```

### Method 2: Using MySQL Workbench

1. Open MySQL Workbench and connect to your MySQL server.
2. Go to **Server > Data Import**.
3. Choose **Import from Self-Contained File**.
4. Select your `.sql` file.
5. Choose the target schema, or create a new one.
6. Click **Start Import**.

You can also open a script manually with **File > Open SQL Script**, then click **Execute** to run it.

### Method 3: Import into a Docker Container

If MySQL is running in Docker, pipe the `.sql` file into the `mysql` client inside the container:

```bash
docker exec -i mysql-container-name mysql -u username -p database_name < database_name.sql
```

The `-i` option keeps standard input open so Docker can pass the SQL file into the container.

## Useful Tips

- If your `.sql` file includes `CREATE DATABASE` and `USE` statements, you do not need to create or select the database manually before importing.
- For large imports, run the `SOURCE` command from inside the MySQL shell:

```sql
mysql> USE database_name;
mysql> SOURCE /path/to/file.sql;
```

- Use `--single-transaction` with `mysqldump` for InnoDB databases when you want a consistent backup without locking tables:

```bash
mysqldump -u username -p --single-transaction database_name > database_name.sql
```

- Check the exported file before relying on it as a backup:

```bash
head database_name.sql
```
