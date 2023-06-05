package com.zerobase.gamecollectors.controller;

import com.zerobase.gamecollectors.model.TokenDto;
import com.zerobase.gamecollectors.model.sign.SignInRequestDto;
import com.zerobase.gamecollectors.service.ManagerSignInOutService;
import com.zerobase.gamecollectors.service.UserSignInOutService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sign-in")
@RequiredArgsConstructor
public class SignInController {

    private final ManagerSignInOutService managerSignInOutService;
    private final UserSignInOutService userSignInOutService;

    @ApiOperation(value = "관리자 로그인")
    @PostMapping("/manager")
    public ResponseEntity<TokenDto> signInManager(
        @RequestBody @ApiParam(value = "관리자 로그인 양식") @Valid SignInRequestDto requestDto) {
        return ResponseEntity.ok(managerSignInOutService.signIn(requestDto.toServiceDto()));
    }

    @ApiOperation(value = "관리자 Token 재발급")
    @GetMapping("/manager/reissue")
    public ResponseEntity<TokenDto> reissueManagerToken(
        @RequestHeader(name = "X-AUTH-TOKEN") String refreshToken) {
        return ResponseEntity.ok(managerSignInOutService.reissue(refreshToken));
    }

    @ApiOperation(value = "사용자 로그인")
    @PostMapping("/user")
    public ResponseEntity<TokenDto> signInUser(
        @RequestBody @ApiParam(value = "사용자 로그인 양식") @Valid SignInRequestDto requestDto) {
        return ResponseEntity.ok(userSignInOutService.signIn(requestDto.toServiceDto()));
    }

    @ApiOperation(value = "사용자 Token 재발급")
    @GetMapping("/user/reissue")
    public ResponseEntity<TokenDto> reissueUserToken(
        @RequestHeader(name = "X-AUTH-TOKEN") String refreshToken) {
        return ResponseEntity.ok(userSignInOutService.reissue(refreshToken));
    }
}
