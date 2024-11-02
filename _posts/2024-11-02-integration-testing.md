# Integration Testing

In a Java backend application, **integration testing** involves testing the application’s components or modules together as a group to verify that they work correctly in conjunction with one another. Unlike unit tests, which focus on individual methods or classes, integration tests check the interactions between components, such as:

1. **Database interactions** – Verifying that the backend application can connect to, query, and modify data in the database as expected.
2. **API endpoints** – Ensuring that endpoints are correctly exposed and return the expected responses when called, often through HTTP.
3. **Service layer** – Testing the business logic and service classes together with the repository and/or controller layers.
4. **External systems** – Checking integration with external services, APIs, or libraries used by the application.

### Common Tools and Frameworks for Integration Testing in Java
For Java applications, several frameworks and tools are commonly used for integration testing:

- **JUnit** – Popular for writing integration tests, as it provides setup and teardown methods for each test case.
- **Spring Boot Test** – Provides tools like `@SpringBootTest` and `@TestConfiguration` for bootstrapping the Spring application context in tests.
- **MockMvc** – Often used for testing Spring controllers by simulating HTTP requests and responses.
- **Testcontainers** – For spinning up actual databases, message brokers, or other dependencies in Docker containers during testing.
- **Mockito** – For mocking dependencies within the test setup, allowing for isolated integration tests without reliance on certain external factors.

### Example of an Integration Test with Spring Boot
An integration test for a REST controller might look like this in a Spring Boot application:

```java
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnUserDetails() throws Exception {
        mockMvc.perform(get("/api/users/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.name").value("John Doe"));
    }
}
```

In this test:
- `@SpringBootTest` loads the full Spring application context.
- `@AutoConfigureMockMvc` sets up MockMvc to simulate HTTP requests.
- The `perform()` method simulates a GET request, and we check the response status and content.

Integration tests provide confidence that our application's components function correctly together, ensuring better reliability before deploying to production.