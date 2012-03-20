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

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.logging.ExceptionDefaultLogLevel;

/**
 * @author thomas
 * 
 */
public class DialogNoAnswerException extends Exception implements ExceptionDefaultLogLevel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final boolean     causedByDontShowAgain;
    private final boolean     causedByTimeout;

    public DialogNoAnswerException(final int mask) {
        this.causedByDontShowAgain = BinaryLogic.containsSome(mask, Dialog.RETURN_SKIPPED_BY_DONT_SHOW);
        this.causedByTimeout = BinaryLogic.containsSome(mask, Dialog.RETURN_TIMEOUT);
        this.setStackTrace(new StackTraceElement[] {});
    }

    public Level getDefaultLogLevel() {
        // TODO Auto-generated method stub
        return Level.WARNING;
    }

    @Override
    public String getMessage() {
        return "DontShowAgain: " + this.causedByDontShowAgain + " Timeout: " + this.causedByTimeout;
    }

    public boolean isCausedByDontShowAgain() {
        return this.causedByDontShowAgain;
    }

    public boolean isCausedByTimeout() {
        return this.causedByTimeout;
    }
}
