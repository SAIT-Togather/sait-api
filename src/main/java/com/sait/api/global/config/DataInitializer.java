package com.sait.api.global.config;

import com.sait.api.domain.admin.entity.AdminAccount;
import com.sait.api.domain.admin.repository.AdminAccountRepository;
import com.sait.api.domain.member.entity.Member;
import com.sait.api.domain.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            MemberRepository memberRepository,
            AdminAccountRepository adminAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            if (!memberRepository.existsByLoginId("user")) {

                Member member = new Member(
                        "user",
                        passwordEncoder.encode("P@ssw0rd"),
                        "사잇 앱 사용자",
                        "user@sait.com",
                        null,
                        "ACTIVE",
                        "USER"
                );

                memberRepository.save(member);
            }

            if (!adminAccountRepository.existsByLoginId("admin")) {

                AdminAccount admin = new AdminAccount(
                        "admin",
                        passwordEncoder.encode("P@ssw0rd"),
                        "사잇 관리자",
                        "admin@sait.com",
                        "ACTIVE",
                        "ADMIN"
                );

                adminAccountRepository.save(admin);
            }
        };
    }
}