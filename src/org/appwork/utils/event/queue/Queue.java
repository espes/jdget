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

    public boolean isQueueThread() {
        return currentThread() == thread;
    }

    public Queue(String id) {
        super(id);
        start();
    }

    public void add(QueueItem item) {
        if (isQueueThread()) {
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
        if (isQueueThread()) {
            /*
             * call comes from current running item, so lets start item
             */
            startItem(item);
        } else {
            /* call does not come from current running item, so lets queue it */
            add(item);
        }
        while (!item.isFinished()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
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
            }
            queue.clear();
        }
    }

    public void run() {
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

    protected void startItem(QueueItem item) {
        try {
            item.start(this);
        } catch (Exception e) {
            try {
                item.exceptionHandler(e);
            } catch (Exception e2) {
            }
        }
    }
}
