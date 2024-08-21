package ru.otus.services;

import ru.otus.repository.DBContext;
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
    private final DBContext dbContext;
    private final CacheManager cacheManager;

    public UsersService() {
        dbContext = new DBContext();
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
        var user = dbContext.getUserByLogin(model.getLogin());
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
        dbContext.beginTransaction();
        var user = dbContext.getUserById(currentUser.getUserId());
        user.setAge(model.getAge());
        user.setGender(model.getGender());
        user.setUsername(model.getUsername());
        dbContext.update(user);
        dbContext.saveContext();
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

        var roleId = dbContext.getRoleId(RoleEnum.USER);
        var newUser = new User(UUID.randomUUID(), roleId, model.getLogin(), model.getUsername(), model.getPassword());
        dbContext.save(newUser);
        dbContext.saveContext();
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
        var subscriptions = dbContext.getUserSubscriptions(currentUser.getUserId());
        var users = dbContext.getUsers(
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
            var subscription = dbContext.getSubscriptionById(currentUser.getUserId(), userId);
            if (subscription == null) {
                throw new ForbiddenException("Вы не подписаны на этого пользователя");
            }
        }
        var user = dbContext.getUserById(userId);
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

        var user = dbContext.getUserById(userId);
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
        dbContext.update(user);
        dbContext.saveContext();
    }

    public void addSubscription(UserVM currentUser, SubscriptionVM model) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (model.getUserId() == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }
        dbContext.beginTransaction();
        var user = dbContext.getUserById(model.getUserId());
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        var subscription = dbContext.getSubscriptionById(currentUser.getUserId(), model.getUserId());
        if (subscription != null) {
            throw new UnprocessableEntityException(String.format("Вы уже подписаны на пользователя %s.", user.getUsername()));
        }
        subscription = new Subscription(currentUser.getUserId(), model.getUserId(), LocalDateTime.now());
        dbContext.save(subscription);
        dbContext.saveContext();
    }

    public void deleteSubscription(UserVM currentUser, UUID blogOwnerId) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (blogOwnerId == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }

        var user = dbContext.getUserById(blogOwnerId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        var subscription = dbContext.getSubscriptionById(currentUser.getUserId(), blogOwnerId);
        if (subscription == null) {
            throw new NotFoundException(String.format("Вы не подписаны на пользователя %s.", user.getUsername()));
        }
        subscription = new Subscription(currentUser.getUserId(), blogOwnerId, LocalDateTime.now());
        dbContext.delete(subscription);
        dbContext.saveContext();
    }

    public List<SubscriptionInfoVM> getSubscriptions(UserVM currentUser, UUID subscriberId) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (subscriberId == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }

        var user = dbContext.getUserById(subscriberId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!currentUser.getUserId().equals(subscriberId)) {
            var subscriptions = dbContext.getUserSubscriptions(currentUser.getUserId());
            if (subscriptions.stream()
                    .noneMatch(s -> s.getSubscriberId().equals(currentUser.getUserId()) && s.getBlogOwnerId().equals(subscriberId))) {
                throw new ForbiddenException("Вы не подписаны на этого пользователя");
            }
        }
        var subscriptions = dbContext.getUserSubscriptions(subscriberId);
        return subscriptions.stream()
                .map(s -> new SubscriptionInfoVM(s.getBlogOwnerId(), s.getBlogOwner().getUsername()))
                .toList();
    }

    private boolean isLoginAlreadyExist(String login) {
        var user = dbContext.getUserByLogin(login);
        return user != null;
    }

    @Override
    public void close() throws Exception {
        dbContext.close();
    }

}
