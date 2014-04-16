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
import java.util.concurrent.atomic.AtomicReference;

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

    // private final String id;

    private final AtomicReference<DelayedRunnable> hardReference = new AtomicReference<DelayedRunnable>(null);
    private final MinTimeWeakReferenceCleanup      cleanupMinTimeWeakReference;
    private final String                           id;
    private final long                             minLifeTime;

    /**
     * @param ret
     * @param id
     *            TODO
     * @param ret2
     */
    public MinTimeWeakReference(final T ret, final long minlifetime, final String id) {
        this(ret, minlifetime, id, null);
    }

    public MinTimeWeakReference(final T ret, final long minLifeTime, final String id, final MinTimeWeakReferenceCleanup cleanupMinTimeWeakReference) {
        super(ret, MinTimeWeakReference.QUEUE);
        this.id = id;
        this.minLifeTime = minLifeTime;
        /* we get the item at least once to start the cleanup process here */
        this.hardReference.set(new DelayedRunnable(MinTimeWeakReference.EXECUTER, minLifeTime) {
            @SuppressWarnings("unused")
            private final T hardReference = ret;

            @Override
            public void delayedrun() {
                MinTimeWeakReference.this.hardReference.compareAndSet(this, null);
            }

            @Override
            public String getID() {
                return "MinTimeWeakReference_" + id;
            }

            @Override
            public boolean stop() {
                this.delayedrun();
                return super.stop();
            }
        });

        this.get();
        this.cleanupMinTimeWeakReference = cleanupMinTimeWeakReference;
    }

    public void clearReference() {
        final DelayedRunnable old = this.hardReference.getAndSet(null);
        if (old != null) {
            old.stop();
        }
    }

    /**
     * @return
     */
    @Override
    public T get() {
        final T ret = super.get();
        if (ret == null) {
            this.clearReference();
        } else {
            DelayedRunnable minHardReference = this.hardReference.get();
            if (minHardReference != null) {
                minHardReference.resetAndStart();
                this.hardReference.compareAndSet(null, minHardReference);
            } else {
                minHardReference = new DelayedRunnable(MinTimeWeakReference.EXECUTER, this.minLifeTime) {
                    @SuppressWarnings("unused")
                    private final T hardReference = ret;

                    @Override
                    public void delayedrun() {
                        MinTimeWeakReference.this.hardReference.compareAndSet(this, null);
                    }

                    @Override
                    public String getID() {
                        return "MinTimeWeakReference_" + MinTimeWeakReference.this.id;
                    }

                    @Override
                    public boolean stop() {
                        this.delayedrun();
                        return super.stop();
                    }
                };
                if (this.hardReference.compareAndSet(null, minHardReference)) {
                    minHardReference.resetAndStart();
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
            this.clearReference();
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

    @Override
    public String toString() {
        return "MinTimeWeakReference_" + MinTimeWeakReference.this.id + "|Gone:" + this.isGone();
    }

}
