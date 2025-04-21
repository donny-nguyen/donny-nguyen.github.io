## Create New Database Records

To create a new record using a model in Adonis.js (with Lucid ORM), you have several straightforward options. Here are the most common methods:

## Using the Static `create` Method

This is the simplest and most direct way. The `create` method both instantiates and persists the new record in the database in one step.

```javascript
import User from 'App/Models/User'

const user = await User.create({
  username: 'virk',
  email: 'virk@adonisjs.com',
})
console.log(user.$isPersisted) // true
```
This method returns the newly created model instance, already persisted to the database.

## Using a Model Instance and `save()`

Alternatively, you can create a new instance of the model, assign properties, and then call `save()` to persist it:

```javascript
import User from 'App/Models/User'

const user = new User()
user.username = 'virk'
user.email = 'virk@adonisjs.com'
await user.save()
console.log(user.$isPersisted) // true
```
This approach is useful if you want to set properties in multiple steps or perform logic before saving.

## Using `fill()` and `save()`

You can also use the `fill()` method to assign multiple attributes at once before saving:

```javascript
import User from 'App/Models/User'

const user = new User()
user.fill({
  username: 'virk',
  email: 'virk@adonisjs.com'
})
await user.save()
```
This is functionally similar to setting properties individually but can be more concise.

## Conditional Creation: `firstOrCreate` and `updateOrCreate`

- **firstOrCreate**: Finds the first record matching criteria or creates a new one if none is found.
- **updateOrCreate**: Finds a record and updates it, or creates a new one if it doesn't exist.

Example:

```javascript
import User from 'App/Models/User'

const user = await User.firstOrCreate(
  { email: 'virk@adonisjs.com' }, // search payload
  { username: 'virk', password: 'secret' } // creation payload
)
```

```javascript
import User from 'App/Models/User'

const searchPayload = { email: 'virk@adonisjs.com' }
const updateOrCreatePayload = { password: 'secret' }

const user = await User.updateOrCreate(searchPayload, updateOrCreatePayload)
```
These methods are useful for avoiding duplicates and handling upserts.

## Summary Table

| Method                | Description                                       | Example Syntax                                     |
|-----------------------|---------------------------------------------------|----------------------------------------------------|
| `Model.create()`      | Instantiates and saves in one step                | `await User.create({ ... })`                       |
| `new Model() + save()`| Instantiate, set properties, then save            | `const u = new User(); u.prop = val; await u.save()`|
| `fill() + save()`     | Bulk-assign properties before saving              | `u.fill({ ... }); await u.save()`                  |
| `firstOrCreate()`     | Find or create (avoids duplicates)                | `await User.firstOrCreate(search, create)`         |
| `updateOrCreate()`    | Find and update, or create if not found           | `await User.updateOrCreate(search, update)`        |

All these methods are fully supported in Adonis.js with Lucid ORM and can be used according to your use case.