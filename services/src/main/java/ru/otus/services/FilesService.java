package ru.otus.services;

import ru.otus.repository.DBContext;
import ru.otus.repository.entities.FileInfo;
import ru.otus.services.exceptions.ForbiddenException;
import ru.otus.services.exceptions.NotFoundException;
import ru.otus.services.exceptions.ResponseException;
import ru.otus.services.helpers.ApplicationPropertiesHelper;
import ru.otus.services.models.file.DownloadFileVM;
import ru.otus.services.models.file.FileItemVM;
import ru.otus.services.models.file.FileUploadingResponseVM;
import ru.otus.services.models.user.UserVM;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class FilesService implements AutoCloseable {
    private final DBContext dbContext;
    private final String FILES_STORE_DIRECTORY = ApplicationPropertiesHelper.tryGet(ApplicationPropertiesHelper.FILES_STORE_DIRECTORY_PARAM_NANE, String.class);

    public FilesService() {
        dbContext = new DBContext();
    }

    public FileUploadingResponseVM upload(UserVM user, byte[] content, String fileName, String contentType) {
        var newFile = new FileInfo();
        var flleNameParts = fileName.split("\\.");

        newFile.setFileId(UUID.randomUUID());
        newFile.setUserId(user.getUserId());
        newFile.setFileName(fileName);
        newFile.setContentType(contentType);
        newFile.setExtension(flleNameParts[flleNameParts.length - 1].toLowerCase());

        try {

            Path uploadPath = Paths.get(FILES_STORE_DIRECTORY);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            var fileNameForFS = newFile.getFileId() + "." + newFile.getExtension();
            Path uploadFilePath = Paths.get(FILES_STORE_DIRECTORY, fileNameForFS);
            if (Files.exists(uploadFilePath)) {
                throw new IOException("File already exists: " + uploadFilePath);
            }
            try (RandomAccessFile stream = new RandomAccessFile(uploadFilePath.toString(), "rw");
                 FileChannel channel = stream.getChannel();) {

                ByteBuffer buffer = ByteBuffer.allocate(content.length);
                buffer.put(content);
                buffer.flip();
                channel.write(buffer);
            }

// Files.write(uploadFilePath, content);
            newFile.setSourcePath(uploadFilePath.toString());
            try {
                dbContext.save(newFile);
                dbContext.saveContext();
            } catch (Exception e) {
                Files.delete(uploadFilePath);
                throw e;
            }

        } catch (IOException e) {
            throw new ResponseException("Ошибка сохранения файла в файловой системе.", e);
        }

        return new FileUploadingResponseVM(newFile.getFileId(), newFile.getFileName());
    }

    public void deleteFile(UserVM user, UUID fileId) {
        dbContext.beginTransaction();
        var file = tryGetFile(user, fileId);
        Path uploadFilePath = Paths.get(file.getSourcePath());

        dbContext.delete(file);
        dbContext.saveContext();
        if (uploadFilePath.toFile().exists()) {
            try {
                Files.delete(uploadFilePath);
            } catch (IOException e) {
                throw new ResponseException("Ошибка удаления файла из файловой системы", e);
            }
        }
    }

    public DownloadFileVM downloadFile(UserVM user, UUID fileId) {
        try {
            var file = tryGetFile(user, fileId);
            Path uploadFilePath = Paths.get(file.getSourcePath());
            var content = Files.readAllBytes(uploadFilePath);
            return new DownloadFileVM(file.getFileName(), content, content.length, file.getContentType());
        } catch (IOException e) {
            throw new ResponseException(e);
        }
    }

    public List<FileItemVM> getUserFiles(UserVM user, UUID userId) {
        if (user == null || !user.getUserId().equals(userId)) {
            throw new ForbiddenException("Доступ запрещен.");
        }
        return dbContext.getFilesByUserId(userId).stream()
                .map(f -> new FileItemVM(f.getFileId(), f.getFileName(), f.getExtension()))
                .toList();
    }

    private FileInfo tryGetFile(UserVM user, UUID fileId) {
        var file = dbContext.getFileById(fileId);
        if (file == null) {
            throw new NotFoundException("Файл не найден");
        }
        if (!file.getUserId().equals(user.getUserId())) {
            throw new ForbiddenException("Доступ запрещен");
        }
        return file;
    }

    @Override
    public void close() throws Exception {
        dbContext.close();
    }
}
