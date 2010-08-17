/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.logging
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author $Author: unknown$
 * 
 */
public class Log {

    private static Logger LOGGER;
    /**
     * CReate the singleton logger instance
     */

    static {
        Log.LOGGER = Logger.getLogger("org.appwork");
        Log.LOGGER.setUseParentHandlers(false);
        final ConsoleHandler cHandler = new ConsoleHandler();
        cHandler.setLevel(Level.ALL);
        cHandler.setFormatter(new LogFormatter());
        Log.LOGGER.addHandler(cHandler);
        LogToFileHandler fh;
        try {
            fh = new LogToFileHandler();

            fh.setFormatter(new FileLogFormatter());
            Log.LOGGER.addHandler(fh);
        } catch (final Exception e) {

            org.appwork.utils.logging.Log.exception(e);
        }

        Log.LOGGER.addHandler(LogEventHandler.getInstance());
        Log.LOGGER.setLevel(Level.ALL);

    }
    /**
     * For shorter access
     */
    public static Logger L = Log.LOGGER;

    /**
     * Adds an exception to the logger. USe this instead of e.printStackTrace if
     * you like the exception appear in log
     * 
     * @param level
     * @param e
     */
    public static void exception(final Level level, final Throwable e) {
        Log.getLogger().log(level, level.getName() + " Exception occurred", e);
    }

    /**
     * Adds an exception to the logger. USe this instead of e.printStackTrace if
     * you like the exception appear in log
     * 
     * @param e
     */
    public static void exception(Throwable e) {
        if (e == null) {
            try {
                throw new NullPointerException("e is null");
            } catch (final NullPointerException e1) {
                e = e1;
            }
        }
        Log.exception(Level.SEVERE, e);
    }

    /**
     * Returns the loggerinstance for logging events
     * 
     * @return
     */
    public static Logger getLogger() {
        return Log.LOGGER;
    }

}
