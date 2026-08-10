package com.sait.api.infra.oauth.apple;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apple.oauth")
public record AppleOAuthProperties(
        String issuer,
        String clientId,
        String publicKeyUrl
) {
}