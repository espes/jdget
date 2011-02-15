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
import java.net.ServerSocket;
import java.net.Socket;
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

    private InetAddress getLocalHost() {
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */

    public void run() {
        final Thread current = this.controlThread;
        final ServerSocket socket = this.controlSocket;
        try {
            while (true) {
                try {
                    final Socket clientSocket = socket.accept();
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

    public synchronized void start() throws IOException {
        this.controlSocket = new ServerSocket(this.port);
        this.controlThread = new Thread(this.threadGroup, this);
        this.controlThread.setName("FtpServerThread");
        this.controlThread.start();
    }

    public synchronized void stop() {
        this.threadGroup.interrupt();
        try {
            this.controlSocket.close();
        } catch (final Throwable e) {
        }
    }
}
