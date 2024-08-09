package ru.otus.services.models.user;

public class LoginResponseVM {
    private String username;
    private String token;

    public LoginResponseVM(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}
