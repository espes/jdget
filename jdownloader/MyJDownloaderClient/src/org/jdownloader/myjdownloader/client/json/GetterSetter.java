package org.jdownloader.myjdownloader.client.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author Thomas
 * 
 */
public class GetterSetter {
    private Method setter;
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * @param key
     */
    public GetterSetter(final String key) {
        this.key = key;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(final Method setter) {
        this.setter = setter;
    }

    public Method getGetter() {
        return getter;
    }

    public void setGetter(final Method getter) {
        this.getter = getter;
    }

    private Method getter;
    private Field  field;

    public Field getField() {
        return field;
    }

    /**
     * @param field
     */
    public void setField(final Field field) {
        this.field = field;

    }

    public boolean hasField() {
        return field != null;
    }

    public boolean hasGetter() {
        return getter != null;
    }

    public boolean hasSetter() {
        return setter != null;
    }

    /**
     * @param class1
     * @return
     */
    public boolean hasAnnotation(final Class<? extends Annotation> class1) {

        return getAnnotation(class1) != null;
    }

    /**
     * @param class1
     * @return
     */
    public <T extends Annotation> T getAnnotation(final Class<T> class1) {
        if (getter != null) {
            final T ann = getter.getAnnotation(class1);
            if (ann != null) { return ann; }
        }

        if (setter != null) {
            final T ann = setter.getAnnotation(class1);
            if (ann != null) { return ann; }
        }

        if (field != null) {
            final T ann = field.getAnnotation(class1);
            if (ann != null) { return ann; }
        }
        return null;
    }

    /**
     * @return
     */
    public Type getType() {
        if (getter != null) { return getter.getGenericReturnType(); }

        if (setter != null) { return setter.getGenericParameterTypes()[0]; }

        return null;
    }

    /**
     * @param b
     */
    public void setAccessible(final boolean b) {
        if (getter != null) {
            getter.setAccessible(b);
        }

        if (setter != null) {
            setter.setAccessible(b);
        }

        if (field != null) {
            field.setAccessible(b);
        }
    }

    /**
     * @param actionClass
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public Object get(final Object actionClass) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (getter != null) {
            getter.setAccessible(true);
            return getter.invoke(actionClass, new Object[] {});
        }
        if (field != null) {

            field.setAccessible(true);
            return field.get(actionClass);
        }
        throw new NullPointerException("Field and getter not available");
    }

    /**
     * @param action
     * @param v
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public void set(final Object action, final Object v) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (setter != null) {
            setter.setAccessible(true);
            setter.invoke(action, v);
            return;
        }
        if (field != null) {
            field.setAccessible(true);
            field.set(action, v);
        }

        throw new NullPointerException("Field and setter not available");

    }
}
