package ru.otus.services;

import ru.otus.repository.DBRepository;
import ru.otus.repository.entities.GenderEnum;
import ru.otus.repository.entities.RoleEnum;
import ru.otus.repository.entities.Subscription;
import ru.otus.repository.entities.User;
import ru.otus.services.cache.Cachable;
import ru.otus.services.cache.CacheManager;
import ru.otus.services.cache.CacheNames;
import ru.otus.services.exceptions.*;
import ru.otus.services.models.Pagination;
import ru.otus.services.models.subscription.SubscriptionInfoVM;
import ru.otus.services.models.subscription.SubscriptionVM;
import ru.otus.services.models.user.*;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UsersService implements AutoCloseable {
    private final DBRepository repository;
    private final CacheManager cacheManager;

    public UsersService() {
        repository = new DBRepository();
        cacheManager = CacheManager.getInstance();
    }

    private UserVM mapUserToVM(User user) {
        return new UserVM(
                user.getUserId(),
                user.getLogin(),
                user.getUsername(),
                user.getRole().getName(),
                user.getAge(),
                user.getGender(),
                user.getLocked());
    }

    public LoginResponseVM login(LoginRequestVM model) {
        var user = repository.getUserByLogin(model.getLogin());
        if (user == null || !user.getPassword().equals(model.getPassword())) {
            throw new UnauthorizedException("Неправильный логин или пароль.");
        }
        try {
            var token = EncryptionUtility.encrypt(user.getLogin() + user.getPassword());
            Cachable currentUser = mapUserToVM(user);

            cacheManager.addToCache(CacheNames.AUTH, token, currentUser);
            return new LoginResponseVM(user.getUsername(), token, user.getRole().getName());
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            throw new ResponseException("Ошибка во время генерации токена.", e);
        }
    }

    public void editUser(UserVM currentUser, UserEditVM model) {
        if (model == null || model.getUsername() == null || model.getUsername().isBlank()) {
            throw new UnprocessableEntityException("Имя пользователя не задано");
        }
        repository.beginTransaction();
        var user = repository.getUserById(currentUser.getUserId());
        user.setAge(model.getAge());
        user.setGender(model.getGender());
        user.setUsername(model.getUsername());
        repository.update(user);
        repository.saveContext();
    }

    public UUID registration(UserCreateVM model) {
        if (model.getLogin().trim().length() < 3 ||
                model.getPassword().trim().length() < 6 ||
                model.getUsername().trim().isEmpty()) {
            throw new UnprocessableEntityException("Логин 3+ символа, Пароль 6+ символов, Имя пользователя 1+ символ");
        }
        if (isLoginAlreadyExist(model.getLogin())) {
            throw new UnprocessableEntityException("Указанный логин уже занят");
        }

        var roleId = repository.getRoleId(RoleEnum.USER);
        var newUser = new User(UUID.randomUUID(), roleId, model.getLogin(), model.getUsername(), model.getPassword());
        repository.save(newUser);
        repository.saveContext();
        return newUser.getUserId();
    }

    public Pagination<UserShortVM> getUsers(
            UserVM currentUser,
            String username,
            String gender,
            Integer age,
            Integer page,
            Integer limit) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        GenderEnum genderValue = null;
        if (gender != null && !gender.isBlank()) {
            try {
                genderValue = GenderEnum.valueOf(gender.toUpperCase());
            } catch (Exception e) {
                throw new UnprocessableEntityException("Недопустимое значение поля gender " + gender);
            }
        }
        var subscriptions = repository.getUserSubscriptions(currentUser.getUserId());
        var users = repository.getUsers(
                currentUser.getRole() != RoleEnum.ADMIN,
                currentUser.getUserId(),
                username,
                genderValue,
                age,
                page,
                limit);

        var usersList = users.getItems().stream().map(u ->
                        new UserShortVM(
                                u.getUserId(),
                                u.getUsername(),
                                u.getLocked(),
                                subscriptions.stream().anyMatch(s -> s.getSubscriberId().equals(currentUser.getUserId()) && s.getBlogOwnerId().equals(u.getUserId()))))
                .toList();
        return new Pagination<>(users.getTotalCount(), usersList);
    }

    public UserVM getUserById(UserVM currentUser, UUID userId) {
        if (userId == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (!currentUser.getUserId().equals(userId)) {
            var subscriptions = repository.getUserSubscriptions(currentUser.getUserId());
            if (subscriptions.stream()
                    .noneMatch(s -> s.getSubscriberId().equals(currentUser.getUserId()) && s.getBlogOwnerId().equals(userId))) {
                throw new ForbiddenException("Вы не подписаны на этого пользователя");
            }
        }
        var user = repository.getUserById(userId);
        return mapUserToVM(user);
    }

    public void changeLockStatus(UserVM currentUser, UUID userId, LockStatusVM model) {
        if (userId == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }
        if (currentUser == null || currentUser.getRole() != RoleEnum.ADMIN) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (currentUser.getUserId().equals(userId)) {
            throw new ForbiddenException("Вы не можете заблокировать свой личный кабинет.");
        }

        var user = repository.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (model.isLocked()) {
            try {
                cacheManager.removeFromCache(CacheNames.AUTH, EncryptionUtility.encrypt(user.getLogin() + user.getPassword()), UserVM.class);
            } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                throw new ResponseException("Ошибка во время генерации токена.", e);
            }
        }
        user.setLocked(model.isLocked());
        repository.update(user);
        repository.saveContext();
    }

    public void addSubscription(UserVM currentUser, SubscriptionVM model) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (model.getUserId() == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }
        repository.beginTransaction();
        var user = repository.getUserById(model.getUserId());
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        var subscription = repository.getSubscriptionById(currentUser.getUserId(), model.getUserId());
        if (subscription != null) {
            throw new UnprocessableEntityException(String.format("Вы уже подписаны на пользователя %s.", user.getUsername()));
        }
        subscription = new Subscription(currentUser.getUserId(), model.getUserId(), LocalDateTime.now());
        repository.save(subscription);
        repository.saveContext();
    }

    public void deleteSubscription(UserVM currentUser, UUID blogOwnerId) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (blogOwnerId == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }

        var user = repository.getUserById(blogOwnerId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        var subscription = repository.getSubscriptionById(currentUser.getUserId(), blogOwnerId);
        if (subscription == null) {
            throw new NotFoundException(String.format("Вы не подписаны на пользователя %s.", user.getUsername()));
        }
        subscription = new Subscription(currentUser.getUserId(), blogOwnerId, LocalDateTime.now());
        repository.delete(subscription);
        repository.saveContext();
    }

    public List<SubscriptionInfoVM> getSubscriptions(UserVM currentUser, UUID subscriberId) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (subscriberId == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }

        var user = repository.getUserById(subscriberId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!currentUser.getUserId().equals(subscriberId)) {
            var subscriptions = repository.getUserSubscriptions(currentUser.getUserId());
            if (subscriptions.stream()
                    .noneMatch(s -> s.getSubscriberId().equals(currentUser.getUserId()) && s.getBlogOwnerId().equals(subscriberId))) {
                throw new ForbiddenException("Вы не подписаны на этого пользователя");
            }
        }
        var subscriptions = repository.getUserSubscriptions(subscriberId);
        return subscriptions.stream()
                .map(s -> new SubscriptionInfoVM(s.getBlogOwnerId(), s.getBlogOwner().getUsername()))
                .toList();
    }

    private boolean isLoginAlreadyExist(String login) {
        var user = repository.getUserByLogin(login);
        return user != null;
    }

    public boolean isAdmin(String login) {
        if (login == null || login.isBlank()) {
            return false;
        }
        return repository.isInRole(login, RoleEnum.ADMIN);
    }

    @Override
    public void close() throws Exception {
        repository.close();
    }

}
