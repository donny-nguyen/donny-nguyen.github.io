# Testing Controller Layer with Mockito

Testing RESTful APIs (Controller Layer) using Mockito involves creating unit tests to verify that our API endpoints behave as expected. Here's a step-by-step guide to help us get started:

### Step 1: Set Up Our Project
Ensure we have the necessary dependencies in our `pom.xml` (for Maven) or `build.gradle` (for Gradle). For a Spring Boot project, we'll need the `spring-boot-starter-test` dependency, which includes JUnit and Mockito.

#### Maven Example:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

#### Gradle Example:
```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-test'
```

### Step 2: Create Our Controller
Define our RESTful API endpoints in a Spring Boot controller class.

#### Example Controller:
```java
@RestController
@RequestMapping("/api")
public class MyController {
    @GetMapping("/endpoint")
    public String getEndpoint() {
        return "Hello, World!";
    }
}
```

### Step 3: Create Unit Tests
Create unit tests for our controller using JUnit and Mockito. Use the `@WebMvcTest` annotation to focus on the web layer and `MockMvc` to perform requests and assert responses.

#### Example Unit Test:
```java
@WebMvcTest(MyController.class)
public class MyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetEndpoint() throws Exception {
        mockMvc.perform(get("/api/endpoint"))
               .andExpect(status().isOk())
               .andExpect(content().string("Hello, World!"));
    }
}
```

### Step 4: Mock Dependencies (if needed)
If our controller depends on other services or repositories, use Mockito to mock those dependencies.

#### Example with Mocked Service:
```java
@WebMvcTest(MyController.class)
public class MyControllerTest {

    @MockBean
    private MyService myService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetEndpoint() throws Exception {
        when(myService.getData()).thenReturn("Mocked Data");
        mockMvc.perform(get("/api/endpoint"))
               .andExpect(status().isOk())
               .andExpect(content().string("Mocked Data"));
    }
}
```

### Step 5: Run Our Tests
Run our tests using our IDE or build tool to ensure our API endpoints are working correctly.

### Summary
By following these steps, we can effectively test RESTful APIs using Mockito, ensuring that our controller layer behaves as expected. This approach helps catch issues early and improves the reliability of our application.
