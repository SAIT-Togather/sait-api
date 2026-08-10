package com.sait.api.domain.auth.dto.response;

public record SignupResponse(
        Long userId,
        String email,
        String nickname,
        String role
) {
}