package com.fitness.ai_service.config;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("google.genai")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class GoogleGenAiProperties {

    String apiKey;
    String embeddingModel;
}
