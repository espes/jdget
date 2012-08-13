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

    /**
     * 
     */
    public JSonObject() {
        super();

    }

    @Override
    public JSonNode put(final String key, final JSonNode value) {
        // System.out.println(key + " : " + value);
        return super.put(key, value);
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        Entry<String, JSonNode> next;
        for (final Iterator<Entry<String, JSonNode>> it = this.entrySet().iterator(); it.hasNext();) {
            if (sb.length() > 1) {
                sb.append(",");
            }
            next = it.next();
            sb.append("\"");
            sb.append(JSonUtils.escape(next.getKey()));
            sb.append("\"");
            sb.append(":");
            sb.append(next.getValue().toString());
        }
        sb.append("}");
        return sb.toString();

    }

}
