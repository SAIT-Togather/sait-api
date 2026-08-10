package com.sait.api.infra.oauth.apple;

public record AppleTokenPayload(
        String issuer,          //토근 발급자
        String subject,         //사용자 고유 식별값
        String audience,        //토큰의 사용 대상
        long expirationTime,    //토큰만료시간
        long issuedAt,          //토큰발급시간
        String email,           //사용자이메일
        boolean emailVerified,  //애플이 인증한 이메일인지아닌지  true,false
        String nonce            //로그인 요청을 구분하기 위한 일회용 랜덤값
) {
}