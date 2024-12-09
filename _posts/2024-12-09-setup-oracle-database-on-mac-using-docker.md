# Set up Oracle Database on a Mac using Docker

Here's a detailed guide on how to set up Oracle Database on a Mac using Docker:

### Step 1: Install Docker
1. **Download Docker**: Go to the [Docker website](https://www.docker.com/products/docker-desktop) and download Docker Desktop for Mac.
2. **Install Docker**: Follow the installation instructions to install Docker on our Mac.

### Step 2: Pull Oracle Database Image
1. **Open Terminal**: Open the Terminal application on our Mac.
2. **Log in to Docker Hub**: If we haven't already, log in to our Docker Hub account using the following command:
   ```sh
   docker login
   ```
3. **Pull Oracle Database Image**: Pull the Oracle Database image from Docker Hub:
   ```sh
   docker pull oracle/database:19.3.0-ee
   ```

### Step 3: Create a Docker Container
1. **Create a Docker Compose File**: Create a `docker-compose.yml` file in a directory of our choice. Add the following content to the file:
   ```yaml
   version: '3'
   services:
     oracle:
       image: oracle/database:19.3.0-ee
       ports:
         - "1521:1521"
       environment:
         ORACLE_PWD: your_password
   ```
   Replace `your_password` with a secure password of our choice.

2. **Run Docker Compose**: Navigate to the directory containing the `docker-compose.yml` file and run the following command:
   ```sh
   docker-compose up -d
   ```
   This command will start the Docker container in the background.

### Step 4: Connect to the Database
1. **Open SQL Developer**: Open Oracle SQL Developer or any other database management tool.
2. **Create a New Connection**: Create a new database connection with the following details:
   - **Username**: SYS
   - **Password**: your_password
   - **Hostname**: localhost
   - **Port**: 1521
   - **SID**: ORCLCDB

3. **Test the Connection**: Test the connection to ensure everything is set up correctly.

### Step 5: Stop and Start the Container
1. **Stop the Container**: To stop the container, run the following command:
   ```sh
   docker-compose stop
   ```
2. **Start the Container**: To start the container again, run the following command:
   ```sh
   docker-compose start
   ```

### Additional Resources
- **[Official Oracle Documentation](https://docs.oracle.com/en/database/oracle/oracle-database/21/deeck/index.html)**: For detailed instructions and best practices.
- **[Video Tutorial](https://www.youtube.com/watch?v=56dSXI2PbCQ)**: A step-by-step video guide on setting up Oracle Database with Docker.

This setup should help us get Oracle Database running on our Mac using Docker.
