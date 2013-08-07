/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.event.queue
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.event.queue;

/**
 * @author daniel
 * 
 */
public class QueueThread extends Thread {

    private final Queue queue;

    public QueueThread(final Queue queue) {
        this.queue = queue;
        this.setName("Queue:" + queue.getID());
        this.setDaemon(true);
        this.start();
    }

    protected QueueAction<?, ? extends Throwable> getLastHistoryItem() {
        return this.queue.getLastHistoryItem();
    };

    protected QueueAction<?, ? extends Throwable> getSourceQueueAction() {
        return this.queue.getSourceQueueAction();
    }

    @Override
    public void run() {
        this.queue.runQueue();
    }
}
