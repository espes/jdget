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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author daniel
 * 
 */
public class Input2OutputStreamForwarder {

    private final InputStream  in;
    private final OutputStream out;

    final byte[]               buffer;
    long                       inC      = 0;
    long                       outC     = 0;

    int                        readP    = 0;

    int                        writeF   = 0;

    int                        readF    = 0;
    int                        writeP   = 0;
    int                        readS    = 0;
    int                        writeS   = 0;
    final Object               LOCK     = new Object();
    protected Thread           thread   = null;
    private IOException        outE     = null;
    private volatile boolean   eof      = false;
    private volatile boolean   readDone = false;

    public Input2OutputStreamForwarder(final InputStream in, final OutputStream out) {
        this.in = in;
        this.out = out;
        this.buffer = new byte[1 * 1024 * 1024];
        this.createstartThread();
    }

    public Input2OutputStreamForwarder(final InputStream in, final OutputStream out, final byte[] buffer) {
        this.in = in;
        this.out = out;
        if (buffer == null || buffer.length < 1024) { throw new IllegalArgumentException("invalid buffer"); }
        this.buffer = buffer;
        this.createstartThread();
    }

    public Input2OutputStreamForwarder(final InputStream in, final OutputStream out, final int size) {
        this.in = in;
        this.out = out;
        if (size < 1024) { throw new IllegalArgumentException("invalid buffer size"); }
        this.buffer = new byte[size];
        this.createstartThread();
    }

    private void createstartThread() {
        this.thread = new Thread(new Runnable() {

            public void run() {
                try {
                    while (!Input2OutputStreamForwarder.this.thread.isInterrupted()) {
                        synchronized (Input2OutputStreamForwarder.this.LOCK) {
                            if (Input2OutputStreamForwarder.this.writeF > Input2OutputStreamForwarder.this.readF) {
                                Input2OutputStreamForwarder.this.readP = 0;
                                Input2OutputStreamForwarder.this.readF = Input2OutputStreamForwarder.this.writeF;
                                // System.out.println("writer flip");
                            }
                            if (Input2OutputStreamForwarder.this.readP < Input2OutputStreamForwarder.this.writeP) {
                                /*
                                 * write pointer > read pointer, there must be
                                 * data to get written
                                 */
                                Input2OutputStreamForwarder.this.readS = Input2OutputStreamForwarder.this.writeP - Input2OutputStreamForwarder.this.readP;
                                // System.out.println("writer normal");
                            } else if (Input2OutputStreamForwarder.this.writeP < Input2OutputStreamForwarder.this.readP) {
                                /* write pointer < read pointer */
                                Input2OutputStreamForwarder.this.readS = Input2OutputStreamForwarder.this.buffer.length - Input2OutputStreamForwarder.this.readP;
                                // System.out.println("writer RestBuffer");
                            } else {
                                /* read pointer=write pointer, no data available */
                                if (Input2OutputStreamForwarder.this.eof || Input2OutputStreamForwarder.this.readDone) {
                                    // System.out.println("writer normal end");
                                    break;
                                }
                                // System.out.println("writer wait");
                                try {
                                    Input2OutputStreamForwarder.this.LOCK.wait(100);
                                    continue;
                                } catch (final InterruptedException e) {
                                    break;
                                }
                            }
                        }
                        // System.out.println("Writer: " +
                        // SizeFormatter.formatBytes(Input2OutputStreamForwarder.this.readS));
                        Input2OutputStreamForwarder.this.out.write(Input2OutputStreamForwarder.this.buffer, Input2OutputStreamForwarder.this.readP, Input2OutputStreamForwarder.this.readS);
                        Input2OutputStreamForwarder.this.outC = Input2OutputStreamForwarder.this.outC + Input2OutputStreamForwarder.this.readS;
                        System.out.println(thread.getName()+" : "+outC+" bytes");
                        synchronized (Input2OutputStreamForwarder.this.LOCK) {
                            Input2OutputStreamForwarder.this.readP = Input2OutputStreamForwarder.this.readP + Input2OutputStreamForwarder.this.readS;
                            Input2OutputStreamForwarder.this.LOCK.notifyAll();
                        }
                    }
                } catch (final IOException e) {
                    Input2OutputStreamForwarder.this.outE = e;
                } finally {
                    synchronized (Input2OutputStreamForwarder.this.LOCK) {
                        Input2OutputStreamForwarder.this.LOCK.notifyAll();
                    }
                }
            }

        },this.in+" >> "+out);
    }

    public void forward() throws IOException, InterruptedException {
        this.forward(null);
    }

    public void forward(final Runnable runAfter) throws IOException, InterruptedException {
        try {
            this.thread.start();
            int read = 0;
            while (!this.thread.isInterrupted() && this.thread.isAlive()) {
                /*
                 * TODO: lets fill at the beginning, when writer is still in
                 * progress
                 */
                synchronized (this.LOCK) {
                    if (this.readP == this.buffer.length && this.readF == this.writeF) {
                        /* read pointer at the end, set write pointer to start */
                        this.writeP = 0;
                        this.writeF++;
                        // System.out.println("reader flip");
                    }
                    if (this.writeP < this.buffer.length) {
                        /* we still have buffer left to use */
                        this.writeS = this.buffer.length - this.writeP;
                        // System.out.println("read restbuffer");
                    } else {
                        /* no buffer left, wait for signal */
                        // System.out.println("read wait");
                        this.LOCK.notifyAll();
                        try {
                            if (!this.thread.isAlive() || this.thread.isInterrupted()) {
                                break;
                            }
                            this.LOCK.wait(100);
                            continue;
                        } catch (final InterruptedException e) {
                            break;
                        }
                    }
                }
                /* read into buffer */
                read = this.in.read(this.buffer, this.writeP, this.writeS);
                // System.out.println("Reader: " + this.writeP + " " +
                // this.writeS + " read " + read);
                if (read == -1) {
                    // System.out.println("reader normal end");
                    this.eof = true;
                    break;
                }
                this.inC = this.inC + read;
                synchronized (this.LOCK) {
                    /* set new write pointer to next position */
                    this.writeP = this.writeP + read;
                }
            }
            if (this.outE != null) { 
                
                throw this.outE; 
                
            }
        }catch(IOException e){
          
            throw e;
        } finally {
            try {
                this.readDone = true;
                synchronized (this.LOCK) {
                    this.LOCK.notifyAll();
                }
                /* wait for thread to finish */
                while (this.thread.isAlive()) {
                    synchronized (this.LOCK) {
                        this.LOCK.wait(100);
                    }
                }
            } finally {
                if (runAfter != null) {
                    runAfter.run();
                }
            }
        }
    }

    public long getInC() {
        return this.inC;
    }

    public long getOutC() {
        return this.outC;
    }
}
