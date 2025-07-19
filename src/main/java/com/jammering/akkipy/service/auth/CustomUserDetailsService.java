package com.jammering.akkipy.service.auth;

import com.jammering.akkipy.domain.user.User;
import com.jammering.akkipy.domain.user.UserRepository;
import com.jammering.akkipy.domain.userLogin.CustomUserDetails;
import com.jammering.akkipy.domain.userLogin.CustomUserInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
            // 회원가입 시에는 User 객체를 생성하고 저장하는 로직을 추가할 수 있습니다.
            Long id = Long.parseLong(userId);
            // 예: userRepository.save(new User(userId, "defaultPassword", "defaultNickname"));
            User user = userRepository.findById(Long.parseLong(userId)).orElseThrow(() -> new UsernameNotFoundException("해당하는 유저가 없다"));
            CustomUserInfoDto userInfoDto = CustomUserInfoDto.toUserDto(user);
            return new CustomUserDetails(userInfoDto);


    }
}
