package com.jammering.akkipy.domain.userLogin;

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
public class UserLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userLoginId;
    @Column
    private Provider provider;
    private String providerId;
    private String email;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public static UserLogin toEntity(User savedUser, String providerId, Provider provider) {
        return UserLogin.builder()
                .user(savedUser)
                .providerId(providerId)
                .provider(provider)
                .email("email.ex")
                .build();
    }
}
