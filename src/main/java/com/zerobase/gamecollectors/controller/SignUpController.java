package com.zerobase.gamecollectors.controller;

import com.zerobase.gamecollectors.model.ManagerSignUpRequestDto;
import com.zerobase.gamecollectors.model.UserSignUpRequestDto;
import com.zerobase.gamecollectors.response.ResponseCode;
import com.zerobase.gamecollectors.service.ManagerSignUpService;
import com.zerobase.gamecollectors.service.UserSignUpService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/sign-up")
@RequiredArgsConstructor
public class SignUpController {

    private final ManagerSignUpService managerSignUpService;
    private final UserSignUpService userSignUpService;

    @ApiOperation(value = "관리자 회원 가입")
    @PostMapping("/manager")
    public ResponseEntity<?> managerSignUp(
        @RequestBody @ApiParam(value = "관리자 회원가입 양식") @Valid ManagerSignUpRequestDto request) {
        managerSignUpService.signUp(request.toServiceDto());
        return ResponseEntity.status(ResponseCode.SIGN_UP_SUCCESS.getHttpStatus())
            .body(ResponseCode.SIGN_UP_SUCCESS.getMessage());
    }

    @ApiOperation(value = "관리자 이메일 인증")
    @GetMapping("/manager/verify")
    public ResponseEntity<?> managerVerifyEmail(
        @RequestParam @ApiParam(value = "관리자 이메일", example = "abc@example.com") String email,
        @RequestParam @ApiParam(value = "이메일 인증코드", example = "abc0123ABC") String code) {
        managerSignUpService.verifyEmail(email, code);
        return ResponseEntity.status(ResponseCode.VERIFICATION_COMPLETED.getHttpStatus())
            .body(ResponseCode.VERIFICATION_COMPLETED.getMessage());
    }

    @ApiOperation(value = "관리자 이메일 인증코드 재발급")
    @GetMapping("/manager/verify/reissue")
    public ResponseEntity<?> managerReissueVerificationCode(
        @RequestParam @ApiParam(value = "관리자 아이디", example = "1") Long id) {
        managerSignUpService.reissueVerificationCode(id);
        return ResponseEntity.status(ResponseCode.VERIFICATION_CODE_REISSUED.getHttpStatus())
            .body(ResponseCode.VERIFICATION_CODE_REISSUED.getMessage());
    }

    @ApiOperation(value = "사용자 회원 가입")
    @PostMapping("/user")
    public ResponseEntity<?> userSignUp(
        @RequestBody @ApiParam(value = "사용자 회원가입 양식") @Valid UserSignUpRequestDto request) {
        userSignUpService.signUp(request.toServiceDto());
        return ResponseEntity.status(ResponseCode.SIGN_UP_SUCCESS.getHttpStatus())
            .body(ResponseCode.SIGN_UP_SUCCESS.getMessage());
    }

    @ApiOperation(value = "사용자 이메일 인증")
    @GetMapping("/user/verify")
    public ResponseEntity<?> userVerifyEmail(
        @RequestParam @ApiParam(value = "사용자 이메일", example = "abc@example.com") String email,
        @RequestParam @ApiParam(value = "이메일 인증코드", example = "abc0123ABC") String code) {
        userSignUpService.verifyEmail(email, code);
        return ResponseEntity.status(ResponseCode.VERIFICATION_COMPLETED.getHttpStatus())
            .body(ResponseCode.VERIFICATION_COMPLETED.getMessage());
    }

    @ApiOperation(value = "사용자 이메일 인증코드 재발급")
    @GetMapping("/user/verify/reissue")
    public ResponseEntity<?> userReissueVerificationCode(
        @RequestParam @ApiParam(value = "사용자 아이디", example = "1") Long id) {
        userSignUpService.reissueVerificationCode(id);
        return ResponseEntity.status(ResponseCode.VERIFICATION_CODE_REISSUED.getHttpStatus())
            .body(ResponseCode.VERIFICATION_CODE_REISSUED.getMessage());
    }

}
