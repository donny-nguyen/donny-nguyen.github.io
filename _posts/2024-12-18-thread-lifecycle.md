# Thread Lifecycle

The thread lifecycle in Java represents the various stages a thread goes through from its creation to termination. Here's a detailed look at each stage in the lifecycle of a thread:

### 1. **New (or Born) State**
- **Definition**: A thread is in this state when an instance of the `Thread` class or a class implementing the `Runnable` interface is created.
- **Transition**: The thread remains in this state until the `start()` method is called on the thread object.
- **Example**:
  ```java
  Thread thread = new Thread();
  // The thread is now in the New state
  ```

### 2. **Runnable State**
- **Definition**: After the `start()` method is called, the thread enters the runnable state. A thread in this state is ready to run and is waiting for the CPU to allocate time for its execution.
- **Key Points**:
  - The thread might not be running immediately after calling `start()`; it depends on the thread scheduler.
  - The thread can transition between runnable and running states based on the availability of the CPU.
- **Example**:
  ```java
  thread.start();
  // The thread is now in the Runnable state, waiting for CPU time
  ```

### 3. **Running State**
- **Definition**: When the thread scheduler picks the thread from the runnable pool and assigns CPU time, the thread enters the running state and executes its `run()` method.
- **Key Points**:
  - The thread stays in this state as long as it has CPU time and does not get preempted or voluntarily give up its time.
  - The actual execution of the `run()` method happens in this state.
- **Example**:
  ```java
  public void run() {
      // Code executed in the Running state
  }
  ```

### 4. **Blocked/Waiting State**
- **Definition**: A thread enters the blocked or waiting state when it is waiting for some resource or condition to be met.
- **Key Points**:
  - **Blocked**: Waiting for a monitor lock to enter a synchronized block/method.
  - **Waiting**: Explicitly waiting for another thread to perform a particular action (e.g., calling `wait()` on an object, `join()` on another thread, or `sleep()`).
- **Example**:
  ```java
  synchronized(someObject) {
      someObject.wait(); // The thread is now in the Waiting state
  }
  ```

### 5. **Timed Waiting State**
- **Definition**: A thread enters this state when it calls methods like `sleep()`, `wait(long timeout)`, or `join(long timeout)` that cause it to wait for a specified period.
- **Key Points**:
  - The thread automatically transitions back to the runnable state after the specified wait time has elapsed.
- **Example**:
  ```java
  Thread.sleep(1000); // The thread is now in the Timed Waiting state for 1 second
  ```

### 6. **Terminated (or Dead) State**
- **Definition**: A thread enters the terminated state when it has completed its execution or has been explicitly stopped.
- **Key Points**:
  - Once a thread reaches this state, it cannot be restarted.
  - Typically, a thread enters this state upon returning from the `run()` method or if the `stop()` method is called.
- **Example**:
  ```java
  public void run() {
      // Thread's task
  }
  // After the run() method completes, the thread is in the Terminated state
  ```

### Summary Diagram of Thread Lifecycle
Here's a simple visualization of the thread lifecycle:

```
New (Born)
   |
   v
Runnable <-> Running <-> Blocked/Waiting
                ^
                |
                v
            Timed Waiting
                |
                v
            Terminated
```

Understanding the thread lifecycle is crucial for managing multithreaded applications effectively. Proper synchronization and resource management can help prevent issues like deadlocks and ensure smooth concurrent execution.