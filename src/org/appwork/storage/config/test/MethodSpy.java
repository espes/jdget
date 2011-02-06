/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author thomas
 * 
 */
public class MethodSpy {
    private static final String fmt = "%24s: %s%n";

    public static void main(final String... args) {
        final Class<?> c = MyInterface.class;
        final Method[] allMethods = c.getDeclaredMethods();
        for (final Method m : allMethods) {

            System.out.format("%s%n", m.toGenericString());

            System.out.format(MethodSpy.fmt, "ReturnType", m.getReturnType());
            System.out.format(MethodSpy.fmt, "GenericReturnType", m.getGenericReturnType());

            final Class<?>[] pType = m.getParameterTypes();
            final Type[] gpType = m.getGenericParameterTypes();
            for (int i = 0; i < pType.length; i++) {
                System.out.format(MethodSpy.fmt, "ParameterType", pType[i]);
                System.out.format(MethodSpy.fmt, "GenericParameterType", gpType[i]);
            }

        }
    }

    // for the morbidly curious
    <E extends RuntimeException> void genericThrow() throws E {
    }
}
