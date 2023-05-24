package com.zerobase.gamecollectors.service;

import com.zerobase.gamecollectors.client.mailgun.MailgunClient;
import com.zerobase.gamecollectors.domain.entity.Manager;
import com.zerobase.gamecollectors.domain.repository.ManagerRepository;
import com.zerobase.gamecollectors.exception.CustomException;
import com.zerobase.gamecollectors.exception.ErrorCode;
import com.zerobase.gamecollectors.model.ManagerSignUpServiceDto;
import com.zerobase.gamecollectors.model.SendEmailServiceDto;
import com.zerobase.gamecollectors.redis.RedisUtil;
import com.zerobase.gamecollectors.util.RandomCodeGenerator;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerSignUpService {

    private final ManagerRepository managerRepository;
    private final MailgunClient mailgunClient;
    private final PasswordEncoder passwordEncoder;

    @Value(value = "${mailgun.api.emailSender}")
    private String emailSender;

    @Value(value = "${gamecollectors.host}")
    private String host;

    @Value(value = "${server.port}")
    private String port;

    @Transactional
    public void signUp(ManagerSignUpServiceDto dto) {
        if (managerRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_USER);
        }

        dto.setPassword(this.passwordEncoder.encode(dto.getPassword()));
        managerRepository.save(dto.toEntity());

        String code = getVerificationCode(dto.getEmail());
        setVerificationCode(dto.getEmail(), code);

        mailgunClient.sendEmail(SendEmailServiceDto.builder()
            .from(emailSender)
            .to(dto.getEmail())
            .subject("Verification Email!")
            .text(getEmailVerificationLink(dto.getEmail(), code))
            .build());
    }

    @Transactional
    public void verifyEmail(String email, String code) {

        Manager manager = managerRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (manager.isEmailAuth()) {
            throw new CustomException(ErrorCode.ALREADY_VERIFIED);
        } else if (!RedisUtil.existData(email)) {
            throw new CustomException(ErrorCode.NEED_NEW_VERIFICATION_CODE);
        } else if (!code.equals(RedisUtil.getData(email))) {
            throw new CustomException(ErrorCode.WRONG_VERIFICATION);
        }

        manager.setEmailAuth(true);
        RedisUtil.deleteData(manager.getEmail());
    }

    public void reissueVerificationCode(Long id) {
        Manager manager = managerRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (manager.isEmailAuth()) {
            throw new CustomException(ErrorCode.ALREADY_VERIFIED);
        }

        String code = getVerificationCode(manager.getEmail());
        setVerificationCode(manager.getEmail(), code);

        mailgunClient.sendEmail(SendEmailServiceDto.builder()
            .from(emailSender)
            .to(manager.getEmail())
            .subject("Verification Email!")
            .text(getEmailVerificationLink(manager.getEmail(), code))
            .build());
    }

    private String getVerificationCode(String email) {
        return RandomCodeGenerator.generateRandomCode(10, true, true, true);
    }

    private void setVerificationCode(String email, String code) {
        RedisUtil.setDataExpireSec(email, code, 60 * 5L);
    }

    private String getEmailVerificationLink(String email, String code) {
        StringBuilder builder = new StringBuilder();
        return builder.append("Hello Manager! Please Click Link for verification\n\n")
            .append("http://")
            .append(host)
            .append(":")
            .append(port)
            .append("/sign-up/manager/verify?email=")
            .append(email)
            .append("&code=")
            .append(code).toString();
    }
}