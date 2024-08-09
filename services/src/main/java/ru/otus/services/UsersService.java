package ru.otus.services;

import ru.otus.repository.UsersRepository;
import ru.otus.repository.entities.RoleEnum;
import ru.otus.repository.entities.Subscription;
import ru.otus.repository.entities.User;
import ru.otus.services.cache.Cachable;
import ru.otus.services.cache.CacheManager;
import ru.otus.services.cache.CacheNames;
import ru.otus.services.exceptions.*;
import ru.otus.services.models.subscription.SubscriptionInfoVM;
import ru.otus.services.models.subscription.SubscriptionVM;
import ru.otus.services.models.user.*;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UsersService implements AutoCloseable {
    private final UsersRepository usersRepository;
    private final CacheManager cacheManager;

    public UsersService() {
        usersRepository = new UsersRepository();
        cacheManager = CacheManager.getInstance();
    }

    public LoginResponseVM login(LoginRequestVM model) {
        var user = usersRepository.getUserByLogin(model.getLogin());
        if (user == null || !user.getPassword().equals(model.getPassword())) {
            throw new UnauthorizedException("Неправильный логин или пароль.");
        }
        try {
            var token = EncryptionUtility.encrypt(user.getLogin() + user.getPassword());
            Cachable currentUser = new UserVM(user.getUserId(), user.getLogin(), user.getUsername(), user.getRole().getName(), user.getLocked());

            cacheManager.addToCache(CacheNames.AUTH, token, currentUser);
            return new LoginResponseVM(user.getUsername(), token);
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            throw new ResponseException("Ошибка во время генерации токена.", e);
        }
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

        var roleId = usersRepository.getRoleId(RoleEnum.USER);
        var newUser = new User(UUID.randomUUID(), roleId, model.getLogin(), model.getUsername(), model.getPassword());
        usersRepository.createUser(newUser);
        usersRepository.saveContext();
        return newUser.getUserId();
    }

    public List<UserShortVM> getUsers(UserVM currentUser) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }

        var subscriptions = usersRepository.getUserSubscriptions(currentUser.getUserId());
        var users = usersRepository.getUsers(currentUser.getRole() != RoleEnum.ADMIN);

        return users.stream().map(u ->
                        new UserShortVM(
                                u.getUserId(),
                                u.getUsername(),
                                u.getLocked(),
                                subscriptions.stream().anyMatch(s -> s.getSubscriberId().equals(currentUser.getUserId()) && s.getBlogOwnerId().equals(u.getUserId()))))
                .toList();
    }

    public UserVM getUserById(UserVM currentUser, UUID userId) {
        if (userId == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }
        if (currentUser == null || (currentUser.getRole() != RoleEnum.ADMIN && !currentUser.getUserId().equals(userId))) {
            throw new ForbiddenException("Доступ запрещен.");
        }

        var user = usersRepository.getUserById(userId);
        return new UserVM(user.getUserId(), user.getLogin(), user.getUsername(), user.getRole().getName(), user.getLocked());
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

        var user = usersRepository.getUserById(userId);
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
        usersRepository.updateUser(user);
        usersRepository.saveContext();
    }

    public void addSubscription(UserVM currentUser, SubscriptionVM model) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (model.getUserId() == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }

        var user = usersRepository.getUserById(model.getUserId());
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        var subscription = usersRepository.getSubscriptionById(currentUser.getUserId(), model.getUserId());
        if (subscription == null) {
            throw new UnprocessableEntityException(String.format("Вы уже подписаны на пользователя %s.", user.getUsername()));
        }
        subscription = new Subscription(currentUser.getUserId(), model.getUserId(), LocalDateTime.now());
        usersRepository.saveSubscription(subscription);
        usersRepository.saveContext();
    }

    public void deleteSubscription(UserVM currentUser, UUID blogOwnerId) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (blogOwnerId == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }

        var user = usersRepository.getUserById(blogOwnerId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        var subscription = usersRepository.getSubscriptionById(currentUser.getUserId(), blogOwnerId);
        if (subscription == null) {
            throw new NotFoundException(String.format("Вы не подписаны на пользователя %s.", user.getUsername()));
        }
        subscription = new Subscription(currentUser.getUserId(), blogOwnerId, LocalDateTime.now());
        usersRepository.deleteSubscription(subscription);
        usersRepository.saveContext();
    }

    public List<SubscriptionInfoVM> getSubscriptions(UserVM currentUser, UUID subscriberId) {
        if (currentUser == null ||
                (currentUser.getRole() != RoleEnum.ADMIN && !currentUser.getUserId().equals(subscriberId))) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (subscriberId == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }

        var user = usersRepository.getUserById(subscriberId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        var subscriptions = usersRepository.getUserSubscriptions(subscriberId);
        return subscriptions.stream().map(s -> new SubscriptionInfoVM(s.getBlogOwnerId(), s.getBlogOwner().getUsername())).toList();
    }

    private boolean isLoginAlreadyExist(String login) {
        var user = usersRepository.getUserByLogin(login);
        return user != null;
    }

    public boolean isAdmin(String login) {
        if (login == null || login.isBlank()) {
            return false;
        }
        return usersRepository.isInRole(login, RoleEnum.ADMIN);
    }

    @Override
    public void close() throws Exception {
        usersRepository.close();
    }

}
