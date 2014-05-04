/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remotecall.server
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remotecall.server;

import java.io.IOException;
import java.lang.reflect.Constructor;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.SimpleMapper;
import org.appwork.storage.Storable;
import org.appwork.storage.simplejson.Ignores;


/**
 * @author thomas
 */
@Ignores({ "" })
public class ExceptionWrapper implements Storable {

    private String _exception;

    private String name;

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private ExceptionWrapper() {
        // we need this for JSON Serial.
    }

    private static final SimpleMapper JSON_MAPPER = new SimpleMapper();

    public ExceptionWrapper(final Throwable e) throws IOException {
        if (e instanceof RemoteCallException) {
            this._exception = JSON_MAPPER.objectToString(e);
        } else {
            if(e.getStackTrace().length>0){
                message = e.getMessage()+ " @"+e.getStackTrace()[0].getClassName()+"."+e.getStackTrace()[0].getMethodName()+"("+e.getStackTrace()[0].getFileName()+":"+e.getStackTrace()[0].getLineNumber()+")";  
            }else{
            message = e.getMessage();
            }
        }

        this.name = e.getClass().getName();
    }

    public Throwable deserialiseException() throws ClassNotFoundException, IOException {
        // tries to cast to the correct exception
        final Class<?> clazz = Class.forName(this.name);
        if (_exception != null) {

            return (Throwable) JSonStorage.restoreFromString(this._exception, clazz);
        } else {
            try {
                Constructor<?> c = clazz.getConstructor(new Class[] { String.class });
                Object ret = c.newInstance(new Object[] { message });
                return (Throwable) ret;
            } catch (Throwable e) {

                try {
                    return (RuntimeException) clazz.newInstance();
                } catch (InstantiationException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }

            return new RuntimeException(name);
        }
    }

    public String getException() {
        return this._exception;
    }

    public String getName() {
        return this.name;
    }

    public void setException(final String _exception) {
        this._exception = _exception;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
