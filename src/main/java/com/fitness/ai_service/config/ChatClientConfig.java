package com.fitness.ai_service.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

//    @Bean
//    public ChatMemory chatMemory(MongoChatMemoryRepository mongoChatMemoryRepository) {
//        return MessageWindowChatMemory.builder()
//                .chatMemoryRepository(mongoChatMemoryRepository)
////                .maxMessages(30) // Keep the last 30 messages in memory
//                .build();
//    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    @Bean
    @Qualifier("chatClient")
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

//        return ChatClient.builder(ollamaChatModel)
//                .defaultOptions(OllamaChatOptions.builder()
//                        .temperature(0.7) // Set the temperature for response generation
//                        .build())
//                .build();

    @Bean
    @Qualifier("internalClient")
    public ChatClient internalClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
