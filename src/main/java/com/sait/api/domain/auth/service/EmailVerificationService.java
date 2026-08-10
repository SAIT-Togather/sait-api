package com.sait.api.domain.auth.service;

import com.sait.api.domain.auth.type.EmailVerificationPurpose;

public interface EmailVerificationService {

    /*
     * 인증번호 발송
     */
    void sendVerificationCode(
            String email,
            EmailVerificationPurpose purpose
    );

    /*
     * 인증번호 검증
     */
    boolean verifyCode(
            String email,
            String code,
            EmailVerificationPurpose purpose
    );

    /*
     * 이메일 인증 완료 여부
     */
    boolean isVerified(
            String email,
            EmailVerificationPurpose purpose
    );

    /*
     * 이메일 인증정보 삭제
     */
    void clearVerification(
            String email,
            EmailVerificationPurpose purpose
    );
}