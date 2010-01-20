/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.storage
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.storage;

public class DBException extends Error {

    public DBException(Exception e) {
        super(e);
    }

    /**
     * @param message
     */
    public DBException(String message) {
        super(message);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5046118648749420384L;

}
