package com.zerobase.gamecollectors.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.zerobase.gamecollectors.common.TokenType;
import com.zerobase.gamecollectors.common.UserType;
import com.zerobase.gamecollectors.common.UserVo;
import com.zerobase.gamecollectors.config.JwtAuthenticationProvider;
import com.zerobase.gamecollectors.domain.entity.Manager;
import com.zerobase.gamecollectors.domain.repository.ManagerRepository;
import com.zerobase.gamecollectors.exception.CustomException;
import com.zerobase.gamecollectors.exception.ErrorCode;
import com.zerobase.gamecollectors.model.TokenDto;
import com.zerobase.gamecollectors.model.sign.SignInServiceDto;
import com.zerobase.gamecollectors.redis.RedisUtil;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ManagerSignInOutServiceTest {

    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private ManagerSignInOutService managerSignInOutService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtAuthenticationProvider provider;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        RedisUtil.setTemplate(stringRedisTemplate);
    }

    @Test
    @DisplayName("로그인 성공")
    void testSignInSuccess() {
        //given
        SignInServiceDto serviceDto = SignInServiceDto.builder()
            .email("abc@example.com")
            .password("1")
            .build();

        given(managerRepository.findByEmail(anyString())).willReturn(
            Optional.of(Manager.builder()
                .id(1L)
                .email("abc@example.com")
                .password("1")
                .emailAuth(true)
                .build()));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(provider.createAccessToken(anyString(), anyLong(), any())).willReturn("Access_Token");
        given(provider.createRefreshToken(anyString(), anyLong(), any())).willReturn("Refresh_Token");

        //when
        TokenDto tokenDto = managerSignInOutService.signIn(serviceDto);

        //then
        verify(managerRepository).findByEmail(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(provider).createAccessToken(anyString(), anyLong(), any());
        verify(provider).createRefreshToken(anyString(), anyLong(), any());
        assertEquals("Access_Token", tokenDto.getAccessToken());
        assertEquals("Refresh_Token", tokenDto.getRefreshToken());
    }

    @Test
    @DisplayName("로그인 실패 - 일치하는 회원 없음")
    void testSignInFail_NotFoundUser() {
        //given
        SignInServiceDto serviceDto = SignInServiceDto.builder()
            .email("abc@example.com")
            .password("1")
            .build();

        given(managerRepository.findByEmail(serviceDto.getEmail())).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignInOutService.signIn(serviceDto));

        //then
        verify(managerRepository).findByEmail(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(provider, never()).createAccessToken(anyString(), anyLong(), any());
        verify(provider, never()).createRefreshToken(anyString(), anyLong(), any());
        assertEquals(ErrorCode.NOT_FOUND_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void testSignInFail_Mismatched_Password() {
        //given
        SignInServiceDto serviceDto = SignInServiceDto.builder()
            .email("abc@example.com")
            .password("1")
            .build();

        given(managerRepository.findByEmail(anyString())).willReturn(
            Optional.of(Manager.builder()
                .id(1L)
                .email("abc@example.com")
                .password("2")
                .emailAuth(true)
                .build()));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignInOutService.signIn(serviceDto));

        //then
        verify(managerRepository).findByEmail(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(provider, never()).createAccessToken(anyString(), anyLong(), any());
        verify(provider, never()).createRefreshToken(anyString(), anyLong(), any());
        assertEquals(ErrorCode.MISMATCHED_PASSWORD, exception.getErrorCode());
    }


    @Test
    @DisplayName("토큰 재발급 성공 - Refresh Token 미발급")
    void testReissueSuccess_ExceptRefreshToken() {
        //given
        Date now = new Date();

        try (MockedStatic<RedisUtil> redisUtilMockedStatic = mockStatic(RedisUtil.class)) {
            given(provider.validateToken(anyString(), any(UserType.class))).willReturn(TokenType.REFRESH_TOKEN);
            given(provider.getUserVo(anyString())).willReturn(new UserVo(1L, "abc@example.com"));
            given(managerRepository.findByIdAndEmail(anyLong(), anyString())).willReturn(
                Optional.of(Manager.builder()
                    .id(1L)
                    .email("abc@example.com")
                    .password("1")
                    .emailAuth(true)
                    .build()));
            given(RedisUtil.existData(anyString())).willReturn(true);
            given(RedisUtil.getData(anyString())).willReturn("Refresh_Token");
            given(provider.createAccessToken(anyString(), anyLong(), any())).willReturn("New_Access_Token");
            given(provider.getExpiration(anyString())).willReturn(now.getTime() + 1000L * 60 * 60 * 24 * 14);

            //when
            TokenDto tokenDto = managerSignInOutService.reissue("Refresh_Token");

            //then
            verify(provider).validateToken(anyString(), any(UserType.class));
            verify(provider).getUserVo(anyString());
            verify(managerRepository).findByIdAndEmail(anyLong(), anyString());
            verify(provider).createAccessToken(anyString(), anyLong(), any());
            verify(provider, never()).createRefreshToken(anyString(), anyLong(), any());
            redisUtilMockedStatic.verify(() -> RedisUtil.existData(anyString()));
            redisUtilMockedStatic.verify(() -> RedisUtil.getData(anyString()));
            assertEquals(tokenDto.getAccessToken(), "New_Access_Token");
            assertEquals(tokenDto.getRefreshToken(), "Refresh_Token");
        }
    }

    @Test
    @DisplayName("토큰 재발급 성공 - Refresh Token도 재발급")
    void testReissueSuccess_RenewRefreshToken() {
        //given
        Date now = new Date();

        try (MockedStatic<RedisUtil> redisUtilMockedStatic = mockStatic(RedisUtil.class)) {
            given(provider.validateToken(anyString(), any(UserType.class))).willReturn(TokenType.REFRESH_TOKEN);
            given(provider.getUserVo(anyString())).willReturn(new UserVo(1L, "abc@example.com"));
            given(managerRepository.findByIdAndEmail(anyLong(), anyString())).willReturn(
                Optional.of(Manager.builder()
                    .id(1L)
                    .email("abc@example.com")
                    .password("1")
                    .emailAuth(true)
                    .build()));
            given(RedisUtil.existData(anyString())).willReturn(true);
            given(RedisUtil.getData(anyString())).willReturn("Refresh_Token");
            given(provider.createAccessToken(anyString(), anyLong(), any())).willReturn("New_Access_Token");
            given(provider.createRefreshToken(anyString(), anyLong(), any())).willReturn("New_Refresh_Token");
            given(provider.getExpiration(anyString())).willReturn(now.getTime() + 1000L * 60 * 60 * 2);

            //when
            TokenDto tokenDto = managerSignInOutService.reissue("Refresh_Token");

            //then
            verify(provider).validateToken(anyString(), any(UserType.class));
            verify(provider).getUserVo(anyString());
            verify(managerRepository).findByIdAndEmail(anyLong(), anyString());
            verify(provider).createAccessToken(anyString(), anyLong(), any());
            verify(provider).createRefreshToken(anyString(), anyLong(), any());
            redisUtilMockedStatic.verify(() -> RedisUtil.existData(anyString()));
            redisUtilMockedStatic.verify(() -> RedisUtil.getData(anyString()));
            assertEquals(tokenDto.getAccessToken(), "New_Access_Token");
            assertEquals(tokenDto.getRefreshToken(), "New_Refresh_Token");
        }
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 Refresh Token")
    void testReissueFail_RenewRefreshToken() {
        //given
        given(provider.validateToken(anyString(), any(UserType.class))).willReturn(TokenType.INVALID_TOKEN);

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignInOutService.reissue("Invalid_Token"));

        //then
        verify(provider).validateToken(anyString(), any(UserType.class));
        verify(provider, never()).getUserVo(anyString());
        verify(managerRepository, never()).findByIdAndEmail(anyLong(), anyString());
        verify(provider, never()).createAccessToken(anyString(), anyLong(), any());
        verify(provider, never()).createRefreshToken(anyString(), anyLong(), any());
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 일치하는 회원 없음")
    void testReissueFail_NotFoundUser() {
        //given
        given(provider.validateToken(anyString(), any(UserType.class))).willReturn(TokenType.REFRESH_TOKEN);
        given(provider.getUserVo(anyString())).willReturn(new UserVo(1L, "abc@example.com"));
        given(managerRepository.findByIdAndEmail(anyLong(), anyString())).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignInOutService.reissue("Refresh_Token"));

        //then
        verify(provider).validateToken(anyString(), any(UserType.class));
        verify(provider).getUserVo(anyString());
        verify(managerRepository).findByIdAndEmail(anyLong(), anyString());
        verify(provider, never()).createAccessToken(anyString(), anyLong(), any());
        verify(provider, never()).createRefreshToken(anyString(), anyLong(), any());
        assertEquals(ErrorCode.NOT_FOUND_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Redis에 Refresh Token 없음")
    void testReissueFail_NotExistRefreshToken() {
        try (MockedStatic<RedisUtil> redisUtilMockedStatic = mockStatic(RedisUtil.class)) {
            //given
            given(provider.validateToken(anyString(), any(UserType.class))).willReturn(TokenType.REFRESH_TOKEN);
            given(provider.getUserVo(anyString())).willReturn(new UserVo(1L, "abc@example.com"));
            given(managerRepository.findByIdAndEmail(anyLong(), anyString())).willReturn(
                Optional.of(Manager.builder()
                    .id(1L)
                    .email("abc@example.com")
                    .password("1")
                    .emailAuth(true)
                    .build()));
            given(RedisUtil.existData(anyString())).willReturn(false);

            //when
            CustomException exception = assertThrows(CustomException.class,
                () -> managerSignInOutService.reissue("Refresh_Token"));

            //then
            verify(provider).validateToken(anyString(), any(UserType.class));
            verify(provider).getUserVo(anyString());
            verify(managerRepository).findByIdAndEmail(anyLong(), anyString());
            verify(provider, never()).createAccessToken(anyString(), anyLong(), any());
            verify(provider, never()).createRefreshToken(anyString(), anyLong(), any());
            redisUtilMockedStatic.verify(() -> RedisUtil.existData(anyString()));
            redisUtilMockedStatic.verify(() -> RedisUtil.getData(anyString()), never());
            assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Redis의 Refresh Token과 불일치")
    void testReissueFail_MismatchRefreshToken() {
        try (MockedStatic<RedisUtil> redisUtilMockedStatic = mockStatic(RedisUtil.class)) {
            //given
            given(provider.validateToken(anyString(), any(UserType.class))).willReturn(TokenType.REFRESH_TOKEN);
            given(provider.getUserVo(anyString())).willReturn(new UserVo(1L, "abc@example.com"));
            given(managerRepository.findByIdAndEmail(anyLong(), anyString())).willReturn(
                Optional.of(Manager.builder()
                    .id(1L)
                    .email("abc@example.com")
                    .password("1")
                    .emailAuth(true)
                    .build()));
            given(RedisUtil.existData(anyString())).willReturn(true);
            given(RedisUtil.getData(anyString())).willReturn("Refresh_Token");

            //when
            CustomException exception = assertThrows(CustomException.class,
                () -> managerSignInOutService.reissue("Invalid_Refresh_Token"));

            //then
            verify(provider).validateToken(anyString(), any(UserType.class));
            verify(provider).getUserVo(anyString());
            verify(managerRepository).findByIdAndEmail(anyLong(), anyString());
            verify(provider, never()).createAccessToken(anyString(), anyLong(), any());
            verify(provider, never()).createRefreshToken(anyString(), anyLong(), any());
            redisUtilMockedStatic.verify(() -> RedisUtil.existData(anyString()));
            redisUtilMockedStatic.verify(() -> RedisUtil.getData(anyString()));
            assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("로그아웃 성공")
    void testSignOutSuccess() {
        try (MockedStatic<RedisUtil> redisUtilMockedStatic = mockStatic(RedisUtil.class)) {
            //given
            given(provider.validateToken(anyString(), any(UserType.class))).willReturn(TokenType.ACCESS_TOKEN);
            given(provider.getUserVo(anyString())).willReturn(new UserVo(1L, "abc@example.com"));
            given(managerRepository.findByIdAndEmail(anyLong(), anyString())).willReturn(
                Optional.of(Manager.builder()
                    .id(1L)
                    .email("abc@example.com")
                    .password("1")
                    .emailAuth(true)
                    .build()));

            //when
            managerSignInOutService.signOut("Access_Token");

            //then
            verify(provider).validateToken(anyString(), any(UserType.class));
            verify(provider).getUserVo(anyString());
            verify(managerRepository).findByIdAndEmail(anyLong(), anyString());
            redisUtilMockedStatic.verify(() -> RedisUtil.setBlacklist(anyString(), anyString(), anyLong()));
            redisUtilMockedStatic.verify(() -> RedisUtil.deleteData(anyString()));
        }
    }

    @Test
    @DisplayName("로그아웃 실패 - 유효하지 않은 Access Token")
    void testSignOutFail_InvalidAccessToken() {
        //given
        given(provider.validateToken(anyString(), any(UserType.class))).willReturn(TokenType.INVALID_TOKEN);

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignInOutService.signOut("Invalid_Token"));

        //then
        verify(provider).validateToken(anyString(), any(UserType.class));
        verify(provider, never()).getUserVo(anyString());
        verify(managerRepository, never()).findByIdAndEmail(anyLong(), anyString());
        assertEquals(ErrorCode.INVALID_ACCESS_TOKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그아웃 실패 - 일치하는 회원 없음")
    void testSignOutFail_NotFoundUser() {
        //given
        given(provider.validateToken(anyString(), any(UserType.class))).willReturn(TokenType.ACCESS_TOKEN);
        given(provider.getUserVo(anyString())).willReturn(new UserVo(1L, "abc@example.com"));
        given(managerRepository.findByIdAndEmail(anyLong(), anyString())).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignInOutService.signOut("Access_Token"));

        //then
        verify(provider).validateToken(anyString(), any(UserType.class));
        verify(provider).getUserVo(anyString());
        verify(managerRepository).findByIdAndEmail(anyLong(), anyString());
        assertEquals(ErrorCode.NOT_FOUND_USER, exception.getErrorCode());
    }
}