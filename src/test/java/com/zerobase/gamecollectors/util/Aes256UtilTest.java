package com.zerobase.gamecollectors.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Aes256UtilTest {

    @Value(value = "${spring.aes256.secret}")
    private String key;

    @Test
    @DisplayName("Aes256 암호화 복호화")
    void encryptAndDecrypt() {
        //given
        String plainText = "Plain Text";

        //when
        String encrypt = Aes256Util.encrypt(plainText, key);

        //then
        assertEquals(Aes256Util.decrypt(encrypt, key), plainText);
    }
}