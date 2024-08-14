package ru.otus.web.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.models.user.UserVM;

import java.io.IOException;
import java.net.Socket;

public class HttpContext implements AutoCloseable {
    private Socket connection;
    private HttpRequest request;
    private HttpResponse response;
    private UserVM principal;
    private static final Logger logger = LoggerFactory.getLogger(HttpContext.class);

    public HttpContext(Socket connection) {
        this.connection = connection;
        try {
            request = new HttpRequest(connection.getInputStream());
            response = new HttpResponse(connection.getOutputStream(), request.getProtocol());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания объекта HttpContext.", e);
        }
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public UserVM getPrincipal() {
        return principal;
    }

    public void setPrincipal(UserVM principal) {
        this.principal = principal;
    }

    @Override
    public void close() {
        try {
            request = null;
            response = null;
            if (!connection.isClosed() && connection.isConnected()) {
                connection.close();
            }
            connection = null;
        } catch (IOException e) {
            logger.error("Ошибка завершения работы HttpContext", e);
        }
    }
}
