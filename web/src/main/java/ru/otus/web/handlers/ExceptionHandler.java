package ru.otus.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.web.exceptions.ResponseException;
import ru.otus.web.http.HttpContext;

import java.io.IOException;

public class ExceptionHandler implements HttpContextHandler {
    private final HttpContextHandler nextHandler;
    private final ExceptionResponseProcessor processor;
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public ExceptionHandler(HttpContextHandler nextHandler) {
        this.nextHandler = nextHandler;
        this.processor = new ExceptionResponseProcessor();
    }

    @Override
    public void execute(HttpContext context) throws IOException {
        logger.debug("ExceptionHandler execute");
        try {
            nextHandler.execute(context);
        } catch (ResponseException e) {
            processor.execute(context, e);
        }
    }
}
