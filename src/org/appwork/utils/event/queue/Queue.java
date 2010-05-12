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
 * 
 */
public abstract class Queue<E extends QueueItem> extends Thread {

    public enum QueuePriority {
        HIGH,
        NORM,
        LOW
    }

    protected HashMap<QueuePriority, ArrayList<E>> queue = new HashMap<QueuePriority, ArrayList<E>>();
    protected final Object queueLock = new Object();
    protected boolean waitFlag = true;
    protected E item = null;
    protected Thread thread = null;
    protected QueuePriority[] prios;

    /*
     * this functions returns true if the current running Thread is our
     * QueueThread OR the SourceQueueItem chain is rooted in current running
     * QueueItem
     */
    public boolean isQueueThread(E item) {
        if (currentThread() == thread) return true;
        QueueItem source = item.getSourceQueueItem();
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
            queue.put(prio, new ArrayList<E>());
        }
        start();
    }

    public boolean isWaiting() {
        return waitFlag;
    }

    public void add(E item) {
        if (isQueueThread(item)) {
            /*
             * call comes from current running item, so lets start item
             */
            startItem(item);
        } else {
            /* call does not come from current running item, so lets queue it */
            internalAdd(item);
        }
    }

    public void enqueue(E item) {
        internalAdd(item);
    }

    protected void internalAdd(E item) {
        synchronized (queueLock) {
            queue.get(item.getQueuePrio()).add(item);
        }
        synchronized (this) {
            if (waitFlag) {
                waitFlag = false;
                notify();
            }
        }
    }

    public Object addWait(E item) throws Exception {
        if (isQueueThread(item)) {
            /*
             * call comes from current running item, so lets start item
             */
            startItem(item);
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
        if (item.gotKilled()) throw new Exception("Queue got killed!");
        if (item.getException() != null) throw item.getException();
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
                for (QueueItem item : queue.get(prio)) {
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
        while (true) {
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
            startItem(item);
        }
    }

    /* if you override this, DON'T forget to notify item when its done! */
    protected void startItem(E item) {
        try {
            item.start(this);
        } catch (Exception e) {
            try {
                item.exceptionHandler(e);
            } catch (Exception e2) {
                org.appwork.utils.logging.Log.exception(e2);
            }
        } finally {
            synchronized (item) {
                item.notify();
            }
        }
    }
}
