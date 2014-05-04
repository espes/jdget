/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.reflection
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.reflection;

import java.lang.reflect.Type;

/**
 * @author thomas
 * 
 */
public class Clazz {
    /**
     * @param class1
     * @return
     */
    public static String getPackage(final Class<?> clazz) {
        return clazz.getPackage().getName();
    }

    /**
     * returns true if type is a boolean. No Matter if primitive or it's object
     * wrapper
     * 
     * @param type
     * @return
     */
    public static boolean isBoolean(final Type type) {
        return type == Boolean.class || type == boolean.class;
    }

    /**
     * returns true if type is a byte. No Matter if primitive or it's object
     * wrapper
     * 
     * @param type
     * @return
     */
    public static boolean isByte(final Type type) {
        return type == Byte.class || type == byte.class;
    }

    /**
     * returns true if type is a char. No Matter if primitive or it's object
     * wrapper
     * 
     * @param type
     * @return
     */
    public static boolean isCharacter(final Type type) {
        return type == Character.class || type == char.class;
    }

    /**
     * returns true if type is a double. No Matter if primitive or it's object
     * wrapper
     * 
     * @param type
     * @return
     */
    public static boolean isDouble(final Type type) {
        return type == Double.class || type == double.class;
    }

    /**
     * returns true if type is a float. No Matter if primitive or it's object
     * wrapper
     * 
     * @param type
     * @return
     */
    public static boolean isFloat(final Type type) {
        return type == Float.class || type == float.class;
    }

    /**
     * returns true if type is a int. No Matter if primitive or it's object
     * wrapper
     * 
     * @param type
     * @return
     */
    public static boolean isInteger(final Type type) {
        return type == Integer.class || type == int.class;
    }

    /**
     * returns true if type is a long. No Matter if primitive or it's object
     * wrapper
     * 
     * @param type
     * @return
     */
    public static boolean isLong(final Type type) {
        return type == Long.class || type == long.class;
    }

    /**
     * returns true if type is a primitive or a priomitive object wrapper
     * 
     * @param type
     * @return
     */
    public static boolean isPrimitive(final Type type) {
        if (type instanceof Class) { return ((Class<?>) type).isPrimitive() || Clazz.isPrimitiveWrapper(type); }
        return false;
    }

    /**
     * returns true if type os a primitive object wrapper
     * 
     * @param type
     * @return
     */
    public static boolean isPrimitiveWrapper(final Type type) {
        return type == Boolean.class || type == Integer.class || type == Long.class || type == Byte.class || type == Short.class || type == Float.class || type == Double.class || type == Character.class || type == Void.class;

    }

    /**
     * returns true if type is a short. No Matter if primitive or it's object
     * wrapper
     * 
     * @param type
     * @return
     */
    public static boolean isShort(final Type type) {
        return type == Short.class || type == short.class;
    }

    /**
     * returns true if type is a void. No Matter if primitive or it's object
     * wrapper
     * 
     * @param type
     * @return
     */
    public static boolean isVoid(final Type type) {
        return type == Void.class || type == void.class;
    }

    /**
     * @param type
     * @return
     */
    public static boolean isString(final Type type) {

        return type == String.class;
    }

    /**
     * @param type
     * @return
     */
    public static boolean isEnum(final Type type) {

        return type instanceof Class && ((Class<?>) type).isEnum();
    }

    /**
     * @param genericReturnType
     * @return
     */
    public static boolean isByteArray(final Type genericReturnType) {
        // TODO Auto-generated method stub
        return genericReturnType == byte[].class;
    }

    /**
     * is a instanceof b
     * @param c
     * @param class1
     * @return
     */
    public static boolean isInstanceof(final Class<?> a, final Class<?> b) {
        final boolean ret = b.isAssignableFrom(a);
        return ret;
    }

}
