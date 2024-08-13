package ru.otus.repository.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "notes")
public class Note {
    @Id
    @Column(name = "note_id", columnDefinition = "uuid", updatable = false)
    private UUID noteId;
    @Column(name = "parent_note_id", columnDefinition = "uuid", nullable = true, updatable = false)
    private UUID parentNoteId;
    @Column(name = "user_id", nullable = false, updatable = false, unique = false)
    private UUID userId;
    @Column(name = "content", nullable = false, updatable = true, unique = false)
    private String content;
    @Column(name = "created", nullable = false, updatable = false, unique = false)
    private LocalDateTime created;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_notes_users_user_id"))
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "parent_note_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_notes_notes_note_id"))
    private Note parentNote;

    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
    private Set<Grade> grades = new HashSet<>();

    @OneToMany(mappedBy = "parentNote", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private Set<Note> notes = new HashSet<>();

    public Note() {
    }

    public Note(UUID noteId, UUID parentNoteId, UUID userId, String content, LocalDateTime created, User creator, Note parentNote, Set<Note> notes, Set<Grade> grades) {
        this.noteId = noteId;
        this.parentNoteId = parentNoteId;
        this.userId = userId;
        this.content = content;
        this.created = created;
        this.creator = creator;
        this.parentNote = parentNote;
        this.notes = notes;
        this.grades = grades;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Note getParentNote() {
        return parentNote;
    }

    public void setParentNote(Note parentNote) {
        this.parentNote = parentNote;
    }

    public Set<Grade> getGrades() {
        return grades;
    }

    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }

    public Set<Note> getNotes() {
        return notes;
    }

    public void setNotes(Set<Note> notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return Objects.equals(noteId, note.noteId) && Objects.equals(parentNoteId, note.parentNoteId) && Objects.equals(userId, note.userId) && Objects.equals(content, note.content) && Objects.equals(created, note.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noteId, parentNoteId, userId, content, created);
    }
}
