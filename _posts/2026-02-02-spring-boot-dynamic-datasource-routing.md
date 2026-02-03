# Spring Boot Dynamic DataSource Routing with AWS RDS

To connect a Spring Boot application to both a **Primary** (Read-Write) and **Replica** (Read-Only) AWS RDS instance, the most efficient approach is **Dynamic DataSource Routing**.

This method allows you to use a single Repository layer while automatically routing queries based on the `@Transactional(readOnly = true)` flag.

---

## 1. Define Properties

In your `application.yml`, define separate configurations for your primary and replica endpoints.

```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:mysql://primary-instance.xxxx.us-east-1.rds.amazonaws.com:3306/mydb
      username: admin
      password: password123
      driver-class-name: com.mysql.cj.jdbc.Driver
    replica:
      jdbc-url: jdbc:mysql://replica-instance.xxxx.us-east-1.rds.amazonaws.com:3306/mydb
      username: admin
      password: password123
      driver-class-name: com.mysql.cj.jdbc.Driver

```

---

## 2. Create the Routing Logic

Spring provides `AbstractRoutingDataSource`, which acts as a "switchboard." It looks at a "key" to decide which database to use for the current thread.

### The Routing Class

```java
public class ReplicationRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // Returns "PRIMARY" or "REPLICA" based on the transaction context
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly() 
                ? "REPLICA" : "PRIMARY";
    }
}

```

---

## 3. Configuration Class

You must manually configure the `DataSource` beans to prevent Spring Boot from trying to auto-configure a single connection.

> **Note:** Use `LazyConnectionDataSourceProxy`. This ensures the connection is only fetched from the pool *after* the transaction manager has determined if the transaction is `readOnly`.

```java
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.replica")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public DataSource routingDataSource(
            @Qualifier("primaryDataSource") DataSource primary,
            @Qualifier("replicaDataSource") DataSource replica) {
        
        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();
        
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("PRIMARY", primary);
        dataSourceMap.put("REPLICA", replica);
        
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(primary);
        return routingDataSource;
    }

    @Primary
    @Bean
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }
}

```

---

## 4. How to Use It

Now, you don't need to change your Repositories. Simply manage the routing at the **Service layer** using standard Spring annotations:

* **Writes (Primary):** Use `@Transactional` (default is `readOnly = false`).
* **Reads (Replica):** Use `@Transactional(readOnly = true)`.

```java
@Service
public class UserService {
    
    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(); // Routes to REPLICA
    }

    @Transactional
    public void updateUser(User user) {
        userRepository.save(user); // Routes to PRIMARY
    }
}

```

---

### Key Considerations

* **Replication Lag:** Keep in mind that RDS replication is asynchronous. If you write to the primary and immediately try to read from the replica, the data might not be there yet (eventual consistency).
* **Aurora Clusters:** If you are using **Amazon Aurora**, you can alternatively use the **AWS JDBC Driver for MySQL/Postgres**, which handles read/write splitting at the driver level using a single connection string.
