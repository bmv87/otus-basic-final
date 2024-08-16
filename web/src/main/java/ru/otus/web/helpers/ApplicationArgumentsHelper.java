package ru.otus.web.helpers;

import ru.otus.services.helpers.TypesHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ApplicationArgumentsHelper {
    private static final Map<String, String> argsuments = new HashMap<>();

    public static void tryParse(String[] args) {
        Arrays.stream(args).filter(a -> a.contains(":") && a.startsWith("-")).forEach(a -> {
            argsuments.put(getKey(a), getValue(a));
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> T tryGet(String key, Class<T> clazz) {
        String value = argsuments.get(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException(String.format("Аргумент %s не задан при запуске приложения.", key));
        }
        var result = TypesHelper.getTypedValue(clazz, value);
        if (result == null) {
            throw new RuntimeException("Ошибка парсинга аргументов приложения.");
        }

        return (T) result;
    }

    private static String getKey(String argStr) {
        var separatorIndex = argStr.indexOf(":");
        return argStr.substring(0, separatorIndex);
    }

    private static String getValue(String argStr) {
        var separatorIndex = argStr.indexOf(":");
        return argStr.substring(separatorIndex + 1);
    }
}
