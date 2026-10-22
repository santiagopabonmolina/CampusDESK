package com.spring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<Ticket> tickets = new HashSet<>();

    public Tag(String nombre) {
        this.nombre = nombre;
    }
}