/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

/**
 * @author daniel
 * 
 */
public class HeaderCollection implements Iterable<HTTPHeader> {
    private LinkedList<HTTPHeader>          headers;
    private static HashMap<String, Boolean> DUPES_ALLOWED = new HashMap<String, Boolean>();
    static {
        HeaderCollection.DUPES_ALLOWED.put("Set-Cookies".toLowerCase(Locale.ENGLISH), true);
    }

    public void add(final HTTPHeader header) {
        HTTPHeader existingHeader = null;
        if ((existingHeader = get(header.getKey())) != null) {
            if (!HeaderCollection.DUPES_ALLOWED.containsKey(header.getKey().toLowerCase(Locale.ENGLISH))) {
                // overwrite
                if (existingHeader.isAllowOverwrite()) {
                    // Log.L.warning("Overwrite Header: " + header);
                    headers.remove(existingHeader);
                } else {
                    // Log.L.warning("Header must not be overwritten: " +
                    // header);
                    return;
                }
            }
        }
        this.headers.add(header);
    }

    public boolean remove(final HTTPHeader header) {
        return remove(header.getKey());
    }

    public boolean remove(final String key) {
        HTTPHeader existingHeader = get(key);
        if (existingHeader != null) {
            headers.remove(existingHeader);
            return true;
        } else {
            return false;
        }
    }

    public HeaderCollection clone() {
        HeaderCollection ret = new HeaderCollection();
        ret.headers = new LinkedList<HTTPHeader>(this.headers);
        return ret;
    }

    public HeaderCollection() {
        this.headers = new LinkedList<HTTPHeader>();
    }

    public String toString() {
        return headers.toString();
    }

    public void clear() {
        headers = new LinkedList<HTTPHeader>();
    }

    public HTTPHeader get(final String key) {
        for (final Iterator<HTTPHeader> it = this.headers.iterator(); it.hasNext();) {
            HTTPHeader elem;
            if ((elem = it.next()).getKey().equalsIgnoreCase(key)) { return elem; }
        }
        return null;
    }

    public String getValue(final String key) {
        final HTTPHeader ret = get(key);
        if (ret != null) { return ret.getValue(); }
        return null;
    }

    @Override
    public Iterator<HTTPHeader> iterator() {
        return this.headers.iterator();
    }

}
