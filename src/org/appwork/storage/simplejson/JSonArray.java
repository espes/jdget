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

import java.util.ArrayList;

/**
 * @author thomas
 * 
 */
public class JSonArray extends ArrayList<JSonNode> implements JSonNode {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public JSonArray() {
        super();
    }

    @Override
    public boolean add(final JSonNode e) {
        // System.err.println(e);
        return super.add(e);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("[");
        for (final JSonNode n : this) {
            if (sb.length() > 1) {
                sb.append(",");
            }
            sb.append(n.toString());
        }
        sb.append("]");
        return sb.toString();

    }
}
