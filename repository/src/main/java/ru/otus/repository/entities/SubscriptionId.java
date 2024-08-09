package ru.otus.repository.entities;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class SubscriptionId  implements Serializable {

    private UUID subscriberId;
    private UUID blogOwnerId;

    public SubscriptionId() {
    }

    public SubscriptionId(UUID subscriberId, UUID blogOwnerId) {
        this.subscriberId = subscriberId;
        this.blogOwnerId = blogOwnerId;
    }

    public UUID getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(UUID subscriberId) {
        this.subscriberId = subscriberId;
    }

    public UUID getBlogOwnerId() {
        return blogOwnerId;
    }

    public void setBlogOwnerId(UUID blogOwnerId) {
        this.blogOwnerId = blogOwnerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionId that = (SubscriptionId) o;
        return Objects.equals(subscriberId, that.subscriberId) && Objects.equals(blogOwnerId, that.blogOwnerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriberId, blogOwnerId);
    }
}
