/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.scheduler
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.scheduler;

import java.lang.reflect.Modifier;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author daniel
 * 
 */
public abstract class DelayedRunnable implements Runnable {

    public static String getCaller() {
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
                return currentClassName;
            }
        } catch (final Throwable e2) {
        }
        return null;
    }

    /**
     * return a ScheduledExecutorService with deamon Threads,
     * allowCoreThreadTimeOut(true) and maxPoolSize(1)
     */
    public static ScheduledExecutorService getNewScheduledExecutorService() {
        final String caller = DelayedRunnable.getCaller();
        /*
         * changed core to 1 because of
         * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7091003
         */
        final ScheduledThreadPoolExecutor ret = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {

            @Override
            public Thread newThread(final Runnable r) {
                final Thread thread = new Thread(r);
                if (caller != null) {
                    thread.setName("Scheduler:" + caller);
                }
                thread.setDaemon(true);
                return thread;
            }
        });
        ret.setMaximumPoolSize(1);
        ret.setKeepAliveTime(10000, TimeUnit.MILLISECONDS);
        ret.allowCoreThreadTimeOut(true);
        return ret;
    }

    private final ScheduledExecutorService service;
    private final long                     delayInMS;
    private final AtomicLong               lastRunRequest  = new AtomicLong(0);
    private final AtomicLong               firstRunRequest = new AtomicLong(0);
    private final AtomicBoolean            delayerSet      = new AtomicBoolean(false);
    private final long                     maxInMS;
    private final AtomicBoolean            delayerEnabled  = new AtomicBoolean(true);

    public DelayedRunnable(final long minDelayInMS) {
        this(DelayedRunnable.getNewScheduledExecutorService(), minDelayInMS);
    }

    public DelayedRunnable(final long minDelayInMS, final long maxDelayInMS) {
        this(DelayedRunnable.getNewScheduledExecutorService(), minDelayInMS, maxDelayInMS);
    }

    public DelayedRunnable(final ScheduledExecutorService service, final long delayInMS) {
        this(service, delayInMS, -1);
    }

    public DelayedRunnable(final ScheduledExecutorService service, final long minDelayInMS, final long maxDelayInMS) {
        this.service = service;
        this.delayInMS = minDelayInMS;
        this.maxInMS = maxDelayInMS;
        if (this.delayInMS <= 0) { throw new IllegalArgumentException("minDelay must be >0"); }
        if (this.maxInMS == 0) { throw new IllegalArgumentException("maxDelay must be !=0"); }
    }

    abstract public void delayedrun();

    public String getID() {
        return null;
    }

    public boolean isDelayerEnabled() {
        return this.delayerEnabled.get();
    }

    public void resetAndStart() {
        this.run();
    }

    @Override
    public void run() {
        if (this.isDelayerEnabled() == false) {
            DelayedRunnable.this.delayedrun();
            return;
        }
        this.lastRunRequest.set(System.currentTimeMillis());
        if (this.delayerSet.getAndSet(true) == true) { return; }
        this.firstRunRequest.compareAndSet(0, System.currentTimeMillis());
        this.service.schedule(new Runnable() {

            private void delayAgain(final long currentTime, Long nextDelay, final long minDif, final long thisRequestRun) {
                if (DelayedRunnable.this.delayerSet.get() == false) { return; }
                if (nextDelay == null) {
                    nextDelay = Math.max(0, DelayedRunnable.this.delayInMS - minDif);
                }
                if (nextDelay < 10) {
                    this.runNow(currentTime, thisRequestRun, minDif);
                    return;
                }
                DelayedRunnable.this.service.schedule(this, nextDelay, TimeUnit.MILLISECONDS);
            }

            public void run() {
                if (DelayedRunnable.this.delayerSet.get() == false) { return; }
                final long thisRunRequest = DelayedRunnable.this.lastRunRequest.get();
                final long currentTime = System.currentTimeMillis();
                final long minDif = currentTime - thisRunRequest;
                if (minDif >= DelayedRunnable.this.delayInMS) {
                    /* minDelay reached, run now */
                    this.runNow(currentTime, thisRunRequest, minDif);
                    return;
                }
                final long firstRunRequest = DelayedRunnable.this.firstRunRequest.get();
                Long nextDelay = null;
                if (DelayedRunnable.this.maxInMS > 0) {
                    final long maxDif = currentTime - firstRunRequest;
                    if (maxDif >= DelayedRunnable.this.maxInMS) {
                        /* maxDelay reached, run now */
                        this.runNow(currentTime, thisRunRequest, minDif);
                        return;
                    }
                    final long delay = DelayedRunnable.this.maxInMS - maxDif;
                    nextDelay = Math.min(delay, DelayedRunnable.this.delayInMS);
                }
                this.delayAgain(currentTime, nextDelay, minDif, thisRunRequest);
            }

            private void runNow(final long currentTime, final long thisRunRequest, final long minDif) {
                DelayedRunnable.this.delayedrun();
                if (thisRunRequest != DelayedRunnable.this.lastRunRequest.get()) {
                    DelayedRunnable.this.firstRunRequest.set(currentTime);
                    this.delayAgain(currentTime, DelayedRunnable.this.delayInMS, minDif, thisRunRequest);
                } else {
                    this.stop();
                }
            }

            private void stop() {
                DelayedRunnable.this.firstRunRequest.set(0);
                DelayedRunnable.this.delayerSet.set(false);
            }

        }, DelayedRunnable.this.delayInMS, TimeUnit.MILLISECONDS);
    }

    public void setDelayerEnabled(final boolean b) {
        if (this.delayerEnabled.getAndSet(b) == b) { return; }
        if (!b) {
            this.stop();
        }
    }

    public void stop() {
        this.delayerSet.set(false);
    }

}
