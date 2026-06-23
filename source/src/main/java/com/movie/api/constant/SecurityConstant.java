package com.movie.api.constant;

import java.util.List;

public class SecurityConstant {
    public static final List<String> ENDPOINTS_BYPASS_JWT = List.of(
            // LOGIN
            "/v1/employee/login",
            "/v1/user/login",
            "/v1/user/register",
            "/v1/user/verify-otp",
            "/v1/user/resend-otp",
            "/v1/user/request-forgot-password",
            "/v1/user/forgot-password",
            "/v1/user/auth/social-login",
            "/v1/user/auth/web-callback",
            "/v1/user/auth/mobile-callback",

            // Public GET APIs
            "/v1/category/get/**",
            "/v1/category/list",
            "/v1/movie/get/**",
            "/v1/movie/list",
            "/v1/movie/suggestion/**",
            "/v1/movie/top-views/**",
            "/v1/movie/next-episode/**",
            "/v1/movie/schedule",
            "/v1/movie-item/get/**",
            "/v1/movie-item/list",
            "/v1/movie-person/list",
            "/v1/person/get/**",
            "/v1/person/list",
            "/v1/person/auto-complete",
            "/v1/sidebar/get/**",
            "/v1/sidebar/list",
            "/v1/comment/list",
            "/v1/review/list",
            "/v1/review/get",
            "/v1/app-version/check-version/**",
            "/v1/app-version/latest",
            "/v1/collection/get/**",
            "/v1/collection/list",
            "/v1/collection/topics",
            "/v1/collection-item/list",

            // Internal APIs
            "/v1/server-config/internal/**",
            "/v1/room/internal/**",
            "/v1/setting/internal/**",
            "/v1/setting/public"
    );

    private SecurityConstant() {
        throw new IllegalStateException("Utility class");
    }
}
