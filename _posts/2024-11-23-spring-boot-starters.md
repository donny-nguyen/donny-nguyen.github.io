# Spring Boot Starters

Spring Boot Starters are a set of convenient dependency descriptors we can include in our application to quickly set up a specific type of application or functionality. Here are some commonly used Spring Boot Starters and their corresponding dependencies if we don't use the starters:

### 1. **Spring Boot Starter Web**
- **Purpose**: To build web applications, including RESTful services.
- **Starter Dependency**:
  ```xml
  <!-- Spring Boot Starter Web -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  ```
- **Individual Dependencies**:
  ```xml
  <!-- Spring MVC -->
  <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-web</artifactId>
  </dependency>
  <!-- Spring Boot Starter Tomcat -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-tomcat</artifactId>
  </dependency>
  <!-- Spring Boot Starter Validation -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  ```

### 2. **Spring Boot Starter Data JPA**
- **Purpose**: To build applications that use Spring Data JPA for data access.
- **Starter Dependency**:
  ```xml
  <!-- Spring Boot Starter Data JPA -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>
  ```
- **Individual Dependencies**:
  ```xml
  <!-- Spring Data JPA -->
  <dependency>
      <groupId>org.springframework.data</groupId>
      <artifactId>spring-data-jpa</artifactId>
  </dependency>
  <!-- Hibernate ORM -->
  <dependency>
      <groupId>org.hibernate</groupId>
      <artifactId>hibernate-core</artifactId>
  </dependency>
  <!-- Spring Boot Starter JDBC -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-jdbc</artifactId>
  </dependency>
  ```

### 3. **Spring Boot Starter Security**
- **Purpose**: To build applications that require authentication and authorization.
- **Starter Dependency**:
  ```xml
  <!-- Spring Boot Starter Security -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
  </dependency>
  ```
- **Individual Dependencies**:
  ```xml
  <!-- Spring Security Core -->
  <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-core</artifactId>
  </dependency>
  <!-- Spring Security Web -->
  <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-web</artifactId>
  </dependency>
  <!-- Spring Security Config -->
  <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-config</artifactId>
  </dependency>
  ```

### 4. **Spring Boot Starter Thymeleaf**
- **Purpose**: To build web applications using Thymeleaf as the view layer.
- **Starter Dependency**:
  ```xml
  <!-- Spring Boot Starter Thymeleaf -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-thymeleaf</artifactId>
  </dependency>
  ```
- **Individual Dependencies**:
  ```xml
  <!-- Thymeleaf -->
  <dependency>
      <groupId>org.thymeleaf</groupId>
      <artifactId>thymeleaf-spring5</artifactId>
  </dependency>
  <!-- Thymeleaf Layout Dialect -->
  <dependency>
      <groupId>nz.net.ultraq.thymeleaf</groupId>
      <artifactId>thymeleaf-layout-dialect</artifactId>
  </dependency>
  ```

### 5. **Spring Boot Starter Test**
- **Purpose**: To build and test Spring Boot applications.
- **Starter Dependency**:
  ```xml
  <!-- Spring Boot Starter Test -->
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
  </dependency>
  ```
- **Individual Dependencies**:
  ```xml
  <!-- JUnit -->
  <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
      <scope>test</scope>
  </dependency>
  <!-- Spring Test -->
  <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-test</artifactId>
      <scope>test</scope>
  </dependency>
  <!-- MockMvc -->
  <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-test</artifactId>
      <scope>test</scope>
  </dependency>
  ```

### Summary
Spring Boot Starters streamline the setup process by bundling common dependencies, saving time and reducing boilerplate. If we're not using these starters, we'll need to manually add and configure the required individual dependencies, as shown in the examples above.
