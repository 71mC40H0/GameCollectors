package com.zerobase.gamecollectors.controller;

import com.zerobase.gamecollectors.response.ResponseCode;
import com.zerobase.gamecollectors.service.ManagerSignInOutService;
import com.zerobase.gamecollectors.service.UserSignInOutService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sign-out")
@RequiredArgsConstructor
public class SignOutController {

    private final ManagerSignInOutService managerSignInOutService;
    private final UserSignInOutService userSignInOutService;

    @ApiOperation(value = "관리자 로그아웃")
    @GetMapping("/manager")
    public ResponseEntity<?> signOutManager(@RequestHeader(name = "X-AUTH-TOKEN") String accessToken) {
        managerSignInOutService.signOut(accessToken);
        return ResponseEntity.status(ResponseCode.SIGN_OUT_SUCCESS.getHttpStatus())
            .body(ResponseCode.SIGN_OUT_SUCCESS);
    }

    @ApiOperation(value = "사용자 로그아웃")
    @GetMapping("/user")
    public ResponseEntity<?> signOutUser(@RequestHeader(name = "X-AUTH-TOKEN") String accessToken) {
        userSignInOutService.signOut(accessToken);
        return ResponseEntity.status(ResponseCode.SIGN_OUT_SUCCESS.getHttpStatus())
            .body(ResponseCode.SIGN_OUT_SUCCESS);
    }
}
