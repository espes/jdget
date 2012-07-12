/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.logging2
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.logging2;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.appwork.utils.Exceptions;

public class LogSourceFormatter extends SimpleFormatter {

    private final Date       dat                    = new Date();
    private final DateFormat longTimestamp          = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

    private int              lastThreadID;

    protected StringBuilder  formatterStringBuilder = null;

    @Override
    public synchronized String format(final LogRecord record) {
        StringBuilder sb = this.formatterStringBuilder;
        if (sb == null) {
            /*
             * create new local StringBuilder in case we don't have once set
             * externally
             */
            sb = new StringBuilder();
        }
        // Minimize memory allocations here.
        this.dat.setTime(record.getMillis());
        final String message = this.formatMessage(record);
        final int th = record.getThreadID();
        if (th != this.lastThreadID) {
            sb.append("------------------------Thread: ");
            sb.append(th);
            sb.append(":" + record.getLoggerName());
            sb.append("-----------------------\r\n");
        }
        this.lastThreadID = th;
        /* we have this line for easier logfile purifier :) */
        sb.append("--ID:" + th + "TS:" + record.getMillis() + "-");
        sb.append(this.longTimestamp.format(this.dat));
        sb.append(" - ");
        sb.append(" [");
        String tmp = null;
        if ((tmp = record.getSourceClassName()) != null) {
            sb.append(tmp);
        }
        if ((tmp = record.getSourceMethodName()) != null) {
            sb.append('(');
            sb.append(tmp);
            sb.append(')');
        }
        sb.append("] ");
        sb.append("-> ");
        sb.append(message);
        sb.append("\r\n");
        if (record.getThrown() != null) {
            Exceptions.getStackTrace(sb, record.getThrown());
            sb.append("\r\n");
        }
        if (this.formatterStringBuilder == sb) { return ""; }
        return sb.toString();
    }

    public StringBuilder getFormatterStringBuilder() {
        return this.formatterStringBuilder;
    }

    public void setFormatterStringBuilder(final StringBuilder formatterStringBuilder) {
        this.formatterStringBuilder = formatterStringBuilder;
    }
}