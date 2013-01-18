/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.util.logging.Level;

import org.appwork.utils.logging.ExceptionDefaultLogLevel;

/**
 * @author thomas
 */
public class DialogClosedException extends DialogNoAnswerException implements ExceptionDefaultLogLevel {

    private static final long serialVersionUID = -6193184564008529988L;

    public DialogClosedException(final int mask) {
        this(mask, null);
    }

    /**
     * @param returnInterrupt
     * @param interruptException
     */
    public DialogClosedException(int mask, Throwable cause) {
        super(mask, cause);
    }

    @Override
    public Level getDefaultLogLevel() {
        return Level.WARNING;
    }

}
