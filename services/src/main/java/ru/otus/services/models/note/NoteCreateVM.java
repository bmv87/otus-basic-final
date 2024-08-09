package ru.otus.services.models.note;

public class NoteCreateVM {
    private String content;

    public NoteCreateVM() {
    }

    public NoteCreateVM(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
