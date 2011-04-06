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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    private final DateFormat dateFormat = new SimpleDateFormat();

    @Override
    public synchronized String format(final LogRecord record) {
        /* clear StringBuilder buffer */
        final StringBuilder sb = new StringBuilder();
        sb.append(this.dateFormat.format(record.getMillis()));
        sb.append(" : ");
        sb.append(record.getMessage());
        sb.append("\r\n");
        return sb.toString();
    }
}
