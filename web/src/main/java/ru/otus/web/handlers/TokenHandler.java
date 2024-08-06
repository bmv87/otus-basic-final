package ru.otus.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.web.exceptions.UnauthorizedException;
import ru.otus.web.http.Constants;
import ru.otus.web.http.HttpContext;

import java.io.IOException;

public class TokenHandler implements HttpContextHandler {
    private static final Logger logger = LoggerFactory.getLogger(TokenHandler.class);

    private final HttpContextHandler nextHandler;

    public TokenHandler(HttpContextHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public void execute(HttpContext context) throws IOException {
        logger.debug("TokenHandler execute");
        var token = context.getRequest().getHeaders().get(Constants.Headers.TOKEN);
        if (token == null) {
            throw new UnauthorizedException("Пользователь не аутентифицирован");
        }
        logger.debug("Token: {}", token);
        //TODO: validate header token: **** for existing in memory cache
        if (nextHandler == null) {
            return;
        }
        nextHandler.execute(context);
    }
}
