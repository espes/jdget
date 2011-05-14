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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author thomas
 * 
 */
public class SingleSchedule {
    // testcase only
    private static int  COUNTER;
    // testcase only
    private static long TIME;

    /**
     * This testcase should run in the given interval of 500 ms. no matter of
     * the random submit interval
     * 
     * @param args
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws InterruptedException {
        final SingleSchedule scheduler = new SingleSchedule(1000);
        SingleSchedule.COUNTER = 0;
        SingleSchedule.TIME = System.currentTimeMillis();
        while (true) {
            scheduler.submit(new Runnable() {

                @Override
                public void run() {
                    System.out.println(SingleSchedule.COUNTER + ": " + (System.currentTimeMillis() - SingleSchedule.TIME));
                    SingleSchedule.TIME = System.currentTimeMillis();

                }
            });
            SingleSchedule.COUNTER++;
            Thread.sleep((long) (Math.random() * 5000));
        }
    }

    private final int                      delay;
    private final ScheduledExecutorService executer;
    private ScheduledFuture<?>             active;

    /**
     * @param i
     */
    public SingleSchedule(final int ms) {

        this.delay = ms;
        this.executer = Executors.newSingleThreadScheduledExecutor();

    }

    public void run() {

    }

    /**
     * @param runnable
     */
    public synchronized void submit(final Runnable runnable) {

        if (this.active != null) {
            this.active.cancel(true);
        }
        this.active = this.executer.schedule(new Runnable() {

            @Override
            public void run() {

                try {
                    Thread.sleep(SingleSchedule.this.delay);
                } catch (final InterruptedException e) {

                    return;
                }

                runnable.run();

            }
        }, this.delay, TimeUnit.MILLISECONDS);

    }
}
