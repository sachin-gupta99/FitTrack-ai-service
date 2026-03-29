package com.fitness.ai_service.exceptions;

import com.fitness.ai_service.dto.ErrorDTO;
import com.fitness.ai_service.dto.GlobalResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponseDTO<ErrorDTO>> handleException(Exception exception) {

        ErrorDTO error = null;

        switch(exception) {

            case IllegalArgumentException illegalArgumentException -> {
                error = ErrorDTO.of("Bad Request", "The request is invalid");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 400));
            }
            case RuntimeException runtimeException -> {
                error = ErrorDTO.of("Something went wrong", "An unexpected error occurred");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 500));
            }
            case null, default -> {
                assert exception != null;
                error = ErrorDTO.of("Internal Server Error", exception.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(GlobalResponseDTO.failure(error.getMessage() + " : " + error.getDescription(), 500));
    }
}

