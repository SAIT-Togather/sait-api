package com.sait.api.domain.member.controller;

import com.sait.api.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MemberController {

    @GetMapping("/api/app/members/health")
    public ApiResponse<Map<String, Object>> appMemberHealth() {
        return ApiResponse.success(Map.of(
                "service", "SAIT APP MEMBER API",
                "status", "OK"
        ));
    }

    @GetMapping("/api/admin/members/health")
    public ApiResponse<Map<String, Object>> adminMemberHealth() {
        return ApiResponse.success(Map.of(
                "service", "SAIT ADMIN MEMBER API",
                "status", "OK"
        ));
    }
}