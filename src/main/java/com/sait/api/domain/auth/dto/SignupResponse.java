package com.sait.api.domain.auth.dto;

public record SignupResponse(
        Long userId,
        String email,
        String nickname,
        String role
) {
}