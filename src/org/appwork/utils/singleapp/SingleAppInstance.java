/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.singleapp
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.singleapp;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownRunableEvent;
import org.appwork.utils.Application;
import org.appwork.utils.IO;

/**
 * @author daniel
 * 
 */
public class SingleAppInstance {

    private static class ShutdownHook implements Runnable {
        private SingleAppInstance instance = null;

        public ShutdownHook(final SingleAppInstance instance) {
            this.instance = instance;
        }

        public void run() {
            if (instance != null) {
                instance.exit();
            }
        }
    }

    private final String            appID;
    private InstanceMessageListener listener      = null;
    private File                    lockFile      = null;
    private FileLock                fileLock      = null;
    private FileChannel             lockChannel   = null;
    private boolean                 daemonRunning = false;
    private boolean                 alreadyUsed   = false;
    private ServerSocket            serverSocket  = null;
    private final String            SINGLEAPP     = "SingleAppInstance";
    private Thread                  daemon        = null;

    private File                    portFile      = null;

    public SingleAppInstance(final String appID) {
        this(appID, new File(Application.getHome()));
    }

    public SingleAppInstance(final String appID, final File directory) {
        this.appID = appID;
        directory.mkdirs();
        lockFile = new File(directory, appID + ".lock");
        portFile = new File(directory, appID + ".port");
        ShutdownController.getInstance().addShutdownEvent(new ShutdownRunableEvent(new ShutdownHook(this)));

    }

    private synchronized void cannotStart(final String cause) throws UncheckableInstanceException {
        alreadyUsed = true;
        lockChannel = null;
        fileLock = null;
        throw new UncheckableInstanceException(cause);
    }

    public synchronized void exit() {
        if (fileLock == null) { return; }
        daemonRunning = false;
        if (daemon != null) {
            daemon.interrupt();
        }
        try {
            try {
                fileLock.release();
            } catch (final IOException e) {
            }
            try {
                lockChannel.close();
            } catch (final IOException e) {
            }
        } finally {
            lockChannel = null;
            fileLock = null;
            lockFile.delete();
            portFile.delete();
        }
    }

    private synchronized void foundRunningInstance() throws AnotherInstanceRunningException {
        alreadyUsed = true;
        lockChannel = null;
        fileLock = null;
        throw new AnotherInstanceRunningException(appID);
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

    private String readLine(final BufferedInputStream in) {
        final ByteArrayOutputStream inbuffer = new ByteArrayOutputStream();
        if (in == null) { return ""; }
        int c;
        try {
            in.mark(1);
            if (in.read() == -1) {
                return null;
            } else {
                in.reset();
            }
            while ((c = in.read()) >= 0) {
                if (c == 0 || c == 10 || c == 13) {
                    break;
                } else {
                    inbuffer.write(c);
                }
            }
            if (c == 13) {
                in.mark(1);
                if (in.read() != 10) {
                    in.reset();
                }
            }
        } catch (final Exception e) {
        }
        try {
            return inbuffer.toString("UTF-8");
        } catch (final UnsupportedEncodingException e) {
            return "";
        }
    }

    private int readPortFromPortFile() {
        if (!portFile.exists()) { return 0; }
        try {
            final String port = IO.readFileToString(portFile);
            return Integer.parseInt(String.valueOf(port).trim());
        } catch (final Exception e) {
            return 0;
        }
    }

    public synchronized boolean sendToRunningInstance(final String[] message) {
        if (portFile.exists()) {
            final int port = readPortFromPortFile();
            Socket runninginstance = null;
            if (port != 0) {
                try {
                    runninginstance = new Socket();
                    InetSocketAddress con = new InetSocketAddress(getLocalHost(), port);
                    runninginstance.connect(con, 1000);
                    runninginstance.setSoTimeout(2000);/* set Timeout */
                    final BufferedInputStream in = new BufferedInputStream(runninginstance.getInputStream());
                    final OutputStream out = runninginstance.getOutputStream();
                    final String response = readLine(in);
                    if (response == null || !response.equalsIgnoreCase(SINGLEAPP)) {
                        /* invalid server response */
                        return false;
                    }
                    if (message == null || message.length == 0) {
                        writeLine(out, "0");
                    } else {
                        writeLine(out, message.length + "");
                        for (final String msg : message) {
                            writeLine(out, msg);
                        }
                    }                
                } catch (final IOException e) {                    
                    return false;
                } finally {
                    if (runninginstance != null) {
                        try {
                            runninginstance.shutdownInput();
                        } catch (final Throwable e) {
                        }
                        try {
                            runninginstance.shutdownOutput();
                        } catch (final Throwable e) {
                        }
                        try {
                            runninginstance.close();
                        } catch (final Throwable e) {
                        }
                        runninginstance = null;
                    }
                }
                return true;
            }
        }

        return false;
    }

    public synchronized void setInstanceMessageListener(final InstanceMessageListener listener) {
        this.listener = listener;
    }

    public synchronized void start() throws AnotherInstanceRunningException, UncheckableInstanceException {

        if (fileLock != null) { return; }
        if (alreadyUsed) {
            cannotStart("create new instance!");
        }
        try {
            if (sendToRunningInstance(null)) {
                foundRunningInstance();
            }
            lockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
            try {
                fileLock = lockChannel.tryLock();
                if (fileLock == null) {
                    foundRunningInstance();
                }
            } catch (final OverlappingFileLockException e) {
                foundRunningInstance();
            } catch (final IOException e) {
                foundRunningInstance();
            }
            portFile.delete();
            serverSocket = new ServerSocket();
            final SocketAddress socketAddress = new InetSocketAddress(getLocalHost(), 0);
            serverSocket.bind(socketAddress);
            FileOutputStream portWriter = null;
            try {
                portWriter = new FileOutputStream(portFile);
                portWriter.write((serverSocket.getLocalPort() + "").getBytes());
                portWriter.flush();
                startDaemon();
                return;
            } catch (final Throwable t) {
                /* network communication not possible */
            } finally {
                try {
                    portWriter.close();
                } catch (final Throwable t) {
                }
            }
            cannotStart("could not create instance!");
        } catch (final FileNotFoundException e) {
            cannotStart(e.getMessage());
        } catch (final IOException e) {
            try {
                serverSocket.close();
            } catch (final Throwable t) {
            }
            cannotStart(e.getMessage());
        }
    }

    private synchronized void startDaemon() {
        if (daemon != null) { return; }
        daemon = new Thread(new Runnable() {

            public void run() {
                daemonRunning = true;
                while (daemonRunning) {
                    if (daemon.isInterrupted()) {
                        break;
                    }
                    Socket client = null;
                    try {
                        /* accept new request */
                        client = serverSocket.accept();
                        client.setSoTimeout(10000);/* set Timeout */
                        final BufferedInputStream in = new BufferedInputStream(client.getInputStream());
                        final OutputStream out = client.getOutputStream();
                        SingleAppInstance.this.writeLine(out, SINGLEAPP);
                        final String line = SingleAppInstance.this.readLine(in);
                        if (line != null && line.length() > 0) {
                            final int lines = Integer.parseInt(line);
                            if (lines != 0) {
                                final String[] message = new String[lines];
                                for (int index = 0; index < lines; index++) {
                                    message[index] = SingleAppInstance.this.readLine(in);
                                }
                                if (listener != null) {
                                    try {
                                        listener.parseMessage(message);
                                    } catch (final Throwable e) {
                                    }
                                }
                            }
                        }
                    } catch (final IOException e) {
                        org.appwork.utils.logging.Log.exception(e);
                    } finally {
                        if (client != null) {
                            try {
                                client.shutdownInput();
                            } catch (final Throwable e) {
                            }
                            try {
                                client.shutdownOutput();
                            } catch (final Throwable e) {
                            }
                            try {
                                client.close();
                            } catch (final Throwable e) {
                            }
                            client = null;
                        }
                    }
                }
                try {
                    serverSocket.close();
                } catch (final Throwable e) {
                    org.appwork.utils.logging.Log.exception(e);
                }
            }
        });
        daemon.setName("SingleAppInstance: " + appID);
        /* set daemonmode so java does not wait for this thread */
        daemon.setDaemon(true);
        daemon.start();
    }

    private void writeLine(final OutputStream outputStream, final String line) {
        if (outputStream == null || line == null) { return; }
        try {
            outputStream.write(line.getBytes("UTF-8"));
            outputStream.write("\r\n".getBytes());
            outputStream.flush();
        } catch (final Exception e) {
        }
    }
}
