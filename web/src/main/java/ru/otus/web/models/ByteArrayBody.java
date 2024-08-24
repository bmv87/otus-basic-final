package ru.otus.web.models;

public interface ByteArrayBody {
    String getFileName();

    String getContentType();

    long getSize();

    byte[] getContent();

    void setFileName(String fileName);

    void setContentType(String contentType);

    void setSize(long size);

    void setContent(byte[] content);
}
