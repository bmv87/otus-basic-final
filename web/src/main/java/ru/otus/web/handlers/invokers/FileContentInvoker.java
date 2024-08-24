package ru.otus.web.handlers.invokers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.services.exceptions.ResponseException;
import ru.otus.web.http.HttpResponse;
import ru.otus.web.models.ByteArrayBody;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class FileContentInvoker extends ControllerInvoker {
    private static final Logger logger = LoggerFactory.getLogger(FileContentInvoker.class);

    private final Method method;

    public FileContentInvoker(Method method) {
        this.method = method;
    }

    @Override
    public void tryInvoke(HttpResponse response) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            var body = method.invoke(inst);
            if (body instanceof ByteArrayBody bar) {
                response.file(bar);
            } else {
                throw new ResponseException(String.format("Некорректный тип возвращаемого значения для метода %s класса %s", method.getName(), method.getReturnType().getSimpleName()));
            }
        } finally {
            close(inst);
        }
    }

    @Override
    public void tryInvokeWithParams(HttpResponse response, List<Object> params) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        var inst = method.getDeclaringClass().getConstructor().newInstance();
        try {
            var body = method.invoke(inst, params.toArray());
            if (body instanceof ByteArrayBody bar) {
                response.file(bar);
            } else {
                throw new ResponseException(String.format("Некорректный тип возвращаемого значения для метода %s класса %s", method.getName(), method.getReturnType().getSimpleName()));
            }
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
