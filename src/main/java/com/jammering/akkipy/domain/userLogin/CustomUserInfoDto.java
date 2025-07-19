package com.jammering.akkipy.domain.userLogin;

import com.jammering.akkipy.domain.user.Role;
import com.jammering.akkipy.domain.user.User;
import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class CustomUserInfoDto {
    private Long userId;
    private String uid;
    private boolean isBanned;
    private Role role;
    private String password;
    private boolean isDeleted;
    private Provider provider;

    public static CustomUserInfoDto toGuestDto(String uid, String provider, String authority) {
        return CustomUserInfoDto.builder()
                .userId(0L)
                .uid(uid)
                .isBanned(false)
                .role(Role.valueOf(authority))
                .password("")
                .provider(Provider.valueOf(provider))
                .isDeleted(false)
                .build();
    }
    public static CustomUserInfoDto toUserDto(User user) {
        return CustomUserInfoDto.builder()
                .userId(user.getUserId())
                .uid("")
                .isBanned(false)
                .role(Role.USER)
                .password("")
                .isDeleted(false)
                .build();
    }
}
