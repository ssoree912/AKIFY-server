package com.jammering.akkipy.controller.dto.response;

import com.jammering.akkipy.domain.userLogin.Authority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class TokenResponse {
    @Builder
    @Setter
    @Getter
    @AllArgsConstructor
    public static class ToKenInfo {
        private String grantType;
        private String accessToken;
        private String refreshToken;
        private Long refreshTokenExpirationTime;
        private Authority authority;
    }
    public static ToKenInfo toTokenInfo(String BEARER_TYPE, String accessToken, String refreshToken, Long REFRESH_TOKEN_EXPIRE_TIME, Authority authority) {
        return ToKenInfo.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenExpirationTime(REFRESH_TOKEN_EXPIRE_TIME)
                .authority(authority)
                .build();
    }

}
