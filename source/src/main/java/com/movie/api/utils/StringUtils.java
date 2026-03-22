package com.movie.api.utils;

import org.apache.commons.lang.RandomStringUtils;

import java.text.Normalizer;

public class StringUtils {
    public static String generateRandomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length).toLowerCase();
    }

    public static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return withoutDiacritics.trim()
                .toLowerCase()
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    public static boolean isNullOrEmpty(String string) {
        if (string == null) {
            return true;
        }
        if (string.isEmpty()) {
            return true;
        }
        return string.isBlank();
    }
}
