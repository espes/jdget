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

import java.lang.reflect.Method;
import java.net.URLEncoder;

import org.appwork.remotecall.client.SerialiseException;
import org.appwork.storage.JSonStorage;

/**
 * @author thomas
 * 
 */
public class Utils {

    public static final char PARAMETER_DELIMINATOR = '\n';

    /**
     * @param returnValue
     * @param returnType
     * @return
     */
    public static Object convert(final Object obj, final Class<?> class1) {

        if (class1 == int.class) {
            return ((Number) obj).intValue();
        } else if (class1 == long.class) {
            //
            return ((Number) obj).longValue();
        } else if (class1 == void.class) { return null; }

        return obj;

    }

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

    public static String serialise(final Object[] args) throws SerialiseException {

        if (args == null) { return ""; }
        final StringBuilder sb = new StringBuilder();
        for (final Object o : args) {
            if (sb.length() > 0) {
                sb.append(Utils.PARAMETER_DELIMINATOR);
            }
            sb.append(Utils.serialiseSingleObject(o));
        }
        return sb.toString();
    }

    public static String serialiseSingleObject(final Object o) throws SerialiseException {
        try {
            return URLEncoder.encode(JSonStorage.toString(o), "UTF-8");

        } catch (final Exception e) {
            throw new SerialiseException(e);

        }
    }

}
