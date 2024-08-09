package ru.otus.services.exceptions;

import ru.otus.services.http.StatusCode;

public class NotFoundException extends ResponseException {
    public NotFoundException(String message) {
        super(message);
        code = StatusCode.NOT_FOUND;
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
        code = StatusCode.NOT_FOUND;
    }

    public NotFoundException(Throwable cause) {
        super(cause);
        code = StatusCode.NOT_FOUND;
    }
}
