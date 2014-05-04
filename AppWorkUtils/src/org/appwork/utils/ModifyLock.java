/**
 * Copyright (c) 2009 - 2014 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author daniel
 * 
 */
public class ModifyLock {

    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public final boolean readLock() {
        if (!this.lock.writeLock().isHeldByCurrentThread()) {
            this.lock.readLock().lock();
            return true;
        }
        return false;
    }

    public void readUnlock(final boolean state) throws IllegalMonitorStateException {
        if (state == false) { return; }
        this.lock.readLock().unlock();
    }

    public final void runReadLock(final Runnable run) {
        Boolean readL = null;
        try {
            readL = this.readLock();
            run.run();
        } finally {
            if (readL != null) {
                this.readUnlock(readL);
            }
        }
    }

    public final void runWriteLock(final Runnable run) {
        try {
            this.writeLock();
            run.run();
        } finally {
            this.writeUnlock();
        }
    }

    public final void writeLock() {
        this.lock.writeLock().lock();
    }

    public void writeUnlock() throws IllegalMonitorStateException {
        this.lock.writeLock().unlock();
    }
}
