package com.jammering.akkipy.config.jwt;

import com.jammering.akkipy.controller.dto.response.TokenResponse;
import com.jammering.akkipy.domain.user.Role;
import com.jammering.akkipy.domain.userLogin.Provider;
import com.jammering.akkipy.service.RedisService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    private static final String BEARER_TYPE = "Bearer";
    private final long JWT_GUEST_TOKEN_EXPIRATION=1000 * 60 * 60;
    private final long JWT_ACCESS_TOKEN_EXPIRATION=1000 * 60 * 60 * 24 * 7 ;
    private final RedisService redisService;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
    // 임시 토큰 생성 (소셜 로그인 성공 직후)
    public TokenResponse.ToKenInfo generateTemporaryToken(String socialId, Provider provider) {
        Claims claims = Jwts.claims().setSubject(socialId);
        claims.put("role", Role.GUEST);
        claims.put("provider", provider.name());
        claims.put("signup", false);

        return createToken(claims, JWT_GUEST_TOKEN_EXPIRATION, Role.GUEST);
    }

    // 정식 토큰 생성 (회원가입 완료 후)
    public TokenResponse.ToKenInfo generateToken(Long userId, Role role) {
        Claims claims = Jwts.claims().setSubject(userId.toString());
        claims.put("role", role);
        claims.put("signup", true);

        return createToken(claims, JWT_ACCESS_TOKEN_EXPIRATION,role );
    }

    private TokenResponse.ToKenInfo createToken(Claims claims, long expireTime, Role role) {
        Date now = new Date();
        String accessToken =  Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        long refreshTokenExpireTime = expireTime * 2;
        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenExpireTime )) // Refresh token은 2배로 설정
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
        String userId = claims.getSubject();
        redisService.setValuesWithTimeUnit("RefreshToken:" + userId, refreshToken, refreshTokenExpireTime, TimeUnit.MILLISECONDS);
        return new TokenResponse.ToKenInfo(BEARER_TYPE,accessToken,refreshToken , refreshTokenExpireTime, role);
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTemporaryToken(String token) {
        return !(Boolean) getClaims(token).get("signup");
    }
    public boolean validateToken(String token) throws SecurityException, ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, IllegalArgumentException {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
            // 401 Unauthorized
            throw e;
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
            // 401 Expired
            throw e;
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
            // 400 Bad Request
            throw e;
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
            // 400 Bad Request
            throw e;
        }
    }
}
