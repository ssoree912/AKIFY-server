package com.jammering.akkipy.domain.userLogin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {
    Optional<UserLogin> findByProviderAndProviderId(Provider provider, String providerId);
}
