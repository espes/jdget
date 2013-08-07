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

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledExecutorService;

import org.appwork.scheduler.DelayedRunnable;

/**
 * @author thomas
 * 
 */
public class MinTimeWeakReference<T> extends WeakReference<T> {

    private static final ScheduledExecutorService EXECUTER = DelayedRunnable.getNewScheduledExecutorService();

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
    private T               hard;

    // private final String id;

    private DelayedRunnable delayer;

    /**
     * @param ret
     * @param id
     *            TODO
     * @param ret2
     */
    public MinTimeWeakReference(final T ret, final long minlifetime, final String id) {
        // super(ret, MinTimeWeakReference.QUEUE);
        super(ret);
        this.hard = ret;
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
