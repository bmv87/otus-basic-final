package ru.otus.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.web.exceptions.ResponseException;
import ru.otus.web.http.HttpContext;
import ru.otus.web.models.ErrorVM;
import ru.otus.web.models.ResponseEntity;

import java.io.IOException;

public class ExceptionResponseProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionResponseProcessor.class);

    public void execute(HttpContext context, Exception e) throws IOException {
        ErrorVM errorVM = null;
        logger.error(e.getMessage(), e);
        if (e instanceof ResponseException respE) {
            errorVM = respE.getModelForResponse();
        } else {
            errorVM = new ResponseException(e).getModelForResponse();
        }
        var respEntity = new ResponseEntity<ErrorVM>(errorVM, errorVM.getStatus());
        context.getResponse().setResponse(respEntity);
        try {
            context.getResponse().send();
        } catch (IOException ex) {
            logger.error("Ошибка при отправке ответа.", ex);
            throw ex;
        }
    }
}
