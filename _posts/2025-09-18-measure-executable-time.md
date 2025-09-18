# Measure the Executable Time

To measure the executable time of a method in JavaScript, use one of these standard approaches: `performance.now()`, `console.time()` / `console.timeEnd()`, or `Date.now()` for millisecond accuracy.[2][3][5][7]

### Using performance.now()

This method gives high-resolution timing, precise to fractions of a millisecond.

```typescript
const start = performance.now();
myMethod(); // The method to measure
const end = performance.now();
console.log(`myMethod took ${end - start}ms`);
```


### Using console.time()

This is a quick way to log timing directly to the console.

```typescript
console.time("myMethod");
myMethod();
console.timeEnd("myMethod"); // Logs: myMethod: X ms
```


### Using Date.now()

Best for less precise needs or if you want to work with raw integer milliseconds.

```typescript
const start = Date.now();
myMethod();
const end = Date.now();
console.log(`myMethod took ${end - start}ms`);
```


### Summary

- Use `performance.now()` for most accurate measurement.
- `console.time()`/`console.timeEnd()` work great for quick profiling.
- `Date.now()` or `new Date().getTime()` are simple but less accurate.

All approaches work in TypeScript as they are provided by the underlying JavaScript engine.[7][9]

[1](https://nono.ma/measure-time-elapsed-typescript)
[2](https://www.w3docs.com/snippets/javascript/how-to-measure-time-taken-by-a-function-to-execute.html)
[3](https://www.geeksforgeeks.org/javascript/how-to-measure-time-taken-by-a-function-to-execute-using-javascript/)
[4](https://www.reddit.com/r/learnjavascript/comments/1cz0y3l/how_can_i_measure_the_performance_of_this_function/)
[5](https://www.educative.io/answers/how-to-find-the-time-taken-to-execute-a-function-in-javascript)
[6](https://www.reddit.com/r/vscode/comments/97lotb/how_would_you_test_for_function_execution_time/)
[7](https://stackoverflow.com/questions/313893/how-to-measure-time-taken-by-a-function-to-execute)
[8](https://dev.to/saranshk/how-to-measure-javascript-execution-time-5h2)
[9](https://dev.to/typescripttv/measure-execution-times-in-browsers-node-js-js-ts-1kik)
[10](https://www.gavsblog.com/blog/javascript/measuring-code-execution-performance-in-javascript)