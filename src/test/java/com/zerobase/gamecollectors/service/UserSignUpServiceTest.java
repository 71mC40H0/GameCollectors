package com.zerobase.gamecollectors.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.zerobase.gamecollectors.client.mailgun.MailgunClient;
import com.zerobase.gamecollectors.domain.entity.Deposit;
import com.zerobase.gamecollectors.domain.entity.Point;
import com.zerobase.gamecollectors.domain.entity.User;
import com.zerobase.gamecollectors.domain.repository.DepositRepository;
import com.zerobase.gamecollectors.domain.repository.PointRepository;
import com.zerobase.gamecollectors.domain.repository.UserRepository;
import com.zerobase.gamecollectors.exception.CustomException;
import com.zerobase.gamecollectors.exception.ErrorCode;
import com.zerobase.gamecollectors.model.SendEmailServiceDto;
import com.zerobase.gamecollectors.model.UserSignUpServiceDto;
import com.zerobase.gamecollectors.redis.RedisUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserSignUpServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepositRepository depositRepository;

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private UserSignUpService userSignUpService;

    @Mock
    private MailgunClient mailgunClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        RedisUtil.setTemplate(stringRedisTemplate);
    }

    @Test
    @DisplayName("회원 가입 성공")
    void testSignUpSuccess() {
        //given
        UserSignUpServiceDto serviceDto = UserSignUpServiceDto.builder()
            .email("abc@test.com")
            .password("123")
            .nickname("Kim")
            .emailAuth(false)
            .build();

        try (MockedStatic<RedisUtil> redisUtilMockedStatic = mockStatic(RedisUtil.class)) {
            given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(
                    User.builder()
                        .id(1L)
                        .email("abc@test.com")
                        .password("123")
                        .nickname("Kim")
                        .emailAuth(false)
                        .build()));
            given(depositRepository.findByUserId(anyLong())).willReturn(Optional.empty());
            given(pointRepository.findByUserId(anyLong())).willReturn(Optional.empty());
            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<Deposit> depositArgumentCaptor = ArgumentCaptor.forClass(Deposit.class);
            ArgumentCaptor<Point> pointArgumentCaptor = ArgumentCaptor.forClass(Point.class);
            ArgumentCaptor<SendEmailServiceDto> sendEmailServiceDtoArgumentCaptor = ArgumentCaptor.forClass(
                SendEmailServiceDto.class);

            //when
            userSignUpService.signUp(serviceDto);

            //then
            verify(userRepository).save(userArgumentCaptor.capture());
            verify(mailgunClient).sendEmail(sendEmailServiceDtoArgumentCaptor.capture());
            verify(depositRepository).save(depositArgumentCaptor.capture());
            verify(pointRepository).save(pointArgumentCaptor.capture());
            assertEquals("abc@test.com", userArgumentCaptor.getValue().getEmail());
            assertEquals(passwordEncoder.encode("123"), userArgumentCaptor.getValue().getPassword());
            assertEquals("Kim", userArgumentCaptor.getValue().getNickname());
            redisUtilMockedStatic.verify(() -> RedisUtil.setDataExpireSec(anyString(), anyString(), anyLong()));
            assertFalse(userArgumentCaptor.getValue().isEmailAuth());
        }
    }

    @Test
    @DisplayName("회원 가입 실패 - 이미 등록된 사용자")
    void testSignUpFail_AlreadyRegisteredUser() {
        //given
        UserSignUpServiceDto serviceDto = UserSignUpServiceDto.builder()
            .email("abc@test.com")
            .password("123")
            .nickname("Kim")
            .emailAuth(false)
            .build();

        given(userRepository.findByEmail(anyString()))
            .willReturn(Optional.of(
                User.builder()
                    .id(1L)
                    .email("abc@test.com")
                    .password("123")
                    .nickname("Kim")
                    .emailAuth(false)
                    .build()));

        //when
        CustomException exception = assertThrows(CustomException.class, () -> userSignUpService.signUp(serviceDto));

        //then
        verify(userRepository, never()).save(any());
        verify(mailgunClient, never()).sendEmail(any());
        verify(depositRepository, never()).save(any());
        verify(pointRepository, never()).save(any());
        assertEquals(ErrorCode.ALREADY_REGISTERED_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 가입 실패 - 이미 존재하는 예치금 Entity")
    void testSignUpFail_InvalidDeposit() {
        //given
        UserSignUpServiceDto serviceDto = UserSignUpServiceDto.builder()
            .email("abc@example.com")
            .password("123")
            .nickname("Kim")
            .emailAuth(false)
            .build();

        given(userRepository.findByEmail(anyString()))
            .willReturn(Optional.empty())
            .willReturn(Optional.of(
                User.builder()
                    .id(1L)
                    .email("abc@test.com")
                    .password("123")
                    .nickname("Kim")
                    .emailAuth(false)
                    .build()));

        given(depositRepository.findByUserId(anyLong())).willReturn(Optional.of(
            Deposit.builder()
                .id(1L)
                .user(User.builder()
                    .id(1L)
                    .email("abcd@test.com")
                    .password("1234")
                    .nickname("Lee")
                    .emailAuth(true)
                    .build())
                .deposit(0)
                .build()));

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        //when
        CustomException exception = assertThrows(CustomException.class, () -> userSignUpService.signUp(serviceDto));

        //then
        verify(userRepository).save(userArgumentCaptor.capture());
        verify(mailgunClient, never()).sendEmail(any());
        verify(depositRepository, never()).save(any());
        verify(pointRepository, never()).save(any());
        assertEquals(ErrorCode.INVALID_DEPOSIT, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 가입 실패 - 이미 존재하는 적립금 Entity")
    void testSignUpFail_InvalidPoint() {
        //given
        UserSignUpServiceDto serviceDto = UserSignUpServiceDto.builder()
            .email("abc@test.com")
            .password("123")
            .nickname("Kim")
            .emailAuth(false)
            .build();

        given(userRepository.findByEmail(anyString()))
            .willReturn(Optional.empty())
            .willReturn(Optional.of(
                User.builder()
                    .id(1L)
                    .email("abc@test.com")
                    .password("123")
                    .nickname("Kim")
                    .emailAuth(false)
                    .build()));

        given(depositRepository.findByUserId(anyLong())).willReturn(Optional.empty());
        given(pointRepository.findByUserId(anyLong())).willReturn(Optional.of(
            Point.builder()
                .id(1L)
                .user(User.builder()
                    .id(1L)
                    .email("abcd@test.com")
                    .password("1234")
                    .nickname("Lee")
                    .emailAuth(true)
                    .build())
                .point(0)
                .build()));

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Deposit> depositArgumentCaptor = ArgumentCaptor.forClass(Deposit.class);

        //when
        CustomException exception = assertThrows(CustomException.class, () -> userSignUpService.signUp(serviceDto));

        //then
        verify(userRepository).save(userArgumentCaptor.capture());
        verify(mailgunClient, never()).sendEmail(any());
        verify(depositRepository).save(depositArgumentCaptor.capture());
        verify(pointRepository, never()).save(any());
        assertEquals(ErrorCode.INVALID_POINT, exception.getErrorCode());
    }

    @Test
    @DisplayName("이메일 인증 성공")
    void testVerifyEmailSuccess() {
        //given
        User user = User.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .nickname("Kim")
            .emailAuth(false)
            .build();

        try (MockedStatic<RedisUtil> redisUtilMockedStatic = mockStatic(RedisUtil.class)) {
            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
            given(RedisUtil.existData(anyString())).willReturn(true);
            given(RedisUtil.getData(anyString())).willReturn("1aA2bB3cC4");

            //when
            userSignUpService.verifyEmail("abc@test.com", "1aA2bB3cC4");

            //then
            verify(userRepository).findByEmail(anyString());
            redisUtilMockedStatic.verify(() -> RedisUtil.existData(anyString()));
            redisUtilMockedStatic.verify(() -> RedisUtil.getData(anyString()));
            assertTrue(user.isEmailAuth());
        }
    }

    @Test
    @DisplayName("이메일 인증 실패 - 존재하지 않는 사용자")
    void testVerifyEmailFail_NotFoundUser() {
        //given
        User user = User.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .nickname("Kim")
            .emailAuth(false)
            .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        //then
        CustomException exception = assertThrows(CustomException.class,
            () -> userSignUpService.verifyEmail("abc@test.com", "1aA2bB3cC4"));

        //then
        verify(userRepository).findByEmail(anyString());
        assertEquals(ErrorCode.NOT_FOUND_USER, exception.getErrorCode());
        assertFalse(user.isEmailAuth());
    }

    @Test
    @DisplayName("이메일 인증 실패 - 이미 완료된 인증")
    void testVerifyEmailFail_AlreadyVerified() {
        //given
        User user = User.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .nickname("Kim")
            .emailAuth(true)
            .build();

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));

        //then
        CustomException exception = assertThrows(CustomException.class,
            () -> userSignUpService.verifyEmail("abc@test.com", "1aA2bB3cC4"));

        //then
        verify(userRepository).findByEmail(anyString());
        assertEquals(ErrorCode.ALREADY_VERIFIED, exception.getErrorCode());
        assertTrue(user.isEmailAuth());
    }

    @Test
    @DisplayName("이메일 인증 실패 - 인증코드 재발급 필요")
    void testVerifyEmailFail_NeedNewVerificationCode() {
        //given
        User user = User.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .nickname("Kim")
            .emailAuth(false)
            .build();

        try (MockedStatic<RedisUtil> redisUtilMockedStatic = mockStatic(RedisUtil.class)) {
            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
            given(RedisUtil.existData(anyString())).willReturn(false);

            //then
            CustomException exception = assertThrows(CustomException.class,
                () -> userSignUpService.verifyEmail("abc@test.com", "1aA2bB3cC4"));

            //then
            verify(userRepository).findByEmail(anyString());
            assertEquals(ErrorCode.NEED_NEW_VERIFICATION_CODE, exception.getErrorCode());
            assertFalse(user.isEmailAuth());
            redisUtilMockedStatic.verify(() -> RedisUtil.existData(anyString()));
            redisUtilMockedStatic.verify(() -> RedisUtil.getData(anyString()), never());
        }
    }

    @Test
    @DisplayName("이메일 인증 실패 - 잘못된 인증")
    void testVerifyEmailFail_WrongVerification() {
        //given
        User user = User.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .nickname("Kim")
            .emailAuth(false)
            .build();

        try (MockedStatic<RedisUtil> redisUtilMockedStatic = mockStatic(RedisUtil.class)) {
            given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
            given(RedisUtil.existData(anyString())).willReturn(true);
            given(RedisUtil.getData(anyString())).willReturn("zxc123asdQ456");

            //then
            CustomException exception = assertThrows(CustomException.class,
                () -> userSignUpService.verifyEmail("abc@test.com", "1aA2bB3cC4"));

            //then
            verify(userRepository).findByEmail(anyString());
            assertEquals(ErrorCode.WRONG_VERIFICATION, exception.getErrorCode());
            assertFalse(user.isEmailAuth());
            redisUtilMockedStatic.verify(() -> RedisUtil.existData(anyString()));
            redisUtilMockedStatic.verify(() -> RedisUtil.getData(anyString()));
        }
    }

    @Test
    @DisplayName("인증 코드 재발급 성공")
    void testReissueVerificationCodeSuccess() {
        //given
        User user = User.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .nickname("Kim")
            .emailAuth(false)
            .build();

        try (MockedStatic<RedisUtil> redisUtilMockedStatic = mockStatic(RedisUtil.class)) {
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            ArgumentCaptor<SendEmailServiceDto> captor = ArgumentCaptor.forClass(SendEmailServiceDto.class);

            //when
            userSignUpService.reissueVerificationCode(anyLong());

            //then
            verify(userRepository).findById(anyLong());
            verify(mailgunClient).sendEmail(captor.capture());
            redisUtilMockedStatic.verify(() -> RedisUtil.setDataExpireSec(anyString(), anyString(), anyLong()));
        }
    }

    @Test
    @DisplayName("인증 코드 재발급 실패 - 존재하지 않는 사용자")
    void testReissueVerificationCodeFail_NotFoundUser() {
        //given
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> userSignUpService.reissueVerificationCode(1L));

        //then
        verify(userRepository).findById(anyLong());
        verify(mailgunClient, never()).sendEmail(any(SendEmailServiceDto.class));
        assertEquals(ErrorCode.NOT_FOUND_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("인증 코드 재발급 실패 - 이미 완료된 인증")
    void testReissueVerificationCodeFail_AlreadyVerified() {
        //given
        User user = User.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .nickname("Kim")
            .emailAuth(true)
            .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> userSignUpService.reissueVerificationCode(1L));

        //then
        verify(userRepository).findById(anyLong());
        verify(mailgunClient, never()).sendEmail(any(SendEmailServiceDto.class));
        assertEquals(ErrorCode.ALREADY_VERIFIED, exception.getErrorCode());
        assertTrue(user.isEmailAuth());
    }
}