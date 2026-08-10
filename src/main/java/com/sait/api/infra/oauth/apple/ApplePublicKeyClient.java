package com.sait.api.infra.oauth.apple;

import com.sait.api.global.exception.BusinessException;
import com.sait.api.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ApplePublicKeyClient {

    private final RestClient restClient;
    private final AppleOAuthProperties properties;

    public ApplePublicKeyClient(RestClient.Builder restClientBuilder,AppleOAuthProperties properties) {
        this.restClient = restClientBuilder.build();
        this.properties = properties;
    }

    public ApplePublicKeyResponse getPublicKeys() {
        try {
            ApplePublicKeyResponse response = restClient
                    .get()
                    .uri(properties.publicKeyUrl())
                    .retrieve()
                    .body(ApplePublicKeyResponse.class);

            if (response == null || response.keys() == null || response.keys().isEmpty()) {

                throw new BusinessException(
                        ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND,
                        "Apple 공개키 응답이 비어 있습니다."
                );
            }

            return response;

        } catch (BusinessException exception) {
            throw exception;

        } catch (Exception exception) {
            throw new BusinessException(
                    ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND,
                    "Apple 공개키 조회에 실패했습니다."
            );
        }
    }
}