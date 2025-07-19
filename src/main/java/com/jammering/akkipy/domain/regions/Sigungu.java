package com.jammering.akkipy.domain.regions;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "si_gun_gu")
@Getter
@Setter
public class Sigungu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long siGunGuId;
    @Column
    private String name;
    @ManyToOne
    @JoinColumn(name = "si_do_id", nullable = false)
    private Sido sido;
}
