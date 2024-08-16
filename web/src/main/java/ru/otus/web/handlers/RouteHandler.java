package ru.otus.web.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.exceptions.ResponseException;
import ru.otus.web.http.HttpContext;
import ru.otus.web.http.HttpResponse;
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

    public RouteHandler(Route route, Method method) {
        this.route = route;
        this.method = method;
        this.paramsCollector = new RouteParamsCollector(route, method.getParameters());
    }

    @Override
    public void execute(HttpContext context) throws IOException {
        logger.debug("RouteHandler execute");
        logger.debug(route.toString());
        var fileAnnotations = method.getAnnotation(File.class);
        boolean isFileResponse = fileAnnotations != null;

        if (isFileResponse && !ByteArrayBody.class.isAssignableFrom(method.getReturnType())) {
            throw new ResponseException("Некорректный тип класса в параметрах метода.");
        }

        try {

            List<Object> params = paramsCollector.collect(context);

            var response = context.getResponse();
            if (params.isEmpty()) {
                tryInvoke(response, isFileResponse);
            } else {
                tryInvokeWithParams(params, response, isFileResponse);
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

    private void tryInvoke(HttpResponse response, boolean isFileResponse) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {

        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            if (method.getReturnType().equals(Void.TYPE)) {
                method.invoke(inst);
                response.noContent();
                return;
            }
            if (!isFileResponse) {
                response.ok(method.invoke(inst));
                return;
            }
            var body = method.invoke(inst);
            if (body instanceof ByteArrayBody bar) {
                response.file(bar);
            } else {
                throw new ResponseException(String.format("Некорректный тип возвращаемого значения для метода %s класса %s", method.getName(), method.getReturnType().getSimpleName()));
            }

        } finally {
            tryClose(inst);
        }
    }

    private void tryInvokeWithParams(List<Object> params, HttpResponse response, boolean isFileResponse) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            if (method.getReturnType().equals(Void.TYPE)) {
                method.invoke(inst, params.toArray());
                response.noContent();
                return;
            }
            if (!isFileResponse) {
                response.ok(method.invoke(inst, params.toArray()));
            }
            var body = method.invoke(inst, params.toArray());
            if (body instanceof ByteArrayBody bar) {
                response.file(bar);
            } else {
                throw new ResponseException(String.format("Некорректный тип возвращаемого значения для метода %s класса %s", method.getName(), method.getReturnType().getSimpleName()));
            }

        } finally {
            tryClose(inst);
        }
    }

    private <T> void tryClose(T instance) {
        if (instance instanceof AutoCloseable closable) {
            try {
                logger.debug(closable.toString());
                closable.close();
            } catch (Exception e) {
                logger.error("Ошибка при закрытии ресурса.", e);
            }
        }
    }
}
