package ru.otus.services;

import ru.otus.repository.UsersRepository;
import ru.otus.repository.entities.Grade;
import ru.otus.repository.entities.Note;
import ru.otus.repository.entities.RoleEnum;
import ru.otus.services.exceptions.ForbiddenException;
import ru.otus.services.exceptions.NotFoundException;
import ru.otus.services.exceptions.UnprocessableEntityException;
import ru.otus.services.models.grade.GradeCreateVM;
import ru.otus.services.models.grade.GradeInfoVM;
import ru.otus.services.models.note.NoteCreateVM;
import ru.otus.services.models.note.NoteInfoVM;
import ru.otus.services.models.user.UserVM;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NotesService implements AutoCloseable {
    private final UsersRepository usersRepository;

    public NotesService() {
        usersRepository = new UsersRepository();
    }

    public List<NoteInfoVM> getUserNotes(UserVM currentUser, UUID userId) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (userId == null) {
            throw new UnprocessableEntityException("Идентификатор пользователя не указан.");
        }

        var user = usersRepository.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (!currentUser.getUserId().equals(userId)) {
            var subscriptions = usersRepository.getUserSubscriptions(currentUser.getUserId());
            if (subscriptions.stream().noneMatch(s -> s.getSubscriberId().equals(currentUser.getUserId()) && s.getBlogOwnerId().equals(userId))) {
                throw new ForbiddenException("Вы не подписаны на этого пользователя");
            }
        }
        var notes = usersRepository.getNotesByUserId(userId);

        return notes.stream().map(this::mapToVM).toList();
    }

    public UUID createNote(UserVM currentUser, NoteCreateVM model) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (model.getContent() == null || model.getContent().isBlank()) {
            throw new UnprocessableEntityException("Заметка не может быть создана без содержания.");
        }
        var newNote = new Note();
        newNote.setNoteId(UUID.randomUUID());
        newNote.setContent(model.getContent());
        newNote.setCreated(LocalDateTime.now());
        newNote.setUserId(currentUser.getUserId());
        usersRepository.saveNote(newNote);
        usersRepository.saveContext();
        return newNote.getNoteId();
    }

    public UUID createCommentForNote(UserVM currentUser, UUID noteId, NoteCreateVM model) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (model.getContent() == null || model.getContent().isBlank()) {
            throw new UnprocessableEntityException("Заметка не может быть создана без содержания.");
        }
        if (noteId == null) {
            throw new UnprocessableEntityException("Идентификатор заметки не указан.");
        }
        usersRepository.beginTransaction();
        var note = usersRepository.getNoteById(noteId);
        if (note == null) {
            throw new UnprocessableEntityException("Заметка не найдена.");
        }
        var newNote = new Note();
        newNote.setNoteId(UUID.randomUUID());
        newNote.setParentNoteId(noteId);
        newNote.setContent(model.getContent());
        newNote.setCreated(LocalDateTime.now());
        newNote.setUserId(currentUser.getUserId());
        usersRepository.saveNote(newNote);
        usersRepository.saveContext();
        return newNote.getNoteId();
    }

    public void editeNote(UserVM currentUser, UUID noteId, NoteCreateVM model) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (model.getContent() == null || model.getContent().isBlank()) {
            throw new UnprocessableEntityException("Заметка не может быть создана без содержания.");
        }
        if (noteId == null) {
            throw new UnprocessableEntityException("Идентификатор заметки не указан.");
        }
        usersRepository.beginTransaction();
        var note = usersRepository.getNoteById(noteId);
        if (note == null) {
            throw new UnprocessableEntityException("Заметка не найдена.");
        }
        if (!note.getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Доступ запрещен.");
        }

        note.setContent(model.getContent());
        usersRepository.updateNote(note);
        usersRepository.saveContext();
    }

    public void addGrade(UserVM currentUser, UUID noteId, GradeCreateVM model) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        if (model.getGradeType() == null) {
            throw new UnprocessableEntityException("Некорректно указан тип реакции на заметку.");
        }
        if (noteId == null) {
            throw new UnprocessableEntityException("Идентификатор заметки не указан.");
        }
        usersRepository.beginTransaction();
        var note = usersRepository.getNoteById(noteId);
        if (note == null) {
            throw new UnprocessableEntityException("Заметка не найдена.");
        }
        if (note.getUserId().equals(currentUser.getUserId())) {
            throw new ForbiddenException("Оценка собственных заметок запрещена.");
        }
        var grade = usersRepository.getGrade(noteId, currentUser.getUserId());
        if (grade == null) {
            grade = new Grade();
            grade.setGradeId(UUID.randomUUID());
            grade.setNoteId(noteId);
            grade.setGradeType(model.getGradeType());
            grade.setUserId(currentUser.getUserId());
            usersRepository.saveGrade(grade);
        } else {
            grade.setGradeType(model.getGradeType());
            usersRepository.updateGrade(grade);
        }

        usersRepository.saveContext();
    }

    public void deleteNote(UserVM currentUser, UUID noteId) {
        if (currentUser == null) {
            throw new ForbiddenException("Доступ запрещен.");
        }

        if (noteId == null) {
            throw new UnprocessableEntityException("Идентификатор заметки не указан.");
        }
        usersRepository.beginTransaction();
        var note = usersRepository.getNoteById(noteId);
        if (note == null) {
            throw new UnprocessableEntityException("Заметка не найдена.");
        }
        if (!note.getUserId().equals(currentUser.getUserId()) && currentUser.getRole() != RoleEnum.ADMIN) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        usersRepository.deleteNote(note);
        usersRepository.saveContext();
    }

    private NoteInfoVM mapToVM(Note note) {
        var noteVM = new NoteInfoVM();
        noteVM.setNoteId(note.getNoteId());
        noteVM.setParentNoteId(note.getParentNoteId());
        noteVM.setUserId(note.getUserId());
        noteVM.setUsername(note.getCreator().getUsername());
        noteVM.setCreated(note.getCreated());
        noteVM.setContent(note.getContent());
        noteVM.setGrades(note.getGrades().stream().map(g -> new GradeInfoVM(g.getGradeId(), g.getNoteId(), g.getUserId(), g.getUser().getUsername(), g.getGradeType())).toList());
        List<NoteInfoVM> notes = new ArrayList<>();
        if (!note.getNotes().isEmpty()) {
            for (Note n : note.getNotes()) {
                notes.add(mapToVM(n));
            }
            noteVM.setNotes(notes);
        }
        return noteVM;
    }

    @Override
    public void close() throws Exception {
        usersRepository.close();
    }
}
