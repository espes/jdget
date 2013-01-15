/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi;

import java.util.HashMap;

import org.appwork.storage.Storable;

/**
 * @author Sebastian
 * 
 */
public class APIQuery extends HashMap<String, Object> implements Storable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public <T> T _getQueryParam(String key, Class<T> clazz, T defaultValue) {
        T result = (T) super.get(key);
        return result != null ? result : defaultValue;
    }

    public Integer getStartAt() {
        return _getQueryParam("startAt", Integer.class, -1);
    }

    public Integer getMaxResults() {
        return _getQueryParam("maxResults", Integer.class, -1);
    }
}