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

    private final FtpConnectionHandler handler;
    private final int                  port;
    private ServerSocket               controlSocket;

    public FtpServer(final FtpConnectionHandler handler, final int port) {
        this.handler = handler;
        this.port = port;
    }

    /**
     * @return
     */
    public FtpConnectionHandler getFtpCommandHandler() {
        return handler;
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (true) {
            try {
                final Socket clientSocket = controlSocket.accept();
                new FtpConnection(this, clientSocket);
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void start() throws IOException {
        controlSocket = new ServerSocket(port);

        new Thread(this).start();
    }

}
