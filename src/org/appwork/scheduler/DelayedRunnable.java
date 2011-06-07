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
    private volatile long                  lastRunRequest = 0;
    private ScheduledFuture<?>             delayer;

    public DelayedRunnable(final ScheduledExecutorService service, final long delayInMS) {
        this.service = service;
        this.delayInMS = delayInMS;
    }

    abstract public void delayedrun();

    @Override
    public void run() {
        synchronized (this) {
            this.lastRunRequest = System.currentTimeMillis();
            if (this.delayer == null) {
                this.delayer = this.service.schedule(new Runnable() {
                    public void run() {
                        boolean delayAgain = false;
                        synchronized (DelayedRunnable.this) {
                            delayAgain = System.currentTimeMillis() - DelayedRunnable.this.lastRunRequest > DelayedRunnable.this.delayInMS;
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
