package com.jammering.akkipy.service.auth;

import com.jammering.akkipy.common.code.ErrorCode;
import com.jammering.akkipy.config.jwt.JwtProvider;
import com.jammering.akkipy.controller.dto.response.TokenResponse;
import com.jammering.akkipy.domain.user.Role;
import com.jammering.akkipy.exception.TokenException;
import com.jammering.akkipy.service.RedisService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    public TokenResponse.ToKenInfo reissueToken(String refreshToken) {
        // 1. Refresh Token 검증
        jwtProvider.validateToken(refreshToken);

        // 2. Refresh Token에서 사용자 ID 가져오기
        Claims claims = jwtProvider.getClaims(refreshToken);
        String userId = claims.getSubject();

        // 3. Redis에서 저장된 Refresh Token 가져오기
        String storedRefreshToken = redisService.getRefrestToken(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new TokenException(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        }

        // 4. 새로운 토큰 생성
        return jwtProvider.generateToken(Long.parseLong(userId), Role.valueOf((String) claims.get("role")));
    }
}
