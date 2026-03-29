package com.fitness.ai_service.controller;

import com.fitness.ai_service.dto.GlobalResponseDTO;
import com.fitness.ai_service.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/user/{userId}/recommendations")
    public ResponseEntity<GlobalResponseDTO<?>> getUserRecommendations(@PathVariable String userId) {
        return ResponseEntity.ok(GlobalResponseDTO.success(recommendationService.getUserRecommendations(userId), "User recommendations retrieved successfully"));
    }

    @GetMapping("/activity/{activityId}/recommendations")
    public ResponseEntity<GlobalResponseDTO<?>> getActivityRecommendations(@PathVariable String activityId) {
        return ResponseEntity.ok(GlobalResponseDTO.success(recommendationService.getActivityRecommendations(activityId), "Activity recommendations retrieved successfully"));
    }
}
