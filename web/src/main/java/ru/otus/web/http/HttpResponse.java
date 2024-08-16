package ru.otus.web.http;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.exceptions.ResponseException;
import ru.otus.web.helpers.GsonConfigurator;
import ru.otus.web.models.ByteArrayBody;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final OutputStream out;

    private Map<String, String> headers = new HashMap<>();
    private String body;
    private byte[] bodyB;
    private String protocol;
    private StatusCode responseCode;
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);


    public HttpResponse(OutputStream out, String protocol) {
        this.out = out;
        this.protocol = protocol;
        this.responseCode = StatusCode.OK;
        setDefaultHeaders();
    }

    public HttpResponse addHeader(String key, String value) {
        key = key.toLowerCase();
        if (!headers.containsKey(key)) {
            headers.put(key, value);
        } else {
            headers.remove(key);
            headers.put(key, value);
        }
        return this;
    }

    public HttpResponse removeHeader(String key) {
        key = key.toLowerCase();
        headers.remove(key);
        return this;
    }

    public HttpResponse setHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            throw new ResponseException("Ошибка конфигурирования ответа.");
        }
        setDefaultHeaders();
        for (var h : headers.entrySet()) {
            addHeader(h.getKey(), h.getValue());
        }
        return this;
    }

    public <T> void body(T bodyObj) {
        if (bodyObj != null) {
            Gson gson = GsonConfigurator.getDefault();
            try {
                body = gson.toJson(bodyObj);
            } catch (Exception e) {
                throw new ResponseException("Ошибка добавления тела ответа.", e);
            }
        }
        bodyB = null;
    }

    public <T> HttpResponse ok(T bodyObj) {
        responseCode = StatusCode.OK;
        body(bodyObj);
        setDefaultHeaders();
        return this;
    }

    public HttpResponse ok() {
        body = null;
        bodyB = null;
        responseCode = StatusCode.OK;
        setDefaultHeaders();
        return this;
    }

    public HttpResponse file(ByteArrayBody content) {
        body = null;
        bodyB = content.getContent();
        responseCode = StatusCode.OK;
        addHeader(Constants.Headers.CONTENT_TYPE, content.getContentType());
        addHeader(Constants.Headers.CONTENT_DISPOSITION, String.format("attachment; filename=*UTF-8'%s", URLEncoder.encode(content.getFileName(), StandardCharsets.UTF_8)));
        addHeader(Constants.Headers.CONTENT_LENGTH, String.valueOf(content.getSize()));
        return this;
    }

    public <T> HttpResponse noContent() {
        body = null;
        bodyB = null;
        responseCode = StatusCode.NO_CONTENT;
        setDefaultHeaders();
        return this;
    }

    private void setDefaultHeaders() {
        addHeader(Constants.Headers.CONTENT_TYPE, Constants.MimeTypes.JSON);
        addHeader(Constants.Headers.CONTENT_TYPE, Constants.MimeTypes.JSON);
        addHeader(Constants.Headers.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        addHeader(Constants.Headers.ACCESS_CONTROL_ALLOW_ORIGIN, Constants.ANY_VALUE);
        var allowedHeaders = Constants.Headers.CONTENT_TYPE + ", " + Constants.Headers.TOKEN + ", " + Constants.Headers.CONTENT_DISPOSITION;
        addHeader(Constants.Headers.ACCESS_CONTROL_ALLOW_HEADERS, allowedHeaders);
        addHeader(Constants.Headers.ACCESS_CONTROL_EXPOSE_HEADERS, allowedHeaders);
        addHeader(Constants.Headers.ACCESS_CONTROL_REQUEST_HEADERS, allowedHeaders);
        removeHeader(Constants.Headers.CONTENT_DISPOSITION);
        removeHeader(Constants.Headers.CONTENT_LENGTH);
    }

    public <T> HttpResponse error(StatusCode status, T errorBody) {
        if (status == null) {
            throw new ResponseException("Ошибка конфигурирования ответа. responseCode");
        }
        if (errorBody == null) {
            throw new ResponseException("Ошибка конфигурирования ответа. errorBody");
        }
        responseCode = status;
        body(errorBody);
        setDefaultHeaders();
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
        if (bodyB != null && responseCode != StatusCode.NO_CONTENT) {
            out.write(bodyB);
        }
    }
}
