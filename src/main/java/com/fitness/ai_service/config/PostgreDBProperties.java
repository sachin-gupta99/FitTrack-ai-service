package com.fitness.ai_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties("aws.postgres-db")
public class PostgreDBProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
