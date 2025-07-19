package com.jammering.akkipy.domain.regions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SidoRepository extends JpaRepository<Sido, Long> {
    Optional<Sido> findByName(String name);
    // Additional query methods can be defined here if needed
}
