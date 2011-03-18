/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.logging
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.logging;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author daniel, inspired by
 *         http://blogs.sun.com/nickstephen/entry/java_redirecting_system_out_and
 * 
 */
public final class StdOutErr2Logging extends Level {

    private static final PrintStream stdout           = System.out;
    private static final PrintStream stderr           = System.err;
    /**
     * 
     */
    private static final long        serialVersionUID = -8193079562712405474L;

    /**
     * Level for STDOUT activity.
     */
    public static final Level        STDOUT           = new StdOutErr2Logging("STDOUT", Level.INFO.intValue() + 53);

    /**
     * Level for STDERR activity
     */
    public static final Level        STDERR           = new StdOutErr2Logging("STDERR", Level.INFO.intValue() + 54);

    public static PrintStream getStdErr() {
        return StdOutErr2Logging.stderr;
    }

    public static PrintStream getStdOut() {
        return StdOutErr2Logging.stdout;
    }

    public static void redirectStdErr2Logger(final Logger logger) {
        System.setErr(new PrintStream(new LoggingOutputStream(logger, StdOutErr2Logging.STDERR), true));
    }

    public static void redirectStdOut2Logger(final Logger logger) {
        System.setOut(new PrintStream(new LoggingOutputStream(logger, StdOutErr2Logging.STDOUT), true));
    }

    private StdOutErr2Logging(final String name, final int value) {
        super(name, value);
    }

    protected Object readResolve() throws ObjectStreamException {
        if (this.intValue() == StdOutErr2Logging.STDOUT.intValue()) { return StdOutErr2Logging.STDOUT; }
        if (this.intValue() == StdOutErr2Logging.STDERR.intValue()) { return StdOutErr2Logging.STDERR; }
        throw new InvalidObjectException("Unknown instance :" + this);
    }
}
