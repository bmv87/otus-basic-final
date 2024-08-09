package ru.otus.services.models.grade;

import ru.otus.repository.entities.GradeEnum;

import java.util.Objects;
import java.util.UUID;

public class GradeInfoVM {
    private UUID gradeId;
    private UUID noteId;
    private UUID userId;
    private String username;
    private GradeEnum gradeType;

    public GradeInfoVM() {
    }

    public GradeInfoVM(UUID gradeId, UUID noteId, UUID userId, String username, GradeEnum gradeType) {
        this.gradeId = gradeId;
        this.noteId = noteId;
        this.userId = userId;
        this.username = username;
        this.gradeType = gradeType;
    }

    public UUID getGradeId() {
        return gradeId;
    }

    public void setGradeId(UUID gradeId) {
        this.gradeId = gradeId;
    }

    public UUID getNoteId() {
        return noteId;
    }

    public void setNoteId(UUID noteId) {
        this.noteId = noteId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public GradeEnum getGradeType() {
        return gradeType;
    }

    public void setGradeType(GradeEnum gradeType) {
        this.gradeType = gradeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradeInfoVM that = (GradeInfoVM) o;
        return Objects.equals(gradeId, that.gradeId) && Objects.equals(noteId, that.noteId) && Objects.equals(userId, that.userId) && Objects.equals(username, that.username) && gradeType == that.gradeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gradeId, noteId, userId, username, gradeType);
    }
}
