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

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * @author thomas
 * 
 */
public class FileLogFormatter extends SimpleFormatter {
    /**
     * Date to convert timestamp to a readable format
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm", Locale.UK);

    /**
     * Strigbuilder is used to create Strings with less memory and CPU usage
     */
    private final StringBuilder sb = new StringBuilder();

    @Override
    public synchronized String format(LogRecord record) {
        /* clear StringBuilder buffer */
        sb.delete(0, sb.capacity());

        sb.delete(0, sb.capacity());
        sb.append(dateFormat.format(record.getMillis()));
        sb.append(" : ");
        sb.append(record.getMessage());
        sb.append("\r\n");
        return sb.toString();
    }
}
