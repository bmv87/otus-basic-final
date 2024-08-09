package ru.otus.services.models.user;

public class UserCreateVM {

    private String login;
    private String username;
    private String password;

    public UserCreateVM() {
    }

    public UserCreateVM(String login, String username, String password) {
        this.login = login;
        this.username = username;
        this.password = password;
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
}
