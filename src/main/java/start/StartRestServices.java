package start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = "org.example")
public class StartRestServices {
    public static void main(String[] args) {
        SpringApplication.run(StartRestServices.class, args);
    }
}