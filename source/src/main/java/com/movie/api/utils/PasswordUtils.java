package com.movie.api.utils;

import lombok.extern.log4j.Log4j2;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class PasswordUtils {
    private static final int LENGTH = 12;
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;':\",.<>?";

    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

    public static String generateSecureRandomPassword() {
        SecureRandom random = new SecureRandom();

        StringBuilder password = new StringBuilder();
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        for (int i = 4; i < LENGTH; i++) {
            password.append(ALL.charAt(random.nextInt(ALL.length())));
        }

        List<Character> chars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(chars, random);

        return chars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
}
