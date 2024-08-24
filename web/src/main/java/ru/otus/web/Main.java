package ru.otus.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.helpers.ApplicationPropertiesHelper;
import ru.otus.web.helpers.ApplicationArgumentsHelper;

public class Main {
    private static final String PORT_ARG_NAME = "-port";
    private static final int DEFAULT_PORT = 8189;
    private static final int DEFAULT_RECEIVE_BUFFER_SIZE = 1048576;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        ApplicationArgumentsHelper.tryParse(args);
        ApplicationPropertiesHelper.load(Main.class);
        int port = DEFAULT_PORT;
        int receiveBufferSize = DEFAULT_RECEIVE_BUFFER_SIZE;
        try {
            port = ApplicationArgumentsHelper.tryGet(PORT_ARG_NAME, Integer.class);
        } catch (RuntimeException e) {
            logger.info(e.getMessage());
        }
        try {
            receiveBufferSize = ApplicationPropertiesHelper.tryGet(ApplicationPropertiesHelper.SOCKET_RECEIVE_BUFFERSIZE, Integer.class);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        try (var server = new HttpServer(port, receiveBufferSize)) {
            server.start();
        }
    }
}