package com.jammering.akkipy.controller;

import com.jammering.akkipy.controller.request.UserRequest;
import com.jammering.akkipy.service.KakaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Slf4j
@RestController
public class AuthController {
    private final KakaoService kakaoService;

    @PostMapping
    public String getToken(@RequestBody UserRequest.Token token) {
        return kakaoService.getKakaoAccessToken(token.getToken());
    }
    @PostMapping("signin")
    public String signIn(@RequestBody UserRequest.Token token) throws Exception {
        return kakaoService.createKakaoUser(token.getToken()).getUid();

//        return "ok";
    }
}
