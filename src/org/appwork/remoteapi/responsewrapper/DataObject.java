/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.responsewrapper
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.responsewrapper;

import org.appwork.storage.Storable;

/**
 * @author Thomas
 * 
 */
public class DataObject implements Storable {
    private Object data;

    public DataObject(/* Storable */) {

    }

    /**
     * @param responseObject
     */
  
    public DataObject(final Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(final Object data) {
        this.data = data;
    }

}
