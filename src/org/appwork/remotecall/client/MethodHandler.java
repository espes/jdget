/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remotecall.client
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remotecall.client;

import java.lang.reflect.Method;

import org.appwork.storage.TypeRef;

/**
 * @author Thomas
 * 
 */
public class MethodHandler {

    private Method            method;
    private TypeRef<Object>[] typeRefs;

    /**
     * @param m
     */
    @SuppressWarnings("unchecked")
    public MethodHandler(Method m) {
        this.method = m;
        typeRefs = (TypeRef<Object>[]) new TypeRef[m.getGenericParameterTypes().length];
        for (int i = 0; i < m.getGenericParameterTypes().length; i++) {
            typeRefs[i] = new TypeRef<Object>(m.getGenericParameterTypes()[i]) {
            };

        }
    }

    /**
     * @param method2
     * @param parameters
     * @return
     */
    public Object call(String method2, String[] parameters) {
        // TODO Auto-generated method stub
        return null;
    }



    /**
     * @return
     */
    public Method getMethod() {
        // TODO Auto-generated method stub
        return method;
    }

    /**
     * @return
     */
    public TypeRef<Object>[] getTypeRefs() {
   
        return typeRefs;
    }

}
