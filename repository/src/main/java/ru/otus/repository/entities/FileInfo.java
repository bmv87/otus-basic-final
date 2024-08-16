package ru.otus.repository.entities;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "files")
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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_files_users_user_id"))
    private User creator;

    public FileInfo() {
    }

    public FileInfo(UUID fileId, UUID userId, String fileName, String extension, String contentType, String sourcePath) {
        this.fileId = fileId;
        this.userId = userId;
        this.fileName = fileName;
        this.extension = extension;
        this.contentType = contentType;
        this.sourcePath = sourcePath;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return Objects.equals(fileId, fileInfo.fileId) && Objects.equals(userId, fileInfo.userId) && Objects.equals(fileName, fileInfo.fileName) && Objects.equals(contentType, fileInfo.contentType) && Objects.equals(sourcePath, fileInfo.sourcePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, userId, fileName, contentType, sourcePath);
    }
}
