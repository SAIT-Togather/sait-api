package com.sait.api.infra.oauth.apple;

import java.util.List;

public record ApplePublicKeyResponse(
        List<ApplePublicKey> keys
) {
}