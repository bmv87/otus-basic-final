package ru.otus.web.handlers.invokers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.web.http.HttpResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class NoContentInvoker extends ControllerInvoker {
    private static final Logger logger = LoggerFactory.getLogger(NoContentInvoker.class);

    private final Method method;

    public NoContentInvoker(Method method) {
        this.method = method;
    }

    @Override
    public void tryInvoke(HttpResponse response) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            method.invoke(inst);
            response.noContent();
        } finally {
            close(inst);
        }
    }

    @Override
    public void tryInvokeWithParams(HttpResponse response, List<Object> params) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            method.invoke(inst, params.toArray());
            response.noContent();
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
