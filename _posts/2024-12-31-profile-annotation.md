# Profile Annotation

The `@Profile` annotation in Spring Boot is a useful feature for defining beans that should only be created when specific profiles are active. Here's a concise guide to using it:

1. **Define Profiles**:
   First, set up your profiles in your `application.properties` or `application.yml` file. For example:
   ```properties
   # application.properties
   spring.profiles.active=dev
   ```
   or
   ```yaml
   # application.yml
   spring:
     profiles:
       active: dev
   ```

2. **Use @Profile Annotation**:
   Apply the `@Profile` annotation to the beans that you want to activate only for certain profiles. For instance:
   ```java
   import org.springframework.context.annotation.Profile;
   import org.springframework.stereotype.Component;

   @Component
   @Profile("dev")
   public class DevService {
       // Development-specific implementation
   }

   @Component
   @Profile("prod")
   public class ProdService {
       // Production-specific implementation
   }
   ```

3. **Activate Profiles**:
   To activate profiles, you can set the `spring.profiles.active` property. This can be done in the `application.properties` or `application.yml` file, or as a command-line argument:
   ```shell
   java -jar myapp.jar --spring.profiles.active=prod
   ```

4. **Conditional Bean Creation**:
   You can also use `@Profile` in configuration classes to conditionally create beans:
   ```java
   import org.springframework.context.annotation.Bean;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.context.annotation.Profile;

   @Configuration
   public class AppConfig {
       
       @Bean
       @Profile("dev")
       public DevService devService() {
           return new DevService();
       }
       
       @Bean
       @Profile("prod")
       public ProdService prodService() {
           return new ProdService();
       }
   }
   ```

That's it! With the `@Profile` annotation, you can easily manage different configurations and beans for different environments. Have fun coding! 🚀