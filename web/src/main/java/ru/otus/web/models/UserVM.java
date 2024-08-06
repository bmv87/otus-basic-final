package ru.otus.web.models;

import java.util.UUID;

public class UserVM {
    private UUID id;
    private String login;
    private String email;
    private String displayName;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UserVM(UUID id, String login, String email, String displayName) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "UserVM{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
