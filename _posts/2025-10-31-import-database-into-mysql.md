# Import Database into MySQL

To import a database into MySQL from a `.sql` file, you can use one of the following methods depending on your environment:

---

### 🖥️ Method 1: Using the MySQL Command Line

```bash
mysql -u username -p database_name < path/to/your/file.sql
```

- Replace `username` with your MySQL username.
- Replace `database_name` with the name of the database you want to import into.
- You'll be prompted for your password.
- Make sure the database already exists. If not, create it first:
  ```bash
  mysql -u username -p -e "CREATE DATABASE database_name;"
  ```

---

### 🧰 Method 2: Using MySQL Workbench

1. Open MySQL Workbench and connect to your server.
2. Go to **File > Open SQL Script** and select your `.sql` file.
3. The script will open in a new tab. Click **Execute** (lightning bolt icon) to run it.
4. Alternatively, use **Server > Data Import** and choose "Import from Self-Contained File".

---

### 🐳 Method 3: Inside a Docker Container

If you're using MySQL in Docker:

```bash
docker exec -i mysql-container-name mysql -u username -p database_name < file.sql
```

- Make sure the `.sql` file is accessible to the container (e.g., via volume mount).

---

### 🧪 Bonus Tips

- If your `.sql` file includes `CREATE DATABASE` statements, you don’t need to pre-create the database.
- For large files, consider using `mysqlimport` or `source` from within the MySQL shell:
  ```sql
  mysql> USE database_name;
  mysql> SOURCE /path/to/file.sql;
  ```