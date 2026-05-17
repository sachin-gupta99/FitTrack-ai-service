package com.fitness.ai_service.config;

import com.fitness.ai_service.service.ParameterStoreService;
import org.springframework.ai.google.genai.GoogleGenAiEmbeddingConnectionDetails;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingModelConfig {

    @Bean
    public GoogleGenAiTextEmbeddingModel googleGenAiTextEmbeddingModel(
            GoogleGenAiProperties props,
            ParameterStoreService parameterStoreService) {

        String apiKey = parameterStoreService.getParameterValue(props.getApiKey());

        GoogleGenAiEmbeddingConnectionDetails details =
                GoogleGenAiEmbeddingConnectionDetails.builder()
                        .apiKey(apiKey)
                        .build();

        GoogleGenAiTextEmbeddingOptions options =
                GoogleGenAiTextEmbeddingOptions.builder()
                        .model(props.getEmbeddingModel())
                        .dimensions(768)
                        .build();

        return new GoogleGenAiTextEmbeddingModel(details, options);
    }
}
