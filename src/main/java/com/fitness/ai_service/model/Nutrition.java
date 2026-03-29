package com.fitness.ai_service.model;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@ToString
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Document(collection = "nutrition")
public class Nutrition {

    String id;
    Integer userId;
    String foodName;
    String mealType; // Breakfast, Lunch, Dinner, Snack
    Integer calories;
    Integer protein;
    Integer carbs;
    Integer fat;
    LocalDateTime loggedAt;
    String servingSize;

    @Nullable
    String notes;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

