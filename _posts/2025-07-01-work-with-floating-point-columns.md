# Working with Floating Point Columns

To work with **floating point columns** in MySQL using **Adonis.js**, you’ll typically interact with them in two main contexts: **migrations** and **model usage**. Here's how to handle both:

---

### 🛠️ Defining Float Columns in Migrations

When creating or altering tables with Lucid's schema builder, use the `.float()` method:

```ts
import BaseSchema from '@ioc:Adonis/Lucid/Schema'

export default class Products extends BaseSchema {
  public async up () {
    this.schema.createTable('products', (table) => {
      table.increments('id')
      table.float('price') // Default precision: 8, scale: 2
      table.float('discount', 10, 4) // Custom precision and scale
    })
  }

  public async down () {
    this.schema.dropTable('products')
  }
}
```

- **Precision**: total number of digits.
- **Scale**: number of digits after the decimal point.

For example, `float('discount', 10, 4)` allows values like `123456.7890`.

---

### 🧠 Using Float Columns in Models

Lucid handles float columns automatically. In your model, you don’t need to do anything special unless you want to customize serialization or transformation:

```ts
import { BaseModel, column } from '@ioc:Adonis/Lucid/Orm'

export default class Product extends BaseModel {
  @column()
  public price: number

  @column()
  public discount: number
}
```

If you want to ensure precision when reading or writing, you can use the `prepare` and `consume` options:

```ts
@column({
  prepare: (value: number) => parseFloat(value.toFixed(4)),
  consume: (value: number) => Number(value),
})
public discount: number
```

- **`prepare`**: Transforms the value *before* it's saved to the database. Useful for rounding, formatting, or converting types (e.g. `toFixed(4)`).
- **`consume`**: Transforms the value *after* it's read from the database, before it's exposed in your model. Handy for parsing or cleanup (e.g. ensuring it's a `number`).

---

### ⚠️ Gotchas to Watch For

- **Rounding errors**: Floats can introduce precision issues. For financial data, consider using `.decimal()` instead.
- **MySQL behavior**: MySQL may round or truncate float values depending on column definition and input. Always test edge cases.

---

If you're doing calculations or comparisons in queries, I can show you how to use raw SQL or query builder expressions too. Want to go there next?