package com.awesomepizza.orderingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Awesome Pizza Order Management API")
                        .version("1.0.0")
                        .description("API for managing pizza orders at Awesome Pizza restaurant. " +
                                "Customers can place orders without registration and track their status. " +
                                "Pizzeria staff can manage the order queue.")
                        .contact(new Contact()
                                .name("Hamid")
                                .email("hamid@awesomepizza.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.awesomepizza.com")
                                .description("Production Server")
                ))
                .tags(List.of(
                        new Tag()
                                .name("Customer Orders")
                                .description("Operations available to customers for managing their orders"),
                        new Tag()
                                .name("Pizzeria Management")
                                .description("Operations for pizzeria staff to manage order queue"),
                        new Tag()
                                .name("Menu")
                                .description("Operations for viewing available pizzas")
                ));
    }
}