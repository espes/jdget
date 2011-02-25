/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * @author daniel
 * 
 */
public class RingBufferOutputStream {

    public class RingBufferElement extends ByteArrayOutputStream {
        public RingBufferElement(final int size) {
            super(size);
        }

        protected byte[] getBuffer() {
            return this.buf;
        }

        protected int getBufferSize() {
            return this.count;
        }
    }

    final private Object                        LOCK     = new Object();

    final Thread                                thread;
    final private LinkedList<RingBufferElement> ringFree = new LinkedList<RingBufferElement>();
    private final LinkedList<RingBufferElement> ringTodo = new LinkedList<RingBufferElement>();

    public RingBufferOutputStream(final OutputStream out) {
        for (int i = 0; i < 1000; i++) {
            this.ringFree.add(new RingBufferElement(16767));
        }
        this.thread = new Thread(new Runnable() {

            public void run() {
                try {
                    while (!RingBufferOutputStream.this.thread.isInterrupted() && RingBufferOutputStream.this.thread.isAlive()) {
                        RingBufferElement todo = null;
                        synchronized (RingBufferOutputStream.this.LOCK) {
                            if (!RingBufferOutputStream.this.ringTodo.isEmpty()) {
                                System.out.println("Free: " + RingBufferOutputStream.this.ringFree.size() + " Todo: " + RingBufferOutputStream.this.ringTodo.size());
                                todo = RingBufferOutputStream.this.ringTodo.removeFirst();
                            } else {
                                try {
                                    RingBufferOutputStream.this.LOCK.wait();
                                } catch (final InterruptedException e) {
                                    e.printStackTrace();
                                    RingBufferOutputStream.this.interrupt();
                                    break;
                                }
                            }
                        }
                        try {
                            if (todo != null) {
                                out.write(todo.getBuffer(), 0, todo.getBufferSize());
                            }
                        } catch (final IOException e) {
                            e.printStackTrace();
                            RingBufferOutputStream.this.interrupt();
                            break;
                        }
                        if (todo != null) {
                            synchronized (RingBufferOutputStream.this.LOCK) {
                                RingBufferOutputStream.this.ringFree.addLast(todo);
                                todo = null;
                                RingBufferOutputStream.this.LOCK.notifyAll();
                            }
                        }
                    }
                } catch (final Throwable e) {
                    e.printStackTrace();
                }
            }

        });
        this.thread.setName("RingBufferOutputStream: " + out);
        this.thread.start();
    }

    public RingBufferElement getNextByteArrayOutputStream() {
        RingBufferElement ret = null;
        while (!this.thread.isInterrupted() && this.thread.isAlive()) {
            synchronized (this.LOCK) {
                if (!this.ringFree.isEmpty()) {
                    ret = this.ringFree.removeFirst();
                    ret.reset();
                    return ret;
                } else {
                    try {
                        this.LOCK.wait();
                    } catch (final InterruptedException e) {
                        this.interrupt();
                        break;
                    }
                }
            }
        }
        return null;
    }

    public void interrupt() {
        RingBufferOutputStream.this.thread.interrupt();
        synchronized (this.LOCK) {
            this.LOCK.notifyAll();
        }
    }

    public void write(final RingBufferElement buffer) {
        synchronized (this.LOCK) {
            this.ringTodo.addLast(buffer);
            this.LOCK.notifyAll();
        }
    }
}
