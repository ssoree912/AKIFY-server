package com.jammering.akkipy.service.auth.strategy;

import com.google.gson.JsonParser;
import com.jammering.akkipy.config.jwt.JwtProvider;
import com.jammering.akkipy.controller.dto.request.UserRequest;
import com.jammering.akkipy.controller.dto.response.TokenResponse;
import com.jammering.akkipy.domain.userLogin.Provider;
import com.jammering.akkipy.domain.userLogin.UserLoginRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class KakaoAuthService extends AbstractOAuthService {

    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.redirect-url}")
    private String redirectUri;
    @Value("${kakao.admin-key}")
    private String adminKey;

    private final UserLoginRepository userLoginRepository;
    private final JwtProvider jwtProvider;

    @Override
    public boolean supports(Provider provider) {
        return provider == Provider.KAKAO;
    }

    @Override
    public TokenResponse.ToKenInfo signIn(UserRequest.Auth auth, Provider provider) throws Exception {
        String accessToken = getKakaoAccessToken(auth.getToken());
        String uid = getUserIdFromAccessToken(accessToken);
        return userLoginRepository.findByProviderAndProviderUid(provider, uid)
                .map(userLogin -> jwtProvider.generateToken(userLogin.getUser().getUserId(), userLogin.getUser().getRole())) // 가입되어 있음 → 정식 토큰
                .orElseGet(() -> jwtProvider.generateTemporaryToken(uid, provider)); // 가입되어 있지 않음 → 임시 토큰
    }

    private String getKakaoAccessToken(String code) throws Exception {
        String url = "https://kauth.kakao.com/oauth/token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, defaultHeaders(MediaType.APPLICATION_FORM_URLENCODED));
        String response = restTemplate.postForObject(url, request, String.class);
        return new JSONObject(response).getString("access_token");
    }

    private String getUserIdFromAccessToken(String accessToken) throws IOException {
        String url = "https://kapi.kakao.com/v2/user/me";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestMethod("POST");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        reader.lines().forEach(response::append);
        return JsonParser.parseString(response.toString()).getAsJsonObject().get("id").getAsString();
    }
}