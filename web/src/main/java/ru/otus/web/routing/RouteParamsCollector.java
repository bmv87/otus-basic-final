package ru.otus.web.routing;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import ru.otus.services.exceptions.BadRequestException;
import ru.otus.services.exceptions.ResponseException;
import ru.otus.services.helpers.TypesHelper;
import ru.otus.web.helpers.GsonConfigurator;
import ru.otus.web.http.Constants;
import ru.otus.web.http.HttpContext;
import ru.otus.web.models.ByteArrayBody;
import ru.otus.web.security.Principal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RouteParamsCollector {
    List<Object> params = new ArrayList<>();

    private final Route route;
    private final Parameter[] parameters;

    public RouteParamsCollector(Route route, Parameter[] parameters) {
        this.route = route;
        this.parameters = parameters;
    }

    public List<Object> collect(HttpContext context) {
        params = new ArrayList<>();
        for (Parameter p : parameters) {

            if (tryAddPrincipal(p, context)) {
                continue;
            }
            if (tryAddPathVariable(p, context)) {
                continue;
            }
            if (tryAddParamVariable(p, context)) {
                continue;
            }
            if (tryAddFromBody(p, context)) {
                continue;
            }
            if (tryAddFileContent(p, context)) {
                continue;
            }
        }
        return params;
    }

    private boolean tryAddPathVariable(Parameter p, HttpContext context) {
        var pathAnnotation = p.getAnnotation(PathVariable.class);
        if (pathAnnotation == null) {
            return false;
        }
        var varIndex = route.getIndexOfPathVariable(pathAnnotation.name());
        if (varIndex == -1) {
            throw new BadRequestException("PathVariable not found");
        }
        var param = context.getRequest().getPath().split("/")[varIndex];
        params.add(TypesHelper.getTypedValue(p.getType(), param));
        return true;
    }

    private boolean tryAddParamVariable(Parameter p, HttpContext context) {
        var paramAnnotation = p.getAnnotation(ParamVariable.class);
        if (paramAnnotation == null) {
            return false;
        }
        var param = context.getRequest().getParameters().get(paramAnnotation.name());
        if (param == null) {
            params.add(TypesHelper.getDefaultValue(p.getType()));
            return true;
        }
        params.add(TypesHelper.getTypedValue(p.getType(), param));
        return true;
    }

    private boolean tryAddPrincipal(Parameter p, HttpContext context) {
        var principalAnnotation = p.getAnnotation(Principal.class);
        if (principalAnnotation == null) {
            return false;
        }
        var param = context.getPrincipal();
        params.add(param);
        return true;
    }

    private boolean tryAddFromBody(Parameter p, HttpContext context) {
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
            params.add(gson.fromJson(body, p.getType()));
        } catch (JsonParseException e) {
            throw new BadRequestException("Некорректный формат входящего JSON объекта");
        }
        return true;
    }


    private boolean tryAddFileContent(Parameter p, HttpContext context) {
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

        params.add(file);

        return true;
    }
}
