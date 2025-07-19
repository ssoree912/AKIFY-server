package com.jammering.akkipy.domain.userLogin;

import com.jammering.akkipy.domain.base.BaseTimeEntity;
import com.jammering.akkipy.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "user_logins")
public class UserLogin extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userLoginId;

    @Column
    @Enumerated(EnumType.STRING)
    private Provider provider;
    private String providerUid;
    private String email;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public static UserLogin toEntity(User savedUser, String provierUid, Provider provider) {
        return UserLogin.builder()
                .user(savedUser)
                .providerUid(provierUid)
                .provider(provider)
                .email("email.ex")
                .build();
    }
}
