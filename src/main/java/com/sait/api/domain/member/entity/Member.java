package com.sait.api.domain.member.entity;

import com.sait.api.domain.auth.AuthProvider;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "members",
        indexes = {
                @Index(
                        name = "idx_members_login_id",
                        columnList = "login_id",
                        unique = true
                ),
                @Index(
                        name = "idx_members_provider_provider_id",
                        columnList = "provider, provider_id",
                        unique = true
                )
        }
)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    /*
     * 일반 로그인 회원의 로그인 ID
     *
     * LOCAL 회원만 사용
     * 소셜 회원은 null
     */
    @Column(
            name = "login_id",
            length = 50,
            unique = true
    )
    private String loginId;

    /*
     * 이메일
     *
     * 회원 연락처 / 인증 등에 사용
     * 소셜 로그인에서는 제공되지 않을 수 있음
     */
    @Column(
            name = "email",
            length = 100
    )
    private String email;

    /*
     * 비밀번호
     *
     * LOCAL 회원만 사용
     * 소셜 회원은 null
     */
    @Column(
            name = "password",
            length = 255
    )
    private String password;

    /*
     * 로그인 제공자
     *
     * LOCAL
     * KAKAO
     * APPLE
     * NAVER
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "provider",
            nullable = false,
            length = 20
    )
    private AuthProvider provider;

    /*
     * 소셜 로그인 서비스의 사용자 고유 식별자
     *
     * LOCAL 회원은 null
     */
    @Column(
            name = "provider_id",
            length = 255
    )
    private String providerId;

    /*
     * 사용자 닉네임
     */
    @Column(
            name = "nickname",
            nullable = false,
            length = 50
    )
    private String nickname;

    /*
     * 생년월일
     */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /*
     * 회원 상태
     */
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private String status;

    /*
     * 권한
     */
    @Column(
            name = "role",
            nullable = false,
            length = 20
    )
    private String role;

    /*
     * 생성 일시
     */
    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    /*
     * 수정 일시
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /*
     * JPA 기본 생성자
     */
    protected Member() {
    }

    /*
     * =========================================================
     * 일반 로그인 회원 생성자
     * =========================================================
     */
    public Member(
            String loginId,
            String password,
            String nickname,
            String email,
            LocalDate birthDate,
            String status,
            String role
    ) {
        this.loginId = loginId;
        this.password = password;

        this.provider = AuthProvider.LOCAL;
        this.providerId = null;

        this.nickname = nickname;
        this.email = email;
        this.birthDate = birthDate;

        this.status = status;
        this.role = role;

        this.createdAt = LocalDateTime.now();
    }

    /*
     * =========================================================
     * 소셜 로그인 회원 생성자
     * =========================================================
     */
    public Member(
            AuthProvider provider,
            String providerId,
            String nickname,
            String email
    ) {
        this.loginId = null;
        this.password = null;

        this.provider = provider;
        this.providerId = providerId;

        this.nickname = nickname;
        this.email = email;
        this.birthDate = null;

        this.status = "ACTIVE";
        this.role = "USER";

        this.createdAt = LocalDateTime.now();
    }

    /*
     * =========================================================
     * INSERT 직전 기본값 설정
     * =========================================================
     */
    @PrePersist
    public void prePersist() {

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.status == null) {
            this.status = "ACTIVE";
        }

        if (this.role == null) {
            this.role = "USER";
        }

        if (this.provider == null) {
            this.provider = AuthProvider.LOCAL;
        }
    }

    /*
     * =========================================================
     * UPDATE 직전 수정일시 설정
     * =========================================================
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /*
     * =========================================================
     * Getter
     * =========================================================
     */

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getNickname() {
        return nickname;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getStatus() {
        return status;
    }

    public String getRole() {
        return role;
    }
}