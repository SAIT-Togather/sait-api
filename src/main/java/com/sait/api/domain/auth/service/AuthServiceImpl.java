package com.sait.api.domain.auth.service;

import com.sait.api.domain.admin.entity.AdminAccount;
import com.sait.api.domain.admin.repository.AdminAccountRepository;
import com.sait.api.domain.auth.AuthProvider;
import com.sait.api.domain.auth.dto.request.AppleLoginRequest;
import com.sait.api.domain.auth.dto.request.LoginRequest;
import com.sait.api.domain.auth.dto.request.SignupRequest;
import com.sait.api.domain.auth.dto.response.AppLoginResponse;
import com.sait.api.domain.auth.dto.response.LoginResponse;
import com.sait.api.domain.auth.dto.response.SignupResponse;
import com.sait.api.domain.auth.type.EmailVerificationPurpose;
import com.sait.api.domain.member.entity.Member;
import com.sait.api.domain.member.repository.MemberRepository;
import com.sait.api.global.exception.BusinessException;
import com.sait.api.global.exception.ErrorCode;
import com.sait.api.infra.jwt.JwtProperties;
import com.sait.api.infra.jwt.JwtTokenProvider;
import com.sait.api.infra.oauth.apple.AppleIdentityTokenVerifier;
import com.sait.api.infra.oauth.apple.AppleTokenPayload;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final MemberRepository memberRepository;
    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AppleIdentityTokenVerifier appleIdentityTokenVerifier;
    private final EmailVerificationService emailVerificationService;

    /*
     * =========================================================
     * 일반 회원가입
     * =========================================================
     */
    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {

        System.out.println("request" + request);

        /*
         * 1. 로그인 아이디 중복 확인
         */
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_MEMBER,
                    "이미 사용 중인 아이디입니다."
            );
        }

        /*
         * 2. 비밀번호 / 비밀번호 확인 비교
         */
        if (!request.password().equals(request.passwordConfirm())) {
            throw new BusinessException(
                    ErrorCode.INVALID_PASSWORD,
                    "비밀번호가 일치하지 않습니다."
            );
        }

        /*
         * 3. 필수 약관 동의 확인
         */
        if (!request.termsAgreed() || !request.privacyAgreed()) {
            throw new BusinessException(
                    ErrorCode.REQUIRED_TERMS_NOT_AGREED,
                    "필수 약관에 동의해야 합니다."
            );
        }

        /*
        * 4. 이메일 인증 여부 확인
        */
        if (!emailVerificationService.isVerified(
                request.email(),
                EmailVerificationPurpose.SIGNUP
        )) {
        throw new BusinessException(
                ErrorCode.EMAIL_NOT_VERIFIED,
                "이메일 인증이 필요합니다."
        );
        }

        /*
         * 5. 회원 생성
         */
        Member member = new Member(
                request.loginId(),
                passwordEncoder.encode(request.password()),
                request.nickname(),
                request.email(),
                request.birthDate(),
                ACTIVE_STATUS,
                "USER"
        );

        Member savedMember = memberRepository.save(member);

        /*
         * 6. 회원가입 결과 반환
         */
        return new SignupResponse(
                savedMember.getId(),
                savedMember.getLoginId(),
                savedMember.getNickname(),
                savedMember.getRole()
        );
    }

    /*
     * =========================================================
     * 일반 앱 로그인
     * =========================================================
     */
    @Override
    public LoginResponse appLogin(LoginRequest request) {

        /*
         * loginId로 회원 조회
         */
        Member member = memberRepository
                .findByLoginId(request.loginId())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.LOGIN_ID_NOT_FOUND
                        )
                );

        /*
         * 회원 상태 확인
         */
        if (!ACTIVE_STATUS.equals(member.getStatus())) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "비활성화된 회원입니다."
            );
        }

        /*
         * 비밀번호 확인
         */
        if (!passwordEncoder.matches(
                request.password(),
                member.getPassword()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_PASSWORD
            );
        }

        /*
         * Access Token 생성
         */
        String accessToken =
                jwtTokenProvider.createAccessToken(
                        member.getId(),
                        member.getRole()
                );

        /*
         * Refresh Token 생성
         */
        String refreshToken =
                jwtTokenProvider.createRefreshToken(
                        member.getId(),
                        member.getRole()
                );

        return new LoginResponse(
                member.getId(),
                member.getLoginId(),
                member.getNickname(),
                member.getRole(),
                accessToken,
                refreshToken,
                jwtProperties.accessTokenExpirationMillis(),
                jwtProperties.refreshTokenExpirationMillis()
        );
    }

    /*
     * =========================================================
     * 관리자 로그인
     * =========================================================
     */
    @Override
    public LoginResponse adminLogin(LoginRequest request) {

        AdminAccount admin =
                adminAccountRepository
                        .findByLoginId(request.loginId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.LOGIN_ID_NOT_FOUND
                                )
                        );

        /*
         * 관리자 상태 확인
         */
        if (!ACTIVE_STATUS.equals(admin.getStatus())) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "비활성화된 관리자 계정입니다."
            );
        }

        /*
         * 비밀번호 확인
         */
        if (!passwordEncoder.matches(
                request.password(),
                admin.getPassword()
        )) {
            throw new BusinessException(
                    ErrorCode.INVALID_PASSWORD
            );
        }

        /*
         * Access Token 생성
         */
        String accessToken =
                jwtTokenProvider.createAccessToken(
                        admin.getId(),
                        admin.getRole()
                );

        /*
         * Refresh Token 생성
         */
        String refreshToken =
                jwtTokenProvider.createRefreshToken(
                        admin.getId(),
                        admin.getRole()
                );

        return new LoginResponse(
                admin.getId(),
                admin.getLoginId(),
                admin.getName(),
                admin.getRole(),
                accessToken,
                refreshToken,
                jwtProperties.accessTokenExpirationMillis(),
                jwtProperties.refreshTokenExpirationMillis()
        );
    }

    /*
     * =========================================================
     * Apple 로그인
     * =========================================================
     */
    @Override
    @Transactional
    public AppLoginResponse appleLogin(
            AppleLoginRequest request
    ) {

        /*
         * Apple Identity Token 검증
         */
        AppleTokenPayload appleTokenPayload =
                appleIdentityTokenVerifier.verify(
                        request.identityToken(),
                        request.nonce()
                );

        /*
         * Apple의 사용자 고유 식별자
         */
        String appleProviderId =
                appleTokenPayload.subject();

        /*
         * 기존 Apple 회원 조회
         */
        Optional<Member> optionalMember =
                memberRepository.findByProviderAndProviderId(
                        AuthProvider.APPLE,
                        appleProviderId
                );

        /*
         * 신규 회원 여부
         */
        boolean newMember =
                optionalMember.isEmpty();

        /*
         * 기존 회원이 없으면 새 Apple 회원 생성
         */
        Member member =
                optionalMember.orElseGet(() ->
                        createAppleMember(
                                appleTokenPayload
                        )
                );

        /*
         * 회원 상태 확인
         */
        if (!ACTIVE_STATUS.equals(member.getStatus())) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "비활성화된 회원입니다."
            );
        }

        /*
         * SAIT Access Token 생성
         */
        String accessToken =
                jwtTokenProvider.createAccessToken(
                        member.getId(),
                        member.getRole()
                );

        /*
         * SAIT Refresh Token 생성
         */
        String refreshToken =
                jwtTokenProvider.createRefreshToken(
                        member.getId(),
                        member.getRole()
                );

        return new AppLoginResponse(
                member.getId(),
                member.getProvider(),
                member.getNickname(),
                member.getRole(),
                newMember,
                accessToken,
                refreshToken,
                jwtProperties.accessTokenExpirationMillis(),
                jwtProperties.refreshTokenExpirationMillis()
        );
    }

    /*
     * =========================================================
     * 신규 Apple 회원 생성
     * =========================================================
     */
    private Member createAppleMember(
            AppleTokenPayload appleTokenPayload
    ) {

        Member member = new Member(
                AuthProvider.APPLE,
                appleTokenPayload.subject(),
                "Apple 사용자",
                appleTokenPayload.email()
        );

        return memberRepository.save(member);
    }

        @Override
        public boolean isLoginIdAvailable(String loginId) {
                /*
                * 아이디 공백 제거
                */
                String normalizedLoginId = loginId.trim();

                /*
                * 동일한 아이디가 존재하지 않으면 사용 가능
                */
                return !memberRepository.existsByLoginId(normalizedLoginId);
        }
}