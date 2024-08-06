package ru.otus.web.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.web.exceptions.BadRequestException;
import ru.otus.web.exceptions.ResponseException;
import ru.otus.web.http.HttpContext;
import ru.otus.web.http.StatusCode;
import ru.otus.web.models.ResponseEntity;
import ru.otus.web.routing.FromBody;
import ru.otus.web.routing.ParamVariable;
import ru.otus.web.routing.PathVariable;
import ru.otus.web.routing.Route;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
        try {
            var inst = method.getDeclaringClass().getConstructor().newInstance();
            List<Object> params = new ArrayList<>();
            for (Parameter p : method.getParameters()) {
                var pathAnnotation = p.getAnnotation(PathVariable.class);
                if (pathAnnotation != null) {
                    var varIndex = route.getIndexOfPathVariable(pathAnnotation.name());
                    if (varIndex == -1) {
                        throw new BadRequestException("PathVariable not found");
                    }
                    var param = context.getRequest().getPath().split("/")[varIndex];
                    params.add(p.getType().cast(param));
                    continue;
                }
                var paramAnnotation = p.getAnnotation(ParamVariable.class);
                if (paramAnnotation != null) {
                    var param = context.getRequest().getParameters().get(paramAnnotation.name());
                    if (param == null) {
                        params.add(PrimitiveDefaults.getDefaultValue(p.getType()));
                        continue;
                    }
                    params.add(p.getType().cast(param));
                }

                var fromBodyAnnotation = p.getAnnotation(FromBody.class);
                if (fromBodyAnnotation != null) {
                    var body = context.getRequest().getBody();
                    if (body == null || body.isBlank()) {
                        throw new BadRequestException("Для данного маршрута тело запроса не может быть пустым.");
                    }
                    try {
                        Gson gson = new Gson();
                        params.add(gson.fromJson(body, p.getType()));
                    } catch (JsonParseException e) {
                        throw new BadRequestException("Некорректный формат входящего JSON объекта");
                    }
                }
            }
            var response = context.getResponse();
            if (params.isEmpty()) {
                if (method.getReturnType().equals(Void.TYPE)) {
                    method.invoke(inst);
                    response.setResponseCode(StatusCode.OK);
                } else if (method.getGenericReturnType() instanceof ResponseEntity<?>) {
                    response.setResponse((ResponseEntity<?>) method.getReturnType().cast(method.invoke(inst)));
                } else {
                    response.setResponse(new ResponseEntity<>(method.getReturnType().cast(method.invoke(inst)), StatusCode.OK));
                }
            } else {
                if (method.getReturnType().equals(Void.TYPE)) {
                    method.invoke(inst, params.toArray());
                } else if (method.getGenericReturnType() instanceof ResponseEntity<?>) {
                    response.setResponse((ResponseEntity<?>) method.getReturnType().cast(method.invoke(inst, params.toArray())));
                } else {
                    response.setResponse(new ResponseEntity<>(method.getReturnType().cast(method.invoke(inst, params.toArray())), StatusCode.OK));
                }
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
