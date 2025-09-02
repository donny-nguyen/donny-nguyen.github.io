# Jobs Parameters Serialization

When you pass an **Adonis.js model instance** as an argument to a job, you can't use it to perform database operations directly inside the job because the instance is **serialized** before being stored in the queue and then **deserialized** when the job is processed. This serialization process loses the active database connection and other internal state that the model instance needs to interact with the database.

-----

### Why this Happens

Adonis.js jobs, by default, use a queuing system (like Redis or a database table) to store the job's data. To be stored, the job data, including any arguments passed to it, must be converted into a **flat, storable format** like a JSON string. This process is called serialization.

1.  **Serialization**: The model instance, which is a complex JavaScript object with methods, getters, and setters, and an active database connection, is converted into a plain object or a string. During this process, the methods and the connection to the database are discarded.

2.  **Storage**: The serialized data is then stored in the queue (e.g., Redis).

3.  **Deserialization**: When the worker process picks up the job, it retrieves the data from the queue and converts it back into a JavaScript object. This newly created object is just a plain data object; it is **not** a live Adonis.js model instance. It lacks the internal magic and database connection required to perform queries like `save()`, `update()`, or `delete()`.

-----

### How to Fix It

Instead of passing the entire model instance, you should pass a **unique identifier** for that model, such as its **primary key (ID)**.

Here's a step-by-step example:

1.  **In your controller or service**, get the model instance and pass its ID to the job:

    ```javascript
    const user = await User.findOrFail(1);
    // Pass only the user's ID to the job
    await DriveJob.dispatch([user.id]); 
    ```

2.  **In your job handler**, use the ID to **fetch a fresh instance** of the model from the database:

    ```javascript
    'use strict'

    const User = use('App/Models/User')

    class DriveJob {
        static get key() {
            return 'drive-job'
        }

        async handle(userId) {
            // Fetch a fresh instance of the user model
            const user = await User.findOrFail(userId);
            
            // Now you can work with the database using this fresh instance
            user.name = 'New Name';
            await user.save();
        }
    }

    module.exports = DriveJob
    ```

By following this pattern, you ensure that your job is working with a **live, fully functional model instance** that has the necessary database connection to perform all model operations.