package ru.otus.repository.entities;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "files")
@Data
@FieldNameConstants
@NoArgsConstructor
public class FileInfo {
    @Id
    @Column(name = "grade_id", columnDefinition = "uuid", updatable = false)
    private UUID fileId;
    @Column(name = "user_id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID userId;
    @Column(name = "file_name", nullable = false, updatable = false, unique = false)
    private String fileName;
    @Column(name = "extension", nullable = false, updatable = false, unique = false)
    private String extension;
    @Column(name = "content_type", nullable = false, updatable = false, unique = false)
    private String contentType;
    @Column(name = "source_path", nullable = false, updatable = true, unique = false)
    private String sourcePath;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_files_users_user_id"))
    private User creator;

    public FileInfo(UUID fileId, UUID userId, String fileName, String extension, String contentType, String sourcePath) {
        this.fileId = fileId;
        this.userId = userId;
        this.fileName = fileName;
        this.extension = extension;
        this.contentType = contentType;
        this.sourcePath = sourcePath;
    }
}
