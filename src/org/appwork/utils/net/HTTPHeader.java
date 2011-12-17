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

/**
 * @author daniel
 * 
 */

public class HTTPHeader {

    public static final byte[] DELIMINATOR    = ": ".getBytes();
    private String             key;
    private String             value;
    private boolean            allowOverwrite = true;

    public boolean isAllowOverwrite() {
        return allowOverwrite;
    }

   

    public HTTPHeader(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    public HTTPHeader(final String key, final String value, boolean overwriteAllowed) {
        this.key = key;
        this.value = value;
        this.allowOverwrite = overwriteAllowed;
    }

    public String format() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.key);
        sb.append(": ");
        sb.append(this.value);
        return sb.toString();
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "HTTP Header: " + this.key + "= " + this.value;
    }

}
