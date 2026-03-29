package com.fitness.ai_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.ai_service.model.Activity;
import com.fitness.ai_service.dto.CaloriesRecord;
import com.fitness.ai_service.repository.ActivityRepository;
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
public class ActivityListenerService {

    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;
    private final ResourceLoader resourceLoader;
    private final ActivityRepository activityRepository;
    private final EmbeddingService embeddingService;

    public ActivityListenerService(ObjectMapper objectMapper,
                                   @Qualifier("internalClient") ChatClient chatClient,
                                   ResourceLoader resourceLoader,
                                   ActivityRepository activityRepository,
                                   EmbeddingService embeddingService) {
        this.objectMapper = objectMapper;
        this.chatClient = chatClient;
        this.resourceLoader = resourceLoader;
        this.activityRepository = activityRepository;
        this.embeddingService = embeddingService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.activity-queue}")
    public void processActivityMessage(
            @Payload String message,
            @Header("action") String action
    ) {
        try {
            Activity activity = objectMapper.readValue(message, Activity.class);

            switch (action.toLowerCase()) {
                case "create" -> handleCreateActivity(activity);
                case "update" -> handleUpdateActivity(activity);
                case "delete" -> handleDeleteActivity(activity.getId());
                default -> log.warn("Received activity message with unknown action: {}", action);
            }
        } catch (Exception e) {
            log.error("Failed to process activity message: {}", message, e);
            throw new RuntimeException("Failed to process activity message: " + e.getMessage(), e);
        }
    }

    private void handleCreateActivity(Activity activity) throws IOException {
        String promptStringTemplate = Files.readString(
                resourceLoader.getResource("classpath:prompts/calories_prompt.st").getFile().toPath()
        );

        Map<String, Object> variables = Map.of(
                "activity", activity.getType(),
                "duration", activity.getDuration(),
                "notes", activity.getMetadata() != null ? activity.getMetadata() : ""
        );

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .template(promptStringTemplate)
                .variables(variables)
                .build();

        CaloriesRecord cal = chatClient.prompt(promptTemplate.create())
                .call()
                .entity(CaloriesRecord.class);

        if(cal != null && cal.calories() != null) {

            activity.setCaloriesBurned(cal.calories());
            activityRepository.save(activity);

            embeddingService.storeText(
                    activity.getUserId(),
                    "activity",
                    activity.getCreatedAt(),
                    activity.getId(),
                    activity.getType() + " for " +
                            activity.getDuration() + " minutes. Notes: " +
                            (activity.getMetadata() != null ? activity.getMetadata() : "") +
                            ". Calories burned: " + cal.calories() + " kcal." +
                            " Logged on: " + activity.getCreatedAt()
            );

            log.info("Calculated calories burned: {} for activity: {}", cal.calories(), activity.getType());
        } else {
            log.warn("Failed to calculate calories for activity: {} - Model returned null or invalid response", activity.getType());
        }
    }

    private void handleUpdateActivity(Activity activity) throws IOException {
        handleDeleteActivity(activity.getId());
        handleCreateActivity(activity);
    }

    private void handleDeleteActivity(String activityId) {
        embeddingService.deleteByReferenceId("activity", activityId);
        log.info("Deleted embedding for activity with id: {}", activityId);
    }
}
