/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson;

import org.appwork.storage.Storable;

/**
 * @author thomas
 * 
 */
public class JSonValue implements JSonNode, Storable {

    private Object value;

    public void setValue(final Object value) {
        this.value = value;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    private Type type;

    private JSonValue(/* Storable */) {
    }

    /**
     * @param b
     */
    public JSonValue(final boolean b) {
        value = b;
        type = Type.BOOLEAN;
    }

    /**
     * @param parseDouble
     */
    public JSonValue(final double d) {
        value = d;
        type = Type.DOUBLE;
    }

    public JSonValue(final long l) {
        value = l;
        type = Type.LONG;
    }

    /**
     * @param object
     */
    public JSonValue(final String str) {
        if (str == null) {
            value = null;
            type = Type.NULL;
        } else {
            value = str;
            type = Type.STRING;
        }
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        switch (type) {
        case BOOLEAN:
        case DOUBLE:
        case LONG:
            return value.toString();
        case STRING:
            return "\"" + JSonUtils.escape(value.toString()) + "\"";
        case NULL:
            return "null";

        }
        return null;
    }
}
