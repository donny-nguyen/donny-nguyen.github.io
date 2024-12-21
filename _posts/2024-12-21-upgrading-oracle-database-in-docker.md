# Upgrading an Oracle Database Running in a Docker Container

Upgrading an Oracle Database running in a Docker container involves the following steps:

---

### **1. Understand the Current Setup**
- Identify the **current Oracle Database version** and the version you want to upgrade to.
- Ensure you have the appropriate Docker image for the target Oracle Database version.
- Back up your database.

---

### **2. Pull the New Docker Image**
- Download the Docker image for the new Oracle Database version from Oracle's container registry or Docker Hub.
  ```bash
  docker pull container-registry.oracle.com/database/enterprise:21.3.0.0
  ```

---

### **3. Backup the Current Database**
- Use Oracle's RMAN or Data Pump utilities to create a backup of the database.
- Alternatively, copy the data files and configuration files from the container to a safe location.

---

### **4. Stop the Current Container**
- Stop the running container of the current Oracle Database.
  ```bash
  docker stop <container_name>
  ```

---

### **5. Create a New Container**
- Run a new container using the upgraded Oracle Database image.
  ```bash
  docker run -d --name oracle-upgrade \
    -p 1521:1521 -p 5500:5500 \
    -e ORACLE_SID=ORCL \
    -e ORACLE_PDB=PDB1 \
    -e ORACLE_PWD=<password> \
    container-registry.oracle.com/database/enterprise:21.3.0.0
  ```

---

### **6. Migrate or Upgrade the Database**
- **Option 1: Data Pump Export/Import**
  - Export the data from the old database.
  - Import the data into the new container's database.

- **Option 2: Manual Data Files Migration**
  - Stop the new container.
  - Copy the old container's data files to the new container's data directory.
  - Use Oracle's `DBUA` (Database Upgrade Assistant) or command-line upgrade utilities inside the new container.

---

### **7. Validate the Upgrade**
- Connect to the upgraded database:
  ```bash
  sqlplus / as sysdba
  ```
- Verify the database version:
  ```sql
  SELECT * FROM v$version;
  ```
- Run post-upgrade scripts provided by Oracle to ensure the upgrade is successful.

---

### **8. Update Configuration Files (if necessary)**
- Update `tnsnames.ora`, `listener.ora`, or any other configuration files if required.

---

### **9. Test Applications**
- Ensure applications connected to the database are functioning correctly after the upgrade.

---

### **10. Cleanup (Optional)**
- If everything is working fine, you can remove the old container and its associated image:
  ```bash
  docker rm <old_container_name>
  docker rmi <old_image_name>
  ```