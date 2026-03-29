package com.fitness.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalResponseDTO<T> {

    private T data;
    private String message;
    private int statusCode;

    public static <T> GlobalResponseDTO<T> success(T data, String message) {
        return new GlobalResponseDTO<T>(data, message, 200);
    }

    public static <T> GlobalResponseDTO<T> failure(String s, int i) {
        return new GlobalResponseDTO<>(null, s, i);
    }
}
