package com.sait.api.domain.auth.service;

import com.sait.api.domain.auth.dto.AppLoginResponse;
import com.sait.api.domain.auth.dto.AppleLoginRequest;
import com.sait.api.domain.auth.dto.KakaoLoginRequest;
import com.sait.api.domain.auth.dto.LoginRequest;
import com.sait.api.domain.auth.dto.LoginResponse;
import com.sait.api.domain.auth.dto.SignupRequest;
import com.sait.api.domain.auth.dto.SignupResponse;

import jakarta.validation.Valid;


public interface AuthService {

    // 사잇 회원가입
    SignupResponse signup(SignupRequest request);
    // 사잇 앱로그인
    LoginResponse appLogin(LoginRequest request);
    // 사잇 관리자 로그인
    LoginResponse adminLogin(LoginRequest request);
    // 애플 로그인
    AppLoginResponse appleLogin(AppleLoginRequest request);
    // 카카오 로그인
    //AppLoginResponse kakaoLogin(KakaoLoginRequest request);
}