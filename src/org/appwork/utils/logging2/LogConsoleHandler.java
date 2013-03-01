/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.logging2
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.logging2;

import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.appwork.utils.logging.LogFormatter;

/**
 * @author daniel
 * 
 */
public class LogConsoleHandler extends ConsoleHandler {

    private ArrayList<String> allowedLoggerNames = new ArrayList<String>();

    public LogConsoleHandler() {
        this.setLevel(Level.ALL);
        this.setFormatter(new LogFormatter());
    }

    public ArrayList<String> getAllowedLoggerNames() {
        return this.allowedLoggerNames;
    }

    @Override
    public boolean isLoggable(final LogRecord record) {
        final ArrayList<String> lallowedLoggerNames = this.allowedLoggerNames;
        if (lallowedLoggerNames == null) { return false; }
        if (lallowedLoggerNames.size() == 0) { return true; }
        for (final String allowedLoggerName : lallowedLoggerNames) {
            if (allowedLoggerName != null && allowedLoggerName.equalsIgnoreCase(record.getLoggerName())) { return true; }
        }
        return false;
    }

    public void setAllowedLoggerNames(final ArrayList<String> allowedLoggerNames) {
        this.allowedLoggerNames = allowedLoggerNames;
    }

    @Override
    public void setFilter(final Filter newFilter) throws SecurityException {
    }

}
