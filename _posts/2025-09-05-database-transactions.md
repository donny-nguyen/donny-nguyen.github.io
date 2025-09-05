# Database Transactions

In AdonisJS, database transactions are used to ensure that multiple database operations either all succeed or are rolled back if an error occurs. This is particularly useful when inserting multiple models to maintain data integrity. AdonisJS provides a straightforward way to handle transactions using its **Database** module or **Lucid ORM**. Below, I’ll explain how to manage database transactions for inserting multiple models and rollback on error.

### Using Transactions in AdonisJS

AdonisJS supports transactions through its `Database` facade or the Lucid ORM's transaction capabilities. When inserting multiple models, you can wrap the operations in a transaction block. If an error occurs, the transaction can be rolled back, ensuring no partial data is committed.

Here’s a step-by-step guide:

#### 1. **Using Lucid ORM with Transactions**
Lucid is the ORM in AdonisJS, and it provides a clean way to handle transactions for model operations. You can use the `database.transaction()` method to create a transaction scope.

```javascript
const { transaction } = require('@adonisjs/lucid/src/Database')
const ModelA = use('App/Models/ModelA')
const ModelB = use('App/Models/ModelB')

async function createMultipleRecords(dataA, dataB) {
  const trx = await Database.transaction() // Start a transaction

  try {
    // Insert into ModelA
    const recordA = await ModelA.create(dataA, trx)

    // Insert into ModelB
    const recordB = await ModelB.create(dataB, trx)

    // Commit the transaction if all operations succeed
    await trx.commit()

    return { recordA, recordB }
  } catch (error) {
    // Rollback the transaction on error
    await trx.rollback()
    throw new Error(`Failed to insert records: ${error.message}`)
  }
}
```

**Key Points:**
- The `trx` object is passed as the second argument to `Model.create()` or other Lucid methods (e.g., `save`, `createMany`).
- If any operation fails, the `catch` block triggers a rollback, undoing all changes made within the transaction.
- Use `trx.commit()` to finalize the transaction when all operations succeed.

#### 2. **Using `createMany` for Bulk Inserts**
If you need to insert multiple records into the same model, you can use `createMany` within a transaction:

```javascript
const { transaction } = require('@adonisjs/lucid/src/Database')
const ModelA = use('App/Models/ModelA')

async function createMultipleRecords(dataArray) {
  const trx = await Database.transaction()

  try {
    // Bulk insert multiple records
    const records = await ModelA.createMany(dataArray, trx)

    // Commit the transaction
    await trx.commit()

    return records
  } catch (error) {
    // Rollback on error
    await trx.rollback()
    throw new Error(`Failed to insert records: ${error.message}`)
  }
}
```

**Note:** `dataArray` should be an array of objects containing the data for each record.

#### 3. **Using Raw Queries with Transactions**
If you’re working with raw queries or need to combine raw queries with Lucid models, you can use the `trx` object for raw queries as well:

```javascript
const Database = use('Database')

async function createWithRawQuery(dataA, dataB) {
  const trx = await Database.transaction()

  try {
    // Insert into ModelA using Lucid
    const recordA = await ModelA.create(dataA, trx)

    // Insert using a raw query
    await trx.raw('INSERT INTO table_b (column1, column2) VALUES (?, ?)', [
      dataB.column1,
      dataB.column2,
    ])

    // Commit the transaction
    await trx.commit()

    return { recordA, message: 'Raw query executed' }
  } catch (error) {
    // Rollback on error
    await trx.rollback()
    throw new Error(`Failed to insert records: ${error.message}`)
  }
}
```

#### 4. **Using Transactions with Relations**
If your models have relationships (e.g., one-to-many, many-to-many), you can handle related data within the same transaction:

```javascript
const Database = use('Database')
const User = use('App/Models/User')
const Post = use('App/Models/Post')

async function createUserWithPosts(userData, postsData) {
  const trx = await Database.transaction()

  try {
    // Create a user
    const user = await User.create(userData, trx)

    // Create related posts
    await user.posts().createMany(postsData, trx)

    // Commit the transaction
    await trx.commit()

    return { user, posts: await user.posts().fetch() }
  } catch (error) {
    // Rollback on error
    await trx.rollback()
    throw new Error(`Failed to create user and posts: ${error.message}`)
  }
}
```

**Key Points:**
- Use `user.posts().createMany(postsData, trx)` to associate the transaction with related model operations.
- Ensure all related operations use the same `trx` instance.

#### 5. **Best Practices**
- **Always Commit or Rollback:** Explicitly call `trx.commit()` or `trx.rollback()` to avoid leaving transactions open, which can lock database resources.
- **Error Handling:** Wrap operations in a `try-catch` block to catch errors and trigger a rollback.
- **Transaction Scope:** Ensure all database operations (Lucid or raw queries) within the transaction use the `trx` object.
- **Avoid Nested Transactions:** AdonisJS does not support nested transactions by default. If you need complex transaction logic, consider breaking it into smaller, independent transactions.
- **Connection Management:** Ensure your database configuration (in `config/database.js`) supports transactions for your database (e.g., PostgreSQL, MySQL).

#### 6. **Example Controller**
Here’s how you might use transactions in a controller:

```javascript
const Database = use('Database')
const User = use('App/Models/User')

class UserController {
  async store({ request, response }) {
    const userData = request.only(['username', 'email', 'password'])
    const profileData = request.only(['first_name', 'last_name'])

    const trx = await Database.transaction()

    try {
      const user = await User.create(userData, trx)
      await user.profile().create(profileData, trx)

      await trx.commit()

      return response.status(201).json({ user, profile: await user.profile().fetch() })
    } catch (error) {
      await trx.rollback()
      return response.status(500).json({ error: `Failed to create user: ${error.message}` })
    }
  }
}
```

#### 7. **Testing Transactions**
When testing, you can use AdonisJS’s testing tools to ensure transactions work as expected. Use the `DatabaseTransactions` trait in your tests to automatically wrap each test in a transaction that rolls back after completion:

```javascript
const { test, trait } = use('Test/Suite')('User Creation')

test('it creates a user and profile within a transaction', async ({ assert }) => {
  trait('DatabaseTransactions')

  const userData = { username: 'john', email: 'john@example.com', password: 'secret' }
  const profileData = { first_name: 'John', last_name: 'Doe' }

  const response = await supertest(app).post('/users').send({ user: userData, profile: profileData })

  assert.equal(response.status, 201)
  assert.exists(response.body.user)
  assert.exists(response.body.profile)
})
```

### Additional Notes
- **Database Support:** Ensure your database engine supports transactions (e.g., PostgreSQL, MySQL with InnoDB). SQLite also supports transactions but has limitations with concurrent writes.
- **Performance:** Transactions can lock tables, so avoid long-running transactions in high-concurrency environments.
- **AdonisJS Version:** The examples above are based on AdonisJS v4. If you’re using AdonisJS v5, the syntax is slightly different due to its modular structure. For example:

  ```javascript
  // AdonisJS v5
  import Database from '@ioc:Adonis/Lucid/Database'
  import User from 'App/Models/User'

  async function createUser(userData) {
    const trx = await Database.transaction()
    try {
      const user = await User.create(userData, { client: trx })
      await trx.commit()
      return user
    } catch (error) {
      await trx.rollback()
      throw error
    }
  }
  ```

  In v5, you pass the transaction client as `{ client: trx }` instead of directly as the second argument.

### Summary
To manage database transactions in/AdonisJS for inserting multiple models:
1. Start a transaction with `Database.transaction()`.
2. Use the `trx` object for all model operations (`create`, `createMany`, etc.) or raw queries.
3. Commit the transaction with `trx.commit()` on success.
4. Rollback with `trx.rollback()` on error.
5. Handle relationships and bulk inserts within the same transaction for consistency.

This approach ensures that if any error occurs during the insertion of multiple models, all changes are rolled back, maintaining database integrity.