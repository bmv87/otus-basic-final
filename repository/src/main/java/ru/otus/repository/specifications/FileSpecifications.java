package ru.otus.repository.specifications;

import ru.otus.repository.entities.FileInfo;
import ru.otus.repository.entities.Role;
import ru.otus.repository.entities.RoleEnum;

import java.io.File;
import java.util.UUID;

public class FileSpecifications {
    public static Specification<FileInfo> getByUserIdSpecification(UUID id) {
        return (root, query, cb) -> cb.equal(root.get(FileInfo.Fields.userId), id);
    }
}
