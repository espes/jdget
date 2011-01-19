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
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.appwork.utils.Application;
import org.appwork.utils.IO;

/**
 * @author daniel
 * 
 */
public class SingleAppInstance {

    private String                  appID;
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
        this(appID, new File(Application.getRoot()));
    }

    public SingleAppInstance(final String appID, File directory) {
        this.appID = appID;
        this.lockFile = new File(directory, appID + ".lock");
        this.portFile = new File(directory, appID + ".port");
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(this)));
    }

    public synchronized void setInstanceMessageListener(final InstanceMessageListener listener) {
        this.listener = listener;
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

    public synchronized boolean sendToRunningInstance(String[] message) {
        if (portFile.exists()) {
            int port = readPortFromPortFile();
            Socket runninginstance = null;
            if (port != 0) {
                try {
                    runninginstance = new Socket(getLocalHost(), port);
                    runninginstance.setSoTimeout(10000);/* set Timeout */
                    BufferedInputStream in = new BufferedInputStream(runninginstance.getInputStream());
                    OutputStream out = runninginstance.getOutputStream();
                    String response = readLine(in);
                    if (response == null || !response.equalsIgnoreCase(SINGLEAPP)) {
                        /* invalid server response */
                        return false;
                    }
                    if (message == null || message.length == 0) {
                        writeLine(out, "0");
                    } else {
                        writeLine(out, message.length + "");
                        for (String msg : message) {
                            writeLine(out, msg);
                        }
                    }
                } catch (UnknownHostException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                } finally {
                    if (runninginstance != null) {
                        try {
                            runninginstance.shutdownInput();
                        } catch (Throwable e) {
                        }
                        try {
                            runninginstance.shutdownOutput();
                        } catch (Throwable e) {
                        }
                        try {
                            runninginstance.close();
                        } catch (Throwable e) {
                        }
                        runninginstance = null;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private synchronized void foundRunningInstance() throws AnotherInstanceRunningException {
        alreadyUsed = true;
        lockChannel = null;
        fileLock = null;
        throw new AnotherInstanceRunningException(appID);
    }

    private synchronized void cannotStart(String cause) throws UncheckableInstanceException {
        alreadyUsed = true;
        lockChannel = null;
        fileLock = null;
        throw new UncheckableInstanceException(cause);
    }

    public synchronized void start() throws AnotherInstanceRunningException, UncheckableInstanceException {
        if (fileLock != null) return;
        if (alreadyUsed) cannotStart("create new instance!");
        try {
            if (sendToRunningInstance(null)) foundRunningInstance();
            lockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
            try {
                fileLock = lockChannel.tryLock();
                if (fileLock == null) foundRunningInstance();
            } catch (OverlappingFileLockException e) {
                foundRunningInstance();
            } catch (IOException e) {
                foundRunningInstance();
            }
            portFile.delete();
            serverSocket = new ServerSocket();
            SocketAddress socketAddress = new InetSocketAddress(getLocalHost(), 0);
            serverSocket.bind(socketAddress);
            FileOutputStream portWriter = null;
            try {
                portWriter = new FileOutputStream(portFile);
                portWriter.write((serverSocket.getLocalPort() + "").getBytes());
                portWriter.flush();
                startDaemon();
                return;
            } catch (Throwable t) {
                /* network communication not possible */
            } finally {
                try {
                    portWriter.close();
                } catch (Throwable t) {
                }
            }
            cannotStart("could not create instance!");
        } catch (FileNotFoundException e) {
            cannotStart(e.getMessage());
        } catch (IOException e) {
            try {
                serverSocket.close();
            } catch (Throwable t) {
            }
            cannotStart(e.getMessage());
        }
    }

    public synchronized void exit() {
        if (fileLock == null) return;
        daemonRunning = false;
        if (daemon != null) daemon.interrupt();
        try {
            try {
                fileLock.release();
            } catch (IOException e) {
            }
            try {
                lockChannel.close();
            } catch (IOException e) {
            }
        } finally {
            lockChannel = null;
            fileLock = null;
            lockFile.delete();
            portFile.delete();
        }
    }

    private static class ShutdownHook implements Runnable {
        private SingleAppInstance instance = null;

        public ShutdownHook(final SingleAppInstance instance) {
            this.instance = instance;
        }

        public void run() {
            if (instance != null) instance.exit();
        }
    }

    private synchronized void startDaemon() {
        if (daemon != null) return;
        daemon = new Thread(new Runnable() {

            public void run() {
                daemonRunning = true;
                while (daemonRunning) {
                    if (daemon.isInterrupted()) break;
                    Socket client = null;
                    try {
                        /* accept new request */
                        client = serverSocket.accept();
                        client.setSoTimeout(10000);/* set Timeout */
                        BufferedInputStream in = new BufferedInputStream(client.getInputStream());
                        OutputStream out = client.getOutputStream();
                        writeLine(out, SINGLEAPP);
                        String line = readLine(in);
                        if (line != null && line.length() > 0) {
                            final int lines = Integer.parseInt(line);
                            if (lines != 0) {
                                final String[] message = new String[lines];
                                for (int index = 0; index < lines; index++) {
                                    message[index] = readLine(in);
                                }
                                if (listener != null) {
                                    try {
                                        listener.parseMessage(message);
                                    } catch (Throwable e) {
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        org.appwork.utils.logging.Log.exception(e);
                    } finally {
                        if (client != null) {
                            try {
                                client.shutdownInput();
                            } catch (Throwable e) {
                            }
                            try {
                                client.shutdownOutput();
                            } catch (Throwable e) {
                            }
                            try {
                                client.close();
                            } catch (Throwable e) {
                            }
                            client = null;
                        }
                    }
                }
                try {
                    serverSocket.close();
                } catch (Throwable e) {
                    org.appwork.utils.logging.Log.exception(e);
                }
            }
        });
        daemon.setName("SingleAppInstance: " + appID);
        /* set daemonmode so java does not wait for this thread */
        daemon.setDaemon(true);
        daemon.start();
    }

    private void writeLine(OutputStream outputStream, String line) {
        if (outputStream == null || line == null) return;
        try {
            outputStream.write(line.getBytes("UTF-8"));
            outputStream.write("\r\n".getBytes());
            outputStream.flush();
        } catch (Exception e) {
        }
    }

    private String readLine(BufferedInputStream in) {
        final ByteArrayOutputStream inbuffer = new ByteArrayOutputStream();
        if (in == null) return "";
        int c;
        try {
            in.mark(1);
            if (in.read() == -1)
                return null;
            else
                in.reset();
            while ((c = in.read()) >= 0) {
                if ((c == 0) || (c == 10) || (c == 13))
                    break;
                else
                    inbuffer.write(c);
            }
            if (c == 13) {
                in.mark(1);
                if (in.read() != 10) in.reset();
            }
        } catch (Exception e) {
        }
        try {
            return inbuffer.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private int readPortFromPortFile() {
        if (!portFile.exists()) return 0;
        try {
            String port = IO.readFileToString(portFile);
            return Integer.parseInt(String.valueOf(port).trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
