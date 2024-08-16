package ru.otus.web.models;

public class FileContent implements ByteArrayBody {

    private String fileName;
    private String contentType;
    private long size;
    private byte[] content;

    public FileContent() {
    }

    public FileContent(String fileName, String contentType, long size, byte[] content) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.content = content;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
