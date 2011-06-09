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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author daniel
 * 
 */
public abstract class DelayedRunnable implements Runnable {

    private final ScheduledExecutorService service;
    private final long                     delayInMS;
    private volatile long                  lastRunRequest  = 0;
    private volatile long                  firstRunRequest = 0;
    private ScheduledFuture<?>             delayer;
    private final long                     maxInMS;

    public DelayedRunnable(final ScheduledExecutorService service, final long delayInMS) {
        this(service, delayInMS, -1);
    }

    public DelayedRunnable(final ScheduledExecutorService service, final long minDelayInMS, final long maxDelayInMS) {
        this.service = service;
        this.delayInMS = minDelayInMS;
        this.maxInMS = maxDelayInMS;
        if (this.delayInMS <= 0) { throw new IllegalArgumentException("minDelay must be >=0"); }
        if (this.maxInMS == 0) { throw new IllegalArgumentException("maxDelay must be !=0"); }
    }

    abstract public void delayedrun();

    @Override
    public void run() {
        synchronized (this) {
            this.lastRunRequest = System.currentTimeMillis();
            if (this.delayer == null) {
                this.firstRunRequest = System.currentTimeMillis();
                this.delayer = this.service.schedule(new Runnable() {
                    public void run() {
                        boolean delayAgain = false;
                        synchronized (DelayedRunnable.this) {
                            delayAgain = System.currentTimeMillis() - DelayedRunnable.this.lastRunRequest > DelayedRunnable.this.delayInMS;
                            if (DelayedRunnable.this.maxInMS > 0 && System.currentTimeMillis() - DelayedRunnable.this.firstRunRequest > DelayedRunnable.this.maxInMS) {
                                delayAgain = false;
                            }
                        }
                        if (!delayAgain) {
                            synchronized (DelayedRunnable.this) {
                                DelayedRunnable.this.delayer = null;
                            }
                            DelayedRunnable.this.delayedrun();
                        } else {
                            synchronized (DelayedRunnable.this) {
                                DelayedRunnable.this.delayer = null;
                                DelayedRunnable.this.run();
                            }
                        }
                    }

                }, DelayedRunnable.this.delayInMS, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void stop() {
        synchronized (this) {
            if (this.delayer != null) {
                this.delayer.cancel(false);
                this.delayer = null;
            }
        }
    }

}
