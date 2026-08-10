package com.sait.api.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String loginId,

        @NotBlank
        String password

) {
}