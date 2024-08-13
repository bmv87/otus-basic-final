package ru.otus.web.handlers;

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

    public static Object getDefaultValue(Class clazz) {
        if (!clazz.isPrimitive()) {
            return null;
        } else if (clazz.equals(boolean.class)) {
            return DEFAULT_BOOLEAN;
        } else if (clazz.equals(byte.class)) {
            return DEFAULT_BYTE;
        } else if (clazz.equals(short.class)) {
            return DEFAULT_SHORT;
        } else if (clazz.equals(int.class)) {
            return DEFAULT_INT;
        } else if (clazz.equals(long.class)) {
            return DEFAULT_LONG;
        } else if (clazz.equals(float.class)) {
            return DEFAULT_FLOAT;
        } else if (clazz.equals(double.class)) {
            return DEFAULT_DOUBLE;
        } else if (clazz.equals(String.class)) {
            return null;
        } else {
            throw new IllegalArgumentException(
                    "Class type " + clazz + " not supported");
        }
    }

    public static Object getTypedValue(Class clazz, String value) {
        boolean isEmptyValue = value == null || value.isBlank();
        if (clazz.equals(boolean.class)) {
            return isEmptyValue ? getDefaultValue(clazz) : Boolean.getBoolean(value);
        } else if (clazz.equals(Boolean.class)) {
            return isEmptyValue ? null : Boolean.parseBoolean(value);
        } else if (clazz.equals(short.class)) {
            return isEmptyValue ? getDefaultValue(clazz) : Short.parseShort(value);
        } else if (clazz.equals(Short.class)) {
            return isEmptyValue ? null : Short.parseShort(value);
        } else if (clazz.equals(int.class)) {
            return isEmptyValue ? getDefaultValue(clazz) : Integer.parseInt(value);
        } else if (clazz.equals(Integer.class)) {
            return isEmptyValue ? null : Integer.parseInt(value);
        } else if (clazz.equals(long.class)) {
            return isEmptyValue ? getDefaultValue(clazz) : Long.parseLong(value);
        } else if (clazz.equals(Long.class)) {
            return isEmptyValue ? null : Long.parseLong(value);
        } else if (clazz.equals(float.class)) {
            return isEmptyValue ? getDefaultValue(clazz) : Float.parseFloat(value);
        } else if (clazz.equals(Float.class)) {
            return isEmptyValue ? null : Float.parseFloat(value);
        } else if (clazz.equals(double.class)) {
            return isEmptyValue ? getDefaultValue(clazz) : Double.parseDouble(value);
        } else if (clazz.equals(Double.class)) {
            return isEmptyValue ? null : Double.parseDouble(value);
        } else if (clazz.equals(UUID.class)) {
            return isEmptyValue ? null : UUID.fromString(value);
        } else if (clazz.equals(String.class)) {
            return value;
        } else {
            throw new IllegalArgumentException(
                    "Class type " + clazz + " not supported");
        }
    }
}
