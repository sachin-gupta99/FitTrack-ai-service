package com.fitness.ai_service.repository;

import com.fitness.ai_service.model.Nutrition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NutritionRepository extends MongoRepository<Nutrition, String> {
    List<Nutrition> findByUserId(Integer userId);
}

