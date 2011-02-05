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

import org.appwork.storage.JSonStorage;
import org.appwork.storage.Storable;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

/**
 * @author thomas
 */
public class ExceptionWrapper implements Storable {

    private String _exception;

    private String name;

    private ExceptionWrapper() {
        // we need this for JSON Serial.
    }

    public ExceptionWrapper(final Throwable e) throws IOException {
        this._exception = JSonStorage.serializeToJson(e);
        this.name = e.getClass().getName();
    }

    public Throwable deserialiseException() throws ClassNotFoundException, JsonParseException, JsonMappingException, IOException {
        // tries to cast to the correct exception
        final Class<?> clazz = Class.forName(this.name);
        return (Throwable) JSonStorage.restoreFromString(this._exception, clazz);
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
