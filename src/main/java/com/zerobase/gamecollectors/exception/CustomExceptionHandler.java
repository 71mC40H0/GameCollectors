package com.zerobase.gamecollectors.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ExceptionResponseDto> handleCustomException(final CustomException e) {
        log.warn("Exception : {}", e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
            .body(ExceptionResponseDto.builder()
                .message(e.getMessage())
                .errorCode(e.getErrorCode())
                .build());
    }
}