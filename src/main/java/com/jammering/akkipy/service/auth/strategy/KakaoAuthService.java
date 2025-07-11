package com.jammering.akkipy.service.auth.strategy;

import com.google.gson.JsonParser;
import com.jammering.akkipy.controller.request.UserRequest;
import com.jammering.akkipy.domain.userLogin.Provider;
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

    @Override
    public boolean supports(Provider provider) {
        return provider == Provider.KAKAO;
    }

    @Override
    public String signIn(UserRequest.Auth auth, Provider provider) throws Exception {
        String accessToken = getKakaoAccessToken(auth.getToken());
        return getUserIdFromAccessToken(accessToken);
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