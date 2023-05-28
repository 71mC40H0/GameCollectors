package com.zerobase.gamecollectors.redis;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class RedisUtil {

    private static StringRedisTemplate template;

    public static String getData(String key) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        return valueOperations.get(key);
    }

    public static boolean existData(String key) {
        return Boolean.TRUE.equals(template.hasKey(key));
    }

    public static void setDataExpireSec(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    public static void setDataExpireMilliSec(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        Duration expireDuration = Duration.ofMillis(duration);
        valueOperations.set(key, value, expireDuration);
    }

    public static void deleteData(String key) {
        template.delete(key);
    }

    public static void setBlacklist(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = template.opsForValue();
        Duration expireDuration = Duration.ofMillis(duration);
        valueOperations.set(key, value, expireDuration);
    }

    public static boolean isBlackList(String key) {
        return Boolean.TRUE.equals(template.hasKey(key));
    }

    public static void setTemplate(StringRedisTemplate template) {
        RedisUtil.template = template;
    }
}