package com.jammering.akkipy.service;

import com.jammering.akkipy.config.jwt.JwtProvider;
import com.jammering.akkipy.controller.dto.response.TokenResponse;
import com.jammering.akkipy.controller.dto.response.UserResponse;
import com.jammering.akkipy.domain.user.User;
import com.jammering.akkipy.domain.user.UserRepository;
import com.jammering.akkipy.domain.userLogin.Provider;
import com.jammering.akkipy.domain.userLogin.UserLogin;
import com.jammering.akkipy.domain.userLogin.UserLoginRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserLoginRepository userLoginRepository;
    private final JwtProvider jwtProvider;
    @Transactional
    public UserResponse.UserTokenInfo registerUser(String nickname, String providerId, Provider provider) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        User savedUser = userRepository.save(User.toEntity(nickname));
        UserLogin userLogin = UserLogin.toEntity(savedUser, providerId, provider);
        UserLogin savedUserLogin = userLoginRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> userLoginRepository.save(userLogin));
        TokenResponse.ToKenInfo toKenInfo = jwtProvider.generateToken(savedUser.getUserId(), savedUser.getRole());
        return UserResponse.toUserTokenInfo(savedUser, toKenInfo);


    }
}
