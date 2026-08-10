package com.sait.api.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SignupRequest(

        @NotBlank
        String loginId,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password,

        @NotBlank
        String passwordConfirm,

        @NotBlank
        String nickname,

        LocalDate birthDate,

        @NotNull
        Boolean termsAgreed,

        @NotNull
        Boolean privacyAgreed,

        Boolean marketingAgreed

) {
}