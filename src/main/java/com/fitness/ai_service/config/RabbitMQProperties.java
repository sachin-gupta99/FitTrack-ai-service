package com.fitness.ai_service.config;

import lombok.Data;
import lombok.experimental.FieldDefaults;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties("rabbitmq")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RabbitMQProperties {

    String addresses;
}
