# Circuit Breaker Pattern

The **Circuit Breaker Pattern** is a design pattern used in distributed systems to handle potential failures in a resilient way. It prevents cascading failures by temporarily stopping the flow of requests to a service when it is likely to fail, and allows the system to recover gracefully. This pattern is commonly used in microservice architectures to maintain system stability and ensure fault tolerance.

## How It Works:
The circuit breaker acts like an electrical circuit breaker. When a service call fails repeatedly, the circuit "opens" blocking further attempts to contact the failing service. Over time, the circuit "half-opens" allowing a few requests to pass through to test if the service has recovered. If the service responds successfully, the circuit "closes" and normal traffic resumes.

## States in Circuit Breaker Pattern:
1. **Closed**: 
   - The service is functioning normally.
   - All requests are allowed to pass through to the service.
   - If a certain threshold of failures is reached, the circuit breaker transitions to the "Open" state.

2. **Open**: 
   - The service has encountered repeated failures.
   - Requests are blocked from reaching the service, and an error or fallback is immediately returned.
   - The circuit breaker stays open for a predefined period (cool-down period).

3. **Half-Open**: 
   - After the cool-down period, the circuit breaker allows a limited number of test requests to pass through to check if the service has recovered.
   - If the service responses are successful, the circuit breaker transitions back to "Closed."
   - If failures continue, the circuit goes back to "Open."

## Benefits:
- **Failure Isolation**: By cutting off requests to a failing service, the system prevents cascading failures and ensures the failure does not spread to other parts of the application.
  
- **Graceful Degradation**: The system can return a fallback response, cached data, or a default message instead of waiting for a slow or failing service.

- **Improved Latency**: Since failing requests are blocked and immediately responded to with an error, the overall system performance can improve by avoiding long wait times.

- **Self-Healing**: The half-open state enables the system to automatically recover once the service becomes healthy, without requiring manual intervention.

## Common Scenarios:
- **Network Issues**: In distributed systems, network problems between services can cause delays or failures. The circuit breaker prevents repeated attempts to reach an unresponsive service.
  
- **High Latency**: When a service starts responding too slowly due to load, the circuit breaker can prevent the entire system from becoming bogged down.

- **Temporary Downtime**: If a service is undergoing maintenance or experiencing temporary issues, the circuit breaker can stop requests until the service is restored.

## Example:

### Java Core Application

Here's a basic example of implementing a circuit breaker pattern in Java without any external libraries or frameworks.

```java
import java.time.Duration;
import java.time.Instant;

public class CircuitBreaker {
    private enum State {
        CLOSED, OPEN, HALF_OPEN
    }
    
    private State state = State.CLOSED;
    private int failureCount = 0;
    private final int failureThreshold;
    private final Duration cooldownDuration;
    private Instant lastFailureTime;
    private final Duration halfOpenTestInterval;
    
    public CircuitBreaker(int failureThreshold, Duration cooldownDuration, Duration halfOpenTestInterval) {
        this.failureThreshold = failureThreshold;
        this.cooldownDuration = cooldownDuration;
        this.halfOpenTestInterval = halfOpenTestInterval;
    }
    
    public synchronized boolean allowRequest() {
        switch (state) {
            case OPEN:
                if (Duration.between(lastFailureTime, Instant.now()).compareTo(cooldownDuration) >= 0) {
                    state = State.HALF_OPEN;
                    return true;  // Allow a test request in HALF_OPEN state
                }
                return false;
            case HALF_OPEN:
                return Duration.between(lastFailureTime, Instant.now()).compareTo(halfOpenTestInterval) >= 0;
            default:
                return true;
        }
    }
    
    public synchronized void onSuccess() {
        if (state == State.HALF_OPEN || state == State.OPEN) {
            reset();
        }
    }
    
    public synchronized void onFailure() {
        failureCount++;
        lastFailureTime = Instant.now();
        if (failureCount >= failureThreshold) {
            state = State.OPEN;
        }
    }
    
    private void reset() {
        state = State.CLOSED;
        failureCount = 0;
    }
}
```

Here’s a simple service call example using this circuit breaker:

```java
public class ServiceCaller {
    private final CircuitBreaker circuitBreaker;

    public ServiceCaller(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public void callService() {
        if (!circuitBreaker.allowRequest()) {
            System.out.println("Service call blocked by Circuit Breaker");
            return;
        }

        try {
            // Simulate a service call
            boolean serviceResult = simulateServiceCall();
            if (serviceResult) {
                System.out.println("Service call succeeded");
                circuitBreaker.onSuccess();
            } else {
                System.out.println("Service call failed");
                circuitBreaker.onFailure();
            }
        } catch (Exception e) {
            System.out.println("Service call failed with exception: " + e.getMessage());
            circuitBreaker.onFailure();
        }
    }

    private boolean simulateServiceCall() {
        // Simulate a random success or failure
        return Math.random() > 0.5;
    }

    public static void main(String[] args) {
        CircuitBreaker circuitBreaker = new CircuitBreaker(3, Duration.ofSeconds(10), Duration.ofSeconds(5));
        ServiceCaller caller = new ServiceCaller(circuitBreaker);

        for (int i = 0; i < 20; i++) {
            caller.callService();
            try {
                Thread.sleep(1000);  // Wait 1 second between calls
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

**Explanation**

1. **Circuit Breaker Logic**: 
   - The `allowRequest` method decides whether to allow a request based on the current state.
   - If the circuit is **Open** and the cooldown period has passed, it transitions to **Half-Open** and allows a test request.
   - If **Half-Open** and the test interval has passed, it allows a test request.

2. **Service Call Simulation**:
   - In the `ServiceCaller` class, `simulateServiceCall` randomly returns success or failure, simulating a real service call.
   - On success, `circuitBreaker.onSuccess()` is called to reset the circuit.
   - On failure, `circuitBreaker.onFailure()` increments the failure count and, if thresholds are exceeded, transitions the breaker to **Open**.

This code provides a simple circuit breaker pattern that can be extended for real-world applications by adding retries, error logging, and adjusting thresholds based on our service's needs.

### Using Spring Application and Resilience4j

To implement the Circuit Breaker pattern in Spring Cloud using Resilience4j, we'll need to configure dependencies and annotations to handle fallback mechanisms, thresholds, and states. Here’s a step-by-step guide on how to set it up:

#### 1. Add Dependencies

To use Resilience4j in a Spring Boot project, add the following dependencies to our `pom.xml` file:

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
    <version>2.0.2</version> <!-- Use the latest stable version -->
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

#### 2. Configure Resilience4j in `application.yml`

Define circuit breaker configurations, such as failure rate threshold, wait duration, and permitted calls during the half-open state:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      myService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10000ms # 10 seconds
        permittedNumberOfCallsInHalfOpenState: 5
        slidingWindowType: COUNT_BASED
        minimumNumberOfCalls: 5
```

- **slidingWindowSize**: Number of calls to evaluate the circuit breaker’s state.
- **failureRateThreshold**: Percentage of failed calls to open the circuit.
- **waitDurationInOpenState**: Duration to wait before moving to Half-Open state.
- **permittedNumberOfCallsInHalfOpenState**: Calls allowed to pass in the Half-Open state.
- **slidingWindowType**: Defines if sliding window is time-based or count-based.

#### 3. Implementing the Circuit Breaker in Code

Use the `@CircuitBreaker` annotation to apply the circuit breaker logic to a specific method, such as a service call. Specify the circuit breaker name and a fallback method to be used if the circuit is open.

##### Example Service Class

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    @CircuitBreaker(name = "myService", fallbackMethod = "fallbackMethod")
    public String callExternalService() {
        // Code to call an external service that might fail
        // For example, using RestTemplate or WebClient to make HTTP requests
        return restTemplate.getForObject("https://unstable-service.com/api/resource", String.class);
    }

    public String fallbackMethod(Throwable throwable) {
        // Fallback response logic
        return "Service is currently unavailable, please try again later.";
    }
}
```

In this example:

- `callExternalService` is the main method that calls an external service.
- `fallbackMethod` is the fallback method that will execute if the circuit is open or if the external service call fails.

#### 4. Testing the Circuit Breaker

To see the circuit breaker in action:

- Run the application and call `callExternalService` multiple times.
- Introduce a failure, such as shutting down the external service or causing errors, to exceed the failure rate threshold.
- Observe that, after a certain number of failed attempts, the circuit breaker opens and returns the fallback response.

#### 5. Additional Configurations (Optional)

Resilience4j also allows configuring other resilience patterns, such as **retry**, **rate limiter**, and **bulkhead**. Here’s an example of how we might add a retry mechanism in conjunction with the circuit breaker:

```yaml
resilience4j:
  retry:
    instances:
      myService:
        maxAttempts: 3
        waitDuration: 500ms
```

Then, use the `@Retry` annotation alongside `@CircuitBreaker`:

```java
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class MyService {

    @CircuitBreaker(name = "myService", fallbackMethod = "fallbackMethod")
    @Retry(name = "myService")
    public String callExternalService() {
        // Call the external service
    }

    public String fallbackMethod(Throwable throwable) {
        return "Service is unavailable. Please try again later.";
    }
}
```

#### 6. Monitoring the Circuit Breaker

We can monitor the circuit breaker status via health indicators or integration with tools like **Spring Boot Actuator** and **Micrometer**. Add the Actuator dependency to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

This provides endpoints like `/actuator/health` to check the circuit breaker’s status and other metrics.

#### Summary

Using Resilience4j with Spring Cloud allows us to easily configure and manage the Circuit Breaker pattern to improve fault tolerance in microservices. With this setup, our service is better equipped to handle failures gracefully, preventing cascading failures in our microservices architecture.
