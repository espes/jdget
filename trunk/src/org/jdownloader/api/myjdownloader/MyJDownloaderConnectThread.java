package org.jdownloader.api.myjdownloader;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.appwork.exceptions.WTFException;
import org.appwork.scheduler.DelayedRunnable;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.utils.Application;
import org.appwork.utils.Exceptions;
import org.appwork.utils.Hash;
import org.appwork.utils.NullsafeAtomicReference;
import org.appwork.utils.StringUtils;
import org.appwork.utils.awfc.AWFCUtils;
import org.appwork.utils.formatter.HexFormatter;
import org.appwork.utils.logging2.LogSource;
import org.jdownloader.api.myjdownloader.MyJDownloaderSettings.MyJDownloaderError;
import org.jdownloader.api.myjdownloader.MyJDownloaderWaitingConnectionThread.MyJDownloaderConnectionRequest;
import org.jdownloader.api.myjdownloader.MyJDownloaderWaitingConnectionThread.MyJDownloaderConnectionResponse;
import org.jdownloader.api.myjdownloader.api.MyJDownloaderAPI;
import org.jdownloader.myjdownloader.client.SessionInfo;
import org.jdownloader.myjdownloader.client.exceptions.AuthException;
import org.jdownloader.myjdownloader.client.exceptions.EmailInvalidException;
import org.jdownloader.myjdownloader.client.exceptions.EmailNotValidatedException;
import org.jdownloader.myjdownloader.client.exceptions.MaintenanceException;
import org.jdownloader.myjdownloader.client.exceptions.MyJDownloaderException;
import org.jdownloader.myjdownloader.client.exceptions.OutdatedException;
import org.jdownloader.myjdownloader.client.exceptions.OverloadException;
import org.jdownloader.myjdownloader.client.exceptions.UnconnectedException;
import org.jdownloader.myjdownloader.client.json.DeviceConnectionStatus;
import org.jdownloader.myjdownloader.client.json.DeviceData;
import org.jdownloader.myjdownloader.client.json.NotificationRequestMessage;
import org.jdownloader.myjdownloader.client.json.NotificationRequestMessage.TYPE;
import org.jdownloader.settings.staticreferences.CFG_MYJD;

public class MyJDownloaderConnectThread extends Thread {

    protected class DeviceConnectionHelper {
        private AtomicLong              backoffCounter = new AtomicLong(0);

        private AtomicBoolean           backOff        = new AtomicBoolean(false);
        private final InetSocketAddress addr;

        public InetSocketAddress getAddr() {
            return addr;
        }

        private DeviceConnectionHelper(int port, String url) {
            addr = new InetSocketAddress(url, port);
        }

        public void requestbackoff() {
            backOff.set(true);
        }

        public void backoff() throws InterruptedException {
            if (api == null) return;
            if (backOff.get()) {
                synchronized (backOff) {
                    if (backOff.get()) {
                        long currentBackOff = backoffCounter.get();
                        try {
                            long timeout = 300 * 1000l;
                            if (currentBackOff <= 5) {
                                timeout = ((long) Math.pow(3.0d, currentBackOff)) * 1000;
                            }
                            timeout = Math.min(300 * 1000l, timeout);
                            timeout = timeout + new Random().nextInt(5000);
                            logger.info("Backoff:" + currentBackOff + "->" + timeout);
                            Thread.sleep(timeout);
                        } finally {
                            backoffCounter.compareAndSet(currentBackOff, currentBackOff + 1);
                            backOff.compareAndSet(true, false);
                        }
                    }
                }
            }
        }

        public boolean backoffrequested() {
            return backOff.get();
        }

        public void reset() {
            synchronized (backOff) {
                backoffCounter.set(0);
                backOff.set(false);
            }
        }
    }

    private final AtomicLong        THREADCOUNTER = new AtomicLong(0);
    private MyJDownloaderController myJDownloaderExtension;

    private MyJDownloaderAPI        api;
    private final LogSource         logger;

    public LogSource getLogger() {
        return logger;
    }

    private AtomicBoolean                                          sessionValid              = new AtomicBoolean(false);
    private AtomicLong                                             syncMark                  = new AtomicLong(-1);
    private ScheduledExecutorService                               THREADQUEUE               = DelayedRunnable.getNewScheduledExecutorService();
    private final DeviceConnectionHelper[]                         deviceConnectionHelper;
    private int                                                    helperIndex               = 0;
    private NullsafeAtomicReference<MyJDownloaderConnectionStatus> connected                 = new NullsafeAtomicReference<MyJDownloaderConnectionStatus>(MyJDownloaderConnectionStatus.UNCONNECTED);
    private String                                                 password;
    private String                                                 email;
    private String                                                 deviceName;
    private HashSet<TYPE>                                          notifyInterests;
    private final static HashMap<Thread, Socket>                   openConnections           = new HashMap<Thread, Socket>();
    private final ArrayDeque<MyJDownloaderConnectionResponse>      responses                 = new ArrayDeque<MyJDownloaderWaitingConnectionThread.MyJDownloaderConnectionResponse>();
    private final ArrayList<MyJDownloaderWaitingConnectionThread>  waitingConnections        = new ArrayList<MyJDownloaderWaitingConnectionThread>();
    private final int                                              minimumWaitingConnections = 1;
    private final int                                              maximumWaitingConnections = 4;
    private final File                                             sessionInfoCache;
    private final static Object                                    SESSIONLOCK               = new Object();

    public MyJDownloaderConnectThread(MyJDownloaderController myJDownloaderExtension) {
        setName("MyJDownloaderConnectThread");
        this.setDaemon(true);
        this.myJDownloaderExtension = myJDownloaderExtension;
        api = new MyJDownloaderAPI(myJDownloaderExtension);
        logger = myJDownloaderExtension.getLogger();
        ArrayList<DeviceConnectionHelper> helper = new ArrayList<DeviceConnectionHelper>();
        for (int port : CFG_MYJD.CFG.getDeviceConnectPorts()) {
            helper.add(new DeviceConnectionHelper(port, CFG_MYJD.CFG.getConnectIP()));
        }
        deviceConnectionHelper = helper.toArray(new DeviceConnectionHelper[helper.size()]);
        notifyInterests = new HashSet<NotificationRequestMessage.TYPE>();
        sessionInfoCache = Application.getResource("tmp/myjd.session");
        loadSessionInfo();
    }

    public boolean putResponse(MyJDownloaderConnectionResponse response) {
        synchronized (waitingConnections) {
            if (waitingConnections.size() == 0) return false;
        }
        synchronized (responses) {
            responses.add(response);
            responses.notify();
        }
        return true;
    }

    private DeviceConnectionHelper getNextDeviceConnectionHelper() {
        DeviceConnectionHelper ret = deviceConnectionHelper[helperIndex];
        helperIndex = (helperIndex + 1) % deviceConnectionHelper.length;
        return ret;
    }

    public boolean isConnected() {
        return connected.get() == MyJDownloaderConnectionStatus.CONNECTED;
    }

    private void invalidateSession() {
        sessionValid.set(false);
        try {
            MyJDownloaderAPI lapi = api;
            if (lapi != null) lapi.disconnect();
        } catch (final Throwable e) {
        }
    }

    private DeviceConnectionStatus handleResponse(MyJDownloaderConnectionResponse response) {
        boolean closeSocket = true;
        DeviceConnectionHelper currentHelper = null;
        try {
            currentHelper = response.getConnectionHelper();
            if (response.getThrowable() != null) throw response.getThrowable();
            DeviceConnectionStatus connectionStatus = response.getConnectionStatus();
            Socket socket = response.getConnectionSocket();
            if (connectionStatus != null) {
                setConnected(MyJDownloaderConnectionStatus.CONNECTED);
                long syncMark = 0;
                currentHelper.reset();
                switch (connectionStatus) {
                case OUTDATED:
                    logger.info("Outdated session");
                    invalidateSession();
                    return connectionStatus;
                case UNBOUND:
                    logger.info("Unbound");
                    invalidateSession();
                    return connectionStatus;
                case KEEPALIVE:
                    try {
                        syncMark = new AWFCUtils(socket.getInputStream()).readLongOptimized();
                        sync(syncMark);
                    } catch (final IOException e) {
                    }
                    logger.info("KeepAlive " + syncMark);
                    return connectionStatus;
                case TOKEN:
                    logger.info("Invalid sessionToken");
                    invalidateSession();
                    return connectionStatus;
                case OK:
                    logger.info("valid connection(old Ok)");
                    response.getThread().putRequest(new MyJDownloaderConnectionRequest(api.getSessionInfo().getSessionToken(), currentHelper));
                    handleConnection(socket);
                    closeSocket = false;
                    return connectionStatus;
                case OK_SYNC:
                    syncMark = new AWFCUtils(socket.getInputStream()).readLongOptimized();
                    logger.info("valid connection (Ok: " + syncMark + ")");
                    response.getThread().putRequest(new MyJDownloaderConnectionRequest(api.getSessionInfo().getSessionToken(), currentHelper));
                    handleConnection(socket);
                    closeSocket = false;
                    sync(syncMark);
                    return connectionStatus;
                case MAINTENANCE:
                case OVERLOAD:
                    logger.info(connectionStatus.name());
                    currentHelper.requestbackoff();
                    return connectionStatus;
                }
            }
            logger.info("Something else!?!?! WTF!");
            currentHelper.requestbackoff();
            return null;
        } catch (ConnectException e) {
            currentHelper.requestbackoff();
            logger.info("Could not connect! Server down?");
            setConnected(MyJDownloaderConnectionStatus.PENDING);
            myJDownloaderExtension.onError(MyJDownloaderError.SERVER_DOWN);
            return null;
        } catch (SocketTimeoutException e) {
            currentHelper.requestbackoff();
            logger.info("ReadTimeout on server connect!");
            setConnected(MyJDownloaderConnectionStatus.PENDING);
            myJDownloaderExtension.onError(MyJDownloaderError.IO);
            return null;
        } catch (Throwable e) {
            currentHelper.requestbackoff();
            logger.log(e);
            myJDownloaderExtension.onError(MyJDownloaderError.UNKNOWN);
            return null;
        } finally {
            if (closeSocket) {
                try {
                    response.getConnectionSocket().close();
                } catch (final Throwable ignore) {
                }
            }
        }
    }

    private MyJDownloaderConnectionResponse pollResponse(boolean wait) throws InterruptedException {
        MyJDownloaderConnectionResponse response = null;
        synchronized (responses) {
            if ((response = responses.poll()) == null && wait) {
                responses.wait();
                response = responses.poll();
            }
        }
        return response;
    }

    @Override
    public void run() {
        DeviceConnectionHelper currentHelper = null;
        int unknownErrorSafeOff = 10;
        try {
            while (myJDownloaderExtension.getConnectThread() == this && api != null) {
                try {
                    try {
                        if (currentHelper == null || currentHelper.backoffrequested()) {
                            currentHelper = getNextDeviceConnectionHelper();
                        }
                        ensureValidSession(currentHelper);
                        if (connected.get() == MyJDownloaderConnectionStatus.UNCONNECTED) {
                            setConnected(MyJDownloaderConnectionStatus.PENDING);
                        }
                        MyJDownloaderConnectionRequest request = null;
                        boolean waitForResponse = false;
                        /* make sure we have at least one alive thread */
                        startWaitingConnections(false);
                        synchronized (waitingConnections) {
                            if (waitingConnections.size() == 0) {
                                logger.info("No WaitingConnection? Maybe disconnected!?");
                                return;
                            }
                            for (MyJDownloaderWaitingConnectionThread waitingThread : waitingConnections) {
                                if (request == null) request = new MyJDownloaderConnectionRequest(api.getSessionInfo().getSessionToken(), currentHelper);
                                if (waitingThread.putRequest(request)) {
                                    waitForResponse = true;
                                    request = null;
                                    continue;
                                } else if (waitingThread.isRunning()) {
                                    waitForResponse = true;
                                }
                            }
                        }
                        MyJDownloaderConnectionResponse response = pollResponse(waitForResponse);
                        while (response != null) {
                            DeviceConnectionStatus status = handleResponse(response);
                            if (status == null || (!DeviceConnectionStatus.OK.equals(status) && !DeviceConnectionStatus.OK_SYNC.equals(status))) {
                                synchronized (waitingConnections) {
                                    if (waitingConnections.size() == 0) {
                                        logger.info("No WaitingConnection? Maybe disconnected!?");
                                        return;
                                    }
                                    if (waitingConnections.size() > minimumWaitingConnections) {
                                        response.getThread().interrupt();
                                        waitingConnections.remove(response.getThread());
                                    }
                                }
                            } else {
                                startWaitingConnections(true);
                            }
                            response = pollResponse(false);
                        }
                    } catch (final MyJDownloaderException e) {
                        setConnected(MyJDownloaderConnectionStatus.PENDING);
                        if (e instanceof MaintenanceException) {
                            logger.info("Maintenance!");
                            currentHelper.requestbackoff();
                        } else if (e instanceof OverloadException) {
                            logger.info("Overload!");
                            currentHelper.requestbackoff();
                        } else if (e instanceof OutdatedException) {
                            logger.info("Outdated Version, Please update!");
                            myJDownloaderExtension.onError(MyJDownloaderError.OUTDATED);
                            return;
                        } else if (e instanceof EmailInvalidException) {
                            logger.info("Invalid email!");
                            myJDownloaderExtension.onError(MyJDownloaderError.EMAIL_INVALID);
                            return;
                        } else if (e instanceof EmailNotValidatedException) {
                            logger.info("Account is not confirmed!");
                            myJDownloaderExtension.onError(MyJDownloaderError.ACCOUNT_UNCONFIRMED);
                            return;
                        } else if (e instanceof AuthException) {
                            logger.info("Wrong Username/Password!");
                            myJDownloaderExtension.onError(MyJDownloaderError.BAD_LOGINS);
                            return;
                        } else if (Exceptions.containsInstanceOf(e, ConnectException.class, SocketTimeoutException.class)) {
                            logger.info("Could not connect! Server down?");
                            myJDownloaderExtension.onError(MyJDownloaderError.SERVER_DOWN);
                            currentHelper.requestbackoff();
                        } else {
                            logger.log(e);
                            currentHelper.requestbackoff();
                            if (unknownErrorSafeOff-- == 0) {
                                myJDownloaderExtension.onError(MyJDownloaderError.OUTDATED);
                                logger.severe("Unknown Error, SafetyOff!");
                                return;
                            }
                            myJDownloaderExtension.onError(MyJDownloaderError.UNKNOWN);
                        }
                    } catch (final Throwable e) {
                        logger.log(e);
                        setConnected(MyJDownloaderConnectionStatus.UNCONNECTED);
                        if (myJDownloaderExtension.getConnectThread() != this || api == null) {
                            // external disconnect
                            return;
                        }
                        myJDownloaderExtension.onError(MyJDownloaderError.UNKNOWN);
                        currentHelper.requestbackoff();
                        if (unknownErrorSafeOff-- == 0) {
                            myJDownloaderExtension.onError(MyJDownloaderError.OUTDATED);
                            logger.severe("Unknown Error, SafetyOff!");
                            return;
                        }
                    }
                } catch (final Throwable e) {
                    logger.log(e);
                }
            }
        } finally {
            disconnect();
        }
    }

    private void setConnected(MyJDownloaderConnectionStatus set) {
        if (connected.getAndSet(set) == set) return;
        myJDownloaderExtension.fireConnectionStatusChanged(set, getEstablishedConnections());
    }

    private void setEstablishedConnections(int connections) {
        myJDownloaderExtension.fireConnectionStatusChanged(connected.get(), connections);
    }

    private void sync(final long nextSyncMark) {
        if (this.syncMark.getAndSet(nextSyncMark) == nextSyncMark) return;
        ScheduledExecutorService lTHREADQUEUE = THREADQUEUE;
        if (lTHREADQUEUE != null) {
            lTHREADQUEUE.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        MyJDownloaderAPI lapi = api;
                        if (lapi == null) return;
                        if (MyJDownloaderConnectThread.this.syncMark.get() != nextSyncMark) return;
                        TYPE[] types = lapi.listrequesteddevicesnotifications();
                        HashSet<TYPE> notifyTypes = new HashSet<TYPE>();
                        if (types != null) {
                            for (TYPE type : types) {
                                notifyTypes.add(type);
                            }
                        }
                        setNotifyTypes(notifyTypes);
                    } catch (final Throwable e) {
                        MyJDownloaderConnectThread.this.syncMark.set(0);
                        logger.log(e);
                    }
                }
            });
        }
    }

    protected void setNotifyTypes(HashSet<TYPE> notifyTypes) {
        notifyInterests = notifyTypes;
    }

    private AtomicLong captchaSendMark = new AtomicLong(0);

    protected void pushCaptchaNotification(final boolean requested) {
        synchronized (notifyInterests) {
            if (!notifyInterests.contains(TYPE.CAPTCHA)) return;
        }
        final long currentMark = captchaSendMark.incrementAndGet();
        ScheduledExecutorService lTHREADQUEUE = THREADQUEUE;
        if (lTHREADQUEUE != null) {
            lTHREADQUEUE.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        MyJDownloaderAPI lapi = api;
                        if (lapi == null) return;
                        synchronized (notifyInterests) {
                            if (!notifyInterests.contains(TYPE.CAPTCHA)) return;
                        }
                        if (MyJDownloaderConnectThread.this.captchaSendMark.get() != currentMark) return;
                        NotificationRequestMessage message = new NotificationRequestMessage();
                        message.setType(TYPE.CAPTCHA);
                        message.setRequested(requested);
                        if (!lapi.pushNotification(message)) {
                            /* no devices are interested in captchas */
                            removeInterest(TYPE.CAPTCHA);

                        }
                    } catch (final Throwable e) {
                        logger.log(e);
                    }
                }
            });
        }
    }

    protected void removeInterest(TYPE captcha) {
        synchronized (notifyInterests) {
            notifyInterests.remove(captcha);
        }
    }

    private void handleConnection(final Socket clientSocket) {
        Thread connectionThread = new Thread("MyJDownloaderConnection:" + THREADCOUNTER.incrementAndGet()) {
            @Override
            public void run() {
                try {
                    MyJDownloaderHttpConnection httpConnection = new MyJDownloaderHttpConnection(clientSocket, api);
                    httpConnection.run();
                } catch (final Throwable e) {
                    logger.log(e);
                } finally {
                    synchronized (openConnections) {
                        openConnections.remove(Thread.currentThread());
                    }
                    setEstablishedConnections(getEstablishedConnections());
                }
            }
        };
        synchronized (openConnections) {
            openConnections.put(connectionThread, clientSocket);
        }
        setEstablishedConnections(getEstablishedConnections());
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void terminateWaitingConnections() {
        ArrayList<MyJDownloaderWaitingConnectionThread> copy = null;
        synchronized (waitingConnections) {
            copy = new ArrayList<MyJDownloaderWaitingConnectionThread>(waitingConnections);
            waitingConnections.clear();
        }
        for (MyJDownloaderWaitingConnectionThread thread : copy) {
            thread.interrupt();
        }
        synchronized (responses) {
            MyJDownloaderConnectionResponse next = null;
            while ((next = responses.poll()) != null) {
                try {
                    next.getConnectionSocket().close();
                } catch (final Throwable e) {
                }
            }
            responses.notifyAll();
        }
    }

    public void disconnect() {
        MyJDownloaderAPI lapi = api;
        api = null;
        terminateWaitingConnections();
        try {
            interrupt();
        } catch (final Throwable e) {
        }
        try {
            lapi.disconnect();
        } catch (final Throwable e) {
        }
        synchronized (openConnections) {
            Iterator<Entry<Thread, Socket>> it = openConnections.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Thread, Socket> next = it.next();
                try {
                    next.getValue().close();
                } catch (final Throwable e) {
                }
                try {
                    next.getKey().interrupt();
                } catch (final Throwable e) {
                }
            }
        }
        setConnected(MyJDownloaderConnectionStatus.UNCONNECTED);
        ScheduledExecutorService lTHREADQUEUE = THREADQUEUE;
        THREADQUEUE = null;
        if (lTHREADQUEUE != null) lTHREADQUEUE.shutdownNow();
        notifyInterests = new HashSet<NotificationRequestMessage.TYPE>();
    }

    private void startWaitingConnections(boolean minimumORmaximum) {
        int max = minimumWaitingConnections;
        if (minimumORmaximum) max = maximumWaitingConnections;
        synchronized (waitingConnections) {
            for (int index = waitingConnections.size() - 1; index >= 0; index--) {
                MyJDownloaderWaitingConnectionThread thread = waitingConnections.get(index);
                if (!thread.isRunning()) waitingConnections.remove(index);
            }
            for (int index = waitingConnections.size(); index < max; index++) {
                MyJDownloaderWaitingConnectionThread thread = new MyJDownloaderWaitingConnectionThread(this);
                waitingConnections.add(thread);
                thread.start();
            }
        }
    }

    private void validateSession() {
        saveSessionInfo();
        startWaitingConnections(false);
        sessionValid.set(true);
    }

    private void saveSessionInfo() {
        synchronized (SESSIONLOCK) {
            try {
                MyJDownloaderAPI lapi = api;
                if (lapi == null) return;
                SessionInfo session = lapi.getSessionInfo();
                if (session == null) return;
                JSonStorage.saveTo(sessionInfoCache, false, HexFormatter.hexToByteArray(Hash.getMD5(CFG_MYJD.PASSWORD.getValue())), JSonStorage.serializeToJson(new SessionInfoStorable(session)));
            } catch (final Throwable e) {
                logger.log(e);
            }
        }
    }

    private void loadSessionInfo() {
        synchronized (SESSIONLOCK) {
            try {
                if (!sessionInfoCache.exists()) return;
                MyJDownloaderAPI lapi = api;
                if (lapi == null) return;
                SessionInfoStorable sessionInfoStorable = JSonStorage.restoreFrom(sessionInfoCache, false, HexFormatter.hexToByteArray(Hash.getMD5(CFG_MYJD.PASSWORD.getValue())), new TypeRef<SessionInfoStorable>() {
                }, null);
                if (sessionInfoStorable == null) return;
                SessionInfo sessionInfo = sessionInfoStorable._getSessionInfo();
                if (sessionInfo == null) return;
                lapi.setSessionInfo(sessionInfo);
            } catch (final Throwable e) {
                logger.log(e);
            }
        }
    }

    protected void ensureValidSession(DeviceConnectionHelper connectionHelper) throws MyJDownloaderException, InterruptedException {
        MyJDownloaderAPI lapi = api;
        if (lapi == null) throw new WTFException("api is null, disconnected?!");
        try {
            if (sessionValid.get() && lapi.getSessionInfo() != null) return;
        } catch (UnconnectedException e) {
            logger.log(e);
            invalidateSession();
        }
        /* fetch new jdToken if needed */
        connectionHelper.backoff();
        try {
            lapi.reconnect();
            /* we need an additional call that will activate the new session */
            lapi.keepalive();
            validateSession();
            return;
        } catch (UnconnectedException e) {
            /* let's connect first */
        } catch (MyJDownloaderException e) {
            invalidateSession();
            ensureValidSession(connectionHelper);
        }
        boolean deviceBound = false;
        try {
            lapi.connect(getEmail(), getPassword());
            DeviceData device = lapi.bindDevice(new DeviceData(CFG_MYJD.CFG.getUniqueDeviceID(), "jd", getDeviceName()));
            if (StringUtils.isNotEmpty(device.getId())) {
                if (!device.getId().equals(CFG_MYJD.CFG.getUniqueDeviceID())) {
                    CFG_MYJD.CFG.setUniqueDeviceID(device.getId());
                    CFG_MYJD.CFG._getStorageHandler().write();
                }
                validateSession();
                deviceBound = true;
            }
        } finally {
            if (deviceBound == false) {
                invalidateSession();
            }
        }

    }

    protected String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        if (StringUtils.isEmpty(deviceName)) deviceName = "JDownloader";
        this.deviceName = deviceName;
    }

    protected String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    protected String getEmail() {
        return email;
    }

    public MyJDownloaderConnectionStatus getConnectionStatus() {
        return connected.get();
    }

    public int getEstablishedConnections() {
        synchronized (openConnections) {
            return openConnections.size();
        }
    }

}
