package com.jammering.akkipy.domain.user;

import com.jammering.akkipy.domain.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "users")
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column
    private String nickname;
    private String name;
    private String phone;
    private boolean isVerified;
    @Enumerated(EnumType.STRING)
    private Role role;

    public static User toEntity(String nickname){
        return User.builder()
                .nickname(nickname)
                .name("미입력")
                .phone("미입력")
                .isVerified(false)
                .role(Role.USER)
                .build();
    }
}
