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
    private long                           nextDelay;
    private volatile long                  lastRunRequest  = 0;
    private volatile long                  firstRunRequest = 0;
    private ScheduledFuture<?>             delayer;
    private final long                     maxInMS;

    public DelayedRunnable(final ScheduledExecutorService service, final long delayInMS) {
        this(service, delayInMS, -1);
    }

    public DelayedRunnable(final ScheduledExecutorService service, final long minDelayInMS, final long maxDelayInMS) {
        this.service = service;
        this.nextDelay = this.delayInMS = minDelayInMS;
        this.maxInMS = maxDelayInMS;
        if (this.delayInMS <= 0) { throw new IllegalArgumentException("minDelay must be >=0"); }
        if (this.maxInMS == 0) { throw new IllegalArgumentException("maxDelay must be !=0"); }
    }

    abstract public void delayedrun();

    public void resetAndStart() {
        this.run();
    }

    @Override
    public void run() {
        synchronized (this) {
            /* lastRunRequest is updated every time */
            this.lastRunRequest = System.currentTimeMillis();
            if (this.delayer == null) {
                if (this.firstRunRequest == 0) {
                    /* firstRunRequest is updated only once */
                    this.firstRunRequest = System.currentTimeMillis();
                }
                this.delayer = this.service.schedule(new Runnable() {
                    public void run() {
                        boolean doWhat = false;
                        synchronized (DelayedRunnable.this) {
                            /* do we have to run now? */
                            final long dif = System.currentTimeMillis() - DelayedRunnable.this.lastRunRequest;
                            boolean runNow = dif >= DelayedRunnable.this.nextDelay;
                            if (DelayedRunnable.this.maxInMS > 0) {
                                /* is a maxDelay set? */
                                if (System.currentTimeMillis() - DelayedRunnable.this.firstRunRequest > DelayedRunnable.this.maxInMS) {
                                    /* we have to run now! */
                                    runNow = true;
                                } else {
                                    /*
                                     * calc new nextDelay so we can reach
                                     * maxDelay better
                                     */
                                    DelayedRunnable.this.nextDelay = Math.max(DelayedRunnable.this.maxInMS - (System.currentTimeMillis() - DelayedRunnable.this.firstRunRequest), 10);
                                }
                            }
                            DelayedRunnable.this.delayer = null;
                            if (runNow) {
                                /* we no longer delay the run */
                                doWhat = true;
                                /* reset nextDelay and firstRunRequest */
                                DelayedRunnable.this.firstRunRequest = System.currentTimeMillis();
                                DelayedRunnable.this.nextDelay = DelayedRunnable.this.delayInMS;
                            } else {
                                /* lets delay it again */
                                DelayedRunnable.this.nextDelay = DelayedRunnable.this.delayInMS - dif;
                                doWhat = false;
                                DelayedRunnable.this.run();
                            }
                        }
                        if (doWhat) {
                            /* we no longer delay the run */
                            DelayedRunnable.this.delayedrun();
                        } else {
                            /* lets delay it again */
                            DelayedRunnable.this.run();
                        }
                    }

                }, DelayedRunnable.this.nextDelay, TimeUnit.MILLISECONDS);

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
