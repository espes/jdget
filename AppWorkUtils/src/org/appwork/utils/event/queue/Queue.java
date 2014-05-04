/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.event.queue
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.event.queue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.appwork.utils.NullsafeAtomicReference;
import org.appwork.utils.logging.Log;

/**
 * @author daniel
 * @param <D>
 * @param <T>
 * 
 */
public abstract class Queue {

    public enum QueuePriority {
        HIGH,
        LOW,
        NORM
    }

    protected boolean                                             debugFlag           = false;
    protected ArrayDeque<QueueAction<?, ? extends Throwable>>     queue               = new ArrayDeque<QueueAction<?, ? extends Throwable>>();

    protected java.util.List<QueueAction<?, ? extends Throwable>> queueThreadHistory  = new ArrayList<QueueAction<?, ? extends Throwable>>(20);
    protected NullsafeAtomicReference<QueueThread>                thread              = new NullsafeAtomicReference<QueueThread>(null);
    private QueueAction<?, ? extends Throwable>                   sourceItem          = null;
    private QueueAction<?, ?>                                     currentJob;

    protected AtomicLong                                          addStats            = new AtomicLong(0);
    protected AtomicLong                                          addWaitStats        = new AtomicLong(0);
    protected AtomicLong                                          addRunStats         = new AtomicLong(0);

    protected static AtomicInteger                                QUEUELOOPPREVENTION = new AtomicInteger(0);
    private final String                                          id;
    protected long                                                timeout             = 10 * 1000l;

    public Queue(final String id) {
        this.id = id;
        Queue.QUEUELOOPPREVENTION.incrementAndGet();
    }

    /**
     * This method adds an action to the queue. if the caller is a queueaction
     * itself, the action will be executed directly. In this case, this method
     * can throw Exceptions. If the caller is not the QUeuethread, this method
     * is not able to throw exceptions, but the exceptions are passed to the
     * exeptionhandler method of the queueaction
     * 
     * @param <T>
     * @param <E>
     * @param item
     * @throws T
     */
    public <E, T extends Throwable> void add(final QueueAction<?, T> action) throws T {
        /* set calling Thread to current item */
        action.reset();
        action.setCallerThread(this, Thread.currentThread());
        if (this.isQueueThread(action)) {
            /*
             * call comes from current running item, so lets start item
             */
            final QueueAction<?, ? extends Throwable> source = ((QueueThread) Thread.currentThread()).getSourceQueueAction();
            if (source != null) {
                /* forward source priority */
                action.setQueuePrio(source.getQueuePrio());
            }
            this.addRunStats.incrementAndGet();
            this.startItem(action, false);
        } else {
            this.addStats.incrementAndGet();
            /* call does not come from current running item, so lets queue it */
            this.internalAdd(action);
        }
    }

    /**
     * Only use this method if you can asure that the caller is NEVER the queue
     * itself. if you are not sure use #add
     * 
     * @param <E>
     * @param <T>
     * @param action
     * @throws T
     */
    public <E, T extends Throwable> void addAsynch(final QueueAction<?, T> action) {
        /* set calling Thread to current item */
        if (action.allowAsync() == false && this.isQueueThread(action)) {
            throw new RuntimeException("called addAsynch from the queue itself");
        } else {
            this.addStats.incrementAndGet();
            action.reset();
            action.setCallerThread(this, Thread.currentThread());
            this.internalAdd(action);
        }
    }

    @SuppressWarnings("unchecked")
    public <E, T extends Throwable> E addWait(final QueueAction<E, T> item) throws T {
        /* set calling Thread to current item */
        item.reset();
        item.setCallerThread(this, Thread.currentThread());
        if (this.isQueueThread(item)) {
            /*
             * call comes from current running item, so lets start item
             * excaption handling is passed to top item. startItem throws an
             * exception in error case
             */
            final QueueAction<?, ? extends Throwable> source = ((QueueThread) Thread.currentThread()).getSourceQueueAction();
            if (source != null) {
                /* forward source priority */
                item.setQueuePrio(source.getQueuePrio());
            }
            this.addRunStats.incrementAndGet();
            this.startItem(item, false);
        } else {
            this.addWaitStats.incrementAndGet();
            /* call does not come from current running item, so lets queue it */
            this.internalAdd(item);
            /* wait till item is finished */
            try {
                while (!item.isFinished()) {
                    synchronized (item) {
                        if (!item.isFinished()) {
                            item.wait(1000);
                        }
                    }
                }
            } catch (final InterruptedException e) {
                item.handleException(e);
            }
            if (item.getExeption() != null) {
                // throw exception if item canot handle the exception itself
                if (!item.callExceptionHandler()) {
                    if (item.getExeption() instanceof RuntimeException) {
                        throw (RuntimeException) item.getExeption();
                    } else {
                        throw (T) item.getExeption();
                    }
                }
            }
            if (item.gotKilled() && !item.gotStarted()) {
                item.handleException(new InterruptedException("Queue got killed!"));
            }
        }
        return item.getResult();
    }

    public void enqueue(final QueueAction<?, ?> action) {
        /* set calling Thread to current item */
        action.reset();
        action.setCallerThread(this, Thread.currentThread());
        this.internalAdd(action);
    }

    protected QueueAction<?, ?> getCurrentJob() {
        return this.currentJob;
    }

    public java.util.List<QueueAction<?, ?>> getEntries() {
        final java.util.List<QueueAction<?, ?>> ret = new ArrayList<QueueAction<?, ?>>();
        synchronized (this.queue) {
            if (this.currentJob != null) {
                ret.add(this.currentJob);
            }
            for (final QueueAction<?, ? extends Throwable> item : this.queue) {
                ret.add(item);
            }
        }
        return ret;
    }

    public String getID() {
        return this.id;
    }

    protected QueueAction<?, ? extends Throwable> getLastHistoryItem() {
        synchronized (this.queueThreadHistory) {
            if (this.queueThreadHistory.size() == 0) { return null; }
            return this.queueThreadHistory.get(this.queueThreadHistory.size() - 1);
        }
    }

    public QueueThread getQueueThread() {
        return this.thread.get();
    }

    protected QueueAction<?, ? extends Throwable> getSourceQueueAction() {
        return this.sourceItem;
    }

    public long getTimeout() {
        return this.timeout;
    }

    /**
     * Overwrite this to hook before a action execution
     */
    protected void handlePreRun() {
        // TODO Auto-generated method stub

    }

    public void internalAdd(final QueueAction<?, ?> action) {
        synchronized (this.queue) {
            switch (action.getQueuePrio()) {
            case NORM:
                this.queue.offer(action);
                break;
            case HIGH:
                this.queue.offerFirst(action);
                break;
            default:
                this.queue.offer(action);
                break;
            }
            if (this.thread.get() == null) {
                this.thread.set(new QueueThread(this));
            }
            this.queue.notifyAll();
        }
    }

    /**
     * returns true if this queue shows debug info
     * 
     * @return
     */
    public boolean isDebug() {
        return this.debugFlag;
    }

    public boolean isEmpty() {
        synchronized (this.queue) {
            return this.queue.size() == 0;
        }
    }

    /**
     * this functions returns true if the current running Thread is our
     * QueueThread OR the SourceQueueItem chain is rooted in current running
     * QueueItem
     */
    public boolean isQueueThread(final QueueAction<?, ? extends Throwable> item) {
        if (Thread.currentThread() == this.thread.get()) { return true; }
        QueueAction<?, ? extends Throwable> last = item;
        Thread t = null;
        /*
         * we walk through actionHistory to check if we are still in our
         * QueueThread
         */
        int loopprevention = 0;
        while (last != null && (t = last.getCallerThread()) != null) {
            if (t != null && t instanceof QueueThread) {
                if (t == this.getQueueThread()) {
                    if (this.debugFlag) {
                        org.appwork.utils.logging.Log.L.warning("Multiple queues detected-> external synchronization may be required! " + item);
                    }
                    return true;
                }
                last = ((QueueThread) t).getLastHistoryItem();
                if (loopprevention > Queue.QUEUELOOPPREVENTION.get()) {
                    /*
                     * loop prevention: while can only loop max
                     * QUEUELOOPPREVENTION times, cause no more different queues
                     * exist
                     */
                    if (this.debugFlag) {
                        org.appwork.utils.logging.Log.L.warning("QueueLoopPrevention!");
                    }
                    break;
                }
                loopprevention++;
            } else {
                break;
            }
        }
        return false;
    }

    /**
     * Does NOT kill the currently running job
     * 
     */
    public void killQueue() {
        final ArrayList<QueueAction<?, ? extends Throwable>> killList = new ArrayList<QueueAction<?, ? extends Throwable>>();
        synchronized (this.queue) {
            System.out.println("Kill: " + this);
            for (final QueueAction<?, ? extends Throwable> item : this.queue) {
                /* kill item */
                System.out.println("K");
                killList.add(item);
            }
            /* clear queue */
            this.queue.clear();
        }
        for (final QueueAction<?, ? extends Throwable> item : killList) {
            item.kill();
        }
    }

    /**
     * @param item
     */
    protected void onItemHandled(final QueueAction<?, ? extends Throwable> item) {
        // TODO Auto-generated method stub

    }

    public boolean remove(final QueueAction<?, ?> action) {
        QueueAction<?, ?> kill = null;
        synchronized (this.queue) {
            if (this.queue.remove(action)) {
                kill = action;
            } else if (action == this.currentJob) {
                kill = action;
            }
        }
        if (kill != null) {
            kill.kill();
            return true;
        }
        return false;

    }

    protected void runQueue() {
        QueueAction<?, ? extends Throwable> item = null;
        while (true) {
            try {
                this.handlePreRun();
                synchronized (this.queue) {
                    item = this.queue.poll();
                    if (item == null) {
                        this.queue.wait(this.getTimeout());
                        item = this.queue.poll();
                        if (item == null) {
                            this.thread.set(null);
                            return;
                        }
                    }
                }
                try {
                    this.sourceItem = item;
                    this.startItem(item, true);
                } catch (final Throwable e) {
                } finally {
                    this.sourceItem = null;
                    this.onItemHandled(item);
                }
            } catch (final Throwable e) {
                Log.L.info("Queue rescued!");
                Log.exception(e);
            }
        }
    }

    /**
     * changes this queue's debugFlag
     * 
     * @param b
     */
    public void setDebug(final boolean b) {
        this.debugFlag = b;
    }

    public void setTimeout(long timeout) {
        timeout = Math.max(0, timeout);
        this.timeout = timeout;
        synchronized (this.queue) {
            this.queue.notifyAll();
        }
    }

    public int size() {
        synchronized (this.queue) {
            return this.queue.size();
        }
    }

    /* if you override this, DON'T forget to notify item when its done! */
    @SuppressWarnings("unchecked")
    protected <T extends Throwable> void startItem(final QueueAction<?, T> item, final boolean callExceptionhandler) throws T {
        try {
            this.currentJob = item;
            if (this.getQueueThread() != item.getCallerThread()) {
                synchronized (this.queueThreadHistory) {
                    this.queueThreadHistory.add(item);
                }
            }
            item.start(this);
        } catch (final Throwable e) {
            if (!callExceptionhandler || !item.callExceptionHandler()) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw (T) e;
                }
            }
        } finally {
            if (this.getQueueThread() != item.getCallerThread()) {
                synchronized (this.queueThreadHistory) {
                    if (this.queueThreadHistory.size() != 0) {
                        this.queueThreadHistory.remove(this.queueThreadHistory.size() - 1);
                    }
                }
            }
            item.setFinished(true);
            this.currentJob = null;

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#toString()
     */
    @Override
    public String toString() {
        return this.id + ": add=" + this.addStats.get() + " addWait=" + this.addWaitStats.get() + " addRun=" + this.addRunStats.get();
    }
}
