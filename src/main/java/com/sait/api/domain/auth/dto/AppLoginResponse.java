package com.sait.api.domain.auth.dto;

import com.sait.api.domain.auth.AuthProvider;

public record AppLoginResponse(
        Long userId,
        AuthProvider provider,
        String name,
        String role,
        boolean newMember,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
}

