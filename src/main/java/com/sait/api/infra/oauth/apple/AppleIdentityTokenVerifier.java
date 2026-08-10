package com.sait.api.infra.oauth.apple;

import com.sait.api.global.exception.BusinessException;
import com.sait.api.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class AppleIdentityTokenVerifier {

    private final ApplePublicKeyClient applePublicKeyClient;
    private final AppleOAuthProperties properties;

    public AppleIdentityTokenVerifier(
            ApplePublicKeyClient applePublicKeyClient,
            AppleOAuthProperties properties
    ) {
        this.applePublicKeyClient = applePublicKeyClient;
        this.properties = properties;
    }

    public AppleTokenPayload verify(String identityToken,String requestNonce) {
        if (!StringUtils.hasText(identityToken)) {
            throw new BusinessException(
                    ErrorCode.INVALID_APPLE_TOKEN,
                    "Apple identity token이 없습니다."
            );
        }

        try {
            Map<String, Object> header = parseHeader(identityToken);

                String kid = getHeaderValue(header, "kid");
                String alg = getHeaderValue(header, "alg");

            if (!StringUtils.hasText(kid)) {
                throw new BusinessException(
                        ErrorCode.INVALID_APPLE_TOKEN,
                        "Apple identity token의 kid가 없습니다."
                );
            }

            if (!"RS256".equals(alg)) {
                throw new BusinessException(
                        ErrorCode.INVALID_APPLE_TOKEN,
                        "지원하지 않는 Apple 토큰 알고리즘입니다."
                );
            }

            ApplePublicKey applePublicKey = findPublicKey(kid, alg);
            PublicKey publicKey = createPublicKey(applePublicKey);

            // ( RSA 공개키로 토큰 서명 검증 )
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(properties.issuer())
                    .requireAudience(properties.clientId())
                    .build()
                    .parseSignedClaims(identityToken)
                    .getPayload();

            // 토큰이 아직 유효한 값인지 확인
            validateExpiration(claims);
            // 앱에서 보낸 nonce와 Apple토큰안의 nonce가 같은 로그인 요청인지 확인
            validateNonce(claims, requestNonce);

            String subject = claims.getSubject();

            if (!StringUtils.hasText(subject)) {
                throw new BusinessException(
                        ErrorCode.INVALID_APPLE_TOKEN,
                        "Apple 사용자 식별자가 없습니다."
                );
            }

            String email = claims.get("email", String.class);
            Boolean emailVerifiedClaim = parseBooleanClaim(claims.get("email_verified"));

            // 애플 사용자 정보
            return new AppleTokenPayload(
                    claims.getIssuer(),
                    subject,
                    extractAudience(claims),
                    claims.getExpiration().getTime(),
                    claims.getIssuedAt() == null
                            ? 0L
                            : claims.getIssuedAt().getTime(),
                    email,
                    Boolean.TRUE.equals(emailVerifiedClaim),
                    claims.get("nonce", String.class)
            );

        } catch (BusinessException exception) {
            throw exception;
        } catch (SignatureException exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_APPLE_TOKEN,
                    "Apple identity token 서명이 올바르지 않습니다."
            );
        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_APPLE_TOKEN,
                    "Apple identity token 검증에 실패했습니다."
            );
        }
    }

    private Map<String, Object> parseHeader(String identityToken) {
        String[] tokenParts = identityToken.split("\\.");

        if (tokenParts.length != 3) {
            throw new BusinessException(
                    ErrorCode.INVALID_APPLE_TOKEN,
                    "Apple identity token 형식이 올바르지 않습니다."
            );
        }

        try {
            byte[] decodedHeader = Base64.getUrlDecoder()
                    .decode(tokenParts[0]);

            String headerJson = new String(
                    decodedHeader,
                    java.nio.charset.StandardCharsets.UTF_8
            );

            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(
                            headerJson,
                            new com.fasterxml.jackson.core.type.TypeReference<>() {
                            }
                    );

        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_APPLE_TOKEN,
                    "Apple identity token 헤더를 읽을 수 없습니다."
            );
        }
    }

    private ApplePublicKey findPublicKey(String kid,String alg) {
        ApplePublicKeyResponse response = applePublicKeyClient.getPublicKeys();

        return response.keys()
                .stream()
                .filter(key -> kid.equals(key.kid()))
                .filter(key -> alg.equals(key.alg()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND,
                        "일치하는 Apple 공개키를 찾을 수 없습니다."
                ));
    }

    private PublicKey createPublicKey(ApplePublicKey key) {
        try {
            byte[] modulusBytes = Base64.getUrlDecoder().decode(key.n());

            byte[] exponentBytes = Base64.getUrlDecoder().decode(key.e());

            BigInteger modulus = new BigInteger(1, modulusBytes);

            BigInteger exponent = new BigInteger(1, exponentBytes);

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePublic(publicKeySpec);

        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.INVALID_APPLE_TOKEN,
                    "Apple RSA 공개키 생성에 실패했습니다."
            );
        }
    }

    private void validateExpiration(Claims claims) {
        if (claims.getExpiration() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_APPLE_TOKEN,
                    "Apple identity token 만료 시간이 없습니다."
            );
        }

        if (claims.getExpiration().toInstant().isBefore(Instant.now())) {
            throw new BusinessException(
                    ErrorCode.EXPIRED_APPLE_TOKEN,
                    "Apple identity token이 만료되었습니다."
            );
        }
    }

    private void validateNonce(Claims claims,String requestNonce) {
        if (!StringUtils.hasText(requestNonce)) {
            return;
        }

        String tokenNonce = claims.get("nonce", String.class);

        if (!StringUtils.hasText(tokenNonce)) {
            throw new BusinessException(
                    ErrorCode.INVALID_APPLE_TOKEN,
                    "Apple identity token에 nonce가 없습니다."
            );
        }

        if (!requestNonce.equals(tokenNonce)) {
            throw new BusinessException(
                    ErrorCode.INVALID_APPLE_TOKEN,
                    "Apple nonce가 일치하지 않습니다."
            );
        }
    }

    private Boolean parseBooleanClaim(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return Boolean.parseBoolean(String.valueOf(value));
    }

    private String extractAudience(Claims claims) {
        Object audience = claims.get("aud");

        if (audience == null) {
            return null;
        }

        if (audience instanceof String stringAudience) {
            return stringAudience;
        }

        return String.valueOf(audience);
    }

    private String getHeaderValue(Map<String, Object> header,String key) {
        Object value = header.get(key);

        return value == null? null : String.valueOf(value);
    }
}