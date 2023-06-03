package com.zerobase.gamecollectors.service;

import com.zerobase.gamecollectors.common.TokenType;
import com.zerobase.gamecollectors.common.UserType;
import com.zerobase.gamecollectors.common.UserVo;
import com.zerobase.gamecollectors.config.JwtAuthenticationProvider;
import com.zerobase.gamecollectors.domain.entity.Manager;
import com.zerobase.gamecollectors.domain.repository.ManagerRepository;
import com.zerobase.gamecollectors.exception.CustomException;
import com.zerobase.gamecollectors.exception.ErrorCode;
import com.zerobase.gamecollectors.model.SignInServiceDto;
import com.zerobase.gamecollectors.model.TokenDto;
import com.zerobase.gamecollectors.redis.RedisUtil;
import java.util.Date;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerSignInOutService {

    private final ManagerRepository managerRepository;
    private final JwtAuthenticationProvider provider;
    private final PasswordEncoder passwordEncoder;

    private static final long REFRESH_EXPIRE_TIME = 1000L * 60 * 60 * 3; // 만료시간까지 3시간 미만인 경우 REFRESH TOKEN 갱신
    private static final String REFRESH_TOKEN_PREFIX = "m_rtk:";
    private static final String BLACKLIST_PREFIX = "m_blacklist:";

    @Transactional
    public TokenDto signIn(SignInServiceDto serviceDto) {
        Manager m = managerRepository.findByEmail(serviceDto.getEmail())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (!passwordEncoder.matches(serviceDto.getPassword(), m.getPassword())) {
            throw new CustomException(ErrorCode.MISMATCHED_PASSWORD);
        }

        return TokenDto.builder()
            .accessToken(provider.createAccessToken(m.getEmail(), m.getId(), UserType.MANAGER))
            .refreshToken(provider.createRefreshToken(m.getEmail(), m.getId(), UserType.MANAGER))
            .build();
    }

    @Transactional
    public TokenDto reissue(String refreshToken) {
        if (!provider.validateToken(refreshToken, UserType.MANAGER).equals(TokenType.REFRESH_TOKEN)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        UserVo userVo = provider.getUserVo(refreshToken);
        Manager m = managerRepository.findByIdAndEmail(userVo.getId(), userVo.getEmail())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        String redisKey = REFRESH_TOKEN_PREFIX + userVo.getEmail();

        if (!RedisUtil.existData(redisKey) || !refreshToken.equals(RedisUtil.getData(redisKey))) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Date now = new Date();

        // refresh token 만료시간까지 3시간 미만 남은 경우 refresh token 재발급
        return TokenDto.builder()
            .accessToken(provider.createAccessToken(m.getEmail(), m.getId(), UserType.MANAGER))
            .refreshToken(provider.getExpiration(refreshToken) - now.getTime() < REFRESH_EXPIRE_TIME
                ? provider.createRefreshToken(m.getEmail(), m.getId(), UserType.MANAGER) : refreshToken)
            .build();
    }

    @Transactional
    public void signOut(String accessToken) {
        if (!provider.validateToken(accessToken, UserType.MANAGER).equals(TokenType.ACCESS_TOKEN)) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        UserVo userVo = provider.getUserVo(accessToken);
        Manager m = managerRepository.findByIdAndEmail(userVo.getId(), userVo.getEmail())
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        Date now = new Date();
        RedisUtil.setBlacklist(BLACKLIST_PREFIX + accessToken, "blacklist access token",
            provider.getExpiration(accessToken) - now.getTime());
        RedisUtil.deleteData(REFRESH_TOKEN_PREFIX + m.getEmail());
        log.info("Sign Out Manager -> id: {}", m.getId());
    }
}
