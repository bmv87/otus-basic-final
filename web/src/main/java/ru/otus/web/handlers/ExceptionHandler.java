package ru.otus.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.exceptions.ResponseException;
import ru.otus.services.models.ErrorVM;
import ru.otus.web.http.HttpContext;
import ru.otus.web.http.StatusCode;
import ru.otus.web.models.ResponseEntity;

import java.io.IOException;

public class ExceptionHandler implements HttpContextHandler {
    private final HttpContextHandler nextHandler;
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public ExceptionHandler(HttpContextHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public void execute(HttpContext context) throws IOException {
        logger.debug("ExceptionHandler execute");
        try {
            nextHandler.execute(context);
        } catch (ResponseException e) {
            execute(context, e);
        }
    }

    private void execute(HttpContext context, Exception e) throws IOException {
        ErrorVM errorVM = null;
        logger.error(e.getMessage(), e);
        if (e instanceof ResponseException respE) {
            errorVM = respE.getModelForResponse();
        } else {
            errorVM = new ResponseException(e).getModelForResponse();
        }
        var respEntity = new ResponseEntity<ErrorVM>(errorVM, StatusCode.valueOf(errorVM.getStatus().name()));
        context.getResponse().setResponse(respEntity);
        try {
            context.getResponse().send();
        } catch (IOException ex) {
            logger.error("Ошибка при отправке ответа.", ex);
            throw ex;
        }
    }
}
