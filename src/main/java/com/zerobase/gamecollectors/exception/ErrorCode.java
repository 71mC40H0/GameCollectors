package com.zerobase.gamecollectors.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    ALREADY_REGISTERED_USER(HttpStatus.BAD_REQUEST, "이미 가입된 회원입니다."),
    NOT_FOUND_USER(HttpStatus.BAD_REQUEST, "일치하는 회원이 없습니다."),
    ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "이미 인증이 완료되었습니다."),
    WRONG_VERIFICATION(HttpStatus.BAD_REQUEST, "잘못된 인증 시도입니다."),
    NEED_NEW_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증 코드를 재발급받아주세요."),
    NOT_VERIFIED_USER(HttpStatus.BAD_REQUEST, "이메일 인증을 해주세요."),

    MISMATCHED_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 틀립니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 Refresh Token입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 Access Token입니다."),

    INVALID_DEPOSIT(HttpStatus.CONFLICT, "해당 사용자의 예치금이 이미 존재합니다. 관리자에게 문의 하세요."),
    INVALID_POINT(HttpStatus.CONFLICT, "해당 사용자의 적립금이 이미 존재합니다. 관리자에게 문의 하세요.");

    private final HttpStatus httpStatus;
    private final String message;

}