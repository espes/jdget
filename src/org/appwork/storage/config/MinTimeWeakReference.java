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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author thomas
 * 
 */
public class MinTimeWeakReference<T> extends WeakReference<T> implements Runnable {

    // private static final ReferenceQueue<? super Object> QUEUE = new
    // ReferenceQueue<Object>();
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

        final MinTimeWeakReference<double[]> ref = new MinTimeWeakReference<double[]>(new double[Integer.MAX_VALUE / 100], 10000, null);
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
            System.out.println(i * 1000 + " - " + ref.superget() != null);
        }

    }

    private Object             hard;

    private ScheduledFuture<?> sustainer;

    private final long         minlifetime;

    private final String       id;

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
        this.minlifetime = minlifetime;
        this.sustainer = MinTimeWeakReference.EXECUTER.schedule(this, minlifetime, TimeUnit.MILLISECONDS);
        System.out.println("Created Week " + id);
    }

    /**
     * @return
     */
    @Override
    public T get() {
        final T ret = super.get();
        if (this.sustainer != null) {
            this.sustainer.cancel(true);
        }
        this.hard = ret;

        if (this.hard == null) {
            this.sustainer = null;
        } else {
            this.sustainer = MinTimeWeakReference.EXECUTER.schedule(this, this.minlifetime, TimeUnit.MILLISECONDS);
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        this.hard = null;

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
