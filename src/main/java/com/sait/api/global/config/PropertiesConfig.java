package com.sait.api.global.config;

import com.sait.api.infra.oauth.apple.AppleOAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppleOAuthProperties.class)
public class PropertiesConfig {
}