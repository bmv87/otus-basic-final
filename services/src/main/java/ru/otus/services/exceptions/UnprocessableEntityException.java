package ru.otus.services.exceptions;

import ru.otus.services.http.StatusCode;

public class UnprocessableEntityException extends ResponseException {
    public UnprocessableEntityException(String message) {
        super(message);
        code = StatusCode.UNPROCESSABLE_ENTITY;
    }

    public UnprocessableEntityException(String message, Throwable cause) {
        super(message, cause);
        code = StatusCode.UNPROCESSABLE_ENTITY;
    }

    public UnprocessableEntityException(Throwable cause) {
        super(cause);
        code = StatusCode.UNPROCESSABLE_ENTITY;
    }
}
