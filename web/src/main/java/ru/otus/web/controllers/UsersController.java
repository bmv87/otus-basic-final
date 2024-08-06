package ru.otus.web.controllers;

import ru.otus.web.exceptions.ResponseException;
import ru.otus.web.http.HttpMethod;
import ru.otus.web.models.UserVM;
import ru.otus.web.routing.*;
import ru.otus.web.security.Autentificated;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
public class UsersController {

    public UsersController() {
    }

    @RoutePath(method = HttpMethod.GET, path = "users")
    @Autentificated
    public List<String> getUsers(@ParamVariable(name = "name") String name) {
        try {
            System.out.println(name);
            var list = Arrays.asList("user1", "user2", "user3");
            return list;
        } catch (Exception e) {
            throw new ResponseException("Ошибка выполнения метода контроллера getUsers", e);
        }
    }

    @RoutePath(method = HttpMethod.GET, path = "users/{id}")
    @Autentificated
    public UserVM getUserById(@PathVariable(name = "id") String id) {
        try {
            System.out.println(id);
            var uId1 = UUID.fromString("hhh");
            var uId = UUID.fromString(id);
            return new UserVM(uId, "user1", "mail@mail.ru", "Tom");
        } catch (Exception e) {
            throw new ResponseException("Ошибка выполнения метода контроллера getUserById", e);
        }
    }


    @RoutePath(method = HttpMethod.POST, path = "users")
    @Autentificated
    public UUID addUser(@FromBody UserVM user) {
        try {
            System.out.println(user);
            return user.getId();
        } catch (Exception e) {
            throw new ResponseException("Ошибка выполнения метода контроллера addUser", e);
        }
    }
}
