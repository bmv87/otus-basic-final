package ru.otus.web.controllers;

import ru.otus.services.NotesService;
import ru.otus.services.models.grade.GradeCreateVM;
import ru.otus.services.models.note.NoteCreateVM;
import ru.otus.services.models.note.NoteInfoVM;
import ru.otus.services.models.user.UserVM;
import ru.otus.web.http.HttpMethod;
import ru.otus.web.routing.Controller;
import ru.otus.web.routing.FromBody;
import ru.otus.web.routing.PathVariable;
import ru.otus.web.routing.RoutePath;
import ru.otus.web.security.Autentificated;
import ru.otus.web.security.Principal;

import java.util.List;
import java.util.UUID;

@Controller
public class NotesController implements AutoCloseable {
    private final NotesService notesService;

    public NotesController() {
        notesService = new NotesService();
    }

    @RoutePath(method = HttpMethod.POST, path = "notes")
    @Autentificated
    public UUID createNote(@Principal UserVM user, @FromBody NoteCreateVM model) {
        return notesService.createNote(user, model);
    }

    @RoutePath(method = HttpMethod.POST, path = "notes/{id}")
    @Autentificated
    public UUID createCommentForNote(@Principal UserVM user, @PathVariable(name = "id") UUID id, @FromBody NoteCreateVM model) {
        return notesService.createCommentForNote(user, id, model);
    }

    @RoutePath(method = HttpMethod.PUT, path = "notes/{id}")
    @Autentificated
    public void editeNote(@Principal UserVM user, @PathVariable(name = "id") UUID id, @FromBody NoteCreateVM model) {
        notesService.editeNote(user, id, model);
    }

    @RoutePath(method = HttpMethod.DELETE, path = "notes/{id}")
    @Autentificated
    public void deleteNote(@Principal UserVM user, @PathVariable(name = "id") UUID id) {
        notesService.deleteNote(user, id);
    }

    @RoutePath(method = HttpMethod.GET, path = "users/{id}/notes")
    @Autentificated
    public List<NoteInfoVM> getUserNotes(@Principal UserVM user, @PathVariable(name = "id") UUID id) {
        return notesService.getUserNotes(user, id);
    }

    @RoutePath(method = HttpMethod.POST, path = "notes/{id}/grades")
    @Autentificated
    public void addGrade(@Principal UserVM user, @PathVariable(name = "id") UUID id, @FromBody GradeCreateVM model) {
        notesService.addGrade(user, id, model);
    }


    @Override
    public void close() throws Exception {
        notesService.close();
    }
}
