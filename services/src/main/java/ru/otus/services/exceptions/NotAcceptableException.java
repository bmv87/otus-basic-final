package ru.otus.services.exceptions;

import ru.otus.services.http.StatusCode;

public class NotAcceptableException extends ResponseException{
    public NotAcceptableException(String message) {
        super(message);
        code = StatusCode.NOT_ACCEPTABLE;
    }

    public NotAcceptableException(String message, Throwable cause) {
        super(message, cause);
        code = StatusCode.NOT_ACCEPTABLE;
    }

    public NotAcceptableException(Throwable cause) {
        super(cause);
        code = StatusCode.NOT_ACCEPTABLE;
    }
}
