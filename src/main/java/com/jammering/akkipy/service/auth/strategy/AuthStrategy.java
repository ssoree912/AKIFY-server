package com.jammering.akkipy.service.auth.strategy;

import com.jammering.akkipy.controller.dto.request.UserRequest;
import com.jammering.akkipy.controller.dto.response.TokenResponse;
import com.jammering.akkipy.domain.userLogin.Provider;

public interface AuthStrategy {
    boolean supports(Provider provider);
    TokenResponse.ToKenInfo signIn(UserRequest.Auth auth, Provider provider) throws Exception;
}
