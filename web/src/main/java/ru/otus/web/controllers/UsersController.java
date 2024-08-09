package ru.otus.web.controllers;

import ru.otus.services.UsersService;
import ru.otus.services.models.subscription.SubscriptionInfoVM;
import ru.otus.services.models.subscription.SubscriptionVM;
import ru.otus.services.models.user.*;
import ru.otus.web.http.HttpMethod;
import ru.otus.web.routing.Controller;
import ru.otus.web.routing.FromBody;
import ru.otus.web.routing.PathVariable;
import ru.otus.web.routing.RoutePath;
import ru.otus.web.security.Autentificated;
import ru.otus.web.security.Principal;

import java.util.List;
import java.util.UUID;

@Controller
public class UsersController implements AutoCloseable {
    private final UsersService usersService;

    public UsersController() {
        usersService = new UsersService();
    }

    @RoutePath(method = HttpMethod.PUT, path = "users/login")
    public LoginResponseVM login(@FromBody LoginRequestVM model) {
        return usersService.login(model);
    }

    @RoutePath(method = HttpMethod.POST, path = "users")
    public UUID registration(@FromBody UserCreateVM model) {
        return usersService.registration(model);
    }

    @RoutePath(method = HttpMethod.GET, path = "users/current")
    @Autentificated
    public UserVM getCurrentUser(@Principal UserVM user) {
        return user;
    }

    @RoutePath(method = HttpMethod.GET, path = "users")
    @Autentificated
    public List<UserShortVM> getUsers(@Principal UserVM user /*, @ParamVariable(name = "name") String name*/) {
        return usersService.getUsers(user);
    }

    @RoutePath(method = HttpMethod.GET, path = "users/{id}")
    @Autentificated
    public UserVM getUserById(@Principal UserVM user, @PathVariable(name = "id") UUID id) {
        return usersService.getUserById(user, id);
    }

    @RoutePath(method = HttpMethod.GET, path = "users/{id}/subscriptions")
    @Autentificated
    public List<SubscriptionInfoVM> getSubscriptions(@Principal UserVM user, @PathVariable(name = "id") UUID id) {
        return usersService.getSubscriptions(user, id);
    }

    @RoutePath(method = HttpMethod.POST, path = "subscriptions")
    @Autentificated
    public void addSubscription(@Principal UserVM user, @FromBody SubscriptionVM model) {
        usersService.addSubscription(user, model);
    }

    @RoutePath(method = HttpMethod.DELETE, path = "users/{id}/subscriptions")
    @Autentificated
    public void addSubscription(@Principal UserVM user, @PathVariable(name = "id") UUID id) {
        usersService.deleteSubscription(user, id);
    }

    @RoutePath(method = HttpMethod.PUT, path = "users/{id}/status")
    @Autentificated
    public void getUserById(@Principal UserVM user, @PathVariable(name = "id") UUID id, @FromBody LockStatusVM model) {
        usersService.changeLockStatus(user, id, model);
    }

    @Override
    public void close() throws Exception {
        usersService.close();
    }
}
