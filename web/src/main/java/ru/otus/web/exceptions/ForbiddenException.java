package ru.otus.web.exceptions;

import ru.otus.web.http.StatusCode;

public class ForbiddenException extends ResponseException {
    public ForbiddenException(String message) {
        super(message);
        code = StatusCode.FORBIDDEN;
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
        code = StatusCode.FORBIDDEN;
    }

    public ForbiddenException(Throwable cause) {
        super(cause);
        code = StatusCode.FORBIDDEN;
    }
}
