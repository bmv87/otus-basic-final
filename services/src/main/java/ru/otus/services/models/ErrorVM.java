package ru.otus.services.models;

import ru.otus.services.http.StatusCode;

public class ErrorVM {
    private final int code;
    private final StatusCode status;
    private final String message;
    private final String description;

    public ErrorVM(int code, StatusCode status, String message, String description) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public StatusCode getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
