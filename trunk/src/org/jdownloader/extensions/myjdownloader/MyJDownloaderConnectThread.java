package org.jdownloader.extensions.myjdownloader;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import jd.http.Browser;
import jd.nutils.encoding.Encoding;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.utils.Hash;
import org.appwork.utils.StringUtils;
import org.appwork.utils.logging2.LogSource;
import org.appwork.utils.net.httpserver.handler.HttpRequestHandler;
import org.appwork.utils.swing.dialog.Dialog;
import org.jdownloader.extensions.myjdownloader.api.MyJDownloaderAPI;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.json.DeviceData;

public class MyJDownloaderConnectThread extends Thread {

    private final AtomicLong              THREADCOUNTER = new AtomicLong(0);
    private MyJDownloaderExtension        myJDownloaderExtension;
    private int                           loginError    = 0;
    private int                           connectError  = 0;
    private MyDownloaderExtensionConfig   config;
    private Socket                        connectionSocket;
    private ArrayList<HttpRequestHandler> requestHandler;
    private final MyJDownloaderAPI        api;
    private LogSource                     logger;
    private boolean                       sessionValid;

    public MyJDownloaderConnectThread(MyJDownloaderExtension myJDownloaderExtension) {
        setName("MyJDownloaderConnectThread");
        this.setDaemon(true);
        this.myJDownloaderExtension = myJDownloaderExtension;
        config = myJDownloaderExtension.getSettings();
        api = new MyJDownloaderAPI(myJDownloaderExtension);
        logger = myJDownloaderExtension.getLogger();
    }

    @Override
    public void run() {
        mainLoop: while (myJDownloaderExtension.getConnectThread() == this) {
            try {
                if (loginError > 5 || connectError > 5) {
                    try {
                        Dialog.getInstance().showErrorDialog("MyJDownloader Error. loginErrors: " + loginError + " connectErrors:" + connectError + "\r\nMyJDownloader Extension is disabled now.");
                        myJDownloaderExtension.setEnabled(false);
                    } catch (final Throwable e) {
                        logger.log(e);
                    }
                    return;
                }
                try {
                    ensureValidSession();
                    loginError = 0;
                } catch (final Throwable e) {
                    sessionValid = false;
                    loginError++;
                    logger.log(e);
                    Thread.sleep(1000);
                    continue mainLoop;
                }
                boolean closeSocket = true;
                try {
                    connectionSocket = new Socket();
                    connectionSocket.setSoTimeout(120000);
                    connectionSocket.setTcpNoDelay(false);
                    InetSocketAddress ia = new InetSocketAddress(this.config.getAPIServerURL(), this.config.getAPIServerPort());
                    logger.info("Connect " + ia);
                    connectionSocket.connect(ia, 30000);
                    connectionSocket.getOutputStream().write(("DEVICE" + api.getSessionInfo().getSessionToken()).getBytes("ISO-8859-1"));
                    int validToken = connectionSocket.getInputStream().read();
                    if (validToken == 4) {
                        logger.info("KeepAlive");
                        closeSocket = true;
                    } else if (validToken == 0) {
                        loginError++;
                        logger.info("Token seems to be invalid!");
                        sessionValid = false;
                    } else if (validToken == 1) {
                        connectError = 0;
                        // logger.info("Connection got established");
                        closeSocket = false;

                        final Socket clientSocket = connectionSocket;
                        Thread connectionThread = new Thread("MyJDownloaderConnection:" + THREADCOUNTER.incrementAndGet()) {
                            @Override
                            public void run() {
                                try {
                                    MyJDownloaderHttpConnection httpConnection = new MyJDownloaderHttpConnection(clientSocket, api);
                                    httpConnection.run();
                                } catch (final Throwable e) {
                                    logger.log(e);
                                }
                            }
                        };
                        connectionThread.setDaemon(true);
                        connectionThread.start();
                    } else {
                        logger.info("Something else!?!?! WTF!" + validToken);
                    }
                } catch (ConnectException e) {
                    logger.info("Could not connect! Server down?");
                    connectError++;
                } catch (SocketTimeoutException e) {
                    logger.info("ReadTimeout!");
                } finally {
                    try {
                        if (closeSocket) connectionSocket.close();
                    } catch (final Throwable e) {
                    }
                }

            } catch (final Throwable e) {
                logger.log(e);
            }
        }
    }

    protected void ensureValidSession() throws MyJDownloaderException {
        if (sessionValid) return;
        /* fetch new jdToken if needed */
        if (api.getSessionInfo() != null) {
            try {
                api.reconnect();
            } catch (MyJDownloaderException e) {
                api.setSessionInfo(null);
                ensureValidSession();
            }
        } else {
            api.connect(config.getEmail(), config.getPassword());
            DeviceData device = api.bindDevice(new DeviceData(config.getUniqueDeviceID(), "jd", config.getDeviceName()));
            if (StringUtils.isNotEmpty(device.getId())) {
                config.setUniqueDeviceID(device.getId());
            }
        }
        // System.out.println(1);
        sessionValid = true;
    }

    public void interruptConnectThread() {
        try {
            connectionSocket.close();
        } catch (final Throwable e) {
        }
    }

    protected HashMap<String, Object> getJDToken() throws IOException {
        Browser br = new Browser();
        return JSonStorage.restoreFromString(br.getPage("http://" + config.getAPIServerURL() + ":" + config.getAPIServerPort() + "/myjdownloader/getJDToken?" + Encoding.urlEncode(config.getEmail()) + "&" + Hash.getSHA256(config.getPassword())), new TypeRef<HashMap<String, Object>>() {
        });
    }

    protected String parseJDTokenResponse(HashMap<String, Object> apiResponse, String field) {
        String ret = null;
        if (apiResponse != null) {
            Object data = apiResponse.get("data");
            if (data != null && data instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) data;
                return (String) dataMap.get(field);
            }
        }
        return ret;

    }

}
