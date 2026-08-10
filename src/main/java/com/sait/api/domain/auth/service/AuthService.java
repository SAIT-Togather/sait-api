package com.sait.api.domain.auth.service;

import com.sait.api.domain.auth.dto.request.AppleLoginRequest;
import com.sait.api.domain.auth.dto.request.KakaoLoginRequest;
import com.sait.api.domain.auth.dto.request.LoginRequest;
import com.sait.api.domain.auth.dto.request.SignupRequest;
import com.sait.api.domain.auth.dto.response.AppLoginResponse;
import com.sait.api.domain.auth.dto.response.LoginResponse;
import com.sait.api.domain.auth.dto.response.SignupResponse;

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
    // 아이디 중복 체크
    boolean isLoginIdAvailable(String loginId);
}