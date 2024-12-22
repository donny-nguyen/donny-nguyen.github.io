# Set up Oracle Database on Docker

---

### **1. Pull the Docker Image**
Run the following command to pull the Oracle Database image:

```bash
docker pull gvenzl/oracle-free:23
```

---

### **2. Run the Oracle Database Container**
To run the container, you can use the `docker run` command. Below are two options: quick setup and a more customized setup.

#### **Quick Setup**
Run the following command to start the container with default settings:

```bash
docker run -d -p 1521:1521 -p 5500:5500 --name oracle-db gvenzl/oracle-free:23
```

- **`-d`**: Runs the container in detached mode.
- **`-p`**: Maps the container's ports to your local machine. Port `1521` is for Oracle SQL*Net and `5500` is for Oracle Enterprise Manager.

#### **Customized Setup**
You can specify environment variables for more control over the database:

```bash
docker run -d \
  -p 1521:1521 -p 5500:5500 \
  --name oracle-db \
  -e ORACLE_PASSWORD=your_password \
  -e APP_USER=my_app_user \
  -e APP_USER_PASSWORD=my_app_password \
  gvenzl/oracle-free:23
```

- **`ORACLE_PASSWORD`**: Sets the password for the `SYS` and `SYSTEM` users.
- **`APP_USER`**: Creates a new application user.
- **`APP_USER_PASSWORD`**: Sets the password for the application user.

---

### **3. Verify the Container is Running**
Check if the container is running:

```bash
docker ps
```

You should see the container `oracle-db` in the list.

---

### **4. Connect to the Oracle Database**
You can connect to the Oracle Database using tools like **SQL*Plus**, **SQL Developer**, or any JDBC-compatible client.

#### **Connect Using SQL*Plus**
Use the following command to connect from the container:

```bash
docker exec -it oracle-db sqlplus system/your_password@localhost:1521/ORCL
```

#### **Connect Using JDBC**
Use the following JDBC URL:

```
jdbc:oracle:thin:@localhost:1521/ORCL
```

---

### **5. Explore Oracle Database Features**
Once connected, you can start working with the Oracle Database, creating schemas, tables, and running queries.

---

### **Additional Tips**
- **Persistent Storage**: Add a volume to persist data:
  ```bash
  docker run -d \
    -p 1521:1521 -p 5500:5500 \
    --name oracle-db \
    -v oracle-data:/opt/oracle/oradata \
    gvenzl/oracle-free:23
  ```
- **Logs**: View logs for troubleshooting:
  ```bash
  docker logs oracle-db
  ```
- **Stop the Container**:
  ```bash
  docker stop oracle-db
  ```
- **Remove the Container**:
  ```bash
  docker rm oracle-db
  ```

With these steps, your Oracle Database should be up and running smoothly!