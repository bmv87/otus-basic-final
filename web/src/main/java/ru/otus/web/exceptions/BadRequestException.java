package ru.otus.web.exceptions;

import ru.otus.web.http.StatusCode;

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
