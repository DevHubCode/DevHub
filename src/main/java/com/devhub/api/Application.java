package com.devhub.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.devhub.api")
@OpenAPIDefinition(info = @Info(title = "API DevHub", version = "1", description = "API desenvolvido para requisiçoes de dados de freelancers e contratantes"))
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
