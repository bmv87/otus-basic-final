package ru.otus.services.models.user;

import ru.otus.repository.entities.RoleEnum;
import ru.otus.services.cache.Cachable;

import java.util.UUID;

public class UserVM implements Cachable {
    private UUID userId;
    private String login;
    private String username;
    private RoleEnum role;
    private Boolean locked;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public UserVM(UUID userId, String login, String username, RoleEnum role, Boolean locked) {
        this.userId = userId;
        this.login = login;
        this.username = username;
        this.role = role;
        this.locked = locked;
    }

    @Override
    public String toString() {
        return "UserVM{" +
                "userId=" + userId +
                ", login='" + login + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", locked=" + locked +
                '}';
    }
}
