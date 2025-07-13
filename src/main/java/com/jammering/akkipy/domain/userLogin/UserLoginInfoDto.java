package com.jammering.akkipy.domain.userLogin;

import com.jammering.akkipy.domain.user.Role;
import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class UserLoginInfoDto {
    Long userId;
    String uid;
    boolean isBanned;
    Role role;
    String password;
    Authority authority;
    boolean isDeleted;

}
