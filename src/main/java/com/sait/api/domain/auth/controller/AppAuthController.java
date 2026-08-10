package com.sait.api.domain.auth.controller;

import com.sait.api.domain.auth.dto.AppLoginResponse;
import com.sait.api.domain.auth.dto.AppleLoginRequest;
import com.sait.api.domain.auth.dto.KakaoLoginRequest;
import com.sait.api.domain.auth.dto.LoginRequest;
import com.sait.api.domain.auth.dto.LoginResponse;
import com.sait.api.domain.auth.dto.SignupRequest;
import com.sait.api.domain.auth.dto.SignupResponse;
import com.sait.api.domain.auth.dto.request.EmailVerificationConfirmRequest;
import com.sait.api.domain.auth.dto.request.EmailVerificationSendRequest;
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

    @PostMapping("/signup")
    public ApiResponse<SignupResponse> signup( @Valid @RequestBody SignupRequest request) {
        return ApiResponse.success("앱 로그인이 완료되었습니다.", authService.signup(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login( @Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("앱 로그인이 완료되었습니다.", authService.appLogin(request));
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

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "service", "SAIT APP AUTH API",
                "status", "OK"
        ));
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