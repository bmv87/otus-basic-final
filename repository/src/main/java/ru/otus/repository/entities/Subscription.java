package ru.otus.repository.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@IdClass(SubscriptionId.class)
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @Column(name = "subscriber_id", columnDefinition = "uuid", updatable = false)
    private UUID subscriberId;

    @Id
    @Column(name = "blog_owner_id",columnDefinition = "uuid", updatable = false)
    private UUID blogOwnerId;

    @Column(name = "date", nullable = false, updatable = false, unique = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subscriber_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_users_subscriberId"))
    private User subscriber;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "blog_owner_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_users_blogOwnerId"))
    private User blogOwner;

    public Subscription() {
    }

    public Subscription(UUID subscriberId, UUID blogOwnerId, LocalDateTime date) {
        this.subscriberId = subscriberId;
        this.blogOwnerId = blogOwnerId;
        this.date = date;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public User getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(User subscriber) {
        this.subscriber = subscriber;
    }

    public User getBlogOwner() {
        return blogOwner;
    }

    public void setBlogOwner(User blogOwner) {
        this.blogOwner = blogOwner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(subscriberId, that.subscriberId) && Objects.equals(blogOwnerId, that.blogOwnerId) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriberId, blogOwnerId, date);
    }
}
