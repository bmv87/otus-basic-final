package ru.otus.services.models.note;

import ru.otus.services.models.grade.GradeInfoVM;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NoteInfoVM {

    private UUID noteId;
    private UUID parentNoteId;
    private UUID userId;
    private String username;
    private String content;
    private LocalDateTime created;
    private List<GradeInfoVM> grades = new ArrayList<>();
    private List<NoteInfoVM> notes = new ArrayList<>();

    public NoteInfoVM() {
    }

    public NoteInfoVM(UUID noteId, UUID parentNoteId, UUID userId, String username, String content, LocalDateTime created) {
        this.noteId = noteId;
        this.parentNoteId = parentNoteId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.created = created;
    }

    public NoteInfoVM(UUID noteId, UUID parentNoteId, UUID userId, String username, String content, LocalDateTime created, List<GradeInfoVM> grades, List<NoteInfoVM> notes) {
        this.noteId = noteId;
        this.parentNoteId = parentNoteId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.created = created;
        this.grades = grades;
        this.notes = notes;
    }

    public UUID getNoteId() {
        return noteId;
    }

    public void setNoteId(UUID noteId) {
        this.noteId = noteId;
    }

    public UUID getParentNoteId() {
        return parentNoteId;
    }

    public void setParentNoteId(UUID parentNoteId) {
        this.parentNoteId = parentNoteId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public List<GradeInfoVM> getGrades() {
        return grades;
    }

    public void setGrades(List<GradeInfoVM> grades) {
        this.grades = grades;
    }

    public List<NoteInfoVM> getNotes() {
        return notes;
    }

    public void setNotes(List<NoteInfoVM> notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteInfoVM that = (NoteInfoVM) o;
        return Objects.equals(noteId, that.noteId) && Objects.equals(parentNoteId, that.parentNoteId) && Objects.equals(userId, that.userId) && Objects.equals(username, that.username) && Objects.equals(content, that.content) && Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noteId, parentNoteId, userId, username, content, created);
    }
}
