package ru.otus.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.cache.CacheManager;
import ru.otus.services.cache.CacheNames;
import ru.otus.services.exceptions.UnauthorizedException;
import ru.otus.services.models.user.UserVM;
import ru.otus.web.http.Constants;
import ru.otus.web.http.HttpContext;

import java.io.IOException;

public class TokenHandler implements HttpContextHandler {
    private static final Logger logger = LoggerFactory.getLogger(TokenHandler.class);

    private final HttpContextHandler nextHandler;
    private final CacheManager cacheManager;

    public TokenHandler(HttpContextHandler nextHandler) {
        this.nextHandler = nextHandler;
        this.cacheManager = CacheManager.getInstance();
    }

    @Override
    public void execute(HttpContext context) throws IOException {
        logger.debug("TokenHandler execute");
        var token = context.getRequest().getHeaders().get(Constants.Headers.TOKEN.toLowerCase());
        if (token == null) {
            throw new UnauthorizedException("Пользователь не аутентифицирован");
        }
        UserVM user = null;
        try {
            user = cacheManager.getFromCache(CacheNames.AUTH, token, UserVM.class);

        } catch (Exception e) {
            throw new UnauthorizedException("Пользователь не аутентифицирован", e);
        }
        if (user == null) {
            throw new UnauthorizedException("Пользователь не аутентифицирован");
        }
        context.setPrincipal(user);
        logger.debug("Token: {}", token);
        logger.debug("Principal: {}", user);
        if (nextHandler == null) {
            return;
        }
        nextHandler.execute(context);
    }
}
