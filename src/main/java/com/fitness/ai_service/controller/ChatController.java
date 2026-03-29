package com.fitness.ai_service.controller;

import com.fitness.ai_service.dto.ChatRequest;
import com.fitness.ai_service.dto.GlobalResponseDTO;
import com.fitness.ai_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest chatRequest) throws IOException {
        log.info("Received chat request: {}", chatRequest);
        return chatService.chatwithFitoAI(chatRequest.getMessage().getContent(), chatRequest.getMessage().getUserId());
    }

    @GetMapping("/chat/history/{userId}")
    public ResponseEntity<GlobalResponseDTO<?>> getChatHistory(@PathVariable Integer userId) {
        log.info("Fetching chat history for userId: {}", userId);
        return ResponseEntity.ok(GlobalResponseDTO.success(
                chatService.getChatHistory(userId),
                "Chat history retrieved successfully"
        ));
    }
}
