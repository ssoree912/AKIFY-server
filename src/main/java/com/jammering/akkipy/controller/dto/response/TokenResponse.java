package com.jammering.akkipy.controller.dto.response;

import com.jammering.akkipy.domain.user.Role;
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
        private Role role;
    }

}
