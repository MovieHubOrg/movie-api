package com.movie.api.utils;

public class Utils {
    public static int convertToCent(double b) {
        int i = (int) (b);
        double k = b - (double) i;
        if (k > 0.5 && k < 1) {
            i += 1;
        }
        return i;
    }
}
