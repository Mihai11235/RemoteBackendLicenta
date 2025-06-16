package start;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/* * This is the main class to start the REST services.
 * It uses Spring Boot to bootstrap the application.
 * The scanBasePackages attribute is set to "org.example" to include all components in that package.
 */
@SpringBootApplication(scanBasePackages = "org.example")
public class StartRestServices {
    public static void main(String[] args) {
        SpringApplication.run(StartRestServices.class, args);
    }
}