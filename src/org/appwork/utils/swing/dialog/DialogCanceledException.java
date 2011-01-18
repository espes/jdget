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
 * 
 */
public class DialogCanceledException extends DialogNoAnswerException implements ExceptionDefaultLogLevel{
    public DialogCanceledException(final int mask) {
        super(mask);
    }

    /* (non-Javadoc)
     * @see org.appwork.utils.logging.ExceptionDefaultLogLevel#getDefaultLogLevel()
     */
    @Override
    public Level getDefaultLogLevel() {
        // TODO Auto-generated method stub
        return Level.WARNING;
    }
}
