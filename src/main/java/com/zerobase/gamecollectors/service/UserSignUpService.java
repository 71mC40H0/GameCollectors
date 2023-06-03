package com.zerobase.gamecollectors.service;

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
import com.zerobase.gamecollectors.util.RandomCodeGenerator;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSignUpService {

    private final UserRepository userRepository;
    private final DepositRepository depositRepository;
    private final PointRepository pointRepository;
    private final MailgunClient mailgunClient;
    private final PasswordEncoder passwordEncoder;

    @Value(value = "${mailgun.api.emailSender}")
    private String emailSender;

    @Value(value = "${server.host}")
    private String host;

    @Value(value = "${server.port}")
    private String port;

    private static final String EMAIL_VERIFICATION_CODE_PREFIX = "u_evcode:";

    @Transactional
    public void signUp(UserSignUpServiceDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_REGISTERED_USER);
        }

        dto.setPassword(this.passwordEncoder.encode(dto.getPassword()));
        userRepository.save(dto.toEntity());

        User user = userRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        createDeposit(user);
        createPoint(user);

        String code = getVerificationCode();
        setVerificationCode(dto.getEmail(), code);

        mailgunClient.sendEmail(SendEmailServiceDto.builder()
            .from(emailSender)
            .to(dto.getEmail())
            .subject("Verification Email!")
            .text(getEmailVerificationLink(dto.getEmail(), dto.getNickname(), code))
            .build());
    }

    @Transactional
    public void verifyEmail(String email, String code) {

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        String redisKey = EMAIL_VERIFICATION_CODE_PREFIX + email;

        if (user.isEmailAuth()) {
            throw new CustomException(ErrorCode.ALREADY_VERIFIED);
        } else if (!RedisUtil.existData(redisKey)) {
            throw new CustomException(ErrorCode.NEED_NEW_VERIFICATION_CODE);
        } else if (!code.equals(RedisUtil.getData(redisKey))) {
            throw new CustomException(ErrorCode.WRONG_VERIFICATION);
        }

        user.setEmailAuth(true);
        RedisUtil.deleteData(redisKey);
    }

    public void reissueVerificationCode(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (user.isEmailAuth()) {
            throw new CustomException(ErrorCode.ALREADY_VERIFIED);
        }

        String code = getVerificationCode();
        setVerificationCode(user.getEmail(), code);

        mailgunClient.sendEmail(SendEmailServiceDto.builder()
            .from(emailSender)
            .to(user.getEmail())
            .subject("Verification Email!")
            .text(getEmailVerificationLink(user.getEmail(), user.getNickname(), code))
            .build());
    }

    private String getVerificationCode() {
        return RandomCodeGenerator.generateRandomCode(10, true, true, true);
    }

    private void setVerificationCode(String email, String code) {
        RedisUtil.setDataExpireSec(EMAIL_VERIFICATION_CODE_PREFIX + email, code, 60 * 5L);
    }

    private String getEmailVerificationLink(String email, String nickname, String code) {
        StringBuilder builder = new StringBuilder();
        return builder.append("Hello ")
            .append(nickname)
            .append("! Please Click Link for verification\n\n")
            .append("http://")
            .append(host)
            .append(":")
            .append(port)
            .append("/sign-up/user/verify?email=")
            .append(email)
            .append("&code=")
            .append(code).toString();
    }

    @Transactional
    private void createDeposit(User user) {
        if (depositRepository.findByUserId(user.getId()).isPresent()) {
            log.warn("INVALID DEPOSIT -> user id : {}", user.getId());
            throw new CustomException(ErrorCode.INVALID_DEPOSIT);
        }

        Deposit deposit = Deposit.builder()
            .user(user)
            .deposit(0)
            .build();

        user.setDeposit(deposit);
        depositRepository.save(deposit);
    }

    @Transactional
    private void createPoint(User user) {
        if (pointRepository.findByUserId(user.getId()).isPresent()) {
            log.warn("INVALID POINT -> user id : {}", user.getId());
            throw new CustomException(ErrorCode.INVALID_POINT);
        }

        Point point = Point.builder()
            .user(user)
            .point(0)
            .build();

        user.setPoint(point);
        pointRepository.save(point);
    }
}
