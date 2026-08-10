package com.sait.api.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AppleLoginRequest(

        @NotBlank(message = "Apple identityToken은 필수입니다.")
        String identityToken,

        String authorizationCode,

        String nonce,

        String name

) {
}