package com.jammering.akkipy.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class NaverService {
    @Value("${naver.client-id}")
    private String CLIENT_ID;
    @Value("${naver.client-secret}")
    private String CLIENT_SECRET;
    @Value("${naver.redirect-url}")
    private String REDIRECT_URI;
    public String getAccessToken(String code, String state) {
        String reqUrl = "https://nid.naver.com/oauth2.0/token";
        RestTemplate restTemplate = new RestTemplate();

        // HttpHeader Object
        HttpHeaders headers = new HttpHeaders();

        // HttpBody Object
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", CLIENT_ID);
        params.add("client_secret", CLIENT_SECRET);
        params.add("code", code);
        params.add("state", state);

        // http body params 와 http headers 를 가진 엔티티
        HttpEntity<MultiValueMap<String, String>> naverTokenRequest = new HttpEntity<>(params, headers);

        // reqUrl로 Http 요청, POST 방식
        ResponseEntity<String> response = restTemplate.exchange(reqUrl,
                HttpMethod.POST,
                naverTokenRequest,
                String.class);

        String responseBody = response.getBody();
        JsonObject asJsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        String accessToken = asJsonObject.get("access_token").getAsString();
        return accessToken;
    }
    public String getUserInfo(String accessToken) {
        String reqUrl = "https://openapi.naver.com/v1/nid/me";

        RestTemplate restTemplate = new RestTemplate();
        try {
            // HttpHeader 오브젝트
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);

            HttpEntity<MultiValueMap<String, String>> naverProfileRequest = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    reqUrl,
                    HttpMethod.POST,
                    naverProfileRequest,
                    String.class
            );

            System.out.println("response = " + response);

            String responseBody = response.getBody();
            JsonObject asJsonObject = JsonParser.parseString(responseBody).getAsJsonObject();

            // response 안의 객체 가져오기
            JsonObject responseObj = asJsonObject.getAsJsonObject("response");
            String uid = responseObj.get("id").getAsString();

            return uid;

        } catch (Exception e) {
            log.error("네이버 사용자 정보 조회 실패: {}", e.getMessage());
            throw new RuntimeException("네이버 사용자 정보 조회 실패", e);
        }
    }

}

