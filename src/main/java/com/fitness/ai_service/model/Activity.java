package com.fitness.ai_service.model;

import com.fitness.ai_service.dto.ActivityType;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Document(collection = "activities")
public class Activity {

    String id;
    Integer userId;
    ActivityType type; // e.g., "Running", "Cycling"
    Integer duration; // in minutes
    LocalDateTime startTime;
    Integer caloriesBurned;

    @Nullable
    String metadata; // For any additional info like distance, pace, etc.
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
