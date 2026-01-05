package ru.springboot.leondemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(
        basePackages = "ru.springboot.leondemo.repository"
)
public class LeondemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeondemoApplication.class, args);
    }
}
