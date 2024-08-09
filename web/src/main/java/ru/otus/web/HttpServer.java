package ru.otus.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.cache.CacheManager;
import ru.otus.services.cache.CacheNames;
import ru.otus.web.routing.RouteDispatcher;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer implements AutoCloseable {
    private final int port;
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public HttpServer(int port) {
        this.port = port;
        CacheManager.getInstance().addCacheStore(CacheNames.AUTH);
    }

    public void start() {
        var dispatcher = new RouteDispatcher();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Сервер запущен на порту: {}", port);
            while (!serverSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                clientPool.submit(new RequestHandler(socket, dispatcher));
            }
        } catch (IOException e) {
            logger.error("Ошибка запуска сервере или ожидания подключения.", e);
            clientPool.shutdown();
        }
    }

    @Override
    public void close() throws Exception {
        clientPool.shutdown();
    }
}