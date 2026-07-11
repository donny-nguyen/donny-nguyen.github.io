# Invoke Lambda Aliases Using Stage Variables in API Gateway

When you version an AWS Lambda function and expose it through Amazon API Gateway, you often want a single API to point at different Lambda aliases (for example `dev`, `test`, and `prod`) without duplicating your API definition. **Stage variables** make this possible: each API Gateway stage can inject a value that decides which Lambda alias is invoked.

This article walks through the approach demonstrated by Federico Tartarini in his tutorial [*Invoke Lambda aliases linked to a version using stage variables in AWS API Gateway*](https://www.youtube.com/watch?v=l9w-J9RuV2w).

---

### 🎯 What You Will Learn

- How Lambda **versions** and **aliases** work together.
- How to connect a single API Gateway to multiple aliases.
- How **stage variables** route each API Gateway stage to the correct alias.

---

### 🧩 Key Concepts

**Lambda versions** are immutable snapshots of your function code and configuration. Once published, a version (`1`, `2`, `3`, …) never changes, which makes deployments predictable and easy to roll back.

**Lambda aliases** are named pointers to a specific version — for example an alias named `prod` might point to version `3`, while `dev` points to version `5`. Clients invoke the alias, and you simply repoint the alias when you promote new code.

**API Gateway stage variables** are name–value pairs configured per stage (`dev`, `test`, `prod`). They can be referenced inside the integration configuration so that the same API method behaves differently depending on which stage handles the request.

---

### 🛠️ Step-by-Step Walkthrough

#### 1. Create the Lambda function

Create a simple Lambda function in the console and add some code that returns a response you can distinguish between versions (for example, a message that mentions the environment).

#### 2. Test the function

Run a test event to confirm the function returns the expected output before adding versions and aliases.

#### 3. Publish a version

From the function page, choose **Actions → Publish new version**. Publishing freezes the current code as an immutable version (e.g. version `1`).

#### 4. Create an alias

Create an alias (e.g. `dev`) and point it at the published version. Repeat as needed so you have aliases such as `dev`, `test`, and `prod`, each pointing to the version you want that environment to run.

#### 5. Create the API Gateway

Create a **REST API** in the API Gateway console. This API will be shared across all environments.

#### 6. Create a resource

Add a resource (for example `/hello`) that represents the endpoint you want to expose.

#### 7. Create a method with a stage variable

Add a method (such as `GET`) and set the integration type to **Lambda Function**. Instead of hard-coding the function name, reference the alias through a stage variable:

```
my-function:${stageVariables.lambdaAlias}
```

API Gateway substitutes `${stageVariables.lambdaAlias}` at runtime with the value defined for the current stage, so the same method invokes a different alias per stage.

> ⚠️ When you use a stage variable in the integration, API Gateway cannot automatically add the invoke permission. You must grant each alias permission to be invoked by API Gateway using the `lambda:AddPermission` API (or the AWS CLI `aws lambda add-permission` command).

#### 8. Deploy the API and create stages

Deploy the API and create the stages that mirror your aliases — for example `dev`, `test`, and `prod`. Each deployment stage gets its own **Invoke URL**.

#### 9. Add stage variables to each stage

On every stage, open the **Stage Variables** tab and define the variable used in the method — for example `lambdaAlias` = `dev` on the dev stage, `lambdaAlias` = `prod` on the prod stage, and so on.

---

### ✅ The Result

With this setup you get a **single API definition** serving multiple environments:

| Stage | Invoke URL suffix | Stage variable `lambdaAlias` | Lambda alias invoked |
|-------|-------------------|------------------------------|----------------------|
| dev   | `/dev/hello`      | `dev`                        | `dev` → version X    |
| test  | `/test/hello`     | `test`                       | `test` → version Y   |
| prod  | `/prod/hello`     | `prod`                       | `prod` → version Z   |

Calling each stage's URL routes the request to the matching Lambda alias, letting you promote code by simply repointing an alias — no changes to the API required.

---

### 💡 Why This Pattern Is Useful

- **Single source of truth:** one API to maintain instead of one per environment.
- **Safe promotions:** move code between environments by updating an alias.
- **Easy rollbacks:** repoint an alias to a previous version instantly.
- **Clean separation:** each stage stays isolated through its own stage variable.

---

### 📚 References

- [Original video by Federico Tartarini](https://www.youtube.com/watch?v=l9w-J9RuV2w)
- [AWS Lambda function versions](https://docs.aws.amazon.com/lambda/latest/dg/configuration-versions.html)
- [AWS Lambda function aliases](https://docs.aws.amazon.com/lambda/latest/dg/configuration-aliases.html)
- [API Gateway stage variables](https://docs.aws.amazon.com/apigateway/latest/developerguide/stage-variables.html)
