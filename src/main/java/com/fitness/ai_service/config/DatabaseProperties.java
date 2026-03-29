package com.fitness.ai_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties("aws.db")
public class DatabaseProperties {

    private String url;
    private String username;
    private String password;
}
