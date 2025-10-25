# Parsing Request Parameters in a Controller Method

In Adonis.js, you can parse request parameters in a controller method using the `request` object provided by the framework. The `request` object gives you access to various methods to retrieve parameters from the URL, query string, or request body. Here's how you can do it:

### 1. **Accessing Route Parameters**
If your route contains dynamic segments (e.g., `/users/:id`), you can access them using `request.params()` or directly via the `params` property in the controller method.

#### Example Route
```javascript
Route.get('/users/:id', 'UserController.show')
```

#### Controller Method
```javascript
class UserController {
  async show({ params }) {
    const userId = params.id // Access the 'id' parameter
    console.log(userId)
    return `User ID: ${userId}`
  }
}
```

Here, `params.id` retrieves the value of the `id` segment from the URL (e.g., `/users/123` → `123`).

---

### 2. **Accessing Query Parameters**
Query parameters (e.g., `/users?name=John`) can be accessed using `request.input('key')`.

#### Example URL
```
GET /users?name=John&age=25
```

#### Controller Method
```javascript
class UserController {
  async index({ request }) {
    const name = request.input('name') // Get 'name' query param
    const age = request.input('age')   // Get 'age' query param
    console.log(name, age)
    return `Name: ${name}, Age: ${age}`
  }
}
```

---

### 3. **Accessing Request Body (POST/PUT Requests)**
For parsing data from the request body (e.g., in a POST request), you can use `request.body()` or `request.all()`.

#### Example Route
```javascript
Route.post('/users', 'UserController.store')
```

#### Controller Method
```javascript
class UserController {
  async store({ request }) {
    const data = request.all() // Get all fields from the request body
    console.log(data)
    return data
  }
}
```

If you send a JSON payload like:
```json
{
  "name": "John",
  "email": "john@example.com"
}
```
`request.all()` will return `{ name: 'John', email: 'john@example.com' }`.

You can also access specific fields:
```javascript
const name = request.input('name') // Get 'name' from body
```

---

### 4. **Using Type Validation (Optional)**
Adonis.js provides a powerful validator to ensure the request params/body meet your requirements. You can use it in the controller like this:

#### Example with Validation
```javascript
const { validate } = use('Validator')

class UserController {
  async store({ request, response }) {
    const rules = {
      name: 'required|string',
      email: 'required|email|unique:users',
    }

    const validation = await validate(request.all(), rules)

    if (validation.fails()) {
      return response.status(400).send(validation.messages())
    }

    const data = request.only(['name', 'email']) // Extract only specific fields
    console.log(data)
    return data
  }
}
```

- `request.only(['key1', 'key2'])`: Extracts specific fields from the request body.
- The validator ensures the data conforms to the defined `rules`.

---

### Summary of Methods
- `params`: For route parameters (e.g., `:id`).
- `request.input('key')`: For query params or body fields.
- `request.all()`: For all body fields.
- `request.only(['key1', 'key2'])`: For specific fields from the body.
- `request.qs()`: For query string object by reference