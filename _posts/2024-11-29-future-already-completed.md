# Bad State: Future Already Completed

The Dart error `Bad state: Future already completed` occurs when you attempt to complete a `Future` more than once. Since a `Future` in Dart can only transition from an uncompleted state to a completed state exactly once, any subsequent attempt to complete it will throw this error.

Here are the most common reasons for this error:

### 1. **Manually Completing a `Completer` Multiple Times**
   - If you are using a `Completer` and call `Completer.complete` or `Completer.completeError` more than once, you will encounter this error.
   ```dart
   final completer = Completer<int>();
   completer.complete(42); // Completes the Future
   completer.complete(43); // Throws: Bad state: Future already completed
   ```

### 2. **Multiple Calls to an `async` Function or Callback**
   - If an `async` function, callback, or event handler tries to resolve the same `Future` multiple times, the error can occur. For example:
   ```dart
   Future<void> handleEvent() async {
     final completer = Completer<void>();
     someAsyncOperation().then((_) {
       completer.complete(); // Completes the Future
       completer.complete(); // Throws error
     });
   }
   ```

### 3. **Stream Subscription and Repeated Calls**
   - Handling streams incorrectly and attempting to complete a `Future` for every event, without proper checks, can lead to this error.
   ```dart
   Stream<int> stream = Stream.fromIterable([1, 2, 3]);
   final completer = Completer<void>();
   stream.listen((event) {
     completer.complete(); // Completes on the first event
     // Subsequent events cause the error
   });
   ```

### 4. **Rethrowing or Propagating Errors**
   - If an error handler or `catchError` block tries to complete a `Future` that has already been completed elsewhere in the code, this error can occur.
   ```dart
   final completer = Completer<void>();
   try {
     completer.complete(); // Completes successfully
     throw Exception('Error'); // Error occurs after completion
   } catch (e) {
     completer.completeError(e); // Throws: Bad state: Future already completed
   }
   ```

### 5. **Race Conditions**
   - When two or more asynchronous tasks attempt to complete the same `Future` without proper synchronization, it can lead to this error. This is especially common when handling multiple listeners or operations that share a single `Completer`.

### 6. **Incorrect Logic in Custom `Future`-Based APIs**
   - If you're writing custom asynchronous APIs using `Future` or `Completer`, you might inadvertently complete the `Future` multiple times due to faulty logic in our implementation.

---

### **How to Fix the Issue**

1. **Check Completion State Before Completing:**
   Use the `Completer.isCompleted` property to verify if the `Completer` is already completed.
   ```dart
   if (!completer.isCompleted) {
     completer.complete();
   }
   ```

2. **Guard Against Multiple Calls:**
   Ensure that `Future` completion logic is invoked only once. For example, debounce or synchronize access to shared resources.

3. **Refactor Code for Better Design:**
   Avoid scenarios where multiple parts of the code compete to complete the same `Future`.

4. **Use Debugging Tools:**
   Add logging or breakpoints to track where the `Future` is being completed to identify duplicate completions.
