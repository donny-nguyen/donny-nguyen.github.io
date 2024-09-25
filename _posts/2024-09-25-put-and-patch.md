# PUT and PATCH in RESTful APIs

Both `PUT` and `PATCH` are HTTP methods used in RESTful APIs to update resources, but they have some key differences:

### **PUT**
- **Purpose**: Used to update a resource or create a resource if it does not exist.
- **Operation**: Replaces the entire resource with the new data provided.
- **Idempotency**: PUT requests are idempotent, meaning multiple identical requests will have the same effect as a single request.
- **Example**: If we have a user resource and we send a PUT request to update the user's information, we need to provide the entire user object, not just the fields we want to update.

```http
PUT /users/123
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "age": 30
}
```

### **PATCH**
- **Purpose**: Used to apply partial updates to a resource.
- **Operation**: Only updates the fields provided in the request, leaving the rest of the resource unchanged.
- **Idempotency**: PATCH requests are not necessarily idempotent, meaning multiple identical requests could have different effects.
- **Example**: If we want to update just the email of a user, we can send a PATCH request with only the email field.

```http
PATCH /users/123
{
  "email": "john.newemail@example.com"
}
```

In summary, use `PUT` when we need to replace the entire resource and `PATCH` when we only need to update specific fields.
