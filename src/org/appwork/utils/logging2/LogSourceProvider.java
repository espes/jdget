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

/**
 * @author daniel
 *
 */

import java.io.File;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.logging.LogFormatter;

public abstract class LogSourceProvider {
    private final HashMap<String, LogSink> logSinks    = new HashMap<String, LogSink>();
    private final int                      maxSize;
    private final int                      maxLogs;
    private final long                     logTimeout;
    private Thread                         flushThread = null;
    private final File                     logFolder;
    private ConsoleHandler                 consoleHandler;
    private final boolean                  instantFlushDefault;

    public LogSourceProvider(final long timeStamp) {
        this.consoleHandler = new ConsoleHandler();
        this.consoleHandler.setLevel(Level.ALL);
        this.consoleHandler.setFormatter(new LogFormatter());
        this.maxSize = JsonConfig.create(LogConfig.class).getMaxLogFileSize();
        this.maxLogs = JsonConfig.create(LogConfig.class).getMaxLogFiles();
        this.logTimeout = JsonConfig.create(LogConfig.class).getLogFlushTimeout() * 1000l;
        this.instantFlushDefault = JsonConfig.create(LogConfig.class).isDebugModeEnabled();
        File llogFolder = Application.getResource("logs/" + timeStamp + "_" + new SimpleDateFormat("HH.mm").format(new Date(timeStamp)) + "/");
        if (llogFolder.exists()) {
            llogFolder = Application.getResource("logs/" + timeStamp + "_" + new SimpleDateFormat("HH.mm.ss").format(new Date(timeStamp)) + "/");
        }
        if (!llogFolder.exists()) {
            llogFolder.mkdirs();
        }
        this.logFolder = llogFolder;
        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {

            @Override
            public void run() {
                LogSourceProvider.this.flushSinks();
            }

            @Override
            public String toString() {
                return "flushing logs to disk";
            }

        });
    }

    protected synchronized void flushSinks() {
        ArrayList<LogSink> logSinks2Flush = null;
        synchronized (this.logSinks) {
            logSinks2Flush = new ArrayList<LogSink>(this.logSinks.size());
            final Iterator<LogSink> it = this.logSinks.values().iterator();
            while (it.hasNext()) {
                final LogSink next = it.next();
                if (next.hasLogSources()) {
                    logSinks2Flush.add(next);
                } else {
                    next.close();
                    it.remove();
                }
            }
            if (this.logSinks.size() == 0) {
                this.flushThread = null;
            }
        }
        for (final LogSink sink : logSinks2Flush) {
            try {
                sink.flushSources();
            } catch (final Throwable e) {
            }
        }
    }

    public LogSource getClassLogger(final Class<?> clazz) {
        return this.getLogger(clazz.getSimpleName());
    }

    /**
     * CL = Class Logger, returns a logger for calling Class
     * 
     * @return
     */
    public LogSource getCurrentClassLogger() {
        Throwable e = null;
        final Throwable stackTrace = new Throwable().fillInStackTrace();
        try {
            for (final StackTraceElement element : stackTrace.getStackTrace()) {
                final String currentClassName = element.getClassName();
                final Class<?> currentClass = Class.forName(currentClassName);
                if (Modifier.isAbstract(currentClass.getModifiers())) {
                    /* we dont want the abstract class to be used */
                    continue;
                }
                if (Modifier.isInterface(currentClass.getModifiers())) {
                    /* we dont want the interface class to be used */
                    continue;
                }
                if (LogSourceProvider.class.isAssignableFrom(currentClass)) {
                    /* we dont want the logging class itself to be used */
                    continue;
                }
                return this.getLogger(currentClassName);
            }
        } catch (final Throwable e2) {
            e = e2;
        }
        final LogSource logger = this.getLogger("LogSourceProvider");
        if (e != null) {
            /* an exception occured during stacktrace walking */
            logger.log(e);
        }
        /*
         * as we could not determine current class, lets put the strackTrace
         * into this generated logger
         */
        logger.log(stackTrace);
        return logger;
    }

    public LogSource getLogger(final String name) {
        LogSink sink = null;
        synchronized (this.logSinks) {
            sink = this.logSinks.get(name);
            if (sink == null) {
                sink = new LogSink(name);
                if (this.consoleHandler != null) {
                    /*
                     * add ConsoleHandler to sink, it will add it to it's
                     * sources
                     */
                    sink.addHandler(this.consoleHandler);
                }
                try {
                    final Handler fileHandler = new FileHandler(new File(this.logFolder, name).getAbsolutePath(), this.maxSize, this.maxLogs, true);
                    sink.addHandler(fileHandler);
                    fileHandler.setLevel(Level.ALL);
                    fileHandler.setFormatter(new LogSourceFormatter());
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
                this.logSinks.put(name, sink);
                this.startFlushThread();
            }
        }
        final LogSource source = new LogSource(name, -1);
        source.setInstantFlush(this.instantFlushDefault);
        sink.addLogSource(source);
        return source;
    }

    public void removeConsoleHandler() {
        synchronized (this.logSinks) {
            if (this.consoleHandler == null) { return; }
            final Iterator<LogSink> it = this.logSinks.values().iterator();
            while (it.hasNext()) {
                final LogSink next = it.next();
                if (next.hasLogSources()) {
                    next.removeHandler(this.consoleHandler);
                } else {
                    next.close();
                    it.remove();
                }
            }
            this.consoleHandler = null;
        }
    }

    protected synchronized void startFlushThread() {
        if (this.flushThread != null && this.flushThread.isAlive()) { return; }
        this.flushThread = new Thread("LogFlushThread") {

            @Override
            public void run() {
                try {
                    while (Thread.currentThread() == LogSourceProvider.this.flushThread) {
                        try {
                            Thread.sleep(LogSourceProvider.this.logTimeout);
                        } catch (final InterruptedException e) {
                            return;
                        }
                        if (Thread.currentThread() == LogSourceProvider.this.flushThread) {
                            LogSourceProvider.this.flushSinks();
                        }
                    }
                } finally {
                    synchronized (LogSourceProvider.this) {
                        if (Thread.currentThread() == LogSourceProvider.this.flushThread) {
                            LogSourceProvider.this.flushThread = null;
                        }
                    }
                }
            }

        };
        this.flushThread.setDaemon(true);
        this.flushThread.start();
    }

}
