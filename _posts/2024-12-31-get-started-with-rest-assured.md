# Getting Started with REST Assured

Let's get you started with REST Assured for API testing. Here’s a step-by-step guide to help you set up and write your first test:

### 1. Set Up Your Project
First, you'll need to create a new Java project. You can do this using any IDE (e.g., IntelliJ IDEA, Eclipse). For this example, I'll use Maven to manage dependencies.

1. **Create a new Maven project**.
2. **Add the REST Assured dependency** to your `pom.xml` file:

    ```xml
    <dependencies>
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <version>4.4.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    ```

### 2. Write Your First Test
Now, let's write a simple test to get started.

1. **Create a new Java class** for your test, e.g., `ApiTest.java`.
2. **Import the necessary REST Assured classes**:

    ```java
    import static io.restassured.RestAssured.*;
    import static org.hamcrest.Matchers.*;
    import org.junit.Test;
    ```

3. **Write a simple GET request test**:

    ```java
    public class ApiTest {

        @Test
        public void testGetRequest() {
            given()
                .baseUri("https://jsonplaceholder.typicode.com")
                .when()
                .get("/todos/1")
                .then()
                .statusCode(200)
                .body("title", equalTo("delectus aut autem"));
        }
    }
    ```

### 3. Run Your Test
You can run your test using your IDE’s built-in test runner or a command-line tool like Maven. For Maven, use the following command:

```bash
mvn test
```

### 4. Explore Further
Once you're comfortable with the basics, you can explore more features of REST Assured, such as:

- **Handling different HTTP methods** (POST, PUT, DELETE, etc.).
- **Passing parameters and headers** in your requests.
- **Validating complex JSON responses** using JsonPath.

Here’s an example of a POST request test:

```java
public class ApiTest {

    @Test
    public void testPostRequest() {
        given()
            .baseUri("https://jsonplaceholder.typicode.com")
            .contentType("application/json")
            .body("{ \"title\": \"foo\", \"body\": \"bar\", \"userId\": 1 }")
            .when()
            .post("/posts")
            .then()
            .statusCode(201)
            .body("title", equalTo("foo"))
            .body("body", equalTo("bar"))
            .body("userId", equalTo(1));
    }
}
```

That's a quick introduction to REST Assured.