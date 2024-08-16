package ru.otus.services.models.file;

public class DownloadFileVM {
    private String fileName;
    private String contentType;
    private long size;
    private byte[] content;

    public DownloadFileVM() {
    }

    public DownloadFileVM(String fileName, byte[] content, long size, String contentType) {
        this.fileName = fileName;
        this.content = content;
        this.size = size;
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
