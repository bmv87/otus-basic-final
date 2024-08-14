package ru.otus.web.http;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.exceptions.ResponseException;
import ru.otus.web.helpers.GsonConfigurator;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final OutputStream out;

    private Map<String, String> headers = new HashMap<>();
    private String body;
    private String protocol;
    private StatusCode responseCode;
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);


    public HttpResponse(OutputStream out, String protocol) {
        this.out = out;
        this.protocol = protocol;
        this.headers.put(Constants.Headers.CONTENT_TYPE, Constants.MimeTypes.JSON);
        this.headers.put(Constants.Headers.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        this.headers.put(Constants.Headers.ACCESS_CONTROL_ALLOW_ORIGIN, Constants.ANY_VALUE);
        var allowedHeaders = Constants.Headers.CONTENT_TYPE + ", " + Constants.Headers.TOKEN;
        this.headers.put(Constants.Headers.ACCESS_CONTROL_ALLOW_HEADERS, allowedHeaders);
        this.headers.put(Constants.Headers.ACCESS_CONTROL_REQUEST_HEADERS, allowedHeaders);
        this.responseCode = StatusCode.OK;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public HttpResponse addHeader(String key, String value) {
        if (!headers.containsKey(key)) {
            headers.put(key, value);
        } else {
            headers.remove(key);
            headers.put(key, value);
        }
        return this;
    }

    public HttpResponse setHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            throw new ResponseException("Ошибка конфигурирования ответа.");
        }
        this.headers = headers;
        return this;
    }

    private <T> void setBody(T bodyObj) {
        if (bodyObj != null) {
            Gson gson = GsonConfigurator.getDefault();
            try {
                body = gson.toJson(bodyObj);
            } catch (Exception e) {
                throw new ResponseException("Ошибка добавления тела ответа.", e);
            }
        }
    }

    public <T> HttpResponse ok(T bodyObj) {
        responseCode = StatusCode.OK;
        setBody(bodyObj);
        return this;
    }

    public HttpResponse ok() {
        body = null;
        responseCode = StatusCode.OK;
        return this;
    }

    public <T> HttpResponse noContent() {
        body = null;
        responseCode = StatusCode.NO_CONTENT;
        return this;
    }

    public <T> HttpResponse error(StatusCode status, T errorBody) {
        if (status == null) {
            throw new ResponseException("Ошибка конфигурирования ответа. responseCode");
        }
        if (errorBody == null) {
            throw new ResponseException("Ошибка конфигурирования ответа. errorBody");
        }
        responseCode = status;
        setBody(errorBody);
        return this;
    }

    public void send() throws IOException {
        StringBuilder sb = new StringBuilder();
        if (protocol == null || protocol.isBlank() || responseCode == null || headers.isEmpty()) {
            throw new IOException("Невозможно сформировать корректный ответ.");
        }
        sb.append(protocol).append(" ")
                .append(responseCode.getCode()).append(" ")
                .append(responseCode).append("\r\n");
        for (var header : headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        if (body != null && !body.isEmpty() &&
                responseCode != StatusCode.NO_CONTENT) {
            sb.append(body);
        }
        var responseStr = sb.toString();
        logger.debug(responseStr);

        out.write(responseStr.getBytes(StandardCharsets.UTF_8));
    }
}
