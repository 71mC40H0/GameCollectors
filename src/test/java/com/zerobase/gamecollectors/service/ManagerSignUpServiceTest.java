package com.zerobase.gamecollectors.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.zerobase.gamecollectors.client.mailgun.MailgunClient;
import com.zerobase.gamecollectors.domain.entity.Manager;
import com.zerobase.gamecollectors.domain.repository.ManagerRepository;
import com.zerobase.gamecollectors.exception.CustomException;
import com.zerobase.gamecollectors.exception.ErrorCode;
import com.zerobase.gamecollectors.model.ManagerSignUpServiceDto;
import com.zerobase.gamecollectors.model.SendEmailServiceDto;
import com.zerobase.gamecollectors.redis.RedisUtil;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ManagerSignUpServiceTest {

    @Mock
    private ManagerRepository managerRepository;

    @InjectMocks
    private ManagerSignUpService managerSignUpService;

    @Mock
    private MailgunClient mailgunClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisUtil redisUtil;


    @Test
    @DisplayName("회원 가입 성공")
    void testSignUpSuccess() {
        //given
        ManagerSignUpServiceDto serviceDto = ManagerSignUpServiceDto.builder()
            .email("abc@example.com")
            .password("123")
            .emailAuth(false)
            .build();

        given(managerRepository.findByEmail(serviceDto.getEmail())).willReturn(Optional.empty());
        ArgumentCaptor<Manager> managerArgumentCaptor = ArgumentCaptor.forClass(Manager.class);
        ArgumentCaptor<SendEmailServiceDto> sendEmailServiceDtoArgumentCaptor = ArgumentCaptor.forClass(
            SendEmailServiceDto.class);

        //when
        managerSignUpService.signUp(serviceDto);

        //then
        verify(managerRepository).save(managerArgumentCaptor.capture());
        verify(mailgunClient).sendEmail(sendEmailServiceDtoArgumentCaptor.capture());
        assertEquals("abc@example.com", managerArgumentCaptor.getValue().getEmail());
        assertEquals(passwordEncoder.encode("123"), managerArgumentCaptor.getValue().getPassword());
        assertFalse(managerArgumentCaptor.getValue().isEmailAuth());
    }

    @Test
    @DisplayName("회원 가입 실패 - 이미 등록된 사용자")
    void testSignUpFail_AlreadyRegisteredUser() {
        //given
        ManagerSignUpServiceDto serviceDto = ManagerSignUpServiceDto.builder()
            .email("abc@example.com")
            .password("123")
            .emailAuth(false)
            .build();

        given(managerRepository.findByEmail(serviceDto.getEmail())).willReturn(
            Optional.of(Manager.builder()
                .id(1L)
                .email("abc@example.com")
                .password("1234")
                .emailAuth(true)
                .build()));

        //when
        CustomException exception = assertThrows(CustomException.class, () -> managerSignUpService.signUp(serviceDto));

        //then
        verify(managerRepository, never()).save(any(Manager.class));
        verify(mailgunClient, never()).sendEmail(any(SendEmailServiceDto.class));
        assertEquals(ErrorCode.ALREADY_REGISTERED_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("이메일 인증 성공")
    void testVerifyEmailSuccess() {
        //given
        String email = "abc@test.com";
        String code = "1aA2bB3cC4";

        Manager manager = Manager.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .emailAuth(false)
            .build();

        given(managerRepository.findByEmail(email)).willReturn(Optional.of(manager));
        given(redisUtil.existData(email)).willReturn(true);
        given(redisUtil.getData(email)).willReturn(code);

        //when
        managerSignUpService.verifyEmail(email, code);

        //then
        verify(managerRepository).findByEmail(email);
        verify(redisUtil).existData(email);
        verify(redisUtil).getData(email);
        assertTrue(manager.isEmailAuth());
    }

    @Test
    @DisplayName("이메일 인증 실패 - 존재하지 않는 사용자")
    void testVerifyEmailFail_NotFoundUser() {
        //given
        String email = "abc@test.com";
        String code = "1aA2bB3cC4";

        Manager manager = Manager.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .emailAuth(false)
            .build();

        given(managerRepository.findByEmail(email)).willReturn(Optional.empty());

        //then
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignUpService.verifyEmail(email, code));

        //then
        verify(managerRepository).findByEmail(email);
        verify(redisUtil, never()).existData(email);
        verify(redisUtil, never()).getData(email);
        assertEquals(ErrorCode.NOT_FOUND_USER, exception.getErrorCode());
        assertFalse(manager.isEmailAuth());
    }

    @Test
    @DisplayName("이메일 인증 실패 - 이미 완료된 인증")
    void testVerifyEmailFail_AlreadyVerified() {
        //given
        String email = "abc@test.com";
        String code = "1aA2bB3cC4";

        Manager manager = Manager.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .emailAuth(true)
            .build();

        given(managerRepository.findByEmail(email)).willReturn(Optional.of(manager));

        //then
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignUpService.verifyEmail(email, code));

        //then
        verify(managerRepository).findByEmail(email);
        verify(redisUtil, never()).existData(email);
        verify(redisUtil, never()).getData(email);
        assertEquals(ErrorCode.ALREADY_VERIFIED, exception.getErrorCode());
        assertTrue(manager.isEmailAuth());
    }

    @Test
    @DisplayName("이메일 인증 실패 - 인증코드 재발급 필요")
    void testVerifyEmailFail_NeedNewVerificationCode() {
        //given
        String email = "abc@test.com";
        String code = "1aA2bB3cC4";

        Manager manager = Manager.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .emailAuth(false)
            .build();

        given(managerRepository.findByEmail(email)).willReturn(Optional.of(manager));
        given(redisUtil.existData(email)).willReturn(false);

        //then
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignUpService.verifyEmail(email, code));

        //then
        verify(managerRepository).findByEmail(email);
        verify(redisUtil).existData(email);
        verify(redisUtil, never()).getData(email);
        assertEquals(ErrorCode.NEED_NEW_VERIFICATION_CODE, exception.getErrorCode());
        assertFalse(manager.isEmailAuth());
    }

    @Test
    @DisplayName("이메일 인증 실패 - 잘못된 인증")
    void testVerifyEmailFail_WrongVerification() {
        //given
        String email = "abc@test.com";
        String code = "1aA2bB3cC4";

        Manager manager = Manager.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .emailAuth(false)
            .build();

        given(managerRepository.findByEmail(email)).willReturn(Optional.of(manager));
        given(redisUtil.existData(email)).willReturn(true);
        given(redisUtil.getData(email)).willReturn("zxc123asdQ456");

        //then
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignUpService.verifyEmail(email, code));

        //then
        verify(managerRepository).findByEmail(email);
        verify(redisUtil).existData(email);
        verify(redisUtil).getData(email);
        assertEquals(ErrorCode.WRONG_VERIFICATION, exception.getErrorCode());
        assertFalse(manager.isEmailAuth());
    }

    @Test
    @DisplayName("인증 코드 재발급 성공")
    void testReissueVerificationCodeSuccess() {
        //given
        Long id = 1L;
        Manager manager = Manager.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .emailAuth(false)
            .build();

        given(managerRepository.findById(id)).willReturn(Optional.of(manager));
        ArgumentCaptor<SendEmailServiceDto> captor = ArgumentCaptor.forClass(SendEmailServiceDto.class);

        //when
        managerSignUpService.reissueVerificationCode(id);

        //then
        verify(managerRepository).findById(id);
        verify(mailgunClient).sendEmail(captor.capture());
    }

    @Test
    @DisplayName("인증 코드 재발급 실패 - 존재하지 않는 사용자")
    void testReissueVerificationCodeFail_NotFoundUser() {
        //given
        Long id = 1L;

        given(managerRepository.findById(id)).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignUpService.reissueVerificationCode(id));

        //then
        verify(managerRepository).findById(id);
        verify(mailgunClient, never()).sendEmail(any(SendEmailServiceDto.class));
        assertEquals(ErrorCode.NOT_FOUND_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("인증 코드 재발급 실패 - 이미 완료된 인증")
    void testReissueVerificationCodeFail_AlreadyVerified() {
        //given
        Long id = 1L;
        Manager manager = Manager.builder()
            .id(1L)
            .email("abc@test.com")
            .password("123")
            .emailAuth(true)
            .build();

        given(managerRepository.findById(id)).willReturn(Optional.of(manager));

        //when
        CustomException exception = assertThrows(CustomException.class,
            () -> managerSignUpService.reissueVerificationCode(id));

        //then
        verify(managerRepository).findById(id);
        verify(mailgunClient, never()).sendEmail(any(SendEmailServiceDto.class));
        assertEquals(ErrorCode.ALREADY_VERIFIED, exception.getErrorCode());
        assertTrue(manager.isEmailAuth());
    }
}