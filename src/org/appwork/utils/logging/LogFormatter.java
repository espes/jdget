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
import java.util.Date;
import java.util.HashMap;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.appwork.utils.Exceptions;

public class LogFormatter extends SimpleFormatter {
    /**
     * Date to convert timestamp to a readable format
     */
    private final Date date = new Date();
    /**
     * For thread controlled logs
     */
    private int lastThreadID;

    /**
     * Dateformat to convert timestamp to a readable format
     */
    private final DateFormat longTimestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private final HashMap<Integer, String> map = new HashMap<Integer, String>();
    /**
     * Strigbuilder is used to create Strings with less memory and CPU usage
     */
    private final StringBuilder sb = new StringBuilder();

    @Override
    public synchronized String format(final LogRecord record) {
        /* clear StringBuilder buffer */
        this.sb.delete(0, this.sb.capacity());

        // Minimize memory allocations here.
        this.date.setTime(record.getMillis());

        final String message = this.formatMessage(record);
        final int th = record.getThreadID();

        // new Thread.
        if (th != this.lastThreadID) {
            this.sb.append("\r\n THREAD: ");
            this.sb.append(th);
            this.sb.append("\r\n");
        }
        this.lastThreadID = th;

        this.sb.append(record.getThreadID());
        this.sb.append(' ');

        this.sb.append(this.longTimestamp.format(this.date));
        this.sb.append(" - ");
        this.sb.append(record.getLevel().getName());
        this.sb.append(" [");
        if (record.getSourceClassName() != null) {
            this.sb.append(record.getSourceClassName());
        } else {
            this.sb.append(record.getLoggerName());
        }
        if (record.getSourceMethodName() != null) {
            this.sb.append('(');
            this.sb.append(record.getSourceMethodName());
            this.sb.append(')');
        }

        this.sb.append("] ");

        this.sb.append("-> ");
        this.sb.append(message);
        this.sb.append("\r\n");
        if (record.getThrown() != null) {
            this.sb.append(Exceptions.getStackTrace(record.getThrown()));
        }
        return this.sb.toString();
    }

}
