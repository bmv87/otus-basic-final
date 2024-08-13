package ru.otus.repository.entities;


import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
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
    private Boolean locked;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "role_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_users_roles_roleId"))
    private Role role;

    public User() {
        this.locked = false;
    }

    public User(UUID userId, UUID roleId, String login, String username, String password) {
        this.userId = userId;
        this.roleId = roleId;
        this.login = login;
        this.username = username;
        this.password = password;
        this.locked = false;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public GenderEnum getGender() {
        return gender;
    }

    public void setGender(GenderEnum gender) {
        this.gender = gender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return age == user.age && Objects.equals(userId, user.userId) && Objects.equals(roleId, user.roleId) && Objects.equals(login, user.login) && Objects.equals(username, user.username) && gender == user.gender && Objects.equals(password, user.password) && Objects.equals(locked, user.locked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId, login, username, age, gender, password, locked);
    }
}

