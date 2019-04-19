package com.example.rabbitdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class RabbitdemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(RabbitdemoApplication.class, args);
    }
}
