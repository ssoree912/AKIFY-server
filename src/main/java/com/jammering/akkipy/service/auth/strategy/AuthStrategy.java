package com.jammering.akkipy.service.auth.strategy;

import com.jammering.akkipy.controller.request.UserRequest;
import com.jammering.akkipy.domain.userLogin.Provider;

public interface AuthStrategy {
    boolean supports(Provider provider);
    String signIn(UserRequest.Auth auth, Provider provider) throws Exception;
}
