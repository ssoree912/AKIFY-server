package com.jammering.akkipy.config;

public final class PermitAllPaths {
    private PermitAllPaths() {} // static-only class

    public static final String[] PATHS = {
            "/",
            "/api/v1/auth/signin/**",
            "/api/v1/auth/email/**",
            "/api/v1/auth/reissue/**",
            "/swagger-ui/index.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "**/index.html"
    };
}