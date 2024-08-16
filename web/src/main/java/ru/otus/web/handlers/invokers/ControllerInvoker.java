package ru.otus.web.handlers.invokers;

import ru.otus.web.http.HttpResponse;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public abstract class ControllerInvoker {

    public abstract void tryInvoke(HttpResponse response) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException;

    public abstract void tryInvokeWithParams(HttpResponse response, List<Object> params) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException;


    <T> void tryClose(T instance) throws Exception {
        if (instance instanceof AutoCloseable closable) {
            closable.close();
        }
    }
}
