# Search for Records with Lucid ORM

In Adonis.js, using Lucid ORM to search for a record is super intuitive. Here are a few common ways to do it:

### 🔍 Basic Lookups

- **Find by primary key**:
  ```ts
  const user = await User.find(1)
  ```

- **Find by column value**:
  ```ts
  const user = await User.findBy('email', 'dung@example.com')
  ```

- **Find multiple by primary keys**:
  ```ts
  const users = await User.findMany([1, 2, 3])
  ```

- **Find the first record**:
  ```ts
  const user = await User.first()
  ```

### 🧠 Using Query Builder

For more complex queries:

```ts
const user = await User.query()
  .where('username', 'dungdev')
  .andWhere('is_active', true)
  .first()
```

You can also use `firstOrFail()` or `findOrFail()` if you want it to throw an error when no record is found.

### 💡 Bonus: Partial Match (LIKE)

```ts
const users = await User.query()
  .where('email', 'like', '%@gmail.com')
  .fetch()
```

Lucid ORM packs some serious power under the hood beyond the usual CRUD and relationship handling. Here are some **additional advanced features** that can really elevate your Adonis.js projects:

---

### 🧠 **SQL Superpowers**
Lucid is built on top of Knex, so it supports:
- **Window functions** – for ranking, running totals, etc.
- **Recursive CTEs** – great for hierarchical data like categories or org charts.
- **Row-level locking** – using `.forUpdate()` or `.forShare()` to prevent race conditions.
- **JSON column operations** – query and manipulate JSON fields directly in SQL.

---

### 🧰 **Read/Write Replicas**
You can configure **read-write splitting** to scale your app:
```ts
{
  connection: 'mysql',
  replicas: {
    write: { host: 'write-db' },
    read: [{ host: 'read-db-1' }, { host: 'read-db-2' }]
  }
}
```
Lucid will automatically route queries to the appropriate replica.

---

### 🧪 **Model Factories & Seeders**
Generate fake data for testing or development:
```ts
const user = await Factory.model('App/Models/User').create()
```
Perfect for simulating real-world scenarios or populating dev environments.

---

### 🪝 **Lifecycle Hooks**
Tap into model events like `beforeCreate`, `afterSave`, etc.:
```ts
@beforeSave()
public static async hashPassword(user: User) {
  if (user.$dirty.password) {
    user.password = await Hash.make(user.password)
  }
}
```

---

### 🧩 **Custom Serialization**
Control how models are converted to JSON:
```ts
public serializeExtras = true
public serializeComputed = true
```
You can also define computed properties and hide sensitive fields.

---

### 🧬 **Multiple Connections**
Need to connect to more than one database? Lucid supports multiple named connections, so you can query across different DBs in the same app.

---

You can also check out the [Lucid documentation](https://lucid.adonisjs.com/docs/introduction) for a deeper dive.