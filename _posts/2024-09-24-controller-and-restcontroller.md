# Controller and RestController

In the Spring framework, `@Controller` and `@RestController` are used to define controllers, but they serve different purposes and are used in different scenarios:

### `@Controller`
- **Purpose**: Used to define a standard web controller in Spring MVC.
- **Response Handling**: Typically returns a view (like an HTML page) using a view resolver.
- **Usage**: Suitable for web applications where the response is usually an HTML page.
- **Example**:
  ```java
  @Controller
  @RequestMapping("/users")
  public class UserController {
      @GetMapping("/{id}")
      public String displayUserInfo(@PathVariable Long id) {
          // logic to get user info
          return "userView"; // returns the view name
      }
  }
  ```

### `@RestController`
- **Purpose**: A specialized version of `@Controller` used to create RESTful web services.
- **Response Handling**: Combines `@Controller` and `@ResponseBody`, so it returns data directly (like JSON or XML) instead of a view.
- **Usage**: Ideal for REST APIs where the response is typically JSON or XML.
- **Example**:
  ```java
  @RestController
  @RequestMapping("/users")
  public class UserRestController {
      @GetMapping("/{id}")
      public User getUserById(@PathVariable Long id) {
          // logic to get user info
          return new User(id, "John Doe"); // returns the user object as JSON
      }
  }
  ```

In summary, `@Controller` is used for traditional web applications that return views, while `@RestController` is used for RESTful web services that return data directly in formats like JSON or XML.

<em>References:</em>
* [Spring @Controller Vs. @RestController: What's Difference? - HowToDoInJava](https://howtodoinjava.com/spring-boot/controller-restcontroller/)
* [The Spring @Controller and @RestController Annotations](https://www.baeldung.com/spring-controller-vs-restcontroller)
* [What is the Difference Between @Controller and @RestController in ...](https://www.codeproject.com/Articles/5388202/What-is-the-Difference-Between-Controller-and-Rest)
* [What is the difference between @Controller vs @RestController in Spring ...](https://symflower.com/en/company/blog/2024/controller-restcontroller-spring-boot/)
* [Spring Framework: @RestController vs. @Controller - DZone](https://dzone.com/articles/spring-framework-restcontroller-vs-controller)