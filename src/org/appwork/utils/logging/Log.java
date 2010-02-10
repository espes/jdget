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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.appwork.utils.Application;

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
        LOGGER = Logger.getLogger("org.appwork");
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler cHandler = new ConsoleHandler();
        cHandler.setLevel(Level.ALL);
        cHandler.setFormatter(new LogFormatter());
        LOGGER.addHandler(cHandler);
        LogToFileHandler fh;
        try {
            fh = new LogToFileHandler();

            fh.setFormatter(new FileLogFormatter());
            LOGGER.addHandler(fh);
        } catch (Exception e) {

            e.printStackTrace();
        }
        LOGGER.addHandler(LogEventHandler.getInstance());
        LOGGER.setLevel(Level.ALL);

    }
    /**
     * For shorter access
     */
    public static Logger L = LOGGER;

    /**
     * Returns the loggerinstance for logging events
     * 
     * @return
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * Adds an exception to the logger. USe this instead of e.printStackTrace if
     * you like the exception appear in log
     * 
     * @param e
     */
    public static void exception(Throwable e) {
        exception(Level.SEVERE, e);
    }

    /**
     * Writes the log to log.txt
     * 
     * @param log
     */
    public static void logToFile(String log) {

        File file = Application.getRessource("log.txt");
        try {
            if (!file.isFile()) file.createNewFile();

            BufferedWriter f = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF8"));
            f.write("\r\n\r\n");
            f.write("------------------------------------------\r\n");
            f.write(new Date().toString() + "\r\n");
            f.write(log);
            f.write("\r\n\r\n");
            f.close();

        } catch (Exception e) {
            Log.exception(e);

        }

    }

    /**
     * Adds an exception to the logger. USe this instead of e.printStackTrace if
     * you like the exception appear in log
     * 
     * @param level
     * @param e
     */
    public static void exception(Level level, Throwable e) {
        getLogger().log(level, level.getName() + " Exception occurred", e);
    }

}
