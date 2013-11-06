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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.appwork.scheduler.DelayedRunnable;

/**
 * @author thomas
 * 
 */
public class MinTimeWeakReference<T> extends WeakReference<T> {

    private static final ScheduledExecutorService EXECUTER     = DelayedRunnable.getNewScheduledExecutorService();
    private static final ScheduledExecutorService QUEUECLEANUP = DelayedRunnable.getNewScheduledExecutorService();
    private static final ReferenceQueue<Object>   QUEUE        = new ReferenceQueue<Object>();
    static {
        MinTimeWeakReference.QUEUECLEANUP.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                try {
                    Reference<?> remove = null;
                    while ((remove = MinTimeWeakReference.QUEUE.poll()) != null) {
                        ((MinTimeWeakReference) remove).onCleanup();
                    }
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            }
        }, 10, 60, TimeUnit.SECONDS);
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

    @SuppressWarnings("unused")
    private T                                 hard;

    // private final String id;

    private DelayedRunnable                   delayer;
    private final MinTimeWeakReferenceCleanup cleanupMinTimeWeakReference;
    private final String                      id;

    /**
     * @param ret
     * @param id
     *            TODO
     * @param ret2
     */
    public MinTimeWeakReference(final T ret, final long minlifetime, final String id) {
        this(ret, minlifetime, id, null);
    }

    public MinTimeWeakReference(final T ret, final long minlifetime, final String id, final MinTimeWeakReferenceCleanup cleanupMinTimeWeakReference) {
        // super(ret, MinTimeWeakReference.QUEUE);
        super(ret, MinTimeWeakReference.QUEUE);
        this.hard = ret;
        this.id = id;
        // this.id = id;
        this.delayer = new DelayedRunnable(MinTimeWeakReference.EXECUTER, minlifetime) {

            @Override
            public void delayedrun() {
                // System.out.println("remove hardRef");
                synchronized (MinTimeWeakReference.this) {
                    MinTimeWeakReference.this.hard = null;
                }
            }

            @Override
            public String getID() {
                return "MinTimeWeakReference_" + id;
            }

        };
        /* we get the item at least once to start the cleanup process here */
        this.get();
        // System.out.println("Created Week " + id);
        this.cleanupMinTimeWeakReference = cleanupMinTimeWeakReference;
    }

    /**
     * @return
     */
    @Override
    public T get() {
        final T ret = super.get();
        if (ret == null) {
            synchronized (MinTimeWeakReference.this) {
                /* T is gone so lets kill hardreference too */
                if (this.delayer != null) {
                    this.delayer.stop();
                }
                this.delayer = null;
                this.hard = null;
                return null;
            }
        } else {
            /* T still exists, lets refresh hardreference */
            synchronized (MinTimeWeakReference.this) {
                if (this.delayer != null) {
                    this.delayer.run();
                    this.hard = ret;
                }
            }
        }
        return ret;
    }

    public String getID() {
        return this.id;
    }

    public boolean isGone() {
        final T ret = super.get();
        if (ret == null) {
            synchronized (MinTimeWeakReference.this) {
                if (this.delayer != null) {
                    this.delayer.stop();
                }
                this.delayer = null;
                this.hard = null;
            }
            return true;
        }
        return false;
    }

    protected void onCleanup() {
        if (this.cleanupMinTimeWeakReference != null) {
            this.cleanupMinTimeWeakReference.onMinTimeWeakReferenceCleanup(this);
        }
    }

    /**
     * @return
     */
    public T superget() {
        return super.get();
    }

    // @Override
    // public String toString() {
    // return " Cacheed " + this.id;
    // }
}
