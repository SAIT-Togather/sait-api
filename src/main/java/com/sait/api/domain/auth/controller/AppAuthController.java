package com.sait.api.domain.auth.controller;

import com.sait.api.domain.auth.dto.request.AppleLoginRequest;
import com.sait.api.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.sait.api.domain.auth.dto.request.EmailVerificationSendRequest;
import com.sait.api.domain.auth.dto.request.KakaoLoginRequest;
import com.sait.api.domain.auth.dto.request.LoginRequest;
import com.sait.api.domain.auth.dto.request.SignupRequest;
import com.sait.api.domain.auth.dto.response.AppLoginResponse;
import com.sait.api.domain.auth.dto.response.LoginIdCheckResponse;
import com.sait.api.domain.auth.dto.response.LoginResponse;
import com.sait.api.domain.auth.dto.response.SignupResponse;
import com.sait.api.domain.auth.service.AuthService;
import com.sait.api.domain.auth.service.EmailVerificationService;
import com.sait.api.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/app/auth")
@RequiredArgsConstructor
public class AppAuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    /*
     * =========================================================
     * 회원가입
     * =========================================================
     */
    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup( @Valid @RequestBody SignupRequest request) {
        return ApiResponse.success("앱 회원가입이 완료되었습니다.", authService.signup(request));
    }
    /*
     * =========================================================
     * 이메일 인증번호 발송
     * =========================================================
     */
    @PostMapping("/email/send")
    public ApiResponse<Void> sendEmailVerificationCode(@Valid @RequestBody EmailVerificationSendRequest request) {

        emailVerificationService.sendVerificationCode(
            request.getEmail(),
            request.getPurpose()
        );

        return ApiResponse.success(
                "인증번호가 발송되었습니다.",
                null
        );
    }

    /*
     * =========================================================
     * 이메일 인증번호 확인
     * =========================================================
     */
    @PostMapping("/email/verify")
    public ApiResponse<Void> verifyEmailCode(
            @Valid @RequestBody EmailVerificationConfirmRequest request
    ) {

        boolean verified = emailVerificationService.verifyCode(
                        request.getEmail(),
                        request.getCode(),
                        request.getPurpose()
                );

        if (!verified) {
            throw new IllegalArgumentException(
                    "인증번호가 올바르지 않거나 만료되었습니다."
            );
        }

        return ApiResponse.success(
                "이메일 인증이 완료되었습니다.",
                null
        );
    }

    @GetMapping("/check-login-id")
    public LoginIdCheckResponse checkLoginId(
            @RequestParam String loginId
    ) {

        boolean available = authService.isLoginIdAvailable(loginId);

        return new LoginIdCheckResponse(
                true,
                "SUCCESS",
                available
                        ? "사용 가능한 아이디입니다."
                        : "이미 사용 중인 아이디입니다.",
                available
        );
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "service", "SAIT APP AUTH API",
                "status", "OK"
        ));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login( @Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("앱 로그인이 완료되었습니다.", authService.appLogin(request));
    }

    @PostMapping("/apple")
    public ApiResponse<AppLoginResponse> appleLogin( @Valid @RequestBody AppleLoginRequest request) {
        return ApiResponse.success("Apple 로그인이 완료되었습니다.",authService.appleLogin(request));
    }

    //@PostMapping("/kakao")
    //public ApiResponse<AppLoginResponse> kakaoLogin( @Valid @RequestBody KakaoLoginRequest request) {
    //    return ApiResponse.success("kakao 로그인이 완료되었습니다.",authService.kakaoLogin(request));
    //}
    
}