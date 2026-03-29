package com.fitness.ai_service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRequest {
    String id;
    Integer type;
    ChatContentRequest message;
    String timestamp;
}

