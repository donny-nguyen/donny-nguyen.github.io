# Schema Helpers

Adonis.js provides a rich set of **schema helpers** via its `SchemaBuilder` API to make defining database tables intuitive and expressive. These helpers abstract common SQL types and constraints, making your migrations cleaner and more consistent.

Here’s a breakdown of some commonly used schema helpers:

---

### 📌 **Column Types**

| Helper | Description |
|--------|-------------|
| `table.increments()` | Auto-incrementing primary key (usually `id`) |
| `table.uuid('id')` | UUID column, great for distributed systems |
| `table.string('name', [length])` | VARCHAR column, default length is 255 |
| `table.text('description')` | TEXT column for longer strings |
| `table.integer('age')` | Integer column |
| `table.bigInteger('views')` | For large integer values |
| `table.float('price')` | Floating-point number |
| `table.decimal('amount', precision, scale)` | Precise decimal (e.g., money) |
| `table.boolean('is_active')` | Boolean column |
| `table.date('published_at')` | Date only |
| `table.dateTime('created_at')` | Date and time |
| `table.timestamp('updated_at')` | Timestamp with timezone |
| `table.enum('status', ['pending', 'approved', 'rejected'])` | Enum column |

---

### 🧷 **Constraints & Modifiers**

| Helper | Description |
|--------|-------------|
| `table.primary(['id'])` | Set primary key |
| `table.unique(['email'])` | Unique constraint |
| `table.index(['name'])` | Add index |
| `table.foreign('user_id').references('id').inTable('users')` | Foreign key |
| `table.notNullable()` | Disallow null values |
| `table.nullable()` | Allow null values |
| `table.defaultTo(value)` | Set default value |

---

### 🕒 **Timestamps & Soft Deletes**

- `table.timestamps()`  
  Adds `created_at` and `updated_at` columns.

- `table.timestamp('deleted_at').nullable()`  
  Used for soft deletes (marking records as deleted without removing them).

---

### 🧰 **Other Useful Helpers**

| Helper | Description |
|--------|-------------|
| `table.json('metadata')` | Store JSON data |
| `table.jsonb('settings')` | JSONB (PostgreSQL only) |
| `table.binary('file_data')` | Binary data |
| `table.specificType('column', 'custom_type')` | Define custom SQL type |

---

### Example Migration Using Helpers

```ts
import BaseSchema from '@ioc:Adonis/Lucid/Schema'

export default class extends BaseSchema {
  public async up () {
    this.schema.createTable('products', (table) => {
      table.increments('id')
      table.string('name').notNullable()
      table.decimal('price', 10, 2).defaultTo(0.00)
      table.enum('status', ['active', 'inactive']).defaultTo('active')
      table.timestamps()
    })
  }

  public async down () {
    this.schema.dropTable('products')
  }
}
```
