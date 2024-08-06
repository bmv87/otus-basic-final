package ru.otus.web.models;

import ru.otus.web.http.StatusCode;

import java.util.HashMap;
import java.util.Map;

public class ResponseEntity<T> {

    private final T body;
    private final Map<String, String> headers = new HashMap<>();
    private final StatusCode statusCode;

    public ResponseEntity(T body, Map<String, String> headers, StatusCode statusCode) {
        this.body = body;
        this.headers.putAll(headers);
        this.statusCode = statusCode;
    }

    public ResponseEntity(T body, StatusCode statusCode) {
        this.body = body;
        this.statusCode = statusCode;
    }

    public T getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}
