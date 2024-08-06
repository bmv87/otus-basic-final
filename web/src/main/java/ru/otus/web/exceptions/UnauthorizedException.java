package ru.otus.web.exceptions;

import ru.otus.web.http.StatusCode;

public class UnauthorizedException extends ResponseException {
    public UnauthorizedException(String message) {
        super(message);
        code = StatusCode.UNAUTHORIZED;
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
        code = StatusCode.UNAUTHORIZED;
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
        code = StatusCode.UNAUTHORIZED;
    }
}
