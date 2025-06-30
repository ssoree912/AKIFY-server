package com.jammering.akkipy.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.jammering.akkipy.controller.request.UserRequest;
import com.jammering.akkipy.domain.userLogin.Provider;
import com.jammering.akkipy.service.FirebaseAuthService;
import com.jammering.akkipy.service.KakaoService;
import com.jammering.akkipy.service.NaverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Slf4j
@RestController
public class AuthController {
    private final KakaoService kakaoService;
    private final FirebaseAuthService firebaseAuthService;
    private final NaverService naverService;

    @PostMapping("signin/{provider}")
    public String signIn(@PathVariable Provider provider, @RequestBody UserRequest.Auth auth) throws Exception {
        String uid = "";
        if (provider == Provider.KAKAO) {
            String accessCode = kakaoService.getKakaoAccessToken(auth.getToken());
            uid = kakaoService.createKakaoUser(accessCode).getUid();
        } else if (provider == Provider.FIREBASE) {
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(auth.getToken());
            uid = firebaseToken.getUid();
        } else if (provider == Provider.NAVER) {
            String accessCode =  naverService.getAccessToken(auth.getToken(), auth.getState());
            uid = naverService.getUserInfo(accessCode);
        }else if (provider == Provider.LOCAL) {
            uid= firebaseAuthService.signInWithEmailAndPassword(auth.getEmail(), auth.getPassword());
        }
        else {
            throw new IllegalArgumentException("지원하지 않는 인증 제공자입니다: " + provider);
        }
        return uid;
    }
    @PostMapping("signup/local")
    public void firebaseSignUp(@RequestBody UserRequest.Firebase firebase) throws Exception {
        firebaseAuthService.createAccountInFirebase(firebase.getEmail(), firebase.getPassword());
    }

}

