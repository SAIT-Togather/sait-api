package com.sait.api.domain.auth.dto;

public record LoginResponse(
        Long userId,
        String email,
        String name,
        String role,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
}