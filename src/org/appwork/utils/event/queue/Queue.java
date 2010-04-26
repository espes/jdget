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

/**
 * @author daniel
 * 
 */
public class Queue extends Thread {

    protected ArrayList<QueueItem> queue = new ArrayList<QueueItem>();
    protected boolean waitFlag = true;
    protected QueueItem item = null;
    protected Thread thread = null;

    /*
     * this functions returns true if the current running Thread is our
     * QueueThread OR the SourceQueueItem chain is rooted in current running
     * QueueItem
     */
    public boolean isQueueThread(QueueItem item) {
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
        start();
    }

    public boolean isWaiting() {
        return waitFlag;
    }

    public void add(QueueItem item) {
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

    protected void internalAdd(QueueItem item) {
        synchronized (queue) {
            queue.add(item);
        }
        synchronized (this) {
            if (waitFlag) {
                waitFlag = false;
                notify();
            }
        }
    }

    public Object addWait(QueueItem item) throws Exception {
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
                try {
                    synchronized (item) {
                        item.wait(1000);
                    }
                } catch (InterruptedException e) {
                }
            }
        }
        if (item.gotKilled()) throw new Exception("Queue got killed!");
        if (item.getException() != null) throw item.getException();
        return item.getResult();
    }

    public boolean isEmpty() {
        synchronized (queue) {
            return queue.isEmpty();
        }
    }

    public void killQueue() {
        synchronized (queue) {
            for (QueueItem item : queue) {
                item.kill();
                synchronized (item) {
                    item.notify();
                }
            }
            queue.clear();
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
                        e.printStackTrace();
                    }
                }
            }
            synchronized (queue) {
                if (queue.size() > 0) {
                    item = queue.remove(0);
                } else {
                    waitFlag = true;
                    item = null;
                }
            }
            if (item == null || waitFlag) continue;
            startItem(item);
        }
    }

    /* if you override this, DON'T forget to notify item when its done! */
    protected void startItem(QueueItem item) {
        try {
            item.start(this);
        } catch (Exception e) {
            try {
                item.exceptionHandler(e);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } finally {
            synchronized (item) {
                item.notifyAll();
            }
        }
    }
}
