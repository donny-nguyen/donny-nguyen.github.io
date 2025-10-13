# Set up API Gateway for Lambda Function

**To set up API Gateway for a Lambda function, you can either use the Lambda console for quick setup or the API Gateway console for more customization.**

Here’s a detailed walkthrough of both methods:

---

### 🛠️ Method 1: Setup via Lambda Console (Quick & Easy)

1. **Create your Lambda function**  
   - Go to the AWS Lambda console.
   - Click “Create function” and configure runtime, permissions, and code.

2. **Add API Gateway trigger**  
   - In the Lambda function page, scroll to “Function overview.”
   - Click “+ Add trigger” → Select **API Gateway**.
   - Choose **Create an API** → Select **HTTP API** (lightweight) or **REST API** (more features).
   - Click “Add” to create and link the API Gateway.

3. **Test your endpoint**  
   - After setup, you’ll see an **Invoke URL** under the trigger.
   - Use tools like Postman or curl to send HTTP requests to this URL.

---

### ⚙️ Method 2: Setup via API Gateway Console (Customizable)

1. **Create an API**
   - Go to the API Gateway console.
   - Choose **HTTP API** or **REST API** depending on your needs.
   - Click “Create API” and name it.

2. **Add a resource and method**
   - For REST API: Create a resource (e.g., `/hello`) and add a method (e.g., `GET` or `POST`).
   - For HTTP API: Define routes directly (e.g., `/hello` with `POST` method).

3. **Integrate with Lambda**
   - Choose **Lambda function** as the integration type.
   - Select your function and grant API Gateway permission to invoke it.

4. **Deploy the API**
   - Create a **stage** (e.g., `dev`, `prod`) and deploy your API.
   - You’ll get a public **Invoke URL** for testing.

---

### 🔐 Optional Enhancements

- **Enable CORS** if your API will be accessed from browsers.
- **Add authentication** using IAM, Cognito, or custom authorizers.
- **Monitor usage** with CloudWatch metrics and logging.

---

### 📚 Helpful Resources

- [AWS Official Guide](https://docs.aws.amazon.com/lambda/latest/dg/services-apigateway.html)
- [Step-by-step tutorial with examples](https://thelinuxcode.com/how-to-trigger-aws-lambda-using-api-gateway/)
- [Baeldung’s integration overview](https://www.baeldung.com/aws-lambda-api-gateway)