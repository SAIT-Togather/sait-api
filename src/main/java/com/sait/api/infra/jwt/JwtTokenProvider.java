package com.sait.api.infra.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String ROLE_CLAIM_NAME = "role";
    private static final String TOKEN_TYPE_CLAIM_NAME = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String createAccessToken(Long userId, String role) {
        return createToken(
                userId,
                role,
                ACCESS_TOKEN_TYPE,
                jwtProperties.accessTokenExpirationMillis()
        );
    }

    public String createRefreshToken(Long userId, String role) {
        return createToken(
                userId,
                role,
                REFRESH_TOKEN_TYPE,
                jwtProperties.refreshTokenExpirationMillis()
        );
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getMemberId(String token) {
        Claims claims = getClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    public String getRole(String token) {
        Claims claims = getClaims(token);
        return claims.get(ROLE_CLAIM_NAME, String.class);
    }

    public String getTokenType(String token) {
        Claims claims = getClaims(token);
        return claims.get(TOKEN_TYPE_CLAIM_NAME, String.class);
    }

    private String createToken(
            Long userId,
            String role,
            String tokenType,
            long expirationMillis
    ) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(ROLE_CLAIM_NAME, role)
                .claim(TOKEN_TYPE_CLAIM_NAME, tokenType)
                .issuedAt(now)
                .expiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}