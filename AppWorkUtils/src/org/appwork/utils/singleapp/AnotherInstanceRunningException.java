/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.singleapp
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.singleapp;

/**
 * @author daniel
 * 
 */
final public class AnotherInstanceRunningException extends Exception {
    /**
	 * 
	 */
    private static final long serialVersionUID = 8030177049286809538L;

    public AnotherInstanceRunningException(final String AppID) {
        super(AppID + " is already running!");
    }
}
