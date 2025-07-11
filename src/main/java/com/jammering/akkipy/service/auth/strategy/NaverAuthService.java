package com.jammering.akkipy.service.auth.strategy;

import com.google.gson.JsonParser;
import com.jammering.akkipy.controller.request.UserRequest;
import com.jammering.akkipy.domain.userLogin.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
@Service
@RequiredArgsConstructor
public class NaverAuthService extends AbstractOAuthService {

    @Value("${naver.client-id}")
    private String clientId;
    @Value("${naver.client-secret}")
    private String clientSecret;
    @Value("${naver.redirect-url}")
    private String redirectUri;

    @Override
    public boolean supports(Provider provider) {
        return provider == Provider.NAVER;
    }

    @Override
    public String signIn(UserRequest.Auth auth, Provider provider) {
        String accessToken = getAccessToken(auth.getToken(), auth.getState());
        return getUserInfo(accessToken);
    }

    private String getAccessToken(String code, String state) {
        String url = "https://nid.naver.com/oauth2.0/token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, new HttpHeaders());
        String body = restTemplate.postForObject(url, request, String.class);
        return JsonParser.parseString(body).getAsJsonObject().get("access_token").getAsString();
    }

    private String getUserInfo(String accessToken) {
        String url = "https://openapi.naver.com/v1/nid/me";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        String body = restTemplate.exchange(url, HttpMethod.POST, request, String.class).getBody();
        return JsonParser.parseString(body).getAsJsonObject().getAsJsonObject("response").get("id").getAsString();
    }
}