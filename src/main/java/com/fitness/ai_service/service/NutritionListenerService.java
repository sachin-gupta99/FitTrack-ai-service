package com.fitness.ai_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.ai_service.model.Nutrition;
import com.fitness.ai_service.dto.NutritionRecord;
import com.fitness.ai_service.repository.NutritionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@Service
@Slf4j
public class NutritionListenerService {

    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;
    private final ResourceLoader resourceLoader;
    private final NutritionRepository nutritionRepository;
    private final EmbeddingService embeddingService;

    public NutritionListenerService(ObjectMapper objectMapper,
                                    @Qualifier("internalClient") ChatClient chatClient,
                                    ResourceLoader resourceLoader,
                                    NutritionRepository nutritionRepository,
                                    EmbeddingService embeddingService) {
        this.objectMapper = objectMapper;
        this.chatClient = chatClient;
        this.resourceLoader = resourceLoader;
        this.nutritionRepository = nutritionRepository;
        this.embeddingService = embeddingService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.nutrition-queue}")
    public void processNutritionMessage(
            @Payload String message,
            @Header("action") String action
    ) {
        try {
            Nutrition nutrition = objectMapper.readValue(message, Nutrition.class);

            switch (action.toLowerCase()) {
                case "create" -> handleCreateNutrition(nutrition);
                case "update" -> handleUpdateNutrition(nutrition);
                case "delete" -> handleDeleteNutrition(nutrition.getId());
                default -> log.warn("Received nutrition message with unknown action: {}", action);
            }
        } catch (Exception e) {
            log.error("Failed to process nutrition message: {}", message, e);
            throw new RuntimeException("Failed to process nutrition message: " + e.getMessage(), e);
        }
    }

    private void handleCreateNutrition(Nutrition nutrition) throws IOException {
        String promptStringTemplate = Files.readString(
                resourceLoader.getResource("classpath:prompts/nutrition_prompt.st").getFile().toPath()
        );

        Map<String, Object> variables = Map.of(
                "foodName", nutrition.getFoodName(),
                "servingSize", nutrition.getServingSize(),
                "mealType", nutrition.getMealType() != null ? nutrition.getMealType() : "",
                "dateTime", nutrition.getLoggedAt() != null ? nutrition.getLoggedAt().toString() : "",
                "notes", nutrition.getNotes() != null ? nutrition.getNotes() : ""
        );

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(promptStringTemplate)
                .variables(variables)
                .build();

        NutritionRecord result = chatClient
                .prompt(promptTemplate.create())
                .system("You are a helpful nutrition coach who helps users understand the nutritional content of their meals based on the provided information about the food item, serving size, meal type, date/time, and any additional notes.")
                .call()
                .entity(NutritionRecord.class);

        if (result != null && result.calories() != null) {
            nutrition.setCalories(result.calories());
            nutrition.setProtein(result.protein());
            nutrition.setCarbs(result.carbs());
            nutrition.setFat(result.fat());
            nutritionRepository.save(nutrition);

            embeddingService.storeText(
                    nutrition.getUserId(),
                    "nutrition",
                    nutrition.getLoggedAt(),
                    nutrition.getId(),
                    nutrition.getFoodName() + " - " +
                            nutrition.getMealType() + ". Serving: " +
                            nutrition.getServingSize() +
                            ". Calories: " + nutrition.getCalories() +
                            ". Protein: " + nutrition.getProtein() +
                            ". Carbs: " + nutrition.getCarbs() +
                            ". Fat: " + nutrition.getFat() +
                            (nutrition.getNotes() != null ? ". Notes: " + nutrition.getNotes() : ".") +
                            " Logged on: " + nutrition.getLoggedAt()
            );

            log.info("Successfully calculated calories for food: {} - Calories: {}", nutrition.getFoodName(), nutrition.getCalories());
        } else {
            log.warn("Failed to calculate calories for food: {} - Model returned null or invalid response", nutrition.getFoodName());
        }
    }

    private void handleUpdateNutrition(Nutrition nutrition) throws IOException {
        handleDeleteNutrition(nutrition.getId());
        handleCreateNutrition(nutrition);
    }

    private void handleDeleteNutrition(String nutritionId) {
        embeddingService.deleteByReferenceId("nutrition", nutritionId);
        log.info("Deleted embedding for nutrition with id: {}", nutritionId);
    }
}
