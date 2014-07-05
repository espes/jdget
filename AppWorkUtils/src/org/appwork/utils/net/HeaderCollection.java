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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author daniel
 * 
 */
public class HeaderCollection implements Iterable<HTTPHeader> {
    private final CopyOnWriteArrayList<HTTPHeader> collection            = new CopyOnWriteArrayList<HTTPHeader>();
    private final CopyOnWriteArraySet<String>      allowedDuplicatedKeys = new CopyOnWriteArraySet<String>();

    public HeaderCollection() {
        this.allowedDuplicatedKeys.add("Set-Cookies".toLowerCase(Locale.ENGLISH));
    }

    public void add(final HTTPHeader header) {
        final HTTPHeader existingHeader = this.get(header.getKey());
        if (existingHeader != null) {
            if (!this.allowedDuplicatedKeys.contains(header.getKey().toLowerCase(Locale.ENGLISH))) {
                if (!existingHeader.isAllowOverwrite()) { return; }
                this.remove(existingHeader);
            }
        }
        this.collection.add(header);
    }

    public void clear() {
        this.collection.clear();
    }

    @Override
    public HeaderCollection clone() {
        final HeaderCollection ret = new HeaderCollection();
        ret.collection.addAll(this.collection);
        return ret;
    }

    public HTTPHeader get(final int index) {
        return this.collection.get(index);
    }

    public HTTPHeader get(final String key) {
        if (key == null) { return null; }
        for (final HTTPHeader header : this.collection) {
            if (header.getKey().equalsIgnoreCase(key)) { return header; }
        }
        return null;
    }

    public List<HTTPHeader> getAll(final String key) {
        final ArrayList<HTTPHeader> ret = new ArrayList<HTTPHeader>();
        for (final HTTPHeader header : this.collection) {
            if (header.getKey().equalsIgnoreCase(key)) {
                ret.add(header);
            }
        }
        if (ret.size() > 0) { return ret; }
        return null;
    }

    public CopyOnWriteArraySet<String> getAllowedDuplicatedKeys() {
        return this.allowedDuplicatedKeys;
    }

    public String getValue(final String key) {
        final HTTPHeader ret = this.get(key);
        if (ret != null) { return ret.getValue(); }
        return null;
    }

    @Override
    public Iterator<HTTPHeader> iterator() {
        return this.collection.iterator();
    }

    public boolean remove(final HTTPHeader header) {
        if (this.collection.remove(header)) {
            return true;
        } else {
            return this.remove(header.getKey());
        }
    }

    public boolean remove(final String key) {
        final HTTPHeader existingHeader = this.get(key);
        if (existingHeader != null) {
            return this.collection.remove(existingHeader);
        } else {
            return false;
        }
    }

    public int size() {
        return this.collection.size();
    }

    @Override
    public String toString() {
        return this.collection.toString();
    }

}
