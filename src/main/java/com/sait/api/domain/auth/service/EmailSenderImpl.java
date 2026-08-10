package com.sait.api.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Override
    public void sendVerificationCode(
            String email,
            String code
    ) {

        try {
            /*
             * HTML 메일 메시지 생성
             */
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            /*
             * true = multipart 사용
             * UTF-8 = 한글 깨짐 방지
             */
            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mimeMessage,
                            true,
                            "UTF-8"
                    );

            /*
             * 수신자
             */
            helper.setTo(email);

            /*
             * 발신자
             * Gmail 계정과 동일하게 맞추는 것을 권장
             */
            helper.setFrom(mailUsername);

            /*
             * 메일 제목
             */
            helper.setSubject("[SAIT] 이메일 인증번호 안내");

            /*
             * HTML 본문
             */
            String html = buildVerificationEmailHtml(code);

            /*
             * true = HTML 메일
             */
            helper.setText(html, true);

            /*
             * 로고 이미지 inline 첨부
             * src/main/resources/mail/sait-logo.png 위치에 파일 필요
             */
            ClassPathResource logoResource = new ClassPathResource("mail/sait-logo.png");

            helper.addInline("saitLogo", logoResource);

            /*
             * 실제 메일 발송
             */
            mailSender.send(mimeMessage);

        } catch (Exception e) {

                e.printStackTrace();

                throw new IllegalStateException(
                        "이메일 발송에 실패했습니다.",
                        e
                );
                }
    }

        private String buildVerificationEmailHtml(String code) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>SAIT 이메일 인증</title>
                </head>

                <body style="
                        margin: 0;
                        padding: 0;
                        background-color: #f5f6f8;
                        font-family:
                                'Pretendard',
                                'Noto Sans KR',
                                'Apple SD Gothic Neo',
                                'Malgun Gothic',
                                Arial,
                                sans-serif;
                        color: #1f2937;
                ">

                        <table
                                width="100%%"
                                cellpadding="0"
                                cellspacing="0"
                                border="0"
                                style="
                                width: 100%%;
                                background-color: #f5f6f8;
                                padding: 48px 16px;
                                "
                        >
                        <tr>
                                <td align="center">

                                <table
                                        width="560"
                                        cellpadding="0"
                                        cellspacing="0"
                                        border="0"
                                        style="
                                                width: 100%%;
                                                max-width: 560px;
                                                background-color: #ffffff;
                                                border: 1px solid #e9ecf1;
                                                border-radius: 18px;
                                                overflow: hidden;
                                                box-shadow: 0 12px 30px rgba(28, 39, 60, 0.06);
                                        "
                                >

                                        <!-- 상단 브랜드 영역 -->
                                        <tr>
                                        <td style="
                                                padding: 34px 40px 0 40px;
                                        ">

                                                <img
                                                        src="cid:saitLogo"
                                                        alt="SAIT"
                                                        style="
                                                        display: block;
                                                        height: 34px;
                                                        width: auto;
                                                        border: 0;
                                                        "
                                                />

                                        </td>
                                        </tr>

                                        <!-- 제목 -->
                                        <tr>
                                        <td style="
                                                padding: 34px 40px 0 40px;
                                        ">

                                                <div style="
                                                        font-size: 25px;
                                                        line-height: 1.45;
                                                        font-weight: 700;
                                                        letter-spacing: -0.6px;
                                                        color: #171b24;
                                                ">
                                                이메일 인증번호 안내
                                                </div>

                                                <div style="
                                                        margin-top: 10px;
                                                        font-size: 15px;
                                                        line-height: 1.75;
                                                        letter-spacing: -0.2px;
                                                        color: #667085;
                                                ">
                                                SAIT 회원가입을 위해<br/>
                                                아래 인증번호를 입력해주세요.
                                                </div>

                                        </td>
                                        </tr>

                                        <!-- 인증번호 -->
                                        <tr>
                                        <td style="
                                                padding: 30px 40px 0 40px;
                                        ">

                                                <table
                                                        width="100%%"
                                                        cellpadding="0"
                                                        cellspacing="0"
                                                        border="0"
                                                        style="
                                                        width: 100%%;
                                                        background-color: #f7f9ff;
                                                        border: 1px solid #e1e7ff;
                                                        border-radius: 14px;
                                                        "
                                                >
                                                <tr>
                                                        <td
                                                                align="center"
                                                                style="
                                                                padding: 26px 20px 28px 20px;
                                                                "
                                                        >

                                                        <div style="
                                                                font-size: 12px;
                                                                font-weight: 600;
                                                                letter-spacing: 1.2px;
                                                                color: #8a94a6;
                                                        ">
                                                                VERIFICATION CODE
                                                        </div>

                                                        <div style="
                                                                margin-top: 12px;
                                                                font-family:
                                                                        Arial,
                                                                        'Pretendard',
                                                                        sans-serif;
                                                                font-size: 36px;
                                                                line-height: 1.2;
                                                                font-weight: 700;
                                                                letter-spacing: 10px;
                                                                color: #3454c4;
                                                        ">
                                                                %s
                                                        </div>

                                                        </td>
                                                </tr>
                                                </table>

                                        </td>
                                        </tr>

                                        <!-- 안내 -->
                                        <tr>
                                        <td style="
                                                padding: 24px 40px 0 40px;
                                        ">

                                                <table
                                                        width="100%%"
                                                        cellpadding="0"
                                                        cellspacing="0"
                                                        border="0"
                                                >
                                                <tr>
                                                        <td
                                                                valign="top"
                                                                style="
                                                                width: 8px;
                                                                padding-top: 7px;
                                                                "
                                                        >
                                                        <div style="
                                                                width: 6px;
                                                                height: 6px;
                                                                border-radius: 50%%;
                                                                background-color: #3454c4;
                                                        ">
                                                        </div>
                                                        </td>

                                                        <td style="
                                                                padding-left: 10px;
                                                                font-size: 14px;
                                                                line-height: 1.7;
                                                                color: #4b5565;
                                                        ">
                                                        인증번호는
                                                        <strong style="
                                                                color: #202532;
                                                                font-weight: 600;
                                                        ">
                                                                5분 동안 유효
                                                        </strong>
                                                        합니다.
                                                        </td>
                                                </tr>
                                                </table>

                                        </td>
                                        </tr>

                                        <!-- 보안 안내 -->
                                        <tr>
                                        <td style="
                                                padding: 14px 40px 36px 40px;
                                        ">

                                                <div style="
                                                        font-size: 13px;
                                                        line-height: 1.75;
                                                        color: #8a94a6;
                                                ">
                                                본인이 요청하지 않은 경우 이 메일을 무시해주세요.<br/>
                                                인증번호는 타인에게 공유하지 마세요.
                                                </div>

                                        </td>
                                        </tr>

                                        <!-- 구분선 -->
                                        <tr>
                                        <td style="
                                                padding: 0 40px;
                                        ">
                                                <div style="
                                                        height: 1px;
                                                        background-color: #eef1f5;
                                                ">
                                                </div>
                                        </td>
                                        </tr>

                                        <!-- 푸터 -->
                                        <tr>
                                        <td style="
                                                padding: 22px 40px 30px 40px;
                                        ">

                                                <div style="
                                                        font-size: 12px;
                                                        line-height: 1.7;
                                                        color: #a3abb7;
                                                ">
                                                본 메일은 발신 전용입니다.
                                                </div>

                                                <div style="
                                                        margin-top: 4px;
                                                        font-size: 12px;
                                                        line-height: 1.7;
                                                        color: #b3bac4;
                                                ">
                                                © SAIT. All rights reserved.
                                                </div>

                                        </td>
                                        </tr>

                                </table>

                                </td>
                        </tr>
                        </table>

                </body>
                </html>
                """.formatted(code);
        }


}