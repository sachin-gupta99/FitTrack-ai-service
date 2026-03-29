package com.fitness.ai_service.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatHistoryResponse {
    String conversationId;
    String message;
    Integer type; // USER or ASSISTANT
    String timestamp;
}

