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
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.shutdown.ShutdownRequest;
import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.Files;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.os.CrossSystem;

public abstract class LogSourceProvider {
    public static void main(final String[] args) {
        final LogSourceProvider test = new LogSourceProvider(System.currentTimeMillis()) {
        };
        for (int i = 0; i < 20000; i++) {
            final LogSource logger = test.getLogger("test");
            final Logger parent = logger.getParent();
            if (parent != null) {
                System.out.println(i + " " + ((LogSink) logger.getParent()).getLogSources().size());
            }
            logger.info(i + "");
        }
        for (int i = 0; i < 100; i++) {
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
        }
        final LogSource logger = test.getLogger("test");
        final Logger parent = logger.getParent();
        if (parent != null) {
            System.out.println(((LogSink) logger.getParent()).getLogSources().size());
        }
    }

    protected final HashMap<String, LogSink> logSinks    = new HashMap<String, LogSink>();
    private final int                        maxSize;
    private final int                        maxLogs;
    protected long                           logTimeout;
    protected Thread                         flushThread = null;
    protected File                           logFolder;
    protected LogConsoleHandler              consoleHandler;

    protected boolean                        instantFlushDefault;
    private long                             initTime;

    public LogSourceProvider(final long timeStamp) {
        initTime = timeStamp;
        consoleHandler = new LogConsoleHandler();
        maxSize = JsonConfig.create(LogConfig.class).getMaxLogFileSize();
        maxLogs = JsonConfig.create(LogConfig.class).getMaxLogFiles();
        logTimeout = JsonConfig.create(LogConfig.class).getLogFlushTimeout() * 1000l;
        instantFlushDefault = JsonConfig.create(LogConfig.class).isDebugModeEnabled();
        File llogFolder = Application.getResource("logs/" + timeStamp + "_" + new SimpleDateFormat("HH.mm").format(new Date(timeStamp)) + "/");
        if (llogFolder.exists()) {
            llogFolder = Application.getResource("logs/" + timeStamp + "_" + new SimpleDateFormat("HH.mm.ss").format(new Date(timeStamp)) + "/");
        }
        if (!llogFolder.exists()) {
            llogFolder.mkdirs();
        }
        logFolder = llogFolder;
        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {

            @Override
            public void onShutdown(final ShutdownRequest shutdownRequest) {
                LogSourceProvider.this.flushSinks(false, true);
            }

            @Override
            public String toString() {
                return "flushing logs to disk";
            }

        });
        new Thread("LogsCleanup") {
            long newestTimeStamp = -1;

            @Override
            public void run() {

                final File oldLogs[] = Application.getResource("logs/").listFiles(new FilenameFilter() {

                    long removeTimeStamp = timeStamp - JsonConfig.create(LogConfig.class).getCleanupLogsOlderThanXDays() * 24 * 60 * 60 * 1000l;

                    @Override
                    public boolean accept(final File dir, final String name) {
                        if (dir.exists() && dir.isDirectory() && name.matches("^\\d+_\\d+\\.\\d+(\\.\\d+)?$")) {
                            final String timeStamp = new Regex(name, "^(\\d+)_").getMatch(0);
                            long times = 0;
                            if (timeStamp != null && (times = Long.parseLong(timeStamp)) < removeTimeStamp) {
                                if (newestTimeStamp == -1 || times > newestTimeStamp) {
                                    /*
                                     * find the latest logfolder, so we can keep
                                     * it
                                     */
                                    newestTimeStamp = times;
                                }
                                return true;
                            }
                        }
                        return false;
                    }
                });
                if (oldLogs != null) {
                    for (final File oldLog : oldLogs) {
                        try {
                            if (newestTimeStamp > 0 && oldLog.getName().contains(newestTimeStamp + "")) {
                                /* always keep at least the last logfolder! */
                                continue;
                            }
                            Files.deleteRecursiv(oldLog);
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }.start();
    }

    /**
     * @param name
     * @param i
     * @return
     */
    protected LogSource createLogSource(final String name, final int i) {
        return new LogSource(name, i);
    }

    public void flushSinks(final boolean flushOnly, final boolean finalFlush) {
        java.util.List<LogSink> logSinks2Flush = null;
        java.util.List<LogSink> logSinks2Close = null;
        synchronized (logSinks) {
            logSinks2Flush = new ArrayList<LogSink>(logSinks.size());
            logSinks2Close = new ArrayList<LogSink>(logSinks.size());
            final Iterator<LogSink> it = logSinks.values().iterator();
            while (it.hasNext()) {
                final LogSink next = it.next();
                if (next.hasLogSources()) {
                    logSinks2Flush.add(next);
                } else {
                    if (flushOnly == false) {
                        it.remove();
                        logSinks2Close.add(next);
                    }
                }
            }
        }
        for (final LogSink sink : logSinks2Close) {
            try {
                sink.close();
            } catch (final Throwable e) {
            }
        }
        for (final LogSink sink : logSinks2Flush) {
            try {
                sink.flushSources(finalFlush);
            } catch (final Throwable e) {
            }
        }
    }

    public LogSource getClassLogger(final Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    public LogConsoleHandler getConsoleHandler() {
        return consoleHandler;
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
                final Class<?> currentClass = Class.forName(currentClassName, true, Thread.currentThread().getContextClassLoader());
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
                return getLogger(currentClassName);
            }
        } catch (final Throwable e2) {
            e = e2;
        }
        final LogSource logger = getLogger("LogSourceProvider");
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

    public long getInitTime() {
        return initTime;
    }

    public LogSource getLogger(String name) {
        LogSink sink = null;
        name = CrossSystem.alleviatePathParts(name);
        if (StringUtils.isEmpty(name)) { return null; }
        if (!name.endsWith(".log")) {
            name = name + ".log";
        }
        synchronized (logSinks) {
            sink = logSinks.get(name);
            if (sink == null) {
                sink = new LogSink(name);
                if (consoleHandler != null) {
                    /*
                     * add ConsoleHandler to sink, it will add it to it's
                     * sources
                     */
                    sink.addHandler(consoleHandler);
                }
                try {
                    final Handler fileHandler = new FileHandler(new File(logFolder, name).getAbsolutePath(), maxSize, maxLogs, true);
                    sink.addHandler(fileHandler);
                    fileHandler.setEncoding("UTF-8");
                    fileHandler.setLevel(Level.ALL);
                    fileHandler.setFormatter(new LogSourceFormatter());
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
                logSinks.put(name, sink);
                startFlushThread();
            }
            final LogSource source = createLogSource(name, -1);
            source.setInstantFlush(instantFlushDefault);
            sink.addLogSource(source);
            return source;
        }
    }

    public LogSource getPreviousThreadLogSource() {
        return LogSource.getPreviousThreadLogSource();
    }

    public void removeConsoleHandler() {
        synchronized (logSinks) {
            if (consoleHandler == null) { return; }
            final Iterator<LogSink> it = logSinks.values().iterator();
            while (it.hasNext()) {
                final LogSink next = it.next();
                if (next.hasLogSources()) {
                    next.removeHandler(consoleHandler);
                } else {
                    next.close();
                    it.remove();
                }
            }
            consoleHandler = null;
        }
    }

    protected void startFlushThread() {
        if (flushThread != null && flushThread.isAlive()) { return; }
        flushThread = new Thread("LogFlushThread") {

            @Override
            public void run() {
                while (true) {
                    synchronized (logSinks) {
                        if (logSinks.size() == 0) {
                            flushThread = null;
                            return;
                        }
                    }
                    try {
                        try {
                            Thread.sleep(logTimeout);
                        } catch (final InterruptedException e) {
                        }
                        LogSourceProvider.this.flushSinks(true, false);
                    } catch (final Throwable e) {
                    }
                }

            }

        };
        flushThread.setDaemon(true);
        flushThread.start();
    }

}
