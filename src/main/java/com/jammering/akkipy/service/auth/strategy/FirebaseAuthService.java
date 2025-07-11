package com.jammering.akkipy.service.auth.strategy;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.jammering.akkipy.controller.request.UserRequest;
import com.jammering.akkipy.domain.userLogin.Provider;
import com.jammering.akkipy.service.auth.strategy.AbstractOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FirebaseAuthService extends AbstractOAuthService {

    @Value("${firebase.api-key}")
    private String apiKey;

    @Override
    public boolean supports(Provider provider) {
        return provider == Provider.FIREBASE || provider == Provider.LOCAL;
    }

    @Override
    public String signIn(UserRequest.Auth auth, Provider provider) throws Exception {
        if (provider == Provider.LOCAL) {
            return signInWithEmailAndPassword(auth.getEmail(), auth.getPassword());
        } else {
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(auth.getToken());
            return firebaseToken.getUid();
        }
    }

    public void createAccountInFirebase(String email, String password) throws FirebaseAuthException {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setEmailVerified(true)
                .setPassword(password)
                .setDisabled(false);
        FirebaseAuth.getInstance().createUser(request);
    }

    private String signInWithEmailAndPassword(String email, String password) throws Exception {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;
        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}", email, password);
        HttpEntity<String> request = new HttpEntity<>(body, defaultHeaders(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return objectMapper.readTree(response.getBody()).get("localId").asText();
        }
        throw new Exception("로그인 실패: " + response.getBody());
    }
}