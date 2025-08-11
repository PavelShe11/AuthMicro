package io.github.pavelshe11.authmicro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuthMicroApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthMicroApplication.class, args);
    }

}
