/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remotecall.server
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remotecall.server;

/**
 * @author thomas
 */
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = -5299365199355353239L;

    public BadRequestException(final String string) {
        super(string);
    }

}
