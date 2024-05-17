package com.app.repository.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String username;
    private String password;
    @Column(name = "is_enabled")
    private Boolean isEnabled;
    @Column(name = "is_account_no_expired")
    private Boolean isAccountNoExpired;
    @Column(name = "is_account_no_locked")
    private Boolean isAccountNoLocked;
    @Column(name = "is_credential_no_expired")
    private Boolean isCredentialNoExpired;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RolEntity> rolEntities = new HashSet<>();
}
