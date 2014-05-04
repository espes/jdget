/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remotecall
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remotecall;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLEncoder;

import org.appwork.remotecall.client.SerialiseException;
import org.appwork.storage.JSonStorage;
import org.appwork.utils.reflection.Clazz;

/**
 * @author thomas
 * 
 */
public class Utils {

    /**
     * @param m
     * @return
     */
    public static String createMethodFingerPrint(final Method m) {
        final StringBuilder sb = new StringBuilder();

        sb.append(m.getName());
        sb.append('(');
        boolean first = true;
        for (final Class<?> c : m.getParameterTypes()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append(c.getName());
        }
        sb.append(')');
        return sb.toString();
    }

    public static String serialise(final Object[] args) throws SerialiseException, UnsupportedEncodingException {

        if (args == null) { return ""; }
        final StringBuilder sb = new StringBuilder();
        for (final Object o : args) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(serialiseSingleObject(o));
        }
        return sb.toString();
    }

    public static String serialiseSingleObject(final Object o) throws SerialiseException {
        try {
            return URLEncoder.encode(JSonStorage.serializeToJson(o), "UTF-8");

        } catch (final Exception e) {
            throw new SerialiseException(e);

        }
    }

    /**
     * @param string
     * @param types
     * @return
     * @throws IOException
     */
    public static Object convert(final Object obj, Type type) throws IOException {

        if (Clazz.isPrimitive(type)) {
            if (Clazz.isByte(type)) {
                return ((Number) obj).byteValue();
            } else if (Clazz.isDouble(type)) {
                return ((Number) obj).doubleValue();
            } else if (Clazz.isFloat(type)) {
                return ((Number) obj).floatValue();
            } else if (Clazz.isLong(type)) {
                return ((Number) obj).longValue();
            } else if (Clazz.isInteger(type)) { return ((Number) obj).intValue(); }

        }
        return obj;

    }

}
