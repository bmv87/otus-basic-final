package ru.otus.web.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.exceptions.BadRequestException;
import ru.otus.services.exceptions.ResponseException;
import ru.otus.web.helpers.GsonConfigurator;
import ru.otus.services.helpers.TypesHelper;
import ru.otus.web.http.Constants;
import ru.otus.web.http.HttpContext;
import ru.otus.web.http.HttpResponse;
import ru.otus.web.models.ByteArrayBody;
import ru.otus.web.routing.*;
import ru.otus.web.security.Principal;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RouteHandler implements HttpContextHandler {
    private static final Logger logger = LoggerFactory.getLogger(RouteHandler.class);

    private final Route route;
    private final Method method;

    public RouteHandler(Route route, Method method) {
        this.route = route;
        this.method = method;
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

            List<Object> params = new ArrayList<>();
            for (Parameter p : method.getParameters()) {

                if (tryAddPrincipal(params, p, context)) {
                    continue;
                }
                if (tryAddPathVariable(params, p, context)) {
                    continue;
                }
                if (tryAddParamVariable(params, p, context)) {
                    continue;
                }
                if (tryAddFromBody(params, p, context)) {
                    continue;
                }
                if (tryAddFileContent(params, p, context)) {
                    continue;
                }
            }
            var response = context.getResponse();
            if (params.isEmpty()) {
                tryInvoke(method, response, isFileResponse);
            } else {
                tryInvokeWithParams(method, params, response, isFileResponse);
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

    private void tryInvoke(Method method, HttpResponse response, boolean isFileResponse) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {

        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            if (method.getReturnType().equals(Void.TYPE)) {
                method.invoke(inst);
                response.noContent();
            } else {
                if (isFileResponse) {
                    var body = method.invoke(inst);
                    if (body instanceof ByteArrayBody bar) {
                        response.file(bar);
                    } else {
                        throw new ResponseException(String.format("Некорректный тип возвращаемого значения для метода %s класса %s", method.getName(), method.getReturnType().getSimpleName()));
                    }
                } else {
                    response.ok(method.invoke(inst));
                }
            }
        } finally {
            tryClose(inst);
        }
    }

    private void tryInvokeWithParams(Method method, List<Object> params, HttpResponse response, boolean isFileResponse) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            if (method.getReturnType().equals(Void.TYPE)) {
                method.invoke(inst, params.toArray());
                response.noContent();

            } else {

                if (isFileResponse) {
                    var body = method.invoke(inst, params.toArray());
                    if (body instanceof ByteArrayBody bar) {
                        response.file(bar);
                    } else {
                        throw new ResponseException(String.format("Некорректный тип возвращаемого значения для метода %s класса %s", method.getName(), method.getReturnType().getSimpleName()));
                    }
                } else {
                    response.ok(method.invoke(inst, params.toArray()));
                }

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

    private boolean tryAddPathVariable(List<Object> distinationList, Parameter p, HttpContext context) {
        var pathAnnotation = p.getAnnotation(PathVariable.class);
        if (pathAnnotation == null) {
            return false;
        }
        var varIndex = route.getIndexOfPathVariable(pathAnnotation.name());
        if (varIndex == -1) {
            throw new BadRequestException("PathVariable not found");
        }
        var param = context.getRequest().getPath().split("/")[varIndex];
        distinationList.add(TypesHelper.getTypedValue(p.getType(), param));
        return true;
    }

    private boolean tryAddParamVariable(List<Object> distinationList, Parameter p, HttpContext context) {
        var paramAnnotation = p.getAnnotation(ParamVariable.class);
        if (paramAnnotation == null) {
            return false;
        }
        var param = context.getRequest().getParameters().get(paramAnnotation.name());
        if (param == null) {
            distinationList.add(TypesHelper.getDefaultValue(p.getType()));
            return true;
        }
        distinationList.add(TypesHelper.getTypedValue(p.getType(), param));
        return true;
    }

    private boolean tryAddPrincipal(List<Object> distinationList, Parameter p, HttpContext context) {
        var principalAnnotation = p.getAnnotation(Principal.class);
        if (principalAnnotation == null) {
            return false;
        }
        var param = context.getPrincipal();
        distinationList.add(param);
        return true;
    }

    private boolean tryAddFromBody(List<Object> distinationList, Parameter p, HttpContext context) {
        var fromBodyAnnotation = p.getAnnotation(FromBody.class);
        if (fromBodyAnnotation == null) {
            return false;
        }
        var body = context.getRequest().getBody();
        if (body == null || body.isBlank()) {
            throw new BadRequestException("Для данного маршрута тело запроса не может быть пустым.");
        }
        try {
            Gson gson = GsonConfigurator.getDefault();
            distinationList.add(gson.fromJson(body, p.getType()));
        } catch (JsonParseException e) {
            throw new BadRequestException("Некорректный формат входящего JSON объекта");
        }
        return true;
    }


    private boolean tryAddFileContent(List<Object> distinationList, Parameter p, HttpContext context) {
        var fileAnnotation = p.getAnnotation(File.class);
        if (fileAnnotation == null) {
            return false;
        }
        if (!ByteArrayBody.class.isAssignableFrom(p.getType())) {
            throw new ResponseException("Некорректный тип класса в параметрах метода.");
        }

        ByteArrayBody file = null;
        try {
            file = (ByteArrayBody) p.getType().getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new ResponseException("Ошибка получения экземпляра класса для хранения содержимого файла.", e);
        }
        var request = context.getRequest();
        var contentTypeHeaderVal = request.getHeaders().get(Constants.Headers.CONTENT_TYPE.toLowerCase());
        var contentDispositionHeaderVal = request.getHeaders().get(Constants.Headers.CONTENT_DISPOSITION.toLowerCase());
        if (contentTypeHeaderVal == null ||
                contentTypeHeaderVal.isBlank()) {
            throw new BadRequestException("Для данного маршрута не указан заголовок. " + Constants.Headers.CONTENT_TYPE);
        }
        if (contentDispositionHeaderVal == null ||
                contentDispositionHeaderVal.isBlank()) {
            throw new BadRequestException("Для данного маршрута не указан заголовок. " + Constants.Headers.CONTENT_DISPOSITION);
        }
        var fileName = contentDispositionHeaderVal.substring(contentDispositionHeaderVal.indexOf("=") + 1);
        try {
            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BadRequestException("Ошибка парсинга имени файла. " + fileName);
        }

        file.setContentType(contentTypeHeaderVal);
        file.setFileName(fileName);
        file.setContent(request.getBodyB());
        file.setSize(request.getBodyB().length);

        distinationList.add(file);

        return true;
    }
}
