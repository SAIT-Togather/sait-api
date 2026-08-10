package com.sait.api.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // Common
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C003", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C005", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C999", "서버 내부 오류가 발생했습니다."),

    // Auth
    LOGIN_ID_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A001", "존재하지 않는 아이디입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "A002", "비밀번호가 올바르지 않습니다."),
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "A003", "아이디 또는 비밀번호가 올바르지 않습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A005", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A006", "리프레시 토큰을 찾을 수 없습니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "A007", "필수 약관에 동의해야 합니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "A008", "이메일 인증이 필요합니다."),
    
    
    // Apple Auth 
    INVALID_APPLE_TOKEN( HttpStatus.UNAUTHORIZED, "A007", "유효하지 않은 Apple identity token입니다." ), 
    EXPIRED_APPLE_TOKEN( HttpStatus.UNAUTHORIZED, "A008", "Apple identity token이 만료되었습니다." ), 
    APPLE_PUBLIC_KEY_NOT_FOUND( HttpStatus.BAD_GATEWAY, "A009", "Apple 공개키를 찾을 수 없습니다." ),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "회원을 찾을 수 없습니다."),
    DUPLICATE_MEMBER(HttpStatus.CONFLICT, "M002", "이미 가입된 회원입니다."),

    // Admin
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "AD001", "관리자 계정을 찾을 수 없습니다."),

    // Notice
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "공지사항을 찾을 수 없습니다."),

    // Terms
    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "약관 정보를 찾을 수 없습니다."),

    // Schedule
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "일정을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}