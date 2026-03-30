package com.fitness.ai_service.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fitness.ai_service.service.ParameterStoreService;

import javax.sql.DataSource;

@Configuration
@AllArgsConstructor
public class PostgreDBConfig {

    private final PostgreDBProperties databaseProperties;
    private final ParameterStoreService parameterStoreService;

    @Bean
    public DataSource getDataSource() {

        String dbUrl = parameterStoreService.getParameterValue(databaseProperties.getUrl());
        String dbUsername = parameterStoreService.getParameterValue(databaseProperties.getUsername());
        String dbPassword = parameterStoreService.getParameterValue(databaseProperties.getPassword());

        System.out.println("Fetched dbUrl: " + dbUrl);
        System.out.println("Fetched dbUsername: " + dbUsername);
        System.out.println("Fetched dbPassword: " + dbPassword);

        return DataSourceBuilder.create()
                .url(dbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .driverClassName(databaseProperties.getDriverClassName())
                .build();

    }
}
