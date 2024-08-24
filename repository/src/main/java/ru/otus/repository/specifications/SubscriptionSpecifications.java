package ru.otus.repository.specifications;

import ru.otus.repository.entities.Subscription;

import java.util.UUID;

public class SubscriptionSpecifications {
    public static Specification<Subscription> getBySubscriberIdSpecification(UUID subscriberId) {
        return (root, query, cb) -> cb.equal(root.get(Subscription.Fields.subscriberId), subscriberId);
    }
}
