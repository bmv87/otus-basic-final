package ru.otus.repository.specifications;

import ru.otus.repository.entities.Grade;
import ru.otus.repository.entities.Note;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.UUID;

public class GradeSpecifications {

    public static Specification<Grade> getByNoteIdUserIdSpecification(UUID noteId, UUID userId) {
        return (root, query, cb) -> {
            var dataPredicates = new ArrayList<Predicate>();
            dataPredicates.add(cb.equal(root.get(Grade.Fields.noteId), noteId));
            dataPredicates.add(cb.equal(root.get(Grade.Fields.userId), userId));
            return cb.and(dataPredicates.toArray(new Predicate[dataPredicates.size()]));
        };
    }
}
