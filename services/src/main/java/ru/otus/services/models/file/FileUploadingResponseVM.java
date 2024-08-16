package ru.otus.services.models.file;

import java.util.UUID;

public class FileUploadingResponseVM {
    private UUID fileId;
    private String fileName;

    public FileUploadingResponseVM() {
    }

    public FileUploadingResponseVM(UUID fileId, String fileName) {
        this.fileId = fileId;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }
}
