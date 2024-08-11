package ru.otus.web.http;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.exceptions.ResponseException;
import ru.otus.web.handlers.GsonConfigurator;
import ru.otus.web.models.ResponseEntity;

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

    public HttpResponse(OutputStream out) {
        this.out = out;
        this.headers.put(Constants.Headers.CONTENT_TYPE, Constants.MimeTypes.JSON);
        this.headers.put(Constants.Headers.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        this.headers.put(Constants.Headers.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        this.headers.put(Constants.Headers.ACCESS_CONTROL_ALLOW_HEADERS, Constants.Headers.CONTENT_TYPE + ", " + Constants.Headers.TOKEN);
        this.headers.put(Constants.Headers.ACCESS_CONTROL_REQUEST_HEADERS, Constants.Headers.CONTENT_TYPE + ", " + Constants.Headers.TOKEN);
        this.setResponseCode(StatusCode.OK);
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void addHeader(String key, String value) {
        if (!headers.containsKey(key)) {
            headers.put(key, value);
        }
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setResponse(ResponseEntity<?> response) {
        for (var header : response.getHeaders().entrySet()) {
            if (!headers.containsKey(header.getKey())) {
                headers.put(header.getKey(), header.getValue());
            }
        }
        responseCode = response.getStatusCode();
        if (response.getBody() != null) {
            Gson gson = GsonConfigurator.getDefault();
            try {
                body = gson.toJson(response.getBody());
            } catch (Exception e) {
                throw new ResponseException("Ошибка добавления тела ответа.", e);
            }
        }
    }

    public void setResponseCode(StatusCode responseCode) {
        this.responseCode = responseCode;
    }

    public void send() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append(" ")
                .append(responseCode.getCode()).append(" ")
                .append(responseCode).append("\r\n");
        for (var header : headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        if (body != null && !body.isEmpty()) {

            sb.append(body);
        }
        var responseStr = sb.toString();
        logger.debug(responseStr);

        out.write(responseStr.getBytes(StandardCharsets.UTF_8));

    }
}
