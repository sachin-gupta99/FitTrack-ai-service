package com.fitness.ai_service.service;

import com.fitness.ai_service.dto.ChatHistoryResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingService embeddingService;
    private final ResourceLoader resourceLoader;

    public ChatService(@Qualifier("chatClient") ChatClient chatClient,
                       JdbcTemplate jdbcTemplate,
                       EmbeddingService embeddingService,
                       ResourceLoader resourceLoader) {
        this.chatClient = chatClient;
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingService = embeddingService;
        this.resourceLoader = resourceLoader;
    }

    public Flux<String> chatwithFitoAI(String userMessage, Integer userId) throws IOException {

        // Retrieve relevant context from vector store for both activity and nutrition
        List<Document> activityDocs = embeddingService.similaritySearch(userId, "activity", userMessage, 3);
        List<Document> nutritionDocs = embeddingService.similaritySearch(userId, "nutrition", userMessage, 3);
        List<Document> userDocs = embeddingService.similaritySearch(userId, "user", userMessage, 3);

        String context = buildContext(activityDocs, nutritionDocs, userDocs);

        String systemPrompt = Files.readString(
                resourceLoader.getResource("classpath:prompts/system_prompt_for_rag.st").getFile().toPath()
        );

        PromptTemplate systemPromptTemplate = PromptTemplate.builder()
                .template(systemPrompt)
                .variables(Map.of("context", context))
                .build();

        return chatClient.prompt()
                .system(systemPromptTemplate.create().getContents())
                .user(userMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, userId.toString()))
                .stream()
                .content();
    }

    private String buildContext(List<Document> activityDocs, List<Document> nutritionDocs, List<Document> userDocs) {
        StringBuilder sb = new StringBuilder();

        if (!activityDocs.isEmpty()) {
            sb.append("=== Activity Data ===\n");
            sb.append(activityDocs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n")));
            sb.append("\n\n");
        }

        if (!nutritionDocs.isEmpty()) {
            sb.append("=== Nutrition Data ===\n");
            sb.append(nutritionDocs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n")));
        }

        if (!userDocs.isEmpty()) {
            sb.append("=== User Data ===\n");
            sb.append(userDocs.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n")));
        }

        return sb.isEmpty() ? "No prior user data available." : sb.toString();
    }

    public List<ChatHistoryResponse> getChatHistory(Integer userId) {
        String sql = "SELECT conversation_id, content, type, \"timestamp\" FROM spring_ai_chat_memory " +
                     "WHERE conversation_id = ? ORDER BY \"timestamp\" ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> ChatHistoryResponse.builder()
                .conversationId(rs.getString("conversation_id"))
                .message(rs.getString("content"))
                .type(rs.getString("type").equals("USER") ? 1 : 2)
                .timestamp(rs.getTimestamp("timestamp").toString())
                .build(),
                userId.toString()
        );
    }
}
