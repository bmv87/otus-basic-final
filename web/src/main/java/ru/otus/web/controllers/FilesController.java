package ru.otus.web.controllers;

import ru.otus.services.FilesService;
import ru.otus.services.models.file.FileItemVM;
import ru.otus.services.models.file.FileUploadingResponseVM;
import ru.otus.services.models.user.UserVM;
import ru.otus.web.http.HttpMethod;
import ru.otus.web.models.FileContent;
import ru.otus.web.routing.*;
import ru.otus.web.security.Autentificated;
import ru.otus.web.security.Principal;

import java.util.List;
import java.util.UUID;

@Controller
public class FilesController {
    private final FilesService filesService;

    public FilesController() {
        filesService = new FilesService();
    }

    @RoutePath(method = HttpMethod.POST, path = "files")
    @Autentificated
    public FileUploadingResponseVM upload(@Principal UserVM user, @File FileContent model) {
        return filesService.upload(user, model.getContent(), model.getFileName(), model.getContentType());
    }

    @RoutePath(method = HttpMethod.DELETE, path = "files/{id}")
    @Autentificated
    public void deleteFile(@Principal UserVM user, @PathVariable(name = "id") UUID id) {
        filesService.deleteFile(user, id);
    }

    @RoutePath(method = HttpMethod.GET, path = "files/{id}")
    @Autentificated
    @File
    public FileContent downloadFile(@Principal UserVM user, @PathVariable(name = "id") UUID id) {
        var result = filesService.downloadFile(user, id);

        return new FileContent(result.getFileName(), result.getContentType(), result.getSize(), result.getContent());
    }

    @RoutePath(method = HttpMethod.GET, path = "users/{id}/files")
    @Autentificated
    public List<FileItemVM> getUserFiles(@Principal UserVM user, @PathVariable(name = "id") UUID id) {
        return filesService.getUserFiles(user, id);
    }

}
