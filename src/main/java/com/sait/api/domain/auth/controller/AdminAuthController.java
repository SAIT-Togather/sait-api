package com.sait.api.domain.auth.controller;

import com.sait.api.domain.auth.dto.request.LoginRequest;
import com.sait.api.domain.auth.dto.response.LoginResponse;
import com.sait.api.domain.auth.service.AuthService;
import com.sait.api.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AuthService authService;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ApiResponse.success("관리자 로그인이 완료되었습니다.", authService.adminLogin(request));
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.success(Map.of(
                "service", "SAIT ADMIN AUTH API",
                "status", "OK"
        ));
    }
}