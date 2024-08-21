package ru.otus.repository.specifications;

import ru.otus.repository.entities.GenderEnum;
import ru.otus.repository.entities.Role;
import ru.otus.repository.entities.RoleEnum;
import ru.otus.repository.entities.User;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.UUID;

public class UserSpecifications {
    public static Specification<User> getUsersListSpecification(boolean onlyActive,
                                                                UUID excludeUserId,
                                                                String username,
                                                                GenderEnum gender,
                                                                Integer age) {
        return (root, query, cb) -> {
            var dataPredicates = new ArrayList<Predicate>();
            if (excludeUserId != null) {
                dataPredicates.add(cb.notEqual(root.get(User.Fields.userId), excludeUserId));
            }
            if (username != null && !username.isBlank()) {
                dataPredicates.add(cb.like(cb.lower(root.get(User.Fields.username)), "%" + username.toLowerCase() + "%"));
            }
            if (gender != null) {
                dataPredicates.add(cb.equal(root.get(User.Fields.gender), gender));
            }
            if (age != null) {
                dataPredicates.add(cb.equal(root.get(User.Fields.age), age));
            }
            if (onlyActive) {
                dataPredicates.add(cb.equal(root.get(User.Fields.locked), false));
            }

            return cb.and(dataPredicates.toArray(new Predicate[dataPredicates.size()]));
        };
    }

    public static Specification<User> getInRoleSpecification(String login, RoleEnum role) {
        return (root, query, cb) -> {
            var dataPredicates = new ArrayList<Predicate>();
            Join<User, Role> joinedRole = root.join(User.Fields.role);
            dataPredicates.add(cb.equal(cb.lower(root.get(User.Fields.login)), login.toLowerCase()));
            dataPredicates.add(cb.equal(root.get(Role.Fields.name), role));
            return cb.and(dataPredicates.toArray(new Predicate[dataPredicates.size()]));
        };
    }

    public static Specification<User> getLoginSpecification(String login) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get(User.Fields.login)), login.toLowerCase());
    }
}
