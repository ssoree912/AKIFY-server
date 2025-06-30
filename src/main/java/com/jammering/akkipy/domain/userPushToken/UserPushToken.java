package com.jammering.akkipy.domain.userPushToken;

import com.jammering.akkipy.domain.base.BaseTimeEntity;
import com.jammering.akkipy.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserPushToken extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userPushTokenId;

    @Column
    private String deviceToken;
    private String platform;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
