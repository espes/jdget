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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author daniel
 * @param <D>
 * @param <T>
 * 
 */
public abstract class Queue extends Thread {

    public enum QueuePriority {
        HIGH,
        NORM,
        LOW
    }

    protected HashMap<QueuePriority, ArrayList<QueueAction<?, ? extends Throwable>>> queue = new HashMap<QueuePriority, ArrayList<QueueAction<?, ? extends Throwable>>>();
    protected final Object queueLock = new Object();
    protected boolean waitFlag = true;

    protected Thread thread = null;
    protected QueuePriority[] prios;

    /**
     * this functions returns true if the current running Thread is our
     * QueueThread OR the SourceQueueItem chain is rooted in current running
     * QueueItem
     */
    public boolean isQueueThread(QueueAction<?, ? extends Throwable> item) {
        if (currentThread() == thread) return true;
        QueueAction<?, ? extends Throwable> source = item.getSourceQueueItem();
        while (source != null) {
            if (source.gotStarted()) return true;
            source = source.getSourceQueueItem();
        }
        return false;
    }

    public Queue(String id) {
        super(id);
        /* init queue */
        prios = QueuePriority.values();
        for (QueuePriority prio : prios) {
            queue.put(prio, new ArrayList<QueueAction<?, ? extends Throwable>>());
        }
        start();
    }

    public boolean isWaiting() {
        return waitFlag;
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
    public <E, T extends Throwable> void add(QueueAction<?, T> action) throws T {
        if (isQueueThread(action)) {
            /*
             * call comes from current running item, so lets start item
             */
            startItem(action, false);
        } else {
            /* call does not come from current running item, so lets queue it */
            internalAdd(action);
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
    public <E, T extends Throwable> void addAsynch(QueueAction<?, T> action) {
        if (isQueueThread(action)) {
            throw new RuntimeException("called addAsynch from the queue itself");
        } else {

            internalAdd(action);
        }

    }

    public void enqueue(QueueAction<?, ?> action) {
        internalAdd(action);
    }

    protected void internalAdd(QueueAction<?, ?> action) {
        synchronized (queueLock) {
            queue.get(action.getQueuePrio()).add(action);
        }
        synchronized (this) {
            if (waitFlag) {
                waitFlag = false;
                notify();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <E, T extends Throwable> E addWait(QueueAction<E, T> item) throws T, InterruptedException {
        if (isQueueThread(item)) {
            /*
             * call comes from current running item, so lets start item
             */
            startItem(item, false);
        } else {
            /* call does not come from current running item, so lets queue it */
            internalAdd(item);
            /* wait till item is finished */
            while (!item.isFinished()) {

                synchronized (item) {
                    item.wait(1000);
                }

            }
        }
        if (item.gotKilled()) { throw new InterruptedException("Queue got killed!"); }
        if (item.getExeption() != null) {
            if (item.getExeption() instanceof RuntimeException) {
                throw (RuntimeException) item.getExeption();
            } else {
                throw (T) item.getExeption();
            }

        }
        return item.getResult();
    }

    public boolean isEmpty() {
        synchronized (queueLock) {
            for (QueuePriority prio : prios) {
                if (!queue.get(prio).isEmpty()) return false;
            }
            return true;
        }
    }

    public void killQueue() {
        synchronized (queueLock) {
            for (QueuePriority prio : prios) {
                for (QueueAction<?, ? extends Throwable> item : queue.get(prio)) {
                    /* kill item */
                    item.kill();
                    synchronized (item) {
                        item.notify();
                    }
                }
                /* clear queue */
                queue.get(prio).clear();
            }

        }
    }

    public void run() {
        if (thread != null) return;
        thread = this;
        QueueAction<?, ? extends Throwable> item = null;
        while (true) {
            handlePreRun();
            synchronized (this) {
                while (waitFlag) {
                    try {
                        wait();
                    } catch (Exception e) {
                        org.appwork.utils.logging.Log.exception(e);
                    }
                }
            }
            synchronized (queueLock) {
                item = null;
                for (QueuePriority prio : prios) {
                    if (queue.get(prio).size() > 0) {
                        item = queue.get(prio).remove(0);
                        break;
                    }
                }
                if (item == null) {
                    waitFlag = true;
                }
            }
            if (item == null || waitFlag) continue;
            try {
                startItem(item, true);
            } catch (Throwable e) {

            }
        }
    }

    /**
     * Overwrite this to hook before a action execution
     */
    protected void handlePreRun() {
        // TODO Auto-generated method stub

    }

    /* if you override this, DON'T forget to notify item when its done! */
    protected <T extends Throwable> void startItem(QueueAction<?, T> item, boolean callExceptionhandler) throws T {
        try {
            item.start(this);

        } finally {
            if (item.getExeption() != null && callExceptionhandler) {
                if (!item.exceptionHandler(item.getExeption())) {
                    // print out exception if code does not handle it
                    // Log.exception(item.getExeption());
                }
            }
            synchronized (item) {
                item.notify();
            }
        }
    }
}
