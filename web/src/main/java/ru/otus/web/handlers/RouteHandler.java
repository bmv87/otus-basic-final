package ru.otus.web.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.exceptions.BadRequestException;
import ru.otus.services.exceptions.ResponseException;
import ru.otus.web.helpers.GsonConfigurator;
import ru.otus.web.helpers.TypesHelper;
import ru.otus.web.http.HttpContext;
import ru.otus.web.http.HttpResponse;
import ru.otus.web.http.StatusCode;
import ru.otus.web.routing.FromBody;
import ru.otus.web.routing.ParamVariable;
import ru.otus.web.routing.PathVariable;
import ru.otus.web.routing.Route;
import ru.otus.web.security.Principal;

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
            }
            var response = context.getResponse();
            if (params.isEmpty()) {
                tryInvoke(method, response);
            } else {
                tryInvokeWithParams(method, params, response);
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

    private void tryInvoke(Method method, HttpResponse response) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {

        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            if (method.getReturnType().equals(Void.TYPE)) {
                method.invoke(inst);
                response.noContent();
            } else {
                response.ok(method.invoke(inst));
            }
        } finally {
            tryClose(inst);
        }
    }

    private void tryInvokeWithParams(Method method, List<Object> params, HttpResponse response) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            if (method.getReturnType().equals(Void.TYPE)) {
                response.noContent();
                method.invoke(inst, params.toArray());
            } else {
                response.ok(method.invoke(inst, params.toArray()));
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
}
