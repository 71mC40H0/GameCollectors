package com.zerobase.gamecollectors.controller;

import com.zerobase.gamecollectors.model.ManagerSignUpRequestDto;
import com.zerobase.gamecollectors.service.ManagerSignUpService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/signUp")
@RequiredArgsConstructor
public class SignUpController {

    private final ManagerSignUpService managerSignUpService;

    @ApiOperation(value = "관리자 회원 가입")
    @PostMapping("/manager")
    public ResponseEntity<String> managerSignUp(
        @RequestBody @ApiParam(value = "관리자 회원가입 양식") ManagerSignUpRequestDto request) {
        managerSignUpService.signUp(request.toServiceDto());
        return ResponseEntity.ok("회원가입이 완료되었습니다. 이메일 인증을 해주세요.");
    }

    @ApiOperation(value = "관리자 이메일 인증")
    @GetMapping("/manager/verify")
    public ResponseEntity<String> managerVerifyEmail(
        @RequestParam @ApiParam(value = "관리자 이메일", example = "abc@example.com") String email,
        @RequestParam @ApiParam(value = "이메일 인증코드", example = "abc0123ABC") String code) {
        managerSignUpService.verifyEmail(email, code);
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    @ApiOperation(value = "관리자 이메일 인증코드 재발급")
    @GetMapping("/manager/verify/reissue")
    public ResponseEntity<String> reissueVerificationCode(
        @RequestParam @ApiParam(value = "관리자 아이디", example = "1") Long id) {
        managerSignUpService.reissueVerificationCode(id);
        return ResponseEntity.ok("이메일 인증코드가 재발급되었습니다.");
    }

}
