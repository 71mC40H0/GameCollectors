package com.zerobase.gamecollectors.controller;

import com.zerobase.gamecollectors.common.TokenType;
import com.zerobase.gamecollectors.common.UserType;
import com.zerobase.gamecollectors.config.JwtAuthenticationProvider;
import com.zerobase.gamecollectors.exception.CustomException;
import com.zerobase.gamecollectors.exception.ErrorCode;
import com.zerobase.gamecollectors.model.game.GameInfoResponseDto;
import com.zerobase.gamecollectors.model.game.GameRegisterRequestDto;
import com.zerobase.gamecollectors.model.game.GameUpdateRequestDto;
import com.zerobase.gamecollectors.response.ResponseCode;
import com.zerobase.gamecollectors.service.ManagerGameService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerGameService managerGameService;
    private final JwtAuthenticationProvider provider;

    @ApiOperation(value = "게임 등록")
    @PostMapping("/game")
    public ResponseEntity<?> registerGame(
        @RequestHeader(name = "X-AUTH-TOKEN") String accessToken,
        @RequestBody @ApiParam(value = "게임 등록 양식") @Valid GameRegisterRequestDto requestDto) {
        if (!provider.validateToken(accessToken, UserType.MANAGER).equals(TokenType.ACCESS_TOKEN)) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        managerGameService.registerGame(requestDto.toServiceDto());
        return ResponseEntity.status(ResponseCode.GAME_REGISTER_SUCCESS.getHttpStatus())
            .body(ResponseCode.GAME_REGISTER_SUCCESS);
    }

    @ApiOperation(value = "관리자 게임 전체 조회")
    @GetMapping("/game")
    public ResponseEntity<List<GameInfoResponseDto>> getGameInfos(
        @RequestHeader(name = "X-AUTH-TOKEN") String accessToken) {
        if (!provider.validateToken(accessToken, UserType.MANAGER).equals(TokenType.ACCESS_TOKEN)) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        return ResponseEntity.ok(managerGameService.getInfos());
    }

    @ApiOperation(value = "관리자 게임 조회")
    @GetMapping("/game/{id}")
    public ResponseEntity<GameInfoResponseDto> getGameInfo(
        @RequestHeader(name = "X-AUTH-TOKEN") String accessToken,
        @PathVariable @ApiParam(value = "게임 ID") Long id) {
        if (!provider.validateToken(accessToken, UserType.MANAGER).equals(TokenType.ACCESS_TOKEN)) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        return ResponseEntity.ok(managerGameService.getInfo(id));
    }

    @ApiOperation(value = "게임 수정")
    @PutMapping("/game/{id}")
    public ResponseEntity<?> updateGame(@RequestHeader(name = "X-AUTH-TOKEN") String accessToken,
        @PathVariable @ApiParam(value = "게임 ID") Long id,
        @RequestBody @ApiParam(value = "게임 등록 양식") @Valid GameUpdateRequestDto requestDto) {
        if (!provider.validateToken(accessToken, UserType.MANAGER).equals(TokenType.ACCESS_TOKEN)) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        managerGameService.updateGame(id, requestDto.toServiceDto());
        return ResponseEntity.status(ResponseCode.GAME_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseCode.GAME_UPDATE_SUCCESS);
    }

    @ApiOperation(value = "게임 삭제")
    @DeleteMapping("/game/{id}")
    public ResponseEntity<?> deleteGame(
        @RequestHeader(name = "X-AUTH-TOKEN") String accessToken,
        @PathVariable @ApiParam(value = "게임 ID") Long id) {
        if (!provider.validateToken(accessToken, UserType.MANAGER).equals(TokenType.ACCESS_TOKEN)) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
        managerGameService.deleteGame(id);
        return ResponseEntity.status(ResponseCode.GAME_DELETE_SUCCESS.getHttpStatus())
            .body(ResponseCode.GAME_DELETE_SUCCESS);
    }
}
