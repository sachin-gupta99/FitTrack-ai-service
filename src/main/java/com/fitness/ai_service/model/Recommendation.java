package com.fitness.ai_service.model;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Document(collection = "fitness-recommendations")
public class Recommendation {

    @Id
    String id;
    String activityId;
    String userId;
    String activityType;
    String recommendationText;
    List<String> improvements;
    List<String> similarActivities;
    List<String> motivationalQuotes;

    @CreatedDate
    LocalDateTime createdAt;

    @LastModifiedDate
    LocalDateTime updatedAt;
}
