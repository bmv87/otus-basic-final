package ru.otus.services.models.subscription;

import java.util.UUID;

public class SubscriptionVM {
    private UUID userId;

    public SubscriptionVM() {
    }

    public SubscriptionVM(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
