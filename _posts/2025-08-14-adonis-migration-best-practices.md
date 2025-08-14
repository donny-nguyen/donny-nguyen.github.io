# Adonis.js Migrations Best Practices

Working with **Adonis.js migrations** effectively ensures your database schema evolves smoothly alongside your application. Here are some best practices to keep your migrations clean, maintainable, and safe:

---

### 🧱 1. **Use Descriptive Migration Names**
- Name migrations based on what they do:  
  ```bash
  node ace make:migration create_users_table
  node ace make:migration add_status_to_orders
  ```
- This helps you and your team understand the purpose at a glance.

---

### 🔄 2. **Keep Migrations Reversible**
- Always define both `up` and `down` methods:
  ```ts
  public async up () {
    this.schema.createTable('users', (table) => {
      table.increments()
      table.string('email').notNullable()
    })
  }

  public async down () {
    this.schema.dropTable('users')
  }
  ```
- This allows you to rollback changes safely during development or deployment.

---

### 🧪 3. **Run Migrations in Development Frequently**
- Use `node ace migration:run` and `node ace migration:rollback` often to test schema changes.
- Catch issues early before they hit production.

---

### 🧼 4. **Avoid Editing Old Migrations**
- Once a migration is committed and possibly deployed, **never edit it**.
- Instead, create a new migration to modify the schema (e.g., add/remove columns).

---

### 🧩 5. **Use Schema Helpers Thoughtfully**
- Adonis provides helpers like `table.timestamps()` and `table.enum()`. Use them for consistency.
- Prefer `table.uuid()` for IDs if you're working with distributed systems.

---

### 🛡️ 6. **Validate Before Applying**
- Always check your migration logic against your models and relationships.
- Use `migration:status` to verify which migrations have run.

---

### 📦 7. **Seed Data Separately**
- Don’t insert data in migrations. Use **seeds** for populating initial or test data:
  ```bash
  node ace make:seed UserSeeder
  node ace db:seed
  ```

---

### 🧠 8. **Use Transactions for Safety**
- Adonis automatically wraps migrations in transactions (if supported by the DB).
- This ensures partial migrations don’t corrupt your schema.

---

### 🧭 9. **Track Migration History**
- Use version control (Git) to track migration changes.
- Consider tagging releases that include schema changes for easier rollback.

---

### 🧰 10. **Use Environment-Specific Databases**
- Never run destructive migrations on production without backups.
- Use `.env` files to isolate dev, staging, and prod environments.
