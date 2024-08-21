package ru.otus.repository.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@IdClass(SubscriptionId.class)
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@FieldNameConstants
public class Subscription {
    @Id
    @Column(name = "subscriber_id", columnDefinition = "uuid", updatable = false)
    private UUID subscriberId;

    @Id
    @Column(name = "blog_owner_id",columnDefinition = "uuid", updatable = false)
    private UUID blogOwnerId;

    @Column(name = "date", nullable = false, updatable = false, unique = false)
    private LocalDateTime date;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subscriber_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_users_subscriberId"))
    private User subscriber;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "blog_owner_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_users_blogOwnerId"))
    private User blogOwner;

    public Subscription(UUID subscriberId, UUID blogOwnerId, LocalDateTime date) {
        this.subscriberId = subscriberId;
        this.blogOwnerId = blogOwnerId;
        this.date = date;
    }
}
