# Retry Pattern

The **Retry Pattern** in microservices is a design strategy used to handle temporary failures that occur during service communication. In a distributed system where microservices interact with each other, transient issues such as network interruptions or brief service outages can lead to failed requests.

### How It Works

1. **Automatic Retries**: When a request fails due to a transient issue, the service automatically retries the operation after a short delay.
2. **Configurable Parameters**: The number of retries, delay between retries, and conditions for retrying can be configured based on the specific needs of the application.
3. **Enhanced Reliability**: By retrying failed requests, the pattern ensures that temporary glitches do not lead to permanent failures, thereby enhancing the reliability of the microservices.

### Steps to Implement

1. **Add Dependencies**: If you're using Spring Boot, you can use the Resilience4j library. Add the following dependencies to your `pom.xml`:
    ```xml
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot2</artifactId>
        <version>1.7.0</version>
    </dependency>
    ```

2. **Configure Retry Properties**: Define the retry configuration in your `application.yml` file:
    ```yaml
    resilience4j.retry:
      instances:
        myService:
          max-attempts: 5
          wait-duration: 500ms
    ```

3. **Annotate Methods**: Use the `@Retry` annotation on the methods where you want to apply the retry logic:
    ```java
    @Service
    public class MyService {
        
        @Retry(name = "myService", fallbackMethod = "fallbackMethod")
        public String callExternalService() {
            // Logic to call external service
        }

        public String fallbackMethod(Exception e) {
            // Fallback logic
            return "Fallback response";
        }
    }
    ```

4. **Test the Retry Mechanism**: You can test the retry mechanism by simulating failures and observing the retry attempts.

The Retry Pattern in microservices is a design approach aimed at managing temporary service communication failures. By attempting to resend failed requests, this pattern helps prevent transient issues from causing lasting failures, thus improving the overall reliability of the microservices.

<em>References:</em>
* [Retry Pattern in Microservices - GeeksforGeeks](://www.geeksforgeeks.org/retry-pattern-in-microservices/)
* [How the Retry Pattern Enhances Reliability in Microservices by Handling ...](://www.momentslog.com/development/design-pattern/how-the-retry-pattern-enhances-reliability-in-microservices-by-handling-transient-failures)
* [Designing Resilient Microservices: Strategies for Fault Tolerance](://www.momentslog.com/development/architecture/designing-resilient-microservices-strategies-for-fault-tolerance)
* [Retry Pattern in Microservices - Abhinav Pandey's Blog](://www.abhinavpandey.dev/blog/retry-pattern)
* [Microservice | Resilience4J Retry Module Implementation With Spring Boot | JavaTechie](https://www.youtube.com/watch?v=Z4CSGsOLb1c)