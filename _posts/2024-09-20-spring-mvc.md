# Spring MVC

Spring MVC (Model-View-Controller) is a framework within the Spring Framework that is used to build web applications. It follows the MVC design pattern, which separates the application into three main components:

1. **Model**: Represents the application's data and business logic.
2. **View**: Represents the presentation layer (UI) of the application.
3. **Controller**: Handles user input and interactions, updating the Model and View accordingly.

Spring MVC is built on the Servlet API and has been part of the Spring Framework from the beginning. It provides a DispatcherServlet that dispatches requests to handlers, with configurable handler mappings, view resolution, locale, and theme resolution. It supports annotations like `@Controller` and `@RequestMapping` to define request handling methods.

Some key features of Spring MVC include:
- **Flexible data binding**: Allows binding of request parameters to Java objects.
- **View resolution**: Supports various view technologies like JSP, Thymeleaf, and more.
- **RESTful support**: Enables the creation of RESTful web services using annotations like `@PathVariable`.
- **Integration with other Spring components**: Works seamlessly with other parts of the Spring ecosystem, such as Spring Boot⁴.

Spring MVC works by following the Model-View-Controller (MVC) design pattern, which helps in separating the different aspects of the application, making it more manageable and scalable. Here's a high-level overview of how it works:

1. **DispatcherServlet**: At the core of Spring MVC is the `DispatcherServlet`, which acts as the front controller. It intercepts all incoming HTTP requests and delegates them to the appropriate handlers.

2. **Handler Mapping**: The `DispatcherServlet` uses handler mappings to determine which controller should handle the request. This is typically done using annotations like `@RequestMapping` on controller methods.

3. **Controller**: The controller processes the request, interacts with the service layer to handle business logic, and prepares the data to be displayed. It then returns a `ModelAndView` object, which contains the model data and the view name.

4. **View Resolver**: The `DispatcherServlet` uses a view resolver to map the view name to an actual view (e.g., a JSP file, Thymeleaf template, etc.). The view resolver helps in rendering the view with the model data.

5. **View**: The view is responsible for presenting the data to the user. It uses the model data provided by the controller to generate the final HTML response.

Here's a simplified flow of how a request is processed in Spring MVC:

![Spring MVC Flow](https://stackify.com/wp-content/uploads/2017/11/How_MVC_Works-881x441-1.png)

1. A user sends an HTTP request to the server.
2. The `DispatcherServlet` intercepts the request.
3. The `DispatcherServlet` consults the handler mappings to find the appropriate controller.
4. The controller processes the request and returns a `ModelAndView` object.
5. The `DispatcherServlet` uses the view resolver to resolve the view name to an actual view.
6. The view renders the model data and generates the final HTML response.
7. The response is sent back to the user.

This separation of concerns makes Spring MVC a powerful and flexible framework for building web applications.

<em>References:</em>
* [Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
* [17. Web MVC framework](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/mvc.html)
* [What is Spring MVC: @Controllers & @RestControllers - Marco Behler](https://www.marcobehler.com/guides/spring-mvc)
* [Spring MVC Tutorial - javatpoint](https://www.javatpoint.com/spring-mvc-tutorial)
* [How Spring Web MVC Really Works - Stackify](https://stackify.com/spring-mvc/)
* [Spring MVC Tutorial - Baeldung](https://www.baeldung.com/spring-mvc-tutorial)
* [17. Web MVC framework](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/mvc.html)
* [Spring MVC Tutorial - Javatpoint](https://www.javatpoint.com/spring-mvc-tutorial)
* [Spring MVC - HowToDoInJava](https://howtodoinjava.com/spring-mvc/spring-mvc-tutorials/)