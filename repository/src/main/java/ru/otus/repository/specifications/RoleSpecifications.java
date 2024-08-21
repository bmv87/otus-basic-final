package ru.otus.repository.specifications;

import ru.otus.repository.entities.GenderEnum;
import ru.otus.repository.entities.Role;
import ru.otus.repository.entities.RoleEnum;
import ru.otus.repository.entities.User;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.UUID;

public class RoleSpecifications {
    public static Specification<Role> getByNameSpecification(RoleEnum role) {
        return (root, query, cb) -> cb.equal(root.get(Role.Fields.name), role);
    }
}
