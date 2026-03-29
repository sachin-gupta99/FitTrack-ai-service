package com.fitness.ai_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.ai_service.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsListenerService {

    private final ObjectMapper objectMapper;
    private final EmbeddingService embeddingService;

    @RabbitListener(queues = "${rabbitmq.queue.user-queue}")
    public void processUserDetails(@Payload String message) throws JsonProcessingException {

        User user = objectMapper.readValue(message, User.class);

        embeddingService.storeText(
                user.getId(),
                "user",
                user.getCreatedAt(),
                user.getId().toString(),
                "User Details: " +
                        "Name: " + user.getFirstName() + " " + user.getLastName() + ", " +
                        "Email: " + user.getEmail() + ", " +
                        "Role: " + user.getRole()
        );
    }

}
