package ru.otus.services.models.file;

import java.util.UUID;

public class FileItemVM {
    private UUID fileId;
    private String fileName;
    private String extension;

    public FileItemVM() {
    }

    public FileItemVM(UUID fileId, String fileName, String extension) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.extension = extension;
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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
