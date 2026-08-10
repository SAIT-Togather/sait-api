package com.sait.api.domain.auth.dto.response;

public record LoginIdCheckResponse(
        boolean success,
        String code,
        String message,
        boolean available
) {
}