package com.zerobase.gamecollectors.util;


import java.util.Locale;
import org.apache.commons.lang3.RandomStringUtils;

public class RandomCodeGenerator {

    public static String generateRandomCode(int count, boolean letters, boolean numbers, boolean caseSensitive) {
        String code = RandomStringUtils.random(count, letters, numbers);
        return caseSensitive ? code : code.toUpperCase(Locale.ROOT);
    }
}
