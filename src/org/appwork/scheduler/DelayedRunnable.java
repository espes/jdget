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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author daniel
 * 
 */
public abstract class DelayedRunnable implements Runnable {

    private final ScheduledExecutorService service;
    private final long                     delayInMS;
    private final AtomicLong               nextDelay       = new AtomicLong(0);
    private final AtomicLong               lastRunRequest  = new AtomicLong(0);
    private final AtomicLong               firstRunRequest = new AtomicLong(0);
    private final AtomicBoolean            delayerSet      = new AtomicBoolean(false);
    private final long                     maxInMS;
    private final AtomicBoolean            delayerEnabled  = new AtomicBoolean(true);

    public DelayedRunnable(final ScheduledExecutorService service, final long delayInMS) {
        this(service, delayInMS, -1);
    }

    public DelayedRunnable(final ScheduledExecutorService service, final long minDelayInMS, final long maxDelayInMS) {
        this.service = service;
        this.delayInMS = minDelayInMS;
        this.nextDelay.set(this.delayInMS);
        this.maxInMS = maxDelayInMS;
        if (this.delayInMS <= 0) { throw new IllegalArgumentException("minDelay must be >0"); }
        if (this.maxInMS == 0) { throw new IllegalArgumentException("maxDelay must be !=0"); }
    }

    abstract public void delayedrun();

    /**
     * @return the id
     */
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
                DelayedRunnable.this.firstRunRequest.set(currentTime);
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
                    this.delayAgain(currentTime, DelayedRunnable.this.delayInMS, minDif, thisRunRequest);
                } else {
                    this.stop();
                }
            }

            private void stop() {
                DelayedRunnable.this.nextDelay.set(DelayedRunnable.this.delayInMS);
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
