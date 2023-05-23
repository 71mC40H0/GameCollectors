package com.zerobase.gamecollectors.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class ExceptionResponseDto {

    private String message;
    private ErrorCode errorCode;
}