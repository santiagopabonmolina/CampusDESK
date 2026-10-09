package com.spring.modules.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.management.relation.Role;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
@NoArgsConstructor
@Getter @Setter
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String first_name;

    @Column(nullable = false, length = 150)
    private String last_name;

    @Column(nullable = false, length = 225)
    private String password;

    @Column(nullable = false)
    private Boolean active;

    @Column(updatable = false)
    private LocalDateTime register_date;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role",
            joinColumns = @JoinColumn(name = "id"),
            inverseJoinColumns = @JoinColumn(name = "id")
    )
    private Set<Role> roles = new HashSet<>(); //

    @PrePersist
    protected void onCreate() {
        this.register_date = LocalDateTime.now(); //
    }


}
