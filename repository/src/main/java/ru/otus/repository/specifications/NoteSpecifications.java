package ru.otus.repository.specifications;

import ru.otus.repository.entities.*;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.UUID;

public class NoteSpecifications {

    public static Specification<Note> getByUserIdSpecification(UUID id) {
        return (root, query, cb) -> {
            var dataPredicates = new ArrayList<Predicate>();
            dataPredicates.add(cb.equal(root.get(Note.Fields.userId), id));
            dataPredicates.add(cb.isNull(root.get(Note.Fields.parentNoteId)));
            return cb.and(dataPredicates.toArray(new Predicate[dataPredicates.size()]));
        };
    }
}
