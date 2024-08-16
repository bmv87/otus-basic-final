package ru.otus.services.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationPropertiesHelper {
    private static final Properties props = new Properties();
    private static final Logger logger = LoggerFactory.getLogger(ApplicationPropertiesHelper.class);

    public static final String FILES_STORE_DIRECTORY_PARAM_NANE = "store.files.path";
    public static final String CORS_ALLOW_ORIGIN_PARAM_NANE = "cors.headers.accessControlAllowOrigin";
    public static final String SOCKET_RECEIVE_BUFFERSIZE = "socket.ReceiveBufferSize";

    public static <T> T tryGet(String key, Class<T> type) {
        var value = props.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String str) {
            return TypesHelper.getTypedValue(type, str);
        }
        return type.cast(value);
    }

    public static <T> void load(Class<T> clazz) {
        try (InputStream input = clazz.getClassLoader().getResourceAsStream("application.properties")) {

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }

            //load a properties file from class path, inside static method
            props.load(input);
            for (var prop : props.entrySet()) {
                logger.debug("property - {}: {}", prop.getKey(), prop.getValue());
            }

        } catch (IOException e) {
            logger.error("Ошибка загрузки конфигурации приложения.", e);
        }
    }
}
