package com.zerobase.gamecollectors.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
    SIGN_UP_SUCCESS(HttpStatus.OK, "회원가입이 완료되었습니다. 이메일 인증을 해주세요."),
    VERIFICATION_COMPLETED(HttpStatus.OK, "이메일 인증이 완료되었습니다."),
    VERIFICATION_CODE_REISSUED(HttpStatus.OK, "이메일 인증코드가 재발급되었습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
