# Creating a DynamoDB Table with Sort Key

You can create a DynamoDB table with a **partition key + sort key** using JavaScript and the **AWS SDK for JavaScript v3**. Since you’re already deep in backend and AWS work, this will feel very natural.

Below is a clean, production‑ready example.

---

# Creating a DynamoDB Table with Sort Key (AWS SDK v3)

## 1. Install the SDK
```bash
npm install @aws-sdk/client-dynamodb
```

## 2. Create the table
```js
import { DynamoDBClient, CreateTableCommand } from "@aws-sdk/client-dynamodb";

const client = new DynamoDBClient({ region: "us-east-1" });

async function createTable() {
  const command = new CreateTableCommand({
    TableName: "Users",
    AttributeDefinitions: [
      { AttributeName: "userId", AttributeType: "S" },   // partition key
      { AttributeName: "createdAt", AttributeType: "S" } // sort key
    ],
    KeySchema: [
      { AttributeName: "userId", KeyType: "HASH" },      // partition key
      { AttributeName: "createdAt", KeyType: "RANGE" }   // sort key
    ],
    BillingMode: "PAY_PER_REQUEST"
  });

  try {
    const result = await client.send(command);
    console.log("Table created:", result);
  } catch (err) {
    console.error("Error creating table:", err);
  }
}

createTable();
```

---

# What’s happening here

### **AttributeDefinitions**
Defines the data types:
- `S` = string  
- `N` = number  
- `B` = binary  

### **KeySchema**
- `HASH` → partition key  
- `RANGE` → sort key  

### **BillingMode**
`PAY_PER_REQUEST` is great for dev or unpredictable workloads.

---

# Quick mental model for sort keys

Use a sort key when you want:

- Multiple items under the same partition key  
- Time‑ordered queries  
- Range queries (`BETWEEN`, `begins_with`)  
- Time‑series data (events, logs, messages)

Example item:
```json
{
  "userId": "123",
  "createdAt": "2025-01-01T10:00:00Z"
}
```
