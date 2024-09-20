# Spring MVC

Spring MVC (Model-View-Controller) is a framework within the Spring Framework that is used to build web applications. It follows the MVC design pattern, which separates the application into three main components:

1. **Model**: Represents the application's data and business logic.
2. **View**: Represents the presentation layer (UI) of the application.
3. **Controller**: Handles user input and interactions, updating the Model and View accordingly.

Spring MVC is built on the Servlet API and has been part of the Spring Framework from the beginning². It provides a DispatcherServlet that dispatches requests to handlers, with configurable handler mappings, view resolution, locale, and theme resolution¹. It supports annotations like `@Controller` and `@RequestMapping` to define request handling methods¹.

Some key features of Spring MVC include:
- **Flexible data binding**: Allows binding of request parameters to Java objects.
- **View resolution**: Supports various view technologies like JSP, Thymeleaf, and more.
- **RESTful support**: Enables the creation of RESTful web services using annotations like `@PathVariable`¹.
- **Integration with other Spring components**: Works seamlessly with other parts of the Spring ecosystem, such as Spring Boot⁴.

<em>References:</em>
* [Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
* [17. Web MVC framework](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/mvc.html)
* [What is Spring MVC: @Controllers & @RestControllers - Marco Behler](https://www.marcobehler.com/guides/spring-mvc)
* [Spring MVC Tutorial - javatpoint](https://www.javatpoint.com/spring-mvc-tutorial)