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

import org.appwork.utils.formatter.SizeFormatter;

/**
 * @author daniel
 * 
 */
public class Input2OutputStreamForwarder {

    private InputStream  in;
    private OutputStream out;

    byte[]               buffer = new byte[1 * 1024 * 1024];
    long                 inC    = 0;
    long                 outC   = 0;

    public long getInC() {
        return inC;
    }

    public long getOutC() {
        return outC;
    }

    int                      readP  = 0;
    int                      writeF = 0;
    int                      readF  = 0;
    int                      writeP = 0;
    int                      readS  = 0;
    int                      writeS = 0;
    Object                   LOCK   = new Object();
    private Thread           thread = null;
    private IOException      outE   = null;
    private volatile boolean eof    = false;

    public Input2OutputStreamForwarder(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        this.thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (!thread.isInterrupted()) {
                        synchronized (LOCK) {
                            if (writeF > readF) {
                                readP = 0;
                                readF = writeF;
                                System.out.println("writer flip");
                            }
                            if (readP < writeP) {
                                /*
                                 * write pointer > read pointer, there must be
                                 * data to get written
                                 */
                                readS = writeP - readP;
                                System.out.println("writer normal");
                            } else if (writeP < readP) {
                                /* write pointer < read pointer */
                                readS = buffer.length - readP;
                                System.out.println("writer RestBuffer");
                            } else {
                                /* read pointer=write pointer, no data available */
                                if (eof) {
                                    System.out.println("writer normal end");
                                    break;
                                }
                                System.out.println("writer wait");
                                try {
                                    LOCK.wait(100);
                                    continue;
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }
                        }
                        System.out.println("Writer: " + SizeFormatter.formatBytes(readS));
                        Input2OutputStreamForwarder.this.out.write(buffer, readP, readS);
                        outC = outC + readS;
                        synchronized (LOCK) {
                            readP = readP + readS;
                            LOCK.notifyAll();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    outE = e;
                } finally {
                    synchronized (LOCK) {
                        LOCK.notifyAll();
                    }
                }
            }

        });
    }

    public void forward() throws IOException {
        try {
            thread.start();
            int read = 0;
            while (!thread.isInterrupted() && thread.isAlive()) {
                synchronized (LOCK) {
                    if (readP == buffer.length && readF == writeF) {
                        /* read pointer at the end, set write pointer to start */
                        writeP = 0;
                        writeF++;
                        System.out.println("reader flip");
                    }
                    if (writeP < buffer.length) {
                        /* we still have buffer left to use */
                        writeS = buffer.length - writeP;
                        System.out.println("read restbuffer");                        
                    } else {
                        /* no buffer left, wait for signal */
                        System.out.println("read wait");
                        LOCK.notifyAll();
                        try {
                            if (!thread.isAlive() || thread.isInterrupted()) break;
                            LOCK.wait();
                            continue;
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
                /* read into buffer */
                read = in.read(buffer, writeP, writeS);
                System.out.println("Reader: " + writeP + " " + writeS + " read " + read);
                if (read == -1) {
                    System.out.println("reader normal end");
                    eof = true;
                    break;
                }
                inC = inC + read;
                synchronized (LOCK) {
                    /* set new write pointer to next position */
                    writeP = writeP + read;
                }
            }
            if (outE != null) throw outE;
        } finally {
            if (!eof) thread.interrupt();
            synchronized (LOCK) {
                LOCK.notifyAll();
            }
        }
    }
}
