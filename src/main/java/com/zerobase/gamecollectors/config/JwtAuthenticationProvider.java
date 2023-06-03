package com.zerobase.gamecollectors.config;

import com.zerobase.gamecollectors.common.TokenType;
import com.zerobase.gamecollectors.common.UserType;
import com.zerobase.gamecollectors.common.UserVo;
import com.zerobase.gamecollectors.redis.RedisUtil;
import com.zerobase.gamecollectors.util.Aes256Util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;

public class JwtAuthenticationProvider {

    @Value("${spring.jwt.secret}")
    private String secretKey;
    public final static long ACCESS_TOKEN_VALIDATION_MILLISECOND = 1000L * 60 * 60; // Access Token 유효시간 1시간
    public final static long REFRESH_TOKEN_VALIDATION_MILLISECOND = 1000L * 60 * 60 * 24 * 14; // Refresh Token 유효기간 14일

    private final static String KEY_ROLES = "roles";
    private final static String TOKEN_TYPE = "token_type";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "rtk:";
    private static final String ACCESS_TOKEN_NAME = "access_token";
    private static final String REFRESH_TOKEN_NAME = "refresh_token";

    @Value(value = "${spring.aes256.secret}")
    private String key;

    public String createAccessToken(String userPk, Long id, UserType userType) {
        return createToken(userPk, id, userType, ACCESS_TOKEN_VALIDATION_MILLISECOND, ACCESS_TOKEN_NAME);
    }

    public String createRefreshToken(String userPk, Long id, UserType userType) {
        String prefixUserType = userType.equals(UserType.USER) ? "u_" : "m_";
        String rtk = createToken(userPk, id, userType, REFRESH_TOKEN_VALIDATION_MILLISECOND, REFRESH_TOKEN_NAME);
        RedisUtil.setDataExpireMilliSec(prefixUserType + REFRESH_TOKEN_PREFIX + userPk, rtk,
            REFRESH_TOKEN_VALIDATION_MILLISECOND);
        return rtk;
    }

    public String createToken(String userPk, Long id, UserType userType, long tokenValidTime, String tokenType) {
        Claims claims = Jwts.claims().setSubject(Aes256Util.encrypt(userPk, key))
            .setId(Aes256Util.encrypt(id.toString(), key));
        claims.put(KEY_ROLES, userType);
        claims.put(TOKEN_TYPE, tokenType);
        Date now = new Date();
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + tokenValidTime))
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }

    public TokenType validateToken(String token, UserType userType) {
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            if (!claimsJws.getBody().get(KEY_ROLES, String.class).equals(userType.toString())) {
                return TokenType.INVALID_TOKEN;
            }

            String prefixUserType = userType.equals(UserType.USER) ? "u_" : "m_";

            if (claimsJws.getBody().getExpiration().before(new Date())) {
                return TokenType.INVALID_TOKEN;
            }

            String tokenType = claimsJws.getBody().get(TOKEN_TYPE, String.class);

            if (tokenType.equals(ACCESS_TOKEN_NAME)) {
                if (RedisUtil.isBlackList(prefixUserType + BLACKLIST_PREFIX + token)) {
                    return TokenType.INVALID_TOKEN;
                }
                return TokenType.ACCESS_TOKEN;
            } else if (tokenType.equals(REFRESH_TOKEN_NAME)) {
                return TokenType.REFRESH_TOKEN;
            }

            return TokenType.INVALID_TOKEN;

        } catch (Exception e) {
            return TokenType.INVALID_TOKEN;
        }
    }

    public long getExpiration(String token) {
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
        return claimsJws.getBody().getExpiration().getTime();
    }


    public UserVo getUserVo(String token) {
        Claims c = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        return new UserVo(Long.valueOf(Objects.requireNonNull(Aes256Util.decrypt(c.getId(), key))),
            Aes256Util.decrypt(c.getSubject(), key));
    }
}
