package com.sait.api.domain.auth.service;

import com.sait.api.domain.auth.type.EmailVerificationPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    /*
     * =========================================================
     * 인증 관련 설정
     * =========================================================
     */

    /*
     * 인증번호 유효시간
     *
     * 인증번호 발송 후
     * 5분 이내 입력
     */
    private static final Duration CODE_EXPIRATION = Duration.ofMinutes(5);


    /*
     * 회원가입 이메일 인증 완료 유효시간
     *
     * 이메일 인증 성공 후
     * 1시간 이내 회원가입 가능
     */
    private static final Duration SIGNUP_VERIFIED_EXPIRATION = Duration.ofHours(1);


    /*
     * 비밀번호 재설정 이메일 인증 완료 유효시간
     *
     * 이메일 인증 성공 후
     * 15분 이내 비밀번호 변경 가능
     */
    private static final Duration RESET_PASSWORD_VERIFIED_EXPIRATION = Duration.ofMinutes(15);


    /*
     * 인증번호 재발송 제한시간
     *
     * 동일 이메일 + 동일 인증목적 기준
     * 60초에 한 번만 발송 가능
     */
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);


    /*
     * 인증번호 최대 입력 실패 횟수
     */
    private static final int MAX_VERIFY_ATTEMPTS = 5;


    /*
     * =========================================================
     * Redis Key Prefix
     * =========================================================
     */

    /*
     * 인증번호
     */
    private static final String CODE_KEY_PREFIX = "email-verification:code:";


    /*
     * 인증 완료 상태
     */
    private static final String VERIFIED_KEY_PREFIX = "email-verification:verified:";


    /*
     * 인증번호 재발송 제한
     */
    private static final String RESEND_KEY_PREFIX = "email-verification:resend:";


    /*
     * 인증번호 입력 실패 횟수
     */
    private static final String ATTEMPT_KEY_PREFIX = "email-verification:attempt:";


    /*
     * =========================================================
     * Dependency
     * =========================================================
     */

    private final StringRedisTemplate redisTemplate;

    /*
     * 실제 이메일 발송 담당
     */
    private final EmailSender emailSender;


    /*
     * 인증번호 생성용
     */
    private final SecureRandom secureRandom = new SecureRandom();


    /*
     * =========================================================
     * 인증번호 발송
     * =========================================================
     */
    @Override
    public void sendVerificationCode(String email,EmailVerificationPurpose purpose) {

        /*
         * 이메일 정규화
         *
         * TEST@GMAIL.COM
         * test@gmail.com
         *
         * 위 두 이메일을 동일한 이메일로 처리
         */
        String normalizedEmail = normalizeEmail(email);


        /*
         * Redis Key 생성
         */
        String codeKey = createCodeKey(normalizedEmail,purpose);
        String verifiedKey = createVerifiedKey(normalizedEmail,purpose);
        String resendKey = createResendKey(normalizedEmail,purpose);
        String attemptKey = createAttemptKey(normalizedEmail,purpose);


        /*
         * =====================================================
         * 재발송 제한
         * =====================================================
         *
         * Redis SET NX 방식
         *
         * resendKey가 존재하지 않을 때만
         * 새 값을 저장
         *
         * 최초 요청
         *      → true
         *
         * 60초 이내 재요청
         *      → false
         */
        Boolean resendAllowed = redisTemplate.opsForValue().setIfAbsent(resendKey,"1",RESEND_COOLDOWN);


        /*
         * 60초 이내 재발송 요청
         */
        if (!Boolean.TRUE.equals(resendAllowed)) {

            throw new IllegalStateException(
                    "인증번호는 60초 후 다시 요청할 수 있습니다."
            );
        }


        /*
         * =====================================================
         * 인증번호 생성
         * =====================================================
         */
        String code = generateCode();


        try {

            /*
             * =================================================
             * 기존 인증 완료 상태 삭제
             * =================================================
             *
             * 인증번호를 다시 요청했다면
             * 이전에 인증 완료된 상태는 무효화
             */
            redisTemplate.delete(verifiedKey);


            /*
             * =================================================
             * 기존 실패 횟수 초기화
             * =================================================
             */
            redisTemplate.delete(attemptKey);


            /*
             * =================================================
             * 인증번호 Redis 저장
             * =================================================
             *
             * TTL: 5분
             */
            redisTemplate.opsForValue().set(codeKey,code,CODE_EXPIRATION);

            /*
             * =================================================
             * 실제 이메일 발송
             * =================================================
             */
            emailSender.sendVerificationCode(normalizedEmail,code);


        } catch (Exception e) {

            /*
             * =================================================
             * 이메일 발송 실패 처리
             * =================================================
             *
             * 실제 이메일 발송이 실패했는데
             * Redis에 인증번호가 남아있으면
             * 사용자는 인증번호를 받을 방법이 없으므로
             * 관련 Redis 데이터를 제거
             */


            /*
             * 인증번호 삭제
             */
            redisTemplate.delete(
                    codeKey
            );


            /*
             * 실패 횟수 삭제
             */
            redisTemplate.delete(
                    attemptKey
            );


            /*
             * 재발송 제한 삭제
             *
             * 이메일 발송 자체가 실패했으므로
             * 사용자가 바로 다시 요청할 수 있도록 처리
             */
            redisTemplate.delete(
                    resendKey
            );


            throw new IllegalStateException(
                    "인증메일 발송에 실패했습니다.",
                    e
            );
        }
    }


    /*
     * =========================================================
     * 인증번호 검증
     * =========================================================
     */
    @Override
    public boolean verifyCode(String email,String code,EmailVerificationPurpose purpose) {

        /*
         * 이메일 정규화
         */
        String normalizedEmail = normalizeEmail(email);


        /*
         * Redis Key 생성
         */
        String codeKey = createCodeKey(normalizedEmail,purpose);
        String verifiedKey = createVerifiedKey(normalizedEmail,purpose);
        String attemptKey = createAttemptKey(normalizedEmail,purpose);


        /*
         * =====================================================
         * 저장된 인증번호 조회
         * =====================================================
         */
        String savedCode =
                redisTemplate
                        .opsForValue()
                        .get(codeKey);


        /*
         * =====================================================
         * 인증번호가 없거나 만료된 경우
         * =====================================================
         *
         * TTL 5분이 지나면 Redis에서
         * 인증번호가 자동으로 삭제됨
         */
        if (savedCode == null) {

            /*
             * 인증번호가 만료되었으므로
             * 실패 횟수 정보도 정리
             */
            redisTemplate.delete(
                    attemptKey
            );

            return false;
        }


        /*
         * =====================================================
         * 인증번호 불일치
         * =====================================================
         */
        if (!savedCode.equals(code)) {

            /*
             * 실패 횟수 증가
             *
             * Redis 값
             *
             * 1
             * 2
             * 3
             * ...
             */
            Long attempts =
                    redisTemplate
                            .opsForValue()
                            .increment(attemptKey);


            /*
             * 최초 실패 시
             * 실패 횟수 Key에도 TTL 설정
             *
             * 인증번호와 동일하게 5분
             */
            if (attempts != null
                    && attempts == 1L) {

                redisTemplate.expire(
                        attemptKey,
                        CODE_EXPIRATION
                );
            }


            /*
             * =================================================
             * 최대 실패 횟수 초과
             * =================================================
             *
             * 5회 실패 시
             * 기존 인증번호 자체를 폐기
             *
             * 다시 인증하려면
             * 인증번호를 새로 발급받아야 함
             */
            if (attempts != null
                    && attempts >= MAX_VERIFY_ATTEMPTS) {

                /*
                 * 인증번호 삭제
                 */
                redisTemplate.delete(
                        codeKey
                );


                /*
                 * 실패 횟수 삭제
                 */
                redisTemplate.delete(
                        attemptKey
                );
            }


            return false;
        }


        /*
         * =====================================================
         * 인증 성공
         * =====================================================
         */


        /*
         * 인증 목적에 따른
         * 인증 완료 상태 유효시간 조회
         *
         * SIGNUP
         *      → 1시간
         *
         * RESET_PASSWORD
         *      → 15분
         */
        Duration verifiedExpiration =
                getVerifiedExpiration(
                        purpose
                );


        /*
         * =====================================================
         * 이메일 인증 완료 상태 저장
         * =====================================================
         */
        redisTemplate
                .opsForValue()
                .set(
                        verifiedKey,
                        "true",
                        verifiedExpiration
                );


        /*
         * =====================================================
         * 사용 완료한 인증번호 삭제
         * =====================================================
         *
         * 동일한 인증번호를 다시 사용할 수 없도록
         * 인증 성공 즉시 삭제
         */
        redisTemplate.delete(
                codeKey
        );


        /*
         * 실패 횟수 삭제
         */
        redisTemplate.delete(
                attemptKey
        );


        return true;
    }


    /*
     * =========================================================
     * 이메일 인증 완료 여부 확인
     * =========================================================
     */
    @Override
    public boolean isVerified(
            String email,
            EmailVerificationPurpose purpose
    ) {

        /*
         * 이메일 정규화
         */
        String normalizedEmail =
                normalizeEmail(email);


        /*
         * 인증 완료 Redis Key 생성
         */
        String verifiedKey =
                createVerifiedKey(
                        normalizedEmail,
                        purpose
                );


        /*
         * Redis 인증 완료 상태 조회
         */
        String verified =
                redisTemplate
                        .opsForValue()
                        .get(verifiedKey);


        /*
         * Redis 값이 실제로
         * "true"인 경우에만 인증 완료 처리
         */
        return "true".equals(verified);
    }


    /*
     * =========================================================
     * 이메일 인증정보 삭제
     * =========================================================
     *
     * 회원가입 완료 또는
     * 비밀번호 변경 완료 후 호출
     */
    @Override
    public void clearVerification(
            String email,
            EmailVerificationPurpose purpose
    ) {

        /*
         * 이메일 정규화
         */
        String normalizedEmail =
                normalizeEmail(email);


        /*
         * =====================================================
         * 인증번호 삭제
         * =====================================================
         */
        redisTemplate.delete(
                createCodeKey(
                        normalizedEmail,
                        purpose
                )
        );


        /*
         * =====================================================
         * 인증 완료 상태 삭제
         * =====================================================
         */
        redisTemplate.delete(
                createVerifiedKey(
                        normalizedEmail,
                        purpose
                )
        );


        /*
         * =====================================================
         * 실패 횟수 삭제
         * =====================================================
         */
        redisTemplate.delete(
                createAttemptKey(
                        normalizedEmail,
                        purpose
                )
        );


        /*
         * =====================================================
         * 재발송 제한 Key는 삭제하지 않음
         * =====================================================
         *
         * 회원가입 완료 또는
         * 비밀번호 변경 완료 직후
         *
         * 이메일 발송 API를 반복 호출하는 것을 방지하기 위해
         * 기존 60초 TTL이 만료될 때까지 유지
         */
    }


    /*
     * =========================================================
     * 인증 목적에 따른 인증 완료 유효시간
     * =========================================================
     */
    private Duration getVerifiedExpiration(
            EmailVerificationPurpose purpose
    ) {

        return switch (purpose) {

            /*
             * 회원가입
             *
             * 인증 완료 후 1시간
             */
            case SIGNUP ->
                    SIGNUP_VERIFIED_EXPIRATION;


            /*
             * 비밀번호 재설정
             *
             * 인증 완료 후 15분
             */
            case RESET_PASSWORD ->
                    RESET_PASSWORD_VERIFIED_EXPIRATION;
        };
    }


    /*
     * =========================================================
     * Redis 인증번호 Key 생성
     * =========================================================
     */
    private String createCodeKey(
            String email,
            EmailVerificationPurpose purpose
    ) {

        return CODE_KEY_PREFIX
                + purpose
                        .name()
                        .toLowerCase(Locale.ROOT)
                + ":"
                + email;
    }


    /*
     * =========================================================
     * Redis 인증 완료 Key 생성
     * =========================================================
     */
    private String createVerifiedKey(
            String email,
            EmailVerificationPurpose purpose
    ) {

        return VERIFIED_KEY_PREFIX
                + purpose
                        .name()
                        .toLowerCase(Locale.ROOT)
                + ":"
                + email;
    }


    /*
     * =========================================================
     * Redis 재발송 제한 Key 생성
     * =========================================================
     */
    private String createResendKey(
            String email,
            EmailVerificationPurpose purpose
    ) {

        return RESEND_KEY_PREFIX
                + purpose
                        .name()
                        .toLowerCase(Locale.ROOT)
                + ":"
                + email;
    }


    /*
     * =========================================================
     * Redis 인증 실패 횟수 Key 생성
     * =========================================================
     */
    private String createAttemptKey(
            String email,
            EmailVerificationPurpose purpose
    ) {

        return ATTEMPT_KEY_PREFIX
                + purpose
                        .name()
                        .toLowerCase(Locale.ROOT)
                + ":"
                + email;
    }


    /*
     * =========================================================
     * 이메일 정규화
     * =========================================================
     */
    private String normalizeEmail(
            String email
    ) {

        /*
         * null 체크
         */
        if (email == null) {

            throw new IllegalArgumentException(
                    "이메일은 필수입니다."
            );
        }


        /*
         * 앞뒤 공백 제거
         * 소문자로 변환
         */
        String normalizedEmail =
                email
                        .trim()
                        .toLowerCase(Locale.ROOT);


        /*
         * 빈 문자열 체크
         */
        if (normalizedEmail.isBlank()) {

            throw new IllegalArgumentException(
                    "이메일은 필수입니다."
            );
        }


        return normalizedEmail;
    }


    /*
     * =========================================================
     * 6자리 인증번호 생성
     * =========================================================
     */
    private String generateCode() {

        /*
         * 100000 ~ 999999
         */
        int number =
                secureRandom.nextInt(900000)
                        + 100000;


        return String.valueOf(number);
    }
}