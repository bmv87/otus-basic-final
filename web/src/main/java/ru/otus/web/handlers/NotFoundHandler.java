package ru.otus.web.handlers;

import ru.otus.services.exceptions.NotFoundException;
import ru.otus.web.http.HttpContext;

import java.io.IOException;

public class NotFoundHandler  implements HttpContextHandler {
    @Override
    public void execute(HttpContext context) throws IOException {
        throw new NotFoundException("Маршрут не зарегистрирован.");
    }
}
