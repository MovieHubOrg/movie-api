package com.movie.api.utils;

import org.hashids.Hashids;

public class HashService {
    static HashService instance;

    private Hashids hashids;

    private HashService() {
        hashids = new Hashids("MOVIE_SERVICE", 6);
    }

    public static final HashService getInstance() {
        if (instance == null) {
            instance = new HashService();
        }
        return instance;
    }


    public String generateHash(long number) {
        return hashids.encode(number);
    }
}
