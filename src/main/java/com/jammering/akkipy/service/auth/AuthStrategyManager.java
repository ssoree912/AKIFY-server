package com.jammering.akkipy.service.auth;

import com.jammering.akkipy.controller.dto.request.UserRequest;
import com.jammering.akkipy.controller.dto.response.TokenResponse;
import com.jammering.akkipy.domain.userLogin.Provider;
import com.jammering.akkipy.service.auth.strategy.AuthStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthStrategyManager {
    private final List<AuthStrategy> strategies;

    public TokenResponse.ToKenInfo signIn(Provider provider, UserRequest.Auth auth) throws Exception {
        return  strategies.stream()
                .filter(strategy -> strategy.supports(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 인증 제공자입니다: " + provider))
                .signIn(auth,provider);
    }
}