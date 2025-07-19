package com.jammering.akkipy.controller;

import com.jammering.akkipy.common.code.SuccessCode;
import com.jammering.akkipy.common.response.APIResponse;
import com.jammering.akkipy.controller.dto.request.UserRequest;
import com.jammering.akkipy.controller.dto.response.TokenResponse;
import com.jammering.akkipy.controller.dto.response.UserResponse;
import com.jammering.akkipy.domain.userLogin.CustomUserDetails;
import com.jammering.akkipy.domain.userLogin.Provider;
import com.jammering.akkipy.service.UserService;
import com.jammering.akkipy.service.auth.AuthStrategyManager;
import com.jammering.akkipy.service.auth.strategy.FirebaseAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Slf4j
@RestController
public class AuthController {
    private final AuthStrategyManager authStrategyManager;
    private final FirebaseAuthService firebaseAuthService;
    private final UserService userService;

    @PostMapping("signin/{provider}")
    public ResponseEntity<APIResponse<TokenResponse.ToKenInfo>> login(@PathVariable Provider provider, @RequestBody UserRequest.Auth auth) throws Exception {
        APIResponse response =  APIResponse.of(SuccessCode.SELECT_SUCCESS,authStrategyManager.signIn(provider, auth));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("signup/email")
    public void firebaseSignUp(@RequestBody UserRequest.Firebase firebase) throws Exception {
        firebaseAuthService.createAccountInFirebase(firebase.getEmail(), firebase.getPassword());
    }
    @PostMapping("signup")
    public ResponseEntity<APIResponse<UserResponse.UserTokenInfo>> signUp(@RequestBody UserRequest.SignUp signUp , @AuthenticationPrincipal CustomUserDetails userDetails)  {
        UserResponse.UserTokenInfo userTokenInfo =userService.registerUser(signUp.getNickname(), userDetails.getCustomUserInfoDto().getUid(), userDetails.getCustomUserInfoDto().getProvider());
        APIResponse response = APIResponse.of(SuccessCode.INSERT_SUCCESS, userTokenInfo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}

