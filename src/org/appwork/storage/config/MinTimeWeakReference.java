/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.appwork.scheduler.DelayedRunnable;

/**
 * @author thomas
 * 
 */
public class MinTimeWeakReference<T> extends WeakReference<T> {

    private static final ScheduledExecutorService       EXECUTER = Executors.newSingleThreadScheduledExecutor();

    private static final ReferenceQueue<? super Object> QUEUE    = new ReferenceQueue<Object>();

    static {
        new Thread() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    Reference<?> sv;
                    while ((sv = MinTimeWeakReference.QUEUE.poll()) != null) {
                        System.out.println("KILLED " + sv);
                    }
                }
            }
        }.start();
    }

    public static void main(final String[] args) {

        final MinTimeWeakReference<double[]> ref = new MinTimeWeakReference<double[]>(new double[20000], 2000, null);
        for (int i = 0; i < 20; i++) {
            System.out.println(i * 1000 + " - " + ref.get().length);
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 20; i++) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.gc();
            System.out.println(i * 1000 + " - " + (ref.superget() != null));
        }

    }

    private T                     hard;

    private final String          id;

    private final DelayedRunnable delayer;

    /**
     * @param ret
     * @param id
     *            TODO
     * @param ret2
     */
    public MinTimeWeakReference(final T ret, final long minlifetime, final String id) {
        super(ret, MinTimeWeakReference.QUEUE);
        this.hard = ret;
        this.id = id;
        this.delayer = new DelayedRunnable(MinTimeWeakReference.EXECUTER, minlifetime) {

            @Override
            public void delayedrun() {
                MinTimeWeakReference.this.hard = null;
            }

        };
        System.out.println("Created Week " + id);
    }

    /**
     * @return
     */
    @Override
    public T get() {
        final T ret = super.get();
        if (ret == null) {
            this.delayer.stop();
        } else {
            this.hard = ret;
            this.delayer.run();
        }
        return this.hard;
    }

    /**
     * @return
     */
    private T superget() {
        return super.get();
    }

    @Override
    public String toString() {
        return " Cacheed " + this.id;
    }
}
