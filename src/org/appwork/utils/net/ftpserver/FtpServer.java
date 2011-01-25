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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author daniel
 * 
 */
public class FtpServer implements Runnable {

    private FtpConnectionHandler handler;
    private int               port;
    private ServerSocket      controlSocket;

    public FtpServer(FtpConnectionHandler handler, final int port) {
        this.handler = handler;
        this.port = port;
    }

    private InetAddress getLocalHost() {
        InetAddress localhost = null;
        try {
            localhost = Inet4Address.getByName("127.0.0.1");
        } catch (UnknownHostException e1) {
        }
        if (localhost != null) return localhost;
        try {
            localhost = Inet4Address.getByName(null);
        } catch (UnknownHostException e1) {
        }
        return localhost;
    }

    public void start() throws IOException {
        this.controlSocket = new ServerSocket(port);
        new Thread(this).start();
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
                Socket clientSocket = controlSocket.accept();
                new FtpConnection(this, clientSocket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * @return
     */
    public FtpConnectionHandler getFtpCommandHandler() {
        return handler;
    }

}
