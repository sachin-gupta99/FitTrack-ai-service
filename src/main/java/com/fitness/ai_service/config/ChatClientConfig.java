package com.fitness.ai_service.config;

import com.fitness.ai_service.service.ParameterStoreService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    @Bean
    public OpenAiApi openAiApi(GroqProperties groq, ParameterStoreService parameterStoreService) {
        String resolvedApiKey = parameterStoreService.getParameterValue(groq.getApiKey());
        return OpenAiApi.builder()
                .baseUrl(groq.getBaseUrl())
                .apiKey(resolvedApiKey)
                .build();
    }

    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi, GroqProperties groq) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(groq.getChatModel())
                        .temperature(0.7)
                        .build())
                .build();
    }

    @Bean(name = "chatClient")
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean(name = "internalClient")
    public ChatClient internalClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
