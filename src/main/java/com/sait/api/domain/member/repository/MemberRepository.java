package com.sait.api.domain.member.repository;

import com.sait.api.domain.auth.AuthProvider;
import com.sait.api.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository
        extends JpaRepository<Member, Long> {

    /*
     * 일반 로그인 회원 조회
     */
    Optional<Member> findByLoginId(
            String loginId
    );

    /*
     * 일반 로그인 아이디 중복 확인
     */
    boolean existsByLoginId(
            String loginId
    );

    /*
     * 소셜 회원 조회
     */
    Optional<Member> findByProviderAndProviderId(
            AuthProvider provider,
            String providerId
    );

    /*
     * 소셜 회원 존재 여부
     */
    boolean existsByProviderAndProviderId(
            AuthProvider provider,
            String providerId
    );
}