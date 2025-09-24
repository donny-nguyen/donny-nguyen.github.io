# Manage Extra Columns of Pivot Table

In Adonis.js, when working with many-to-many relationships, you can manage **extra columns on the pivot table** using the `pivotAttributes` and `pivotColumns` features provided by Lucid ORM. Here's how to **save** and **update** those extra columns.

---

### 🛠 Setup: Define the Relationship with Pivot Columns

Suppose you have `User` and `Project` models with a pivot table `project_user` that includes an extra column like `role`.

In your `User` model:

```ts
@manyToMany(() => Project, {
  pivotTable: 'project_user',
  pivotColumns: ['role'], // declare extra columns
})
public projects: ManyToMany<typeof Project>
```

---

### ✅ Saving with Extra Pivot Columns

To attach a project to a user with a specific role:

```ts
const user = await User.findOrFail(1)

await user.related('projects').attach({
  [projectId]: {
    role: 'admin', // extra pivot column
  },
})
```

You can also attach multiple projects with different roles:

```ts
await user.related('projects').attach({
  1: { role: 'admin' },
  2: { role: 'viewer' },
})
```

---

### 🔄 Updating Extra Pivot Columns

To update the `role` for an existing pivot record:

```ts
await user.related('projects').updatePivot(projectId, {
  role: 'editor',
})
```

---

### 📥 Fetching Pivot Data

When retrieving related records, include pivot data:

```ts
const user = await User.query()
  .where('id', 1)
  .preload('projects', (query) => {
    query.pivotColumns(['role'])
  })
  .firstOrFail()

user.projects.forEach((project) => {
  console.log(project.$extras.role) // access pivot column
})
```

---

### 🧠 Bonus Tips

- Use `$extras` to access pivot columns on the related model.
- You can also use `sync()` to update multiple pivot records at once.
- Always declare `pivotColumns` in the relationship to make them accessible.