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

import java.util.ArrayList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.appwork.utils.Exceptions;
import org.appwork.utils.logging.ExceptionDefaultLogLevel;

public class LogSource extends Logger implements LogInterface {

    public static void exception(final Logger logger, final Throwable e) {
        if (logger == null || e == null) { return; }
        if (logger instanceof LogSource) {
            ((LogSource) logger).log(e);
        } else {
            logger.severe(e.getMessage());
            logger.severe(Exceptions.getStackTrace(e));
        }
    }

    private java.util.List<LogRecord> records           = new ArrayList<LogRecord>();
    private int                       maxLogRecordsInMemory;
    private int                       flushCounter      = 0;
    private int                       recordsCounter    = 0;
    private boolean                   closed            = false;
    private boolean                   allowTimeoutFlush = true;

    private boolean                   instantFlush      = false;
    private boolean                   flushOnFinalize   = false;
    private Logger                    parent            = null;

    public LogSource(final String name) {
        this(name, -1);
    }

    /*
     * creates a LogCollector with given name
     * 
     * maxLogRecordsInMemory defines how many log records this logger will
     * buffer in memory before logging to parent's handlers
     * 
     * <0 = unlimited in memory, manual flush needed
     * 
     * 0 = forward directly to parent's handlers
     * 
     * >0 = limited
     */
    public LogSource(final String name, final int maxLogRecordsInMemory) {
        this(name, (String) null);
        this.maxLogRecordsInMemory = maxLogRecordsInMemory;
        super.setUseParentHandlers(false);
        this.setLevel(Level.ALL);
    }

    protected LogSource(final String name, final String resourceBundleName) {
        super(name, resourceBundleName);
    }

    public synchronized void clear() {
        this.records = null;
    }

    public synchronized void close() {
        this.flush();
        this.closed = true;
        this.records = null;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (this.allowTimeoutFlush || this.flushOnFinalize) {
                this.close();
            }
        } finally {
            super.finalize();
        }
    }

    public synchronized void flush() {
        if (this.closed) { return; }
        if (this.records == null || this.records.size() == 0) {
            this.records = null;
            return;
        }
        final Logger parent = this.getParent();
        if (parent != null) {
            for (final Handler handler : parent.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    /* we dont want logRecords to appear twice on console */
                    continue;
                }
                synchronized (handler) {
                    for (final LogRecord record : this.records) {
                        handler.publish(record);
                    }
                }
            }
            this.flushCounter++;
        }
        this.records = null;
    }

    public int getMaxLogRecordsInMemory() {
        return this.maxLogRecordsInMemory;
    }

    @Override
    public Logger getParent() {
        return this.parent;
    }

    public boolean isAllowTimeoutFlush() {
        return this.allowTimeoutFlush;
    }

    protected boolean isClosed() {
        return this.closed;
    }

    /**
     * @return the flushOnFinalize
     */
    public boolean isFlushOnFinalize() {
        return this.flushOnFinalize;
    }

    /**
     * @return the instantFlush
     */
    public boolean isInstantFlush() {
        return this.instantFlush;
    }

    @Override
    public synchronized void log(final LogRecord record) {
        if (this.closed || record == null) { return; }
        /* make sure we have gathered all information about current class/method */
        /* this will collect current class/method if net set yet */
        record.getSourceClassName();
        record.setLoggerName(Thread.currentThread().getName());
        if (this.maxLogRecordsInMemory == 0 || this.instantFlush) {
            /* maxLogRecordsInMemory == 0, we want to use parent's handlers */
            final Logger parent = this.getParent();
            if (parent != null) {
                for (final Handler handler : parent.getHandlers()) {
                    synchronized (handler) {
                        handler.publish(record);
                    }
                }
            }
            return;
        } else if (this.maxLogRecordsInMemory > 0 && this.records != null && this.records.size() == this.maxLogRecordsInMemory) {
            /* maxLogRecordsInMemory >0 we have limited max records in memory */
            /* we flush in case we reached maxLogRecordsInMemory */
            this.flush();
        }
        if (this.records == null) {
            /* records will be null at first use or after a flush */
            this.records = new ArrayList<LogRecord>();
        }
        this.records.add(record);
        this.recordsCounter++;
        super.log(record);
    }

    public void log(Throwable e) {
        if (e == null) {
            e = new NullPointerException("e is null");
        }
        Level lvl = null;
        if (e instanceof ExceptionDefaultLogLevel) {
            lvl = ((ExceptionDefaultLogLevel) e).getDefaultLogLevel();
        }
        if (lvl == null) {
            lvl = Level.SEVERE;
        }
        this.log(new LogRecord(lvl, Exceptions.getStackTrace(e)));
    }

    public void setAllowTimeoutFlush(final boolean allowTimeoutFlush) {
        this.allowTimeoutFlush = allowTimeoutFlush;
    }

    /**
     * @param flushOnFinalize
     *            the flushOnFinalize to set
     */
    public void setFlushOnFinalize(final boolean flushOnFinalize) {
        this.flushOnFinalize = flushOnFinalize;
    }

    /**
     * @param instantFlush
     *            the instantFlush to set
     */
    public void setInstantFlush(final boolean instantFlush) {
        if (this.instantFlush == instantFlush) { return; }
        if (instantFlush) {
            this.flush();
        }
        this.instantFlush = instantFlush;
    }

    public synchronized void setMaxLogRecordsInMemory(final int newMax) {
        if (this.maxLogRecordsInMemory == newMax) { return; }
        this.flush();
        this.maxLogRecordsInMemory = newMax;
    }

    @Override
    public void setParent(final Logger parent) {
        this.parent = parent;
    }

    @Override
    public void setUseParentHandlers(final boolean useParentHandlers) {
        /* do not allow to change this */
    }

    @Override
    public String toString() {
        return this.toString(0);
    }

    public String toString(final int lastXEntries) {
        final StringBuilder sb = new StringBuilder();
        synchronized (this) {
            sb.append("Log:" + this.getName() + " Records:" + this.recordsCounter + " Flushed:" + this.flushCounter);
            if (this.records != null && this.records.size() > 0) {
                sb.append("\r\n");
                final LogSourceFormatter formatter = new LogSourceFormatter();
                formatter.setFormatterStringBuilder(sb);
                int index = 0;
                if (lastXEntries > 0 && this.records.size() > lastXEntries) {
                    index = this.records.size() - lastXEntries;
                }
                for (; index < this.records.size(); index++) {
                    sb.append(formatter.format(this.records.get(index)));
                }
            }
        }
        return sb.toString();
    }
}
