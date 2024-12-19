# Threads

Threads are a fundamental part of the Java programming language and allow for the execution of multiple parts of a program concurrently. Here's an overview of threads in Java:

### Key Concepts:

1. **What is a Thread?**
   - A thread is a lightweight process, a unit of execution within a program. Java programs can have multiple threads running simultaneously, each performing different tasks.

2. **Multithreading**:
   - Multithreading is the ability of a CPU to execute multiple threads concurrently. In Java, this can be used to make programs more efficient by performing multiple tasks at the same time.

### Creating and Running Threads:

1. **Extending `Thread` Class**:
   - You can create a new thread by extending the `Thread` class and overriding its `run` method.
   - **Example**:
     ```java
     public class MyThread extends Thread {
         public void run() {
             System.out.println("Thread is running...");
         }

         public static void main(String[] args) {
             MyThread thread = new MyThread();
             thread.start(); // Starts the new thread
         }
     }
     ```

2. **Implementing `Runnable` Interface**:
   - Alternatively, you can create a thread by implementing the `Runnable` interface and passing an instance of the class to a `Thread` object.
   - **Example**:
     ```java
     public class MyRunnable implements Runnable {
         public void run() {
             System.out.println("Thread is running...");
         }

         public static void main(String[] args) {
             MyRunnable myRunnable = new MyRunnable();
             Thread thread = new Thread(myRunnable);
             thread.start(); // Starts the new thread
         }
     }
     ```

### Thread Lifecycle:
- **New**: A thread that has been created but not yet started.
- **Runnable**: A thread that is ready to run and is waiting for CPU time.
- **Running**: A thread that is currently executing.
- **Blocked**: A thread that is waiting for a resource or a lock.
- **Terminated**: A thread that has completed its execution or has been stopped.

### Synchronization:
- **Purpose**: Ensures that only one thread can access a resource at a time, preventing data inconsistency and race conditions.
- **Keyword**: `synchronized` can be used to lock a method or a block of code.
- **Example**:
  ```java
  public class Counter {
      private int count = 0;

      public synchronized void increment() {
          count++;
      }

      public int getCount() {
          return count;
      }
  }
  ```

### Thread Communication:
- **Methods**: Threads can communicate and coordinate using methods like `wait()`, `notify()`, and `notifyAll()`.
- **Example**:
  ```java
  public class Message {
      private String message;

      public synchronized void write(String message) {
          this.message = message;
          notify();
      }

      public synchronized String read() throws InterruptedException {
          wait();
          return message;
      }
  }
  ```

### Executors Framework:
- **Purpose**: Provides a high-level API for managing a pool of threads.
- **Example**:
  ```java
  import java.util.concurrent.ExecutorService;
  import java.util.concurrent.Executors;

  public class Main {
      public static void main(String[] args) {
          ExecutorService executor = Executors.newFixedThreadPool(3);
          for (int i = 0; i < 10; i++) {
              executor.submit(() -> System.out.println("Task executed by " + Thread.currentThread().getName()));
          }
          executor.shutdown();
      }
  }
  ```

### Conclusion
Threads in Java are powerful tools for performing concurrent tasks, improving performance and resource utilization. Proper understanding and management of threads, synchronization, and communication are essential for developing robust and efficient applications.