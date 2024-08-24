package ru.otus.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.web.http.HttpContext;
import ru.otus.web.routing.RouteDispatcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RequestHandler implements Runnable {
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private final RouteDispatcher dispatcher;
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    public RequestHandler(Socket connection, RouteDispatcher dispatcher) throws IOException {
        this.socket = connection;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        try (var context = new HttpContext(socket)){
             dispatcher.execute(context);
        } catch (Exception e) {
            logger.error("Ошибка обработки контекста запроса.", e);
        } finally {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                logger.error("Ошибка чистки объекта класса RequestHandler", e);
            }
        }
    }
}
