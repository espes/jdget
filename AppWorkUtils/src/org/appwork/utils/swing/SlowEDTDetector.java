/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.appwork.scheduler.DelayedRunnable;
import org.appwork.utils.logging2.LogSource;

/**
 * @author daniel
 * 
 */
public class SlowEDTDetector {
    private final AtomicLong      lastInvoke        = new AtomicLong(-1);
    private final AtomicLong      lastEDT           = new AtomicLong(-1);
    private final AtomicBoolean   detectEDTBlocking = new AtomicBoolean(false);
    private final AtomicBoolean   doLog             = new AtomicBoolean(true);
    private final DelayedRunnable detector;
    private final long            maxEDTBlockingTime;

    public SlowEDTDetector(final long maxEDTBlockingTime, final LogSource logger) {
        this.maxEDTBlockingTime = maxEDTBlockingTime;
        this.detector = new DelayedRunnable(maxEDTBlockingTime, maxEDTBlockingTime) {

            @Override
            public void delayedrun() {
                try {
                    long blocking = SlowEDTDetector.this.lastEDT.get();
                    if (blocking < 0) {
                        blocking = System.currentTimeMillis() - SlowEDTDetector.this.lastInvoke.get();
                    }
                    if (blocking >= SlowEDTDetector.this.maxEDTBlockingTime) {
                        if (SlowEDTDetector.this.doLog.get()) {
                            try {
                                final Iterator<Entry<Thread, StackTraceElement[]>> it = Thread.getAllStackTraces().entrySet().iterator();
                                while (it.hasNext()) {
                                    final Entry<Thread, StackTraceElement[]> next = it.next();
                                    final StringBuilder sb = new StringBuilder();
                                    sb.append("BlockingEDT Detected(" + blocking + "ms)->Thread: " + next.getKey().getName() + "\r\n");
                                    for (final StackTraceElement stackTraceElement : next.getValue()) {
                                        sb.append("\tat " + stackTraceElement + "\r\n");
                                    }
                                    logger.severe(sb.toString());
                                }
                            } finally {
                                SlowEDTDetector.this.doLog.set(false);
                            }
                        }
                    } else {
                        SlowEDTDetector.this.doLog.set(true);
                    }
                } catch (final Throwable e) {
                    logger.log(e);
                }
                SlowEDTDetector.this.invokeEDT();

            }
        };
        this.invokeEDT();
    }

    protected void invokeEDT() {
        if (this.detectEDTBlocking.compareAndSet(false, true)) {
            this.lastEDT.set(-1);
            this.lastInvoke.set(System.currentTimeMillis());
            new EDTRunner() {

                @Override
                protected void runInEDT() {
                    SlowEDTDetector.this.lastEDT.set(System.currentTimeMillis() - SlowEDTDetector.this.lastInvoke.get());
                    SlowEDTDetector.this.detectEDTBlocking.set(false);
                }
            };
        }
        this.detector.run();
    }
}
