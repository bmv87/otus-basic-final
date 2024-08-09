package ru.otus.repository.entities;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "roles")
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

    public Role() {
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public RoleEnum getName() {
        return name;
    }

    public void setName(RoleEnum name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(roleId, role.roleId) && name == role.name && Objects.equals(description, role.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, name, description);
    }
}
