/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.shutdown
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.shutdown;

/**
 * @author thomas
 * 
 */
public class ShutdownVetoException extends Exception {

    /**
     * 
     */
    private static final long    serialVersionUID = 1L;
    private ShutdownVetoListener source           = null;

    public ShutdownVetoException(final String localizedCause, final ShutdownVetoListener source) {
        super(localizedCause);
        this.source = source;
    }

    /**
     * @return the source
     */
    public ShutdownVetoListener getSource() {
        return this.source;
    }

}
