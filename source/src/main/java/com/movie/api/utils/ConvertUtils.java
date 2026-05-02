package com.movie.api.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConvertUtils {
    public static Long convertStringToLong(String input) {
        try {
            return Long.valueOf(input.trim());
        } catch (RuntimeException e) {
            log.warn("Failed to convert string to long: {}", input, e);
            return null;
        }
    }

    public static int convertToCent(double b) {
        int i = (int) (b);
        double k = b - (double) i;
        if (k > 0.5 && k < 1) {
            i += 1;
        }
        return i;
    }
}
