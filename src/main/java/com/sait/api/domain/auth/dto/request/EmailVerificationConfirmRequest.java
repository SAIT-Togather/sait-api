package com.sait.api.domain.auth.dto.request;

import com.sait.api.domain.auth.type.EmailVerificationPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailVerificationConfirmRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "인증번호를 입력해주세요.")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "인증번호는 6자리 숫자입니다."
    )
    private String code;

    @NotNull(message = "이메일 인증 목적은 필수입니다.")
    private EmailVerificationPurpose purpose;
}