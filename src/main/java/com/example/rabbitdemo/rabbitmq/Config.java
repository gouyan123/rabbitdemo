package com.example.rabbitdemo.rabbitmq;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    @RefreshScope
    @ConfigurationProperties(prefix = "rabbitmq.consumer")
    public Param param(){
        Param param = new Param();
        return param;
    }
}
