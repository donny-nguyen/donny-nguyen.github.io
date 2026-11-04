# Events vs. Commands vs. Messages

In event-driven systems, the words *event*, *command*, and *message* are often used as if they mean the same thing. They don't. Mixing them up leads to confusing topic names, tightly coupled services, and integration bugs that are hard to trace. This article explains what each term means, how they differ, and how to choose the right one when you design communication between services.

## The Short Version

- A **message** is the envelope: any piece of data sent from one component to another through a channel.
- A **command** is a message that asks a specific receiver to **do something**.
- An **event** is a message that announces that **something has already happened**.

So commands and events are both kinds of messages. The difference is in their **intent**: a command looks forward to an action, and an event looks back at a fact.

```
            Message
           /       \
      Command      Event
  "Do this"        "This happened"
```

Some authors also add a third kind, the **query** ("tell me something"), which asks for data without changing state. Queries are usually synchronous request/response calls, so this article focuses on commands and events.

## Messages

A message is the basic unit of communication in a messaging system. It usually has two parts:

- **Headers (metadata)**: A message ID, a timestamp, a type, a correlation ID, a source, and sometimes a schema version.
- **Body (payload)**: The actual data, encoded as JSON, Avro, Protobuf, or another format.

A message on its own says nothing about intent. Whether it is a command or an event depends on what it means and how the sender and receiver treat it. Message brokers such as Apache Kafka, RabbitMQ, Amazon SQS, and Amazon SNS move messages without caring which kind they are.

```json
{
  "headers": {
    "messageId": "7f3c9a2e-1b4d-4e8a-9c21-5d6f7a8b9c0d",
    "type": "OrderPlaced",
    "timestamp": "2026-11-04T10:15:30Z",
    "correlationId": "checkout-42"
  },
  "body": {
    "orderId": "ORD-1001",
    "customerId": "CUST-77",
    "total": 59.90
  }
}
```

## Commands

A command is a request for a specific action. It is directed at **one receiver**, which owns the behavior and decides whether to carry it out.

**Characteristics of a command:**

- **Named in the imperative mood**: `PlaceOrder`, `ChargePayment`, `ReserveInventory`, `SendWelcomeEmail`.
- **Has exactly one intended handler**: The sender knows who should do the work, even if a queue sits in between.
- **Can be rejected**: The receiver validates it and may refuse it, for example when a payment card is declined or the stock is insufficient.
- **Expresses what the sender wants**: The sender depends on the receiver's capability, so there is coupling from sender to receiver.
- **Usually expects an outcome**: A reply, a status, or a follow-up event that says whether it succeeded.

**Example:**

```json
{
  "type": "ReserveInventory",
  "body": {
    "orderId": "ORD-1001",
    "items": [{ "sku": "BOOK-123", "quantity": 2 }]
  }
}
```

The order service sends this command to the inventory service. The inventory service decides whether the reservation is possible.

**Typical transport:** A point-to-point queue (such as an SQS queue or a RabbitMQ queue) or a direct API call, since only one consumer should handle each command.

## Events

An event is a notification that something meaningful has happened in the system. It is a **fact about the past**, so it cannot be rejected or undone. You can only publish a new event that compensates for it.

**Characteristics of an event:**

- **Named in the past tense**: `OrderPlaced`, `PaymentCharged`, `InventoryReserved`, `UserRegistered`.
- **Has zero, one, or many subscribers**: The publisher does not know or care who is listening.
- **Cannot be rejected**: Subscribers may ignore it, but they cannot say "no, that didn't happen".
- **Expresses what the publisher did**: Subscribers depend on the publisher's facts, so the coupling points from receiver to sender, not the other way around.
- **Immutable**: Once published, an event is never changed.

**Example:**

```json
{
  "type": "OrderPlaced",
  "body": {
    "orderId": "ORD-1001",
    "customerId": "CUST-77",
    "items": [{ "sku": "BOOK-123", "quantity": 2 }],
    "total": 59.90
  }
}
```

The order service publishes this event. The inventory, email, analytics, and loyalty services can each react to it in their own way, and new subscribers can be added later without changing the order service.

**Typical transport:** A publish/subscribe channel, such as a Kafka topic, an SNS topic, AWS EventBridge, or Google Cloud Pub/Sub.

## Side-by-Side Comparison

| Aspect | Command | Event |
|---|---|---|
| Intent | "Please do this" | "This happened" |
| Tense of the name | Imperative (`ChargePayment`) | Past (`PaymentCharged`) |
| Time | Refers to the future | Refers to the past |
| Receivers | Exactly one handler | Zero to many subscribers |
| Can it be rejected? | Yes | No, it is already a fact |
| Who owns the contract? | The receiver (it defines what it accepts) | The publisher (it defines what it announces) |
| Coupling direction | Sender depends on receiver | Subscribers depend on publisher |
| Typical channel | Queue or direct call | Topic or event bus |
| Mutability | Can be retried or replaced | Immutable once published |

## Kinds of Events

Not all events carry the same amount of data. Martin Fowler describes several styles that are useful to know.

- **Event notification**: A thin event that only says something changed, often with just an ID (`OrderPlaced { orderId }`). Subscribers call back to the publisher for details. The events are small, but there is more coupling at runtime.
- **Event-carried state transfer**: A fat event that includes all the data subscribers need, so they never have to call back. Subscribers become more independent, but the events are larger and the schema is harder to evolve.
- **Event sourcing**: The events *are* the source of truth. The current state is rebuilt by replaying the history of events.

It also helps to separate:

- **Domain events**: Meaningful inside one bounded context (`OrderLineAdded`).
- **Integration events**: Published deliberately for other services, with a stable and versioned contract (`OrderPlaced`).

## A Worked Example: Checkout

Here is how commands and events work together in an e-commerce checkout.

1. The client sends a **command** `PlaceOrder` to the order service.
2. The order service validates and saves the order, then publishes the **event** `OrderPlaced`.
3. The payment service reacts to `OrderPlaced` and charges the card. It publishes either `PaymentCharged` or `PaymentFailed`.
4. The inventory service reacts to `PaymentCharged` and publishes `InventoryReserved`.
5. The email service reacts to `OrderPlaced` and `PaymentCharged` to notify the customer.
6. If `PaymentFailed` arrives, the order service reacts by cancelling the order and publishes `OrderCancelled`.

This is a **choreographed saga**: services react to each other's events, and nobody controls the whole flow.

In an **orchestrated saga**, a central orchestrator sends **commands** instead (`ChargePayment`, `ReserveInventory`) and listens for the resulting **events** (`PaymentCharged`, `InventoryReserved`). Orchestration makes the flow explicit and easier to follow, while choreography keeps services more loosely coupled.

## When to Use Which

**Use a command when:**

- You need a specific service to perform a specific action.
- The sender needs to know whether the action succeeded.
- The receiver must be able to validate and reject the request.
- There is a clear owner of the behavior, for example "only the payment service charges cards".

**Use an event when:**

- Something important happened, and other parts of the system may care.
- You don't want the publisher to know about its consumers.
- You expect new consumers to appear over time, such as analytics, auditing, or search indexing.
- You want to keep a history that can be replayed.

## Common Mistakes

- **Commands disguised as events**: Publishing `SendEmailRequested` to a topic that only the email service reads. If exactly one service must act and the sender expects it to, it is a command. Name it and route it like one.
- **Events with imperative names**: Calling an event `UpdateCustomer` hides whether it is a request or a fact. Use the past tense, such as `CustomerAddressChanged`.
- **Expecting a reply to an event**: An event publisher should not wait for subscribers. If you need an answer, you need a command or a query.
- **Overly generic events**: An event like `CustomerUpdated` with the whole customer record forces every subscriber to work out what changed. More specific events (`CustomerEmailChanged`, `CustomerTierUpgraded`) are easier to react to.
- **Leaking internal details**: Publishing internal domain events directly to other teams ties them to your internal model. Translate them into stable integration events.
- **Ignoring duplicates and ordering**: Most brokers deliver at least once. Make handlers idempotent, and use keys or partitions when the order of events matters.

## Interview Tips

If you are asked about events vs. commands in an interview, a clear answer covers these points:

1. Both are messages, but they differ in intent.
2. A command is "do this", sent to one handler, and it can be rejected.
3. An event is "this happened", broadcast to any number of subscribers, and it is an immutable fact.
4. Commands point coupling toward the receiver, and events point it toward the publisher, which is why events make it easier to add new consumers.
5. Real systems use both: commands to trigger work, and events to announce the results.

## Conclusion

Messages are the envelopes, commands are requests, and events are facts. Keeping these concepts separate makes your system easier to understand: the names tell you the intent, the channel tells you who should react, and the ownership of each contract is clear. When you design a new interaction, start by asking one question: *"Am I asking someone to do something, or telling everyone that something happened?"* The answer tells you whether you need a command or an event.
