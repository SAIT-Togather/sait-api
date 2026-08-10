package com.sait.api.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationCode(
            String email,
            String code
    ) {

        /*
         * 이메일 메시지 생성
         */
        SimpleMailMessage message = new SimpleMailMessage();

        /*
         * 수신자
         */
        message.setTo(email);

        /*
         * 메일 제목
         */
        message.setSubject(
                "[SAIT] 이메일 인증번호 안내"
        );

        /*
         * 메일 내용
         */
        message.setText(
                """
                SAIT 이메일 인증번호입니다.

                인증번호: %s

                인증번호는 5분 동안 유효합니다.
                본인이 요청하지 않은 경우 이 메일을 무시해주세요.
                """.formatted(code)
        );

        /*
         * 실제 이메일 발송
         */
        mailSender.send(message);
    }
}