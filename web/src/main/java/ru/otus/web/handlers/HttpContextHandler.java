package ru.otus.web.handlers;

import ru.otus.web.http.HttpContext;

import java.io.IOException;

public interface HttpContextHandler {
   void execute(HttpContext context) throws IOException;
}
