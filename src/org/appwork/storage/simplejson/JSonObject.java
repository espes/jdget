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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @author thomas
 * 
 */
public class JSonObject extends HashMap<String, JSonNode> implements JSonNode {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Type        type;

    private final Object      value;

    /**
     * 
     */
    public JSonObject() {
        this.type = Type.OBJECT;
        this.value = null;

    }

    /**
     * @param b
     */
    public JSonObject(final boolean b) {
        this.type = Type.BOOLEAN;
        this.value = b;
    }

    /**
     * @param parseDouble
     */
    public JSonObject(final double d) {
        this.type = Type.DOUBLE;
        this.value = d;
    }

    public JSonObject(final long d) {
        this.type = Type.LONG;
        this.value = d;
    }

    /**
     * @param key
     * @param substring
     */
    public JSonObject(final String value) {
        this.type = value == null ? Type.NULL : Type.STRING;
        this.value = value;

    }

    public Type getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public JSonNode put(final String key, final JSonNode value) {
        // System.out.println(key + " : " + value);
        return super.put(key, value);
    }

    @Override
    public String toString() {

        switch (this.type) {
        case NULL:
            return "null";
        case STRING:
            return "\"" + this.value + "\"";
        case OBJECT:
            final StringBuilder sb = new StringBuilder();
            sb.append("{");
            Entry<String, JSonNode> next;
            for (final Iterator<Entry<String, JSonNode>> it = this.entrySet().iterator(); it.hasNext();) {
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                next = it.next();
                sb.append("\"");
                sb.append(next.getKey());
                sb.append("\"");
                sb.append(" : ");
                sb.append(next.getValue());
            }
            sb.append("}");
            return sb.toString();
        default:
            return this.value + "";
        }

    }
}
