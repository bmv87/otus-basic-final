package ru.otus.services.models.user;

import ru.otus.repository.entities.RoleEnum;

public class LoginResponseVM {
    private String username;
    private String token;
    private RoleEnum role;

    public LoginResponseVM(String username, String token, RoleEnum role) {
        this.username = username;
        this.token = token;
        this.role = role;
    }

    public RoleEnum getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}
