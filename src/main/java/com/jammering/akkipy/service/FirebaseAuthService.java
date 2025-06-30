package com.jammering.akkipy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseAuthService {
    @Value("${firebase.api-key}")
    private String FIREBASE_API_KEY;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void createAccountInFirebase(String email,String password) throws FirebaseAuthException{
        CreateRequest request = new CreateRequest()
                .setEmail(email)
                .setEmailVerified(true)
                .setPassword(password)
                .setDisabled(false);
        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
        log.info(">>>>>>>>>" + userRecord.getUid());

    }
    public String signInWithEmailAndPassword(String email, String password) throws Exception {
        String apiKey = FIREBASE_API_KEY;
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;

        // 요청 body
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", email, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String uid = jsonNode.get("localId").asText();
            return uid;
        } else {
            throw new Exception("로그인 실패: " + response.getBody());
        }
    }
    public Boolean checkAuth(String uid)  {
        try {
            FirebaseAuth.getInstance().getUserByEmail(uid);
            return true;
        }catch (FirebaseAuthException e) {
            return false;
        }
    }
}