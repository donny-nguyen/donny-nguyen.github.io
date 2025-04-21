## Overview of Adonis.js Migrations

Adonis.js migrations are a system for managing and version-controlling your database schema directly from your application code, instead of manually writing SQL queries. Migrations allow you to create, alter, and drop tables and other database structures using programmatic scripts, ensuring that your database schema evolves in sync with your application code.

## How Migrations Work

- Each migration is an independent script, typically written in TypeScript (in AdonisJS v5 and above), that defines changes to your database schema.
- Migrations are stored in the `database/migrations` directory, and each file is timestamped to ensure proper order of execution.
- The migration system keeps track of which migrations have been run using a dedicated table in your database (e.g., `adonis_schema_versions`), ensuring migrations are only applied once.

### Migration Lifecycle

- **Creating Migrations:** Use the Ace CLI command `make:migration` to generate a new migration file.
- **Running Migrations:** Apply all pending migrations with `migration:run`. This executes the `up` method in each migration file, making the intended schema changes.
- **Rolling Back:** Undo the last batch of migrations with `migration:rollback`, which runs the `down` method to revert changes. This is useful during development, but should be used cautiously in production to avoid data loss.
- **Status and History:** Check which migrations have been applied or are pending with `migration:status`. Migrations are grouped into batches, making it easier to manage rollbacks.

## Best Practices

- **Incremental Changes:** Always create a new migration file for each schema change (e.g., adding a column, renaming a field) rather than modifying existing migration files. This approach provides clear version history and makes it easier to collaborate in teams.
- **One Change per Migration:** Avoid making multiple, unrelated changes in a single migration file. Keeping migrations atomic improves traceability and rollback safety.
- **Production Considerations:** Rolling back migrations in production can result in data loss. For production environments, always create new migrations to alter existing tables rather than rolling back and editing past migrations.

## Migration Commands (Ace CLI)

| Command                   | Description                                      |
|---------------------------|--------------------------------------------------|
| `make:migration`          | Create a new migration file                      |
| `migration:run`           | Run all pending migrations                       |
| `migration:rollback`      | Roll back the last batch of migrations           |
| `migration:status`        | Show the status of all migrations                |
| `migration:reset`         | Roll back all migrations                         |

## Example Migration File Structure

A migration file typically contains two methods:

```typescript
export default class UsersTable {
  public async up () {
    // Code to create or alter tables
  }

  public async down () {
    // Code to revert the changes made in up()
  }
}
```

- The `up` method defines the changes to apply (e.g., creating a table).
- The `down` method undoes those changes (e.g., dropping the table).

## Migration in AdonisJS v5 and v6

- **AdonisJS v5:** Migrations are written in TypeScript, and the system is tightly integrated with the Lucid ORM.
- **AdonisJS v6:** The migration process remains similar, but there are significant changes to module imports and the internal IoC container. An official upgrade kit is available to help migrate projects from v5 to v6, but some community packages may need time to become fully compatible.

## Summary

Adonis.js migrations provide a robust, programmatic way to manage your database schema, supporting collaborative development and safe schema evolution. They are essential for maintaining consistency between your codebase and database, especially as your project grows and changes over time.