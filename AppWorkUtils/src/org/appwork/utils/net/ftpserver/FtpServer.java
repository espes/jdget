/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.ftpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.ftpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * @author daniel
 * 
 */
public class FtpServer implements Runnable {

    private final FtpConnectionHandler<? extends FtpFile> handler;
    private final int                                     port;
    private ServerSocket                                  controlSocket;
    private Thread                                        controlThread = null;
    private ThreadGroup                                   threadGroup   = null;
    private boolean                                       localhostOnly = false;
    private boolean                                       debug         = false;

    public FtpServer(final FtpConnectionHandler<? extends FtpFile> handler, final int port) {
        this.handler = handler;
        this.port = port;
        this.threadGroup = new ThreadGroup("FTPServer");
    }

    /**
     * @return
     */
    public FtpConnectionHandler<? extends FtpFile> getFtpCommandHandler() {
        return this.handler;
    }

    protected InetAddress getLocalHost() {
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("127.0.0.1");
        } catch (final UnknownHostException e1) {
        }
        if (localhost != null) { return localhost; }
        try {
            localhost = InetAddress.getByName(null);
        } catch (final UnknownHostException e1) {
        }
        return localhost;
    }

    /**
     * @return the clientThreadGroup
     */
    protected ThreadGroup getThreadGroup() {
        return this.threadGroup;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return this.debug;
    }

    /**
     * @return the localhostOnly
     */
    public boolean isLocalhostOnly() {
        return this.localhostOnly;
    }

    public void run() {
        final Thread current = this.controlThread;
        final ServerSocket socket = this.controlSocket;
        try {
            while (true) {
                try {
                    final Socket clientSocket = socket.accept();
                    /* TODO: handle max client connections here */
                    new FtpConnection(this, clientSocket);
                } catch (final IOException e) {
                    break;
                }
                if (current == null || current.isInterrupted()) {
                    break;
                }
            }
        } finally {
            try {
                socket.close();
            } catch (final Throwable e) {
            }
        }
    }

    /**
     * @param debug
     *            the debug to set
     */
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */

    /**
     * @param localhostOnly
     *            the localhostOnly to set
     */
    public void setLocalhostOnly(final boolean localhostOnly) {
        this.localhostOnly = localhostOnly;
    }

    public synchronized void start() throws IOException {
        if (this.isLocalhostOnly()) {
            /* we only want localhost bound here */
            final SocketAddress socketAddress = new InetSocketAddress(this.getLocalHost(), this.port);
            this.controlSocket = new ServerSocket();
            this.controlSocket.bind(socketAddress);
        } else {
            this.controlSocket = new ServerSocket(this.port);
        }
        this.controlThread = new Thread(this.threadGroup, this);
        this.controlThread.setName("FtpServerThread");
        this.controlThread.start();
    }

    public synchronized void stop() {
        try {
            this.controlSocket.close();
        } catch (final Throwable e) {
        }
        this.threadGroup.interrupt();
    }
}
