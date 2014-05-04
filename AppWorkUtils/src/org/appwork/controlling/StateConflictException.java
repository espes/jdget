/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.controlling
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.controlling;

/**
 * @author thomas
 * 
 */
public class StateConflictException extends Error {

    private static final long serialVersionUID = -5880954175256200217L;

    /**
     * @param string
     */
    public StateConflictException(String string) {
        super(string);
    }

}
