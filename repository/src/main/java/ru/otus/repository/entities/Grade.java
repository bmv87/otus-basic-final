package ru.otus.repository.entities;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "grades",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"note_id", "user_id"},
                        name = "uq_grades_note_id_user_id")})
public class Grade {
    @Id
    @Column(name = "grade_id", columnDefinition = "uuid", updatable = false)
    private UUID gradeId;

    @Column(name = "note_id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID noteId;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_type", nullable = false, updatable = true, unique = false)
    private GradeEnum gradeType;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_grades_users_user_id"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_grades_notes_note_id"))
    private Note note;

    public Grade() {
    }

    public Grade(UUID gradeId, UUID noteId, UUID userId, GradeEnum gradeType) {
        this.gradeId = gradeId;
        this.noteId = noteId;
        this.userId = userId;
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

    public GradeEnum getGradeType() {
        return gradeType;
    }

    public void setGradeType(GradeEnum gradeType) {
        this.gradeType = gradeType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grade grade = (Grade) o;
        return Objects.equals(gradeId, grade.gradeId) && Objects.equals(noteId, grade.noteId) && Objects.equals(userId, grade.userId) && gradeType == grade.gradeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gradeId, noteId, userId, gradeType);
    }
}
