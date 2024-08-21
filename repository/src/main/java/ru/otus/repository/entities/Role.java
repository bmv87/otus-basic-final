package ru.otus.repository.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@FieldNameConstants
public class Role {

    @Id
    @Column(name = "role_id", columnDefinition = "uuid", updatable = false)
    private UUID roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, updatable = true, unique = true)
    private RoleEnum name;

    @Column(name = "description", nullable = true)
    private String description;

    public Role(UUID roleId, RoleEnum name, String description) {
        this.roleId = roleId;
        this.name = name;
        this.description = description;
    }
}
