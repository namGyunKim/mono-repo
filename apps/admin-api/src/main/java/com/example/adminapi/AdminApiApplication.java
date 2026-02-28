package com.example.adminapi;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example")
@AutoConfigurationPackage(basePackages = "com.example")
@EnableJpaRepositories(basePackages = "com.example")
public class AdminApiApplication {
    public static void main(final String[] args) {
        final SpringApplication app = new SpringApplication(AdminApiApplication.class);
        app.setDefaultProperties(Map.of("app.type", "admin"));
        app.run(args);
    }
}
