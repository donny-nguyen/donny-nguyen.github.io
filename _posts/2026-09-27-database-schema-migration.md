# Database Schema Migration in Spring Boot

As an application evolves, so does its database schema. New tables, columns, indexes, and constraints are added; old ones are changed or removed. **Schema migration** is the practice of managing these changes in a controlled, versioned, and repeatable way — so every environment (local, test, staging, production) ends up with exactly the same schema. This article explains why you need migrations, why `ddl-auto` is not enough, and how to use the two most popular tools in the Spring Boot ecosystem: **Flyway** and **Liquibase**.

---

### 1. Why Not Just Use `hibernate.ddl-auto`?

Spring Boot lets Hibernate generate or update your schema automatically:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This is convenient for prototyping, but it is **dangerous in production**:

* `update` never drops or renames columns, so the schema slowly drifts out of sync with your entities.
* It gives you **no version history** — you cannot tell which changes were applied or when.
* It cannot handle data migrations (e.g., backfilling a new column).
* `create` and `create-drop` will **wipe your data** on startup.

The recommended production setting is:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

With `validate`, Hibernate only checks that the schema matches your entities and lets a dedicated migration tool own the actual changes.

---

### 2. What Is a Migration Tool?

A migration tool applies a series of ordered, immutable change scripts to your database and records which ones have already run in a **history table**. On startup it:

1. Reads the history table to see which migrations were applied.
2. Finds any new (pending) migration scripts.
3. Runs them in order, inside transactions where supported.
4. Records each successfully applied migration.

Because scripts are versioned and checked into source control, every developer and every environment applies the **same changes in the same order**.

The two dominant tools in the Spring Boot world are **Flyway** and **Liquibase**. Spring Boot auto-configures both — just add the dependency and drop your scripts in the right folder.

---

### 3. Flyway — Migrations as Plain SQL

Flyway favors simplicity: you write migrations as ordinary SQL files following a strict naming convention.

#### Dependency

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

> For MySQL/MariaDB you also need `flyway-mysql`; for newer PostgreSQL versions the core module is enough.

#### Script Location and Naming

By default Spring Boot looks in `src/main/resources/db/migration`. Files follow the pattern:

```
V<VERSION>__<description>.sql
```

For example:

```
src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__add_email_to_users.sql
└── V3__create_orders_table.sql
```

`V1__create_users_table.sql`:

```sql
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

`V2__add_email_to_users.sql`:

```sql
ALTER TABLE users
    ADD COLUMN email VARCHAR(255);
```

#### Configuration

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

When the application starts, Flyway creates a `flyway_schema_history` table, applies any pending scripts in version order, and records the result. Applied scripts are **immutable** — to change something, you add a new versioned script rather than editing an old one.

#### Versioned vs. Repeatable Migrations

* **Versioned** (`V1__...`, `V2__...`) run once, in order.
* **Repeatable** (`R__...`) re-run whenever their checksum changes — useful for views, stored procedures, and reference data.

---

### 4. Liquibase — Database-Agnostic Changelogs

Liquibase describes changes in a **changelog** using XML, YAML, JSON, or SQL. Its main strength is **database independence**: the same changelog can generate the correct SQL for PostgreSQL, MySQL, Oracle, and more.

#### Dependency

```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

#### Master Changelog

By default Spring Boot looks for `src/main/resources/db/changelog/db.changelog-master.yaml`.

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changes/001-create-users-table.yaml
  - include:
      file: db/changelog/changes/002-add-email-to-users.yaml
```

`001-create-users-table.yaml`:

```yaml
databaseChangeLog:
  - changeSet:
      id: 1
      author: donny
      changes:
        - createTable:
            tableName: users
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: username
                  type: VARCHAR(50)
                  constraints:
                    nullable: false
                    unique: true
```

`002-add-email-to-users.yaml`:

```yaml
databaseChangeLog:
  - changeSet:
      id: 2
      author: donny
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: email
                  type: VARCHAR(255)
```

#### Configuration

```properties
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
```

Each `changeSet` is identified by `id` + `author` + file path. Liquibase tracks applied change sets in the `DATABASECHANGELOG` table and uses `DATABASECHANGELOGLOCK` to prevent two instances from migrating at the same time. Like Flyway, applied change sets should be treated as **immutable**.

---

### 5. Flyway vs. Liquibase — Which Should You Choose?

| Aspect | Flyway | Liquibase |
| --- | --- | --- |
| Change format | Plain SQL (native) | XML / YAML / JSON / SQL |
| Learning curve | Very low | Moderate |
| Database independence | You write DB-specific SQL | Abstracted, DB-agnostic |
| Rollback support | Paid (Teams) or manual undo scripts | Built-in `rollback` blocks |
| Best fit | Teams comfortable writing SQL | Multi-database or complex change tracking |

**Rule of thumb:** choose **Flyway** if you like writing SQL directly and want minimal ceremony; choose **Liquibase** if you need database portability or richer rollback and change-tracking features.

---

### 6. Best Practices

* **Never edit an applied migration.** Add a new one instead — changing a checksum breaks the history.
* **Keep migrations small and focused.** One logical change per script is easier to review and roll back.
* **Version scripts in Git** alongside the code that depends on them.
* **Use `ddl-auto=validate`** in production so migrations are the single source of truth.
* **Separate schema changes from large data backfills** to avoid long-running, locking migrations.
* **Test migrations on a copy of production data** before deploying.
* **Make migrations backward-compatible** during rolling deployments (e.g., add a nullable column first, backfill, then add constraints in a later release).

---

### Summary

Schema migration tools bring the same discipline to your database that version control brings to your code. Instead of relying on Hibernate to silently guess schema changes, you define every change as an ordered, immutable, source-controlled script. **Flyway** offers a simple, SQL-first approach, while **Liquibase** provides a database-agnostic, feature-rich changelog. Either way, set `spring.jpa.hibernate.ddl-auto=validate` and let the migration tool own your schema — giving you consistent, auditable, and safe database evolution across every environment.
