package ru.otus.services.models.subscription;

import java.util.UUID;

public class SubscriptionInfoVM {
    private UUID blogOwnerId;
    private String blogOwnerName;

    public SubscriptionInfoVM() {
    }

    public SubscriptionInfoVM(UUID blogOwnerId, String blogOwnerName) {
        this.blogOwnerId = blogOwnerId;
        this.blogOwnerName = blogOwnerName;
    }

    public UUID getBlogOwnerId() {
        return blogOwnerId;
    }

    public void setBlogOwnerId(UUID blogOwnerId) {
        this.blogOwnerId = blogOwnerId;
    }

    public String getBlogOwnerName() {
        return blogOwnerName;
    }

    public void setBlogOwnerName(String blogOwnerName) {
        this.blogOwnerName = blogOwnerName;
    }
}
