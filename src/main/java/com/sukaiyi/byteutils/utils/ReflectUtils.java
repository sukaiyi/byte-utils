package com.sukaiyi.byteutils.utils;

import java.lang.reflect.Field;

/**
 * @author sukaiyi
 * @date 2020/07/31
 */
public class ReflectUtils {

    private ReflectUtils() {

    }

    public static Object getFieldValue(Object ins, String field) {
        Class<?> clazz = ins.getClass();
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(ins);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getFieldValue(Object ins, Field field) {
        try {
            field.setAccessible(true);
            return field.get(ins);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setFieldValue(Object ins, String field, Object value) {
        Class<?> clazz = ins.getClass();
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            f.set(ins, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setFieldValue(Object ins, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(ins, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
