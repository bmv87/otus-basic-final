package ru.otus.services.models.user;

import java.util.Objects;
import java.util.UUID;

public class UserShortVM {
    private UUID userId;
    private String username;
    private Boolean locked;
    private Boolean subscribed;

    public UserShortVM() {
    }

    public UserShortVM(UUID userId, String username, Boolean locked, Boolean subscribed) {
        this.userId = userId;
        this.username = username;
        this.locked = locked;
        this.subscribed = subscribed;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserShortVM that = (UserShortVM) o;
        return Objects.equals(userId, that.userId) && Objects.equals(username, that.username) && Objects.equals(locked, that.locked) && Objects.equals(subscribed, that.subscribed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, locked, subscribed);
    }
}
