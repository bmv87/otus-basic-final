package ru.otus.repository.entities;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.util.UUID;


@Entity
@Table(name = "grades",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"note_id", "user_id"},
                        name = "uq_grades_note_id_user_id")})
@Data
@FieldNameConstants
@NoArgsConstructor
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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_grades_users_user_id"))
    private User user;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_grades_notes_note_id"))
    private Note note;


    public Grade(UUID gradeId, UUID noteId, UUID userId, GradeEnum gradeType) {
        this.gradeId = gradeId;
        this.noteId = noteId;
        this.userId = userId;
        this.gradeType = gradeType;
    }
}
