/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpserver.requests
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.httpserver.requests;

import org.appwork.storage.Storable;

/**
 * @author daniel
 * 
 */
public class AESJSonRequest implements Storable {

    private String   url;
    private long     timestamp;
    private Object[] param;

    public AESJSonRequest(/* Storable */) {
    }

    public Object[] getParam() {
        return this.param;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getUrl() {
        return this.url;
    }

    public void setParam(final Object[] param) {
        this.param = param;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public void setUrl(final String url) {
        this.url = url;
    }
}
