package ru.otus.repository;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerUtil {
    private static final String PERSISTENCE_UNIT_NAME = "USERS";

    private static EntityManagerFactory factory;

    public synchronized static EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            synchronized (EntityManagerFactory.class) {
                if (factory == null) {
                    factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                }
            }

        }
        return factory;
    }

    public static void shutdown() {
        if (factory != null) {
            factory.close();
        }
    }

}
