package ru.otus.services.helpers;

import java.util.UUID;

public class TypesHelper {
    // These gets initialized to their default values
    private static boolean DEFAULT_BOOLEAN;
    private static byte DEFAULT_BYTE;
    private static short DEFAULT_SHORT;
    private static int DEFAULT_INT;
    private static long DEFAULT_LONG;
    private static float DEFAULT_FLOAT;
    private static double DEFAULT_DOUBLE;

    public static <T> T getDefaultValue(Class<T> clazz) {
        if (!clazz.isPrimitive()) {
            return null;
        } else if (clazz.equals(boolean.class)) {
            return clazz.cast(DEFAULT_BOOLEAN);
        } else if (clazz.equals(byte.class)) {
            return clazz.cast(DEFAULT_BYTE);
        } else if (clazz.equals(short.class)) {
            return clazz.cast(DEFAULT_SHORT);
        } else if (clazz.equals(int.class)) {
            return clazz.cast(DEFAULT_INT);
        } else if (clazz.equals(long.class)) {
            return clazz.cast(DEFAULT_LONG);
        } else if (clazz.equals(float.class)) {
            return clazz.cast(DEFAULT_FLOAT);
        } else if (clazz.equals(double.class)) {
            return clazz.cast(DEFAULT_DOUBLE);
        } else if (clazz.equals(String.class)) {
            return null;
        } else {
            throw new IllegalArgumentException(
                    "Class type " + clazz + " not supported");
        }
    }

    public static <T> T getTypedValue(Class<T> clazz, String value) {
        boolean isEmptyValue = value == null || value.isBlank();
        if (clazz.equals(boolean.class)) {
            return clazz.cast(isEmptyValue ? getDefaultValue(clazz) : Boolean.getBoolean(value));
        } else if (clazz.equals(Boolean.class)) {
            return clazz.cast(isEmptyValue ? null : Boolean.parseBoolean(value));
        } else if (clazz.equals(short.class)) {
            return clazz.cast(isEmptyValue ? getDefaultValue(clazz) : Short.parseShort(value));
        } else if (clazz.equals(Short.class)) {
            return clazz.cast(isEmptyValue ? null : Short.parseShort(value));
        } else if (clazz.equals(int.class)) {
            return clazz.cast(isEmptyValue ? getDefaultValue(clazz) : Integer.parseInt(value));
        } else if (clazz.equals(Integer.class)) {
            return clazz.cast(isEmptyValue ? null : Integer.parseInt(value));
        } else if (clazz.equals(long.class)) {
            return clazz.cast(isEmptyValue ? getDefaultValue(clazz) : Long.parseLong(value));
        } else if (clazz.equals(Long.class)) {
            return clazz.cast(isEmptyValue ? null : Long.parseLong(value));
        } else if (clazz.equals(float.class)) {
            return clazz.cast(isEmptyValue ? getDefaultValue(clazz) : Float.parseFloat(value));
        } else if (clazz.equals(Float.class)) {
            return clazz.cast(isEmptyValue ? null : Float.parseFloat(value));
        } else if (clazz.equals(double.class)) {
            return clazz.cast(isEmptyValue ? getDefaultValue(clazz) : Double.parseDouble(value));
        } else if (clazz.equals(Double.class)) {
            return clazz.cast(isEmptyValue ? null : Double.parseDouble(value));
        } else if (clazz.equals(UUID.class)) {
            return clazz.cast(isEmptyValue ? null : UUID.fromString(value));
        } else if (clazz.equals(String.class)) {
            return clazz.cast(value);
        } else {
            throw new IllegalArgumentException(
                    "Class type " + clazz + " not supported");
        }
    }
}
