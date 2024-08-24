package ru.otus.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.exceptions.NotAcceptableException;
import ru.otus.services.exceptions.ResponseException;
import ru.otus.web.handlers.invokers.ControllerInvoker;
import ru.otus.web.handlers.invokers.FileContentInvoker;
import ru.otus.web.handlers.invokers.JSONContentInvoker;
import ru.otus.web.handlers.invokers.NoContentInvoker;
import ru.otus.web.http.Constants;
import ru.otus.web.http.HttpContext;
import ru.otus.web.models.ByteArrayBody;
import ru.otus.web.routing.File;
import ru.otus.web.routing.Route;
import ru.otus.web.routing.RouteParamsCollector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class RouteHandler implements HttpContextHandler {
    private static final Logger logger = LoggerFactory.getLogger(RouteHandler.class);

    private final Route route;
    private final Method method;
    private final RouteParamsCollector paramsCollector;
    private final ControllerInvoker controllerInvoker;

    public RouteHandler(Route route, Method method) {
        this.route = route;
        this.method = method;

        var fileAnnotations = method.getAnnotation(File.class);
        boolean isFileResponse = fileAnnotations != null;

        if (isFileResponse && !ByteArrayBody.class.isAssignableFrom(method.getReturnType())) {
            throw new ResponseException("Некорректный тип класса в параметрах метода.");
        }
        this.paramsCollector = new RouteParamsCollector(route, method.getParameters());

        if (isFileResponse) {
            this.controllerInvoker = new FileContentInvoker(method);
        } else if (method.getReturnType().equals(Void.TYPE)) {
            this.controllerInvoker = new NoContentInvoker(method);
        } else {
            this.controllerInvoker = new JSONContentInvoker(method);
        }
    }

    @Override
    public void execute(HttpContext context) throws IOException {
        logger.debug("RouteHandler execute");
        logger.debug(route.toString());
        var contentType = context.getRequest().getHeaders().get(Constants.Headers.CONTENT_TYPE.toLowerCase());
        var contentDisposition = context.getRequest().getHeaders().get(Constants.Headers.CONTENT_DISPOSITION.toLowerCase());
        if (contentDisposition == null &&
                contentType != null &&
                !contentType.equalsIgnoreCase(Constants.MimeTypes.JSON)) {
            throw new NotAcceptableException("Тип данных для передачи не поддерживается" + contentType);
        }
        try {
            List<Object> params = paramsCollector.collect(context);

            var response = context.getResponse();
            if (params.isEmpty()) {
                controllerInvoker.tryInvoke(response);
            } else {
                controllerInvoker.tryInvokeWithParams(response, params);
            }
            response.send();
        } catch (InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new ResponseException(String.format("Ошибка выполнения метода %s класса %s", method.getName(), method.getDeclaringClass().getSimpleName()), e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof ResponseException ex) {
                throw ex;
            } else {
                throw new ResponseException(String.format("Ошибка выполнения метода %s класса %s", method.getName(), method.getDeclaringClass().getSimpleName()), e);
            }
        }
    }
}
