/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

import java.util.LinkedList;

/**
 * @author daniel
 * 
 */
public class ShutDownHooksQueue extends Thread {

    private static class ShutDownHookItem extends Thread {
        private final Runnable run;
        private final int      positionWish;

        public ShutDownHookItem(final Runnable run, final int positionWish) {
            this.run = run;
            this.positionWish = positionWish;
        }

        @Override
        public void run() {
            this.run.run();
        }
    }

    public static void add(final Runnable run) {
        ShutDownHooksQueue.add(run, 1);
    }

    public static boolean remove(final Runnable run) {
        if (ShutDownHooksQueue.started) { throw new IllegalStateException("Shutdown in progress"); }
        if (run == null) { throw new NullPointerException(); }
        synchronized (ShutDownHooksQueue.INSTANCE.queue) {
            for (final ShutDownHookItem item : ShutDownHooksQueue.INSTANCE.queue) {
                if (item.run == run) {
                    ShutDownHooksQueue.INSTANCE.queue.remove(item);
                    return true;
                }
            }
        }
        return false;
    }

    private final LinkedList<ShutDownHookItem> queue    = new LinkedList<ShutDownHookItem>();

    private final static ShutDownHooksQueue    INSTANCE = new ShutDownHooksQueue();
    private static volatile boolean            started  = false;

    /**
     * adds new Runnable to ShutdownHook at a wished position
     * 
     * positionWish==0, position must be last in queue, else does not matter
     * 
     * @param run
     * @param positionWish
     */
    public static void add(final Runnable run, final int positionWish) {
        if (ShutDownHooksQueue.started) { throw new IllegalStateException("Shutdown in progress"); }
        if (run == null) { throw new NullPointerException(); }
        synchronized (ShutDownHooksQueue.INSTANCE.queue) {
            for (final ShutDownHookItem item : ShutDownHooksQueue.INSTANCE.queue) {
                if (item.run == run) { throw new IllegalArgumentException("Hook previously registered"); }
            }
            if (positionWish != 0 || ShutDownHooksQueue.INSTANCE.queue.size() == 0) {
                /* add at any place */
                ShutDownHooksQueue.INSTANCE.queue.add(0, new ShutDownHookItem(run, positionWish));
            } else if (positionWish == 0) {
                /* add at end */
                final ShutDownHookItem last = ShutDownHooksQueue.INSTANCE.queue.getLast();
                if (last.positionWish == positionWish) {
                    throw new IllegalArgumentException("Very last Hook already registered");
                } else {
                    /* set new very last position */
                    ShutDownHooksQueue.INSTANCE.queue.addLast(new ShutDownHookItem(run, positionWish));
                }
            }
        }
    }

    private final long MAXWAIT = 30000l;

    private ShutDownHooksQueue() {
        Runtime.getRuntime().addShutdownHook(this);
    }

    @Override
    public void run() {
        ShutDownHooksQueue.started = true;
        LinkedList<ShutDownHookItem> queue = null;
        synchronized (this.queue) {
            queue = new LinkedList<ShutDownHookItem>(this.queue);
        }
        for (final ShutDownHookItem item : queue) {
            System.out.println("ShutDownHooksQueue: start item" + item);
            item.start();
            try {
                item.join(this.MAXWAIT);
            } catch (final Throwable e) {
            }
            if (item.isAlive()) {
                System.out.println("ShutDownHooksQueue: " + item + " is still running!");
            }
        }
    }
}
