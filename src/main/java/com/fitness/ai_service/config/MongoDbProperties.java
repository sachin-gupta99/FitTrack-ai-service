package com.fitness.ai_service.config;

import lombok.Data;
import lombok.experimental.FieldDefaults;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties("mongodb")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MongoDbProperties {

    String uri;
    String database;
}
