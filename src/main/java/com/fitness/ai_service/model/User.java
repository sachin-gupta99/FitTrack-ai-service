package com.fitness.ai_service.model;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@ToString
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class User {

    Integer id;
    String firstName;
    String lastName;
    String email;
    String password;
    String role;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
