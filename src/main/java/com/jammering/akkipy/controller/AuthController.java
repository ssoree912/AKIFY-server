package com.jammering.akkipy.controller;

import com.jammering.akkipy.controller.request.UserRequest;
import com.jammering.akkipy.domain.userLogin.Provider;
import com.jammering.akkipy.service.auth.AuthStrategyManager;
import com.jammering.akkipy.service.auth.strategy.FirebaseAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Slf4j
@RestController
public class AuthController {
    private final AuthStrategyManager authStrategyManager;
    private final FirebaseAuthService firebaseAuthService;

    @PostMapping("signin/{provider}")
    public String login(@PathVariable Provider provider, @RequestBody UserRequest.Auth auth) throws Exception {
        return authStrategyManager.signIn(provider, auth);
    }
    @PostMapping("signup/local")
    public void firebaseSignUp(@RequestBody UserRequest.Firebase firebase) throws Exception {
        firebaseAuthService.createAccountInFirebase(firebase.getEmail(), firebase.getPassword());
    }

}

