package com.jammering.akkipy.domain.regions;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "si_do")
@Getter
@Setter
public class Sido {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long siDoId;

    @Column
    private String name;

    @OneToMany(mappedBy = "sido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sigungu> sigungus = new ArrayList<>();

    public Sido(String name) {
        this.name = name;
    }
}
