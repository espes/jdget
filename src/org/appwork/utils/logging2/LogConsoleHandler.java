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

import java.util.HashSet;
import java.util.Locale;
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

    private HashSet<String> allowedLoggerNames = new HashSet<String>();

    public LogConsoleHandler() {
        setLevel(Level.ALL);
        setFormatter(new LogFormatter());
    }

    public HashSet<String> getAllowedLoggerNames() {
        return allowedLoggerNames;
    }

    @Override
    public boolean isLoggable(final LogRecord record) {
        final HashSet<String> lallowedLoggerNames = allowedLoggerNames;
        if (lallowedLoggerNames == null) { return false; }
        if (lallowedLoggerNames.size() == 0) { return true; }
        if (allowedLoggerNames.contains(record.getLoggerName().toLowerCase(Locale.ENGLISH))) { return true; }

        return false;
    }

    public void setAllowedLoggerNames(final String... strings) {
        if (strings == null) {
            allowedLoggerNames = null;
            return;
        } else {
            final HashSet<String> tmp = new HashSet<String>();
            for (final String s : strings) {
                tmp.add(s.toLowerCase(Locale.ENGLISH));
            }
            allowedLoggerNames = tmp;
        }
    }

    @Override
    public void setFilter(final Filter newFilter) throws SecurityException {
    }

}
