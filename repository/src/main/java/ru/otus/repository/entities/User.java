package ru.otus.repository.entities;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@FieldNameConstants
public class User {
    @Id
    @Column(name = "user_id", columnDefinition = "uuid", updatable = false)
    private UUID userId;

    @Column(name = "role_id", columnDefinition = "uuid", nullable = false, updatable = true)
    private UUID roleId;

    @Column(name = "login", nullable = false, updatable = false, unique = true)
    private String login;

    @Column(name = "username", nullable = false, updatable = true, unique = true)
    private String username;

    @Column(name = "age", nullable = true, updatable = true, unique = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = true, updatable = true, unique = false)
    private GenderEnum gender;

    @Column(name = "password", nullable = false, updatable = true)
    private String password;

    @Column(name = "locked", nullable = false, updatable = true)
    private Boolean locked = false;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "role_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_users_roles_roleId"))
    private Role role;

    public User(UUID userId, UUID roleId, String login, String username, String password) {
        this.userId = userId;
        this.roleId = roleId;
        this.login = login;
        this.username = username;
        this.password = password;
        this.locked = false;
    }
}

