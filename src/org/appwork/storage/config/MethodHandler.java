/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.Storable;
import org.appwork.storage.StorageException;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultDoubleValue;
import org.appwork.storage.config.annotations.DefaultEnumValue;
import org.appwork.storage.config.annotations.DefaultFloatValue;
import org.appwork.storage.config.annotations.DefaultIntValue;
import org.appwork.storage.config.annotations.DefaultLongValue;
import org.appwork.storage.config.annotations.DefaultObjectValue;
import org.appwork.storage.config.annotations.DefaultStringValue;
import org.appwork.storage.config.test.DefaultByteValue;

/**
 * @author thomas
 * 
 */
public class MethodHandler {

    public static enum Type {
        GETTER,
        SETTER;
    }

    private final Type    type;
    private final String  key;
    private final Method  method;

    private final boolean primitive;
    private Class<?>      rawClass;
    private boolean       defaultBoolean = false;
    private long          defaultLong    = 0l;
    private int           defaultInteger = 0;
    private Enum<?>       defaultEnum    = null;
    private double        defaultDouble  = 0.0d;
    private String        defaultString  = null;
    private byte          defaultByte    = 0;
    private Object        defaultObject  = null;
    private float         defaultFloat   = 0.0f;

    /**
     * @param getter
     * @param key
     * @param m
     * @param canStorePrimitive
     */
    public MethodHandler(final Type getter, final String key, final Method m, final boolean primitive) {
        this.type = getter;
        this.key = key;
        this.method = m;

        this.primitive = primitive;
        if (this.isGetter()) {
            this.rawClass = m.getReturnType();
        } else {
            this.rawClass = m.getParameterTypes()[0];
        }

        if (primitive) {
            if (this.rawClass == Boolean.class || this.rawClass == boolean.class) {
                final DefaultBooleanValue ann = m.getAnnotation(DefaultBooleanValue.class);
                if (ann != null) {
                    this.defaultBoolean = ann.value();
                }
            } else if (this.rawClass == Long.class || this.rawClass == long.class) {
                final DefaultLongValue ann = m.getAnnotation(DefaultLongValue.class);
                if (ann != null) {
                    this.defaultLong = ann.value();
                }
            } else if (this.rawClass == Integer.class || this.rawClass == int.class) {
                final DefaultIntValue ann = m.getAnnotation(DefaultIntValue.class);
                if (ann != null) {
                    this.defaultInteger = ann.value();
                }
            } else if (this.rawClass == Byte.class || this.rawClass == byte.class) {
                final DefaultByteValue ann = m.getAnnotation(DefaultByteValue.class);
                if (ann != null) {
                    this.defaultByte = ann.value();
                }

            } else if (this.rawClass == Float.class || this.rawClass == float.class) {
                final DefaultFloatValue ann = m.getAnnotation(DefaultFloatValue.class);
                if (ann != null) {
                    this.defaultFloat = ann.value();
                }
            } else if (this.rawClass == String.class) {
                final DefaultStringValue ann = m.getAnnotation(DefaultStringValue.class);
                if (ann != null) {
                    this.defaultString = ann.value();
                }
            } else if (this.rawClass.isEnum()) {
                final DefaultEnumValue ann = m.getAnnotation(DefaultEnumValue.class);
                if (ann != null) {
                    // chek if this is really the best way to convert string to
                    // enum
                    final int index = ann.value().lastIndexOf(".");
                    final String name = ann.value().substring(index + 1);
                    Class<?> enumClass;
                    try {
                        final String clazz = ann.value().substring(0, index);
                        enumClass = Class.forName(clazz);

                        for (final Object e : enumClass.getEnumConstants()) {
                            if (e.toString().equals(name)) {
                                this.defaultEnum = (Enum<?>) e;
                                break;
                            }
                        }
                    } catch (final ClassNotFoundException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                }
            } else if (this.rawClass == Double.class || this.rawClass == double.class) {
                final DefaultDoubleValue ann = m.getAnnotation(DefaultDoubleValue.class);
                if (ann != null) {
                    this.defaultDouble = ann.value();
                }
            } else {
                throw new StorageException("Invalid datatype: " + this.rawClass);
            }
        } else {

            final DefaultObjectValue ann = m.getAnnotation(DefaultObjectValue.class);
            if (ann != null) {
                this.defaultObject = JSonStorage.restoreFromString(ann.value(), this.rawClass);
            }
            if (this.defaultObject == null) {
                if (Storable.class.isAssignableFrom(this.rawClass)) {
                    Constructor<?> c;
                    try {
                        c = this.rawClass.getDeclaredConstructor(new Class[] {});
                        c.setAccessible(true);
                        this.defaultObject = c.newInstance(null);
                    } catch (final SecurityException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (final NoSuchMethodException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (final IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (final InstantiationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (final IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (final InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public boolean getDefaultBoolean() {
        return this.defaultBoolean;
    }

    public byte getDefaultByte() {
        return this.defaultByte;
    }

    public double getDefaultDouble() {
        return this.defaultDouble;
    }

    public Enum<?> getDefaultEnum() {
        return this.defaultEnum;
    }

    public float getDefaultFloat() {
        return this.defaultFloat;
    }

    public int getDefaultInteger() {
        return this.defaultInteger;
    }

    public long getDefaultLong() {
        return this.defaultLong;
    }

    public Object getDefaultObject() {
        return this.defaultObject;
    }

    public String getDefaultString() {
        return this.defaultString;
    }

    public String getKey() {
        return this.key;
    }

    public Method getMethod() {
        return this.method;
    }

    private String getObjectKey() {
        return "cfg/config_" + this.method.getDeclaringClass().getSimpleName() + "." + this.key + ".json";
    }

    public Class<?> getRawClass() {
        return this.rawClass;
    }

    public Type getType() {
        return this.type;
    }

    /**
     * @return
     */
    public boolean isGetter() {

        return this.type == Type.GETTER;
    }

    public boolean isPrimitive() {
        return this.primitive;
    }

    /**
     * @return
     */
    public Object read() {

        return JSonStorage.restoreFrom(this.getObjectKey(), new TypeRef(this.method.getGenericReturnType()), this.defaultObject);
    }

    public void setDefaultBoolean(final boolean defaultBoolean) {
        this.defaultBoolean = defaultBoolean;
    }

    public void setDefaultByte(final byte defaultByte) {
        this.defaultByte = defaultByte;
    }

    public void setDefaultDouble(final double defaultDouble) {
        this.defaultDouble = defaultDouble;
    }

    public void setDefaultEnum(final Enum<?> defaultEnum) {
        this.defaultEnum = defaultEnum;
    }

    public void setDefaultFloat(final float defaultFloat) {
        this.defaultFloat = defaultFloat;
    }

    public void setDefaultInteger(final int defaultInteger) {
        this.defaultInteger = defaultInteger;
    }

    public void setDefaultLong(final long defaultLong) {
        this.defaultLong = defaultLong;
    }

    public void setDefaultObject(final Object defaultObject) {
        this.defaultObject = defaultObject;
    }

    public void setDefaultString(final String defaultString) {
        this.defaultString = defaultString;
    }

    public void setRawClass(final Class<?> rawClass) {
        this.rawClass = rawClass;
    }

    /**
     * @param object
     */
    public void write(final Object object) {
        JSonStorage.storeTo(this.getObjectKey(), object);

    }
}
