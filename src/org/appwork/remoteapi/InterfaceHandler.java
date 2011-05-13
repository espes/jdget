/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import org.appwork.storage.InvalidTypeException;
import org.appwork.storage.JSonStorage;

/**
 * @author thomas
 * 
 */
public class InterfaceHandler<T> {

    /**
     * @param c
     * @param x
     * @return
     * @throws ParseException
     */
    public static <T extends RemoteAPIInterface> InterfaceHandler<T> create(final Class<T> c, final RemoteAPIInterface x) throws ParseException {
        final InterfaceHandler<T> ret = new InterfaceHandler<T>(c, x);
        ret.parse();
        return ret;
    }

    private final RemoteAPIInterface                                   impl;
    private final Class<T>                                             interfaceClass;
    private final HashMap<String, HashMap<Integer, ArrayList<Method>>> methods;

    /**
     * @param <T>
     * @param c
     * @param x
     */
    private InterfaceHandler(final Class<T> c, final RemoteAPIInterface x) {
        this.interfaceClass = c;
        this.impl = x;
        this.methods = new HashMap<String, HashMap<Integer, ArrayList<Method>>>();
    }

    /**
     * @throws ParseException
     * 
     */
    private void parse() throws ParseException {
        for (final Method m : this.interfaceClass.getMethods()) {
            this.validateMethod(m);
            HashMap<Integer, ArrayList<Method>> methodsByName = this.methods.get(m.getName());
            if (methodsByName == null) {
                methodsByName = new HashMap<Integer, ArrayList<Method>>();
                this.methods.put(m.getName(), methodsByName);
            }
            ArrayList<Method> methodsByNameAndArgumentCount = methodsByName.get(m.getParameterTypes().length);
            if (methodsByNameAndArgumentCount == null) {
                methodsByNameAndArgumentCount = new ArrayList<Method>();
                methodsByName.put(m.getParameterTypes().length, methodsByNameAndArgumentCount);
            }

            methodsByNameAndArgumentCount.add(m);

        }

    }

    /**
     * @param m
     * @throws ParseException
     */
    private void validateMethod(final Method m) throws ParseException {

        try {
            JSonStorage.canStore(m.getGenericReturnType());
        } catch (final InvalidTypeException e) {
            throw new ParseException("return Type of " + m + " is invalid", e);
        }

        for (final Type t : m.getGenericParameterTypes()) {
            try {
                JSonStorage.canStore(t);
            } catch (final InvalidTypeException e) {
                throw new ParseException("Parameter " + t + " of " + m + " is invalid", e);
            }
        }

    }
}
