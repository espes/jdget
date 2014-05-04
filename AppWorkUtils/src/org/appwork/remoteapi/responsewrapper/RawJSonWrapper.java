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

import org.appwork.remoteapi.AbstractResponseWrapper;
import org.appwork.storage.JSonStorage;

/**
 * @author Thomas
 * 
 */
public class RawJSonWrapper extends AbstractResponseWrapper<Object> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.remoteapi.AbstractResponseWrapper#toString(java.lang.Object)
     */
    @Override
    public String toString(final Object responseObject) {

        return JSonStorage.serializeToJson(responseObject);
    }

}
