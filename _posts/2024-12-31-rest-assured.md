# REST Assured

**REST Assured** is a Java library used for testing RESTful web services. It simplifies the process of writing automated tests for APIs by providing a domain-specific language (DSL) that allows you to write readable and maintainable test cases. Here are some key features of REST Assured:

1. **Ease of Use**: REST Assured provides a fluent interface that makes it easy to write tests. You can chain methods together to build up your requests and assertions in a readable way.

2. **HTTP Methods**: It supports all the standard HTTP methods like GET, POST, PUT, DELETE, PATCH, and HEAD, making it versatile for different types of API testing.

3. **Parameter Handling**: REST Assured allows you to easily handle query parameters, path parameters, headers, and body parameters in your requests.

4. **Response Validation**: It provides a wide range of assertions to validate the responses from the API, including status code checks, content type checks, and JSON path assertions.

5. **JsonPath Support**: REST Assured integrates with JsonPath, which allows you to extract and manipulate JSON data easily.

6. **Integration**: It integrates seamlessly with popular testing frameworks like JUnit and TestNG, making it easy to incorporate into your existing test suites.

Here's a simple example of how you might use REST Assured to test a GET request:

```java
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ApiTest {
    @Test
    public void testGetRequest() {
        given()
            .baseUri("https://api.example.com")
            .when()
            .get("/resource")
            .then()
            .statusCode(200)
            .body("name", equalTo("John Doe"));
    }
}
```

In this example, the test sends a GET request to the specified URI, checks that the status code is 200, and verifies that the response body contains the expected JSON data.

REST Assured is widely used in API testing because it simplifies the process and makes tests more readable and maintainable.