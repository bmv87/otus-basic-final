package ru.otus.web.handlers.invokers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.web.http.HttpResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class JSONContentInvoker extends ControllerInvoker {
    private static final Logger logger = LoggerFactory.getLogger(JSONContentInvoker.class);

    private final Method method;

    public JSONContentInvoker(Method method) {
        this.method = method;
    }

    @Override
    public void tryInvoke(HttpResponse response) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            response.ok(method.invoke(inst));
        } finally {
            close(inst);
        }
    }

    @Override
    public void tryInvokeWithParams(HttpResponse response, List<Object> params) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            response.ok(method.invoke(inst, params.toArray()));
        } finally {
            close(inst);
        }
    }

    private <T> void close(T inst) {
        try {
            tryClose(inst);
        } catch (Exception e) {
            logger.error("Ошибка при закрытии ресурса.", e);
        }
    }
}
