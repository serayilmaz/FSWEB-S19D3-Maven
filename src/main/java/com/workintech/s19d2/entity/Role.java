package com.workintech.s19d2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles", schema = "bank")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String authority; // ROLE_USER, ROLE_ADMIN

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<Member> members = new HashSet<>();
}
