package com.jammering.akkipy.service.auth.strategy;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.jammering.akkipy.config.jwt.JwtProvider;
import com.jammering.akkipy.controller.dto.request.UserRequest;
import com.jammering.akkipy.controller.dto.response.TokenResponse;
import com.jammering.akkipy.domain.userLogin.Provider;
import com.jammering.akkipy.domain.userLogin.UserLoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
public class FirebaseAuthService extends AbstractOAuthService {
    private final UserLoginRepository userLoginRepository;
    private final JwtProvider jwtProvider;

    @Value("${firebase.api-key}")
    private String apiKey;

    @Override
    public boolean supports(Provider provider) {
        return provider == Provider.FIREBASE || provider == Provider.LOCAL;
    }

    @Override
    public TokenResponse.ToKenInfo signIn(UserRequest.Auth auth, Provider provider) throws Exception {
        String uid;
        if (provider == Provider.LOCAL) {
            uid = signInWithEmailAndPassword(auth.getEmail(), auth.getPassword());
        } else {
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(auth.getToken());
            uid = firebaseToken.getUid();
        }
        return userLoginRepository.findByProviderAndProviderId( provider, uid)
                .map(userLogin -> jwtProvider.generateToken(userLogin.getUser().getUserId(), userLogin.getUser().getRole())) // 가입되어 있음 → 정식 토큰
                .orElseGet(() -> jwtProvider.generateTemporaryToken(uid, provider));
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
        try {
            String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + apiKey;
            String body = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                    email, password
            );
            HttpEntity<String> request = new HttpEntity<>(body, defaultHeaders(MediaType.APPLICATION_JSON));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return objectMapper.readTree(response.getBody()).get("localId").asText();
            } else {
                throw new RuntimeException("Firebase 로그인 응답이 올바르지 않습니다.");
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody.contains("INVALID_LOGIN_CREDENTIALS")) {
                    throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
                }
            }
            throw new RuntimeException("Firebase 로그인 중 오류 발생: " + e.getMessage(), e);
        }
    }

}