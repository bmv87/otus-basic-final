package ru.otus.services.models.user;

import ru.otus.repository.entities.GenderEnum;

public class UserEditVM {
    private String username;
    private Integer age;
    private GenderEnum gender;

    public UserEditVM() {
    }

    public UserEditVM(String username, Integer age, GenderEnum gender) {
        this.username = username;
        this.age = age;
        this.gender = gender;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
}
