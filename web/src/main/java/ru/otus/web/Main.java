package ru.otus.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.web.helpers.ApplicationArgumentsHelper;

public class Main {
    private static final String PORT_ARG_NAME = "-port";
    private static final int DEFAULT_PORT = 8189;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        ApplicationArgumentsHelper.tryParse(args);
        int port = DEFAULT_PORT;
        try {
            port = ApplicationArgumentsHelper.tryGet(PORT_ARG_NAME, Integer.class);
        } catch (RuntimeException e) {
            logger.info(e.getMessage());
        }

        try (var server = new HttpServer(port)) {
            server.start();
        }
    }
}