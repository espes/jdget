/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.upload
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.upload;

import java.io.IOException;

/**
 * @author daniel
 * 
 */
public class UploadHashException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = 2095361520898288289L;

    /**
     * @param string
     */
    public UploadHashException(final String string) {
        super(string);
    }

}
