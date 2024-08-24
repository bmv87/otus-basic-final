package ru.otus.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Seed {
    private static final Logger logger = LoggerFactory.getLogger(Seed.class);

    public static void main(String[] args) {
        try (var repo = new DBContext()) {

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
