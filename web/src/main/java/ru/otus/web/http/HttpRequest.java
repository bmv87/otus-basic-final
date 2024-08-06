package ru.otus.web.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String rawRequest;
    private String protocol;

    private String uri;
    private String path;
    private HttpMethod method;
    private Map<String, String> parameters;
    private Map<String, String> headers;
    private String body;
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    public HttpRequest(InputStream in) throws IOException {
        byte[] buffer = new byte[8192];
        int n = in.read(buffer);
        if (n < 1) {
            return;
        }
        rawRequest = new String(buffer, 0, n);
        logger.debug("{}{}", System.lineSeparator(), rawRequest);

        this.parse();
    }

    public String getPath() {
        return path;
    }

    public String getUri() {
        return uri;
    }

    public String getRawRequest() {
        return rawRequest;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String getProtocol() {
        return protocol;
    }

    private void parse() {
        int startIndex = rawRequest.indexOf(' ');
        int endIndex = rawRequest.indexOf(' ', startIndex + 1);
        this.uri = rawRequest.substring(startIndex + 1, endIndex);
        this.protocol = rawRequest.substring(endIndex + 1, rawRequest.indexOf('\r'));
        this.path = parsePathString(this.uri);
        this.method = HttpMethod.valueOf(rawRequest.substring(0, startIndex));
        this.parameters = new HashMap<>();
        this.headers = new HashMap<>();
        if (uri.contains("?")) {
            String[] elements = uri.split("[?]");
            this.uri = elements[0];
            String[] keysValues = elements[1].split("&");
            for (String o : keysValues) {
                String[] keyValue = o.split("=");
                this.parameters.put(keyValue[0], keyValue[1]);
            }
        }
        if (method == HttpMethod.POST) {
            this.body = rawRequest.substring(
                    rawRequest.indexOf("\r\n\r\n") + 4
            );
        }

        int startHeadersIndex = rawRequest.indexOf('\n') + 1;
        int endHeadersIndex = rawRequest.indexOf("\r\n\r\n", startHeadersIndex);

        var rawHeaders = rawRequest.substring(startHeadersIndex, endHeadersIndex).split("\r\n");
        for (String header : rawHeaders) {
            var keyValue = header.split(": ", 2);
            headers.put(keyValue[0], keyValue[1]);
        }
        logInfo();
    }

    private String parsePathString(String pathString) {

        var pathEndIndex = pathString.indexOf("?");
        if (pathEndIndex == -1) {
            pathEndIndex = pathString.indexOf("/r/n");
        }
        if (pathEndIndex == -1) {
            return pathString.substring(1);
        }
        return pathString.substring(1, pathEndIndex);
    }

    private void logInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator())
                .append("uri: ").append(uri).append(System.lineSeparator())
                .append("method: ").append(method).append(System.lineSeparator());
        if (body != null && !body.isBlank()) {
            sb.append("body: ").append(body).append(System.lineSeparator());
        }
        logger.info(sb.toString());
    }
}
