# Inserting and Querying with Partition Key and Sort Key

Below is a clean, practical guide to **inserting** and **querying** items in DynamoDB when your table uses a **partition key + sort key**. This fits naturally with the table you created earlier (`userId` as HASH, `createdAt` as RANGE).

---

# Inserting Items (PutItem)

You can insert items using `PutItemCommand` from AWS SDK v3.

```js
import { DynamoDBClient, PutItemCommand } from "@aws-sdk/client-dynamodb";

const client = new DynamoDBClient({ region: "us-east-1" });

async function insertUserEvent() {
  const command = new PutItemCommand({
    TableName: "Users",
    Item: {
      userId: { S: "123" },
      createdAt: { S: new Date().toISOString() },
      action: { S: "LOGIN" },
      ip: { S: "192.168.1.10" }
    }
  });

  await client.send(command);
  console.log("Item inserted");
}

insertUserEvent();
```

### Notes
- You must provide **both** the partition key and sort key.
- Additional attributes are flexible (DynamoDB is schemaless).

---

# Querying Items by Partition Key

To fetch all items for a given user:

```js
import { QueryCommand } from "@aws-sdk/client-dynamodb";

async function getUserEvents(userId) {
  const command = new QueryCommand({
    TableName: "Users",
    KeyConditionExpression: "userId = :uid",
    ExpressionAttributeValues: {
      ":uid": { S: userId }
    }
  });

  const result = await client.send(command);
  return result.Items;
}

getUserEvents("123").then(console.log);
```

### What this does
- Returns **all items** where `userId = "123"`.
- Results are automatically sorted by `createdAt` (ascending).

---

# Querying by Partition Key + Sort Key Range

This is where sort keys shine.

### Example: Get events between two timestamps

```js
async function getEventsInRange(userId, start, end) {
  const command = new QueryCommand({
    TableName: "Users",
    KeyConditionExpression: "userId = :uid AND createdAt BETWEEN :start AND :end",
    ExpressionAttributeValues: {
      ":uid": { S: userId },
      ":start": { S: start },
      ":end": { S: end }
    }
  });

  const result = await client.send(command);
  return result.Items;
}

getEventsInRange(
  "123",
  "2025-01-01T00:00:00Z",
  "2025-01-02T00:00:00Z"
).then(console.log);
```

---

# Querying with `begins_with` (prefix search)

Useful for time‑series data if you store timestamps like `2025-01-01T...`.

```js
async function getEventsForDay(userId, dayPrefix) {
  const command = new QueryCommand({
    TableName: "Users",
    KeyConditionExpression: "userId = :uid AND begins_with(createdAt, :day)",
    ExpressionAttributeValues: {
      ":uid": { S: userId },
      ":day": { S: dayPrefix } // e.g. "2025-01-01"
    }
  });

  const result = await client.send(command);
  return result.Items;
}

getEventsForDay("123", "2025-01-01").then(console.log);
```

---

# Querying in Descending Order

By default, DynamoDB sorts ascending. To reverse:

```js
const command = new QueryCommand({
  TableName: "Users",
  KeyConditionExpression: "userId = :uid",
  ExpressionAttributeValues: {
    ":uid": { S: "123" }
  },
  ScanIndexForward: false // descending
});
```

---

# Querying Only the Latest Item

This is extremely common (e.g., latest login event):

```js
const command = new QueryCommand({
  TableName: "Users",
  KeyConditionExpression: "userId = :uid",
  ExpressionAttributeValues: {
    ":uid": { S: "123" }
  },
  ScanIndexForward: false, // newest first
  Limit: 1
});
```
