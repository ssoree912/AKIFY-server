package com.jammering.akkipy.controller.dto.response;

import com.jammering.akkipy.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
public class UserResponse {
    @Getter
    @Setter
    private static class UserInfo{
        private Long userId;;
        private String nickname;
        private String name;
        private String phone;

        public UserInfo(User user) {
            this.userId = user.getUserId();
            this.nickname = user.getNickname();
            this.name = user.getName();
            this.phone = user.getPhone();
        }

    }
    @Getter
    @Setter
    @Builder
    public static class UserTokenInfo{
        private UserInfo User;
        private TokenResponse.ToKenInfo token;
    }

    public static UserTokenInfo toUserTokenInfo(User user, TokenResponse.ToKenInfo token) {
        UserInfo userInfo = new UserInfo(user);
        return UserTokenInfo.builder()
                .User(userInfo)
                .token(token)
                .build();
    }
}
