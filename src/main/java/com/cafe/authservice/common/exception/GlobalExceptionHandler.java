package com.cafe.authservice.common.exception;

import com.cafe.authservice.common.response.CommonResponse;
import com.cafe.authservice.common.response.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<?>> handleValidation(MethodArgumentNotValidException e) {

        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("유효성 검사 실패");

        return ResponseEntity
                .badRequest()
                .body(CommonResponse.fail(ErrorCode.AUTH_TOKEN_INVALID.getCode(), message));
    }
}
