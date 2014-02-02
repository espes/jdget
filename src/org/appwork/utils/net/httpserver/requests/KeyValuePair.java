/**
 * Copyright (c) 2009 - 2014 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.requests
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.requests;

/**
 * @author thomas
 * 
 */
public class KeyValuePair {
    /**
     * @param decode
     * @param object
     */
    public KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * @param string
     */
    public KeyValuePair(String value) {
        this(null, value);
    }

    public String key;
    public String value;
}
