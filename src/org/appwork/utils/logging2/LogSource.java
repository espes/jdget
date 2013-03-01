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

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.appwork.utils.Exceptions;
import org.appwork.utils.logging.ExceptionDefaultLogLevel;

public class LogSource extends Logger implements LogInterface {

    private static WeakHashMap<Thread, WeakReference<LogSource>> LASTTHREADLOGSOURCE = new WeakHashMap<Thread, WeakReference<LogSource>>();

    public static void exception(final Logger logger, final Throwable e) {
        if (logger == null || e == null) { return; }
        if (logger instanceof LogSource) {
            ((LogSource) logger).log(e);
        } else {
            logger.severe(e.getMessage());
            logger.severe(Exceptions.getStackTrace(e));
        }
    }

    public static LogSource getPreviousThreadLogSource() {
        synchronized (LogSource.LASTTHREADLOGSOURCE) {
            final Thread thread = Thread.currentThread();
            final WeakReference<LogSource> prevLogSource = LogSource.LASTTHREADLOGSOURCE.get(thread);
            if (prevLogSource != null) {
                final LogSource previousLogger = prevLogSource.get();
                if (previousLogger != null && previousLogger.isClosed() == false) { return previousLogger; }
            }
        }
        return null;
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
        setLevel(Level.ALL);
    }

    protected LogSource(final String name, final String resourceBundleName) {
        super(name, resourceBundleName);
        setCurrentThreadLogSource();
    }

    public synchronized void clear() {
        records = null;
    }

    public synchronized void close() {
        flush();
        closed = true;
        records = null;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (allowTimeoutFlush || flushOnFinalize) {
                close();
            }
        } finally {
            super.finalize();
        }
    }

    public synchronized void flush() {
        if (closed) { return; }
        if (records == null || records.size() == 0) {
            records = null;
            return;
        }
        final Logger parent = getParent();
        if (parent != null) {
            for (final Handler handler : parent.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    /* we dont want logRecords to appear twice on console */
                    continue;
                }
                synchronized (handler) {
                    for (final LogRecord record : records) {
                        handler.publish(record);
                    }
                }
            }
            flushCounter++;
        }
        records = null;
    }

    public int getMaxLogRecordsInMemory() {
        return maxLogRecordsInMemory;
    }

    @Override
    public Logger getParent() {
        return parent;
    }

    public boolean isAllowTimeoutFlush() {
        return allowTimeoutFlush;
    }

    protected boolean isClosed() {
        return closed;
    }

    /**
     * @return the flushOnFinalize
     */
    public boolean isFlushOnFinalize() {
        return flushOnFinalize;
    }

    /**
     * @return the instantFlush
     */
    public boolean isInstantFlush() {
        return instantFlush;
    }

    @Override
    public synchronized void log(final LogRecord record) {
        if (closed || record == null) { return; }

        setCurrentThreadLogSource();

        record.setLoggerName(getName());
        /* make sure we have gathered all information about current class/method */
        /* this will collect current class/method if net set yet */
        record.getSourceClassName();
        // record.setLoggerName(Thread.currentThread().getName());
        if (maxLogRecordsInMemory == 0 || instantFlush) {
            /* maxLogRecordsInMemory == 0, we want to use parent's handlers */
            final Logger parent = getParent();
            if (parent != null) {
                for (final Handler handler : parent.getHandlers()) {
                    synchronized (handler) {
                        handler.publish(record);
                    }
                }
            }
            return;
        } else if (maxLogRecordsInMemory > 0 && records != null && records.size() == maxLogRecordsInMemory) {
            /* maxLogRecordsInMemory >0 we have limited max records in memory */
            /* we flush in case we reached maxLogRecordsInMemory */
            flush();
        }
        if (records == null) {
            /* records will be null at first use or after a flush */
            records = new ArrayList<LogRecord>();
        }
        records.add(record);
        recordsCounter++;
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
        final LogRecord lr = new LogRecord(lvl, Exceptions.getStackTrace(e));
        lr.setLoggerName(getName());
        this.log(lr);
    }

    /**
     * @param errorStream
     */
    public void logAsynch(final InputStream is) {
        new InputStreamLogger(is, this).start();
    }

    public void setAllowTimeoutFlush(final boolean allowTimeoutFlush) {
        this.allowTimeoutFlush = allowTimeoutFlush;
    }

    private void setCurrentThreadLogSource() {
        synchronized (LogSource.LASTTHREADLOGSOURCE) {
            final Thread thread = Thread.currentThread();
            final WeakReference<LogSource> prevLogSource = LogSource.LASTTHREADLOGSOURCE.get(thread);
            if (prevLogSource == null || prevLogSource.get() != null) {
                LogSource.LASTTHREADLOGSOURCE.put(Thread.currentThread(), new WeakReference<LogSource>(this));
            }
        }
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
            flush();
        }
        this.instantFlush = instantFlush;
    }

    public synchronized void setMaxLogRecordsInMemory(final int newMax) {
        if (maxLogRecordsInMemory == newMax) { return; }
        flush();
        maxLogRecordsInMemory = newMax;
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
            sb.append("Log:" + getName() + " Records:" + recordsCounter + " Flushed:" + flushCounter);
            if (records != null && records.size() > 0) {
                sb.append("\r\n");
                final LogSourceFormatter formatter = new LogSourceFormatter();
                formatter.setFormatterStringBuilder(sb);
                int index = 0;
                if (lastXEntries > 0 && records.size() > lastXEntries) {
                    index = records.size() - lastXEntries;
                }
                for (; index < records.size(); index++) {
                    sb.append(formatter.format(records.get(index)));
                }
            }
        }
        return sb.toString();
    }
}
