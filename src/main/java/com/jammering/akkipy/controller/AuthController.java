package com.jammering.akkipy.controller;

import com.jammering.akkipy.common.code.SuccessCode;
import com.jammering.akkipy.common.response.APIResponse;
import com.jammering.akkipy.controller.dto.request.UserRequest;
import com.jammering.akkipy.controller.dto.response.TokenResponse;
import com.jammering.akkipy.controller.dto.response.UserResponse;
import com.jammering.akkipy.domain.userLogin.CustomUserDetails;
import com.jammering.akkipy.domain.userLogin.Provider;
import com.jammering.akkipy.common.code.ErrorCode;
import com.jammering.akkipy.exception.EmailVerificationException;
import com.jammering.akkipy.service.EmailService;
import com.jammering.akkipy.service.RedisService;
import com.jammering.akkipy.service.UserService;
import com.jammering.akkipy.service.auth.AuthService;
import com.jammering.akkipy.service.auth.AuthStrategyManager;
import com.jammering.akkipy.service.auth.strategy.FirebaseAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Slf4j
@RestController
public class AuthController {
    private final AuthStrategyManager authStrategyManager;
    private final FirebaseAuthService firebaseAuthService;
    private final UserService userService;
    private final AuthService authService;
    private final EmailService emailService;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    private static final String VERIFICATION_CODE_KEY_PREFIX = "verificationCode:";

    @PostMapping("signin/{provider}")
    public ResponseEntity<APIResponse<TokenResponse.ToKenInfo>> login(@PathVariable Provider provider, @RequestBody UserRequest.Auth auth) throws Exception {
        APIResponse response =  APIResponse.of(SuccessCode.SELECT_SUCCESS,authStrategyManager.signIn(provider, auth));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/email/send")
    public ResponseEntity<APIResponse<Void>> sendVerificationEmail(@RequestBody UserRequest.SendEmailVerification request) {
        String verificationCode = emailService.sendVerificationEmail(request.getEmail());
        // 인증 코드만 Redis에 저장
        redisService.setValues(VERIFICATION_CODE_KEY_PREFIX + request.getEmail(), verificationCode, Duration.ofMinutes(5));

        APIResponse response = APIResponse.of(SuccessCode.INSERT_SUCCESS, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<APIResponse<Void>> verifyEmailAndSignUp(@RequestBody UserRequest.VerifyEmail request) throws Exception {
        String storedCode = redisService.getValues(VERIFICATION_CODE_KEY_PREFIX + request.getEmail());

        if (storedCode == null || !storedCode.equals(request.getCode())) {
            throw new EmailVerificationException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        firebaseAuthService.createAccountInFirebase(request.getEmail(), request.getPassword());

        redisService.deleteValues(VERIFICATION_CODE_KEY_PREFIX + request.getEmail());

        APIResponse response = APIResponse.of(SuccessCode.INSERT_SUCCESS, null);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("signup")
    public ResponseEntity<APIResponse<UserResponse.UserTokenInfo>> signUp(@RequestBody UserRequest.SignUp signUp , @AuthenticationPrincipal CustomUserDetails userDetails)  {
        UserResponse.UserTokenInfo userTokenInfo =userService.registerUser(signUp.getNickname(), userDetails.getCustomUserInfoDto().getUid(), userDetails.getCustomUserInfoDto().getProvider());
        log.info(userDetails.getCustomUserInfoDto().getProvider().toString());
        APIResponse response = APIResponse.of(SuccessCode.INSERT_SUCCESS, userTokenInfo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @PostMapping("/reissue")
    public ResponseEntity<APIResponse<TokenResponse.ToKenInfo>> reissue(@RequestBody UserRequest.Reissue reissue) {
        TokenResponse.ToKenInfo tokenInfo = authService.reissueToken(reissue.getRefreshToken());
        APIResponse response = APIResponse.of(SuccessCode.SELECT_SUCCESS, tokenInfo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

