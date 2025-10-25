# Lambda Proxy Integration

Lambda proxy integration is a powerful feature of Amazon API Gateway that allows you to directly pass the entire HTTP request from API Gateway to your AWS Lambda function. This gives your function full control over handling the request and crafting the response.

Here’s a breakdown of how it works and why it’s useful:

---

### 🔄 What Is Lambda Proxy Integration?

Lambda proxy integration is a mode in API Gateway where:

- **The entire HTTP request** (including headers, query parameters, path variables, and body) is passed to the Lambda function as a JSON object.
- **The Lambda function** is responsible for:
  - Parsing the request
  - Performing logic
  - Returning a properly formatted HTTP response

---

### 🧾 Request Format

When using proxy integration, the Lambda function receives an event object like this:

```json
{
  "resource": "/{proxy+}",
  "path": "/example/path",
  "httpMethod": "GET",
  "headers": { ... },
  "queryStringParameters": { ... },
  "pathParameters": { ... },
  "body": "{...}",
  "isBase64Encoded": false
}
```

This gives your function full visibility into the HTTP request.

---

### 📤 Response Format

Your Lambda function must return a response in this format:

```json
{
  "statusCode": 200,
  "headers": {
    "Content-Type": "application/json"
  },
  "body": "{\"message\": \"Hello World\"}",
  "isBase64Encoded": false
}
```

This allows you to control the HTTP status code, headers, and body content.

---

### ✅ Benefits

- **Full control** over request/response handling
- **Simplified routing**: one Lambda function can handle multiple endpoints
- **Flexible**: supports any HTTP method and custom logic

---

### ⚠️ Considerations

- You must manually parse and validate inputs.
- You’re responsible for crafting valid HTTP responses.
- Debugging can be trickier due to the raw nature of the event object.