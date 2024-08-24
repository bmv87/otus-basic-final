package ru.otus.services.exceptions;

import ru.otus.services.http.StatusCode;

public class BadRequestException extends ResponseException {
    public BadRequestException(String message) {
        super(message);
        code = StatusCode.BAD_REQUEST;
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
        code = StatusCode.BAD_REQUEST;
    }

    public BadRequestException(Throwable cause) {
        super(cause);
        code = StatusCode.BAD_REQUEST;
    }
}
