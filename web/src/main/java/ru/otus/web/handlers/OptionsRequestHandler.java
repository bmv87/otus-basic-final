package ru.otus.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.web.http.Constants;
import ru.otus.web.http.HttpContext;
import ru.otus.web.http.StatusCode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OptionsRequestHandler implements HttpContextHandler {
    private Map<String, String> headers = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(OptionsRequestHandler.class);

    public OptionsRequestHandler(Map<String, String> headers) {
        this.headers.put(Constants.Headers.ACCEPT, Constants.MimeTypes.JSON);
        for (var header : headers.entrySet()) {
            if (!headers.containsKey(header.getKey())) {
                this.headers.put(header.getKey(), header.getValue());
            }
        }
    }

    @Override
    public void execute(HttpContext context) throws IOException {
        logger.debug("OptionsRequestHandler execute");

        var response = context.getResponse();
        for (var header : headers.entrySet()) {
            response.addHeader(header.getKey(), header.getValue());
        }

        response.setResponseCode(StatusCode.OK);
        response.send();
    }
}
