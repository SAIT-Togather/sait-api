package com.sait.api.domain.auth.service;

public interface EmailSender {

    void sendVerificationCode(
            String email,
            String code
    );
}