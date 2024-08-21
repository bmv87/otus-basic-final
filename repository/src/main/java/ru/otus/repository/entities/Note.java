package ru.otus.repository.entities;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "notes")
@Data
@FieldNameConstants
@NoArgsConstructor
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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_notes_users_user_id"))
    private User creator;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "parent_note_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_notes_notes_note_id"))
    private Note parentNote;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE})
    private Set<Grade> grades = new HashSet<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "parentNote", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    private Set<Note> notes = new HashSet<>();

    public Note(UUID noteId, UUID parentNoteId, UUID userId, String content, LocalDateTime created) {
        this.noteId = noteId;
        this.parentNoteId = parentNoteId;
        this.userId = userId;
        this.content = content;
        this.created = created;
    }
}
