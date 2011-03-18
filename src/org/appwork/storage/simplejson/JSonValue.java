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

/**
 * @author thomas
 * 
 */
public class JSonValue implements JSonNode {

    private final Object value;
    private final Type   type;

    /**
     * @param b
     */
    public JSonValue(final boolean b) {
        this.value = b;
        this.type = Type.BOOLEAN;
    }

    /**
     * @param parseDouble
     */
    public JSonValue(final double d) {
        this.value = d;
        this.type = Type.DOUBLE;
    }

    public JSonValue(final long l) {
        this.value = l;
        this.type = Type.LONG;
    }

    /**
     * @param object
     */
    public JSonValue(final String str) {
        if (str == null) {
            this.value = null;
            this.type = Type.NULL;
        } else {
            this.value = str;
            this.type = Type.STRING;
        }
    }

    public Type getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        switch (this.type) {
        case BOOLEAN:
        case DOUBLE:
        case LONG:
            return this.value.toString();
        case STRING:
            return "\"" + JSonUtils.escape(this.value.toString()) + "\"";
        case NULL:
            return "null";

        }
        return null;
    }
}
