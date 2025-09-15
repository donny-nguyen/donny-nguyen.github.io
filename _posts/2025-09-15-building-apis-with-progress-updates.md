# Building APIs with Progress Updates

There are two main approaches to building an Adonis.js API that can provide progress updates for a long-running process: **Polling** and **Server-Sent Events (SSE)**.

### Polling 🔄

This is the more traditional and simpler method. The client makes a request to start the long-running task, receives a task ID in response, and then periodically makes subsequent requests to a different endpoint with that task ID to check on the progress.

#### Backend Steps

1.  **Create a Task Endpoint:** A `POST` endpoint (e.g., `/tasks`) that starts the long-running job. This endpoint should immediately respond with a **task ID** and a `202 Accepted` status code.
2.  **Run the Task in the Background:** The long-running process should be executed asynchronously. To do this effectively in Node.js, you'd typically use a **message queue system** like **BullMQ** or **RabbitMQ**. This offloads the heavy work from the main event loop, keeping your server responsive.
3.  **Store Progress:** The background job should periodically update its progress in a shared data store, like a **Redis** or a database table, using the unique task ID.
4.  **Create a Progress Endpoint:** A `GET` endpoint (e.g., `/tasks/:id/progress`) that a client can query. This endpoint retrieves the progress from the shared data store using the provided task ID and returns a JSON object with the current status (e.g., `{ "status": "processing", "progress": 50 }`).

#### Frontend Steps

1.  Make a `POST` request to the task endpoint.
2.  Receive the task ID from the response.
3.  Set up an interval (`setInterval`) to periodically send `GET` requests to the progress endpoint with the task ID.
4.  Update the UI with the progress data from each response.
5.  Once the progress endpoint indicates the task is complete, clear the interval.

-----

### Server-Sent Events (SSE) 🚀

SSE is a better, more modern approach for this kind of real-time communication. It allows the server to send a continuous stream of data to the client over a single HTTP connection. Adonis.js has a dedicated, opinionated package called **Transmit** that simplifies implementing SSE.

#### Backend Steps

1.  **Install Transmit:** Add the `@adonisjs/transmit` package to your project.

<!-- end list -->

```bash
node ace add @adonisjs/transmit
```

2.  **Define a Channel:** Use `transmit.channel()` in your `start/routes.ts` or a similar file to define an SSE channel that clients can subscribe to. You can use dynamic parameters in the channel name, like `long-task/:id`, to create unique channels for each task.
3.  **Start the Long-Running Task:** In your API endpoint (e.g., `POST /start-task`), kick off the long-running job. As the job processes, use `transmit.broadcast()` to send progress updates to the specific channel.

<!-- end list -->

```typescript
// app/Controllers/Http/TasksController.ts
import transmit from '@adonisjs/transmit/services/main'

export default class TasksController {
  public async startTask({ params }) {
    const taskId = params.id;
    // Simulate a long-running task
    for (let i = 0; i <= 100; i += 10) {
      // Broadcast progress to the channel
      transmit.broadcast(`long-task/${taskId}`, { progress: i, status: 'processing' });
      await new Promise(resolve => setTimeout(resolve, 1000));
    }
    transmit.broadcast(`long-task/${taskId}`, { progress: 100, status: 'completed' });
    return { message: 'Task started successfully.' };
  }
}
```

#### Frontend Steps

1.  **Install Transmit Client:** Install the `@adonisjs/transmit-client` package.

<!-- end list -->

```bash
npm install @adonisjs/transmit-client
```

2.  **Subscribe to the Channel:** On the client side, create a new `Transmit` instance and subscribe to the specific channel using the task ID.

<!-- end list -->

```javascript
// frontend code (e.g., in a Vue, React, or plain JS file)
import { Transmit } from '@adonisjs/transmit-client'

const transmit = new Transmit({
  baseUrl: 'http://localhost:3333' // Your Adonis.js app URL
});

const taskId = 'abc-123'; // The ID you got from the initial API call
const subscription = transmit.subscription(`long-task/${taskId}`);

subscription.onMessage((message) => {
  console.log('Progress update:', message.data);
  // Update your UI with the progress
});

subscription.create();
```

The client will now receive a stream of progress updates as they're broadcasted from the server. This is far more efficient than polling, as it avoids repeated HTTP requests.