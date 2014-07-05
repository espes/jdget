package org.appwork.utils.net.httpconnection;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLSocket;

import org.appwork.net.protocol.http.HTTPConstants;
import org.appwork.scheduler.DelayedRunnable;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.net.Base64InputStream;
import org.appwork.utils.net.ChunkedInputStream;
import org.appwork.utils.net.CountingOutputStream;
import org.appwork.utils.net.LimitedInputStream;
import org.appwork.utils.net.StreamValidEOF;

public class HTTPConnectionImpl implements HTTPConnection {

    public static enum KEEPALIVE {
        /**
         * KEEP-ALIVE is disabled
         */
        DISABLED,
        /**
         * KEEP-ALIVE is enabled for GET/HEAD/OPTIONS/DELETE
         */
        ENABLED_INTERNAL,
        /**
         * KEEP-ALIVE is enabled, caller must handle
         * HTTPKeepAliveSocketException
         */
        EXTERNAL_EXCEPTION
    }

    /**
     * 
     */
    public static final String      UNKNOWN_HTTP_RESPONSE = "unknown HTTP response";

    protected HTTPHeaderMap<String> requestProperties     = null;

    protected volatile long[]       ranges;

    protected String                customcharset         = null;
    protected volatile Socket       connectionSocket      = null;

    protected Socket getConnectionSocket() {
        return this.connectionSocket;
    }

    protected final URL                   httpURL;
    protected final HTTPProxy             proxy;

    protected String                      httpPath;
    protected RequestMethod               httpMethod          = RequestMethod.GET;
    protected HTTPHeaderMap<List<String>> headers             = null;
    protected int                         httpResponseCode    = -1;
    protected String                      httpResponseMessage = "";
    protected volatile int                readTimeout         = 30000;

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    protected volatile int                       connectTimeout       = 30000;
    protected volatile long                      requestTime          = -1;
    protected OutputStream                       outputStream         = null;
    protected InputStream                        inputStream          = null;
    protected InputStream                        convertedInputStream = null;
    protected boolean                            inputStreamConnected = false;

    protected String                             httpHeader           = null;
    protected String                             invalidHttpHeader    = null;
    private boolean                              contentDecoded       = true;
    protected long                               postTodoLength       = -1;
    private int[]                                allowedResponseCodes = new int[0];
    protected final CopyOnWriteArrayList<String> connectExceptions    = new CopyOnWriteArrayList<String>();
    protected volatile KEEPALIVE                 keepAlive            = KEEPALIVE.DISABLED;

    public KEEPALIVE getKeepAlive() {
        return this.keepAlive;
    }

    public void setKeepAlive(KEEPALIVE keepAlive) {
        if (keepAlive == null) {
            keepAlive = KEEPALIVE.DISABLED;
        }
        this.keepAlive = keepAlive;
    }

    /**
     * Keep-Alive stuff
     */
    protected static final HashMap<String, LinkedList<HTTPKeepAliveSocket>> KEEPALIVEPOOL    = new HashMap<String, LinkedList<HTTPKeepAliveSocket>>();
    protected static final DelayedRunnable                                  keepAliveCleanup = new DelayedRunnable(20000, 60000) {

                                                                                                 @Override
                                                                                                 public void delayedrun() {
                                                                                                     synchronized (HTTPConnectionImpl.LOCK) {
                                                                                                         HTTPConnectionImpl.KEEPALIVESOCKETS.isEmpty();
                                                                                                         final Iterator<Entry<String, LinkedList<HTTPKeepAliveSocket>>> idIterator = HTTPConnectionImpl.KEEPALIVEPOOL.entrySet().iterator();
                                                                                                         while (idIterator.hasNext()) {
                                                                                                             final Entry<String, LinkedList<HTTPKeepAliveSocket>> next = idIterator.next();
                                                                                                             final LinkedList<HTTPKeepAliveSocket> keepAliveSockets = next.getValue();
                                                                                                             if (keepAliveSockets != null) {
                                                                                                                 final Iterator<HTTPKeepAliveSocket> keepAliveIterator = keepAliveSockets.iterator();
                                                                                                                 while (keepAliveIterator.hasNext()) {
                                                                                                                     final HTTPKeepAliveSocket keepAliveSocket = keepAliveIterator.next();
                                                                                                                     if (keepAliveSocket.getSocket().isClosed() || keepAliveSocket.getKeepAliveTimestamp() <= System.currentTimeMillis()) {
                                                                                                                         try {
                                                                                                                             keepAliveSocket.getSocket().close();
                                                                                                                         } catch (final Throwable ignore) {
                                                                                                                         }
                                                                                                                         keepAliveIterator.remove();
                                                                                                                     }
                                                                                                                 }
                                                                                                             }
                                                                                                             if (keepAliveSockets == null || keepAliveSockets.size() == 0) {
                                                                                                                 idIterator.remove();
                                                                                                             }
                                                                                                         }
                                                                                                     }
                                                                                                 }
                                                                                             };
    protected static final WeakHashMap<Socket, HTTPKeepAliveSocket>         KEEPALIVESOCKETS = new WeakHashMap<Socket, HTTPKeepAliveSocket>();
    protected static final Object                                           LOCK             = new Object();

    public HTTPConnectionImpl(final URL url) {
        this(url, null);
    }

    public HTTPConnectionImpl(final URL url, final HTTPProxy p) {
        this.httpURL = url;
        this.proxy = p;
        this.requestProperties = new HTTPHeaderMap<String>();
        this.headers = new HTTPHeaderMap<List<String>>();
        String httpPath = new org.appwork.utils.Regex(this.httpURL.toString(), "https?://.*?(/.+)").getMatch(0);
        if (httpPath == null) {
            this.httpPath = "/";
        } else {
            this.httpPath = httpPath;
        }
    }

    protected long getDefaultKeepAliveMaxRequests() {
        return 128;
    }

    protected long getMaxKeepAliveSockets() {
        return 5;
    }

    protected long getDefaultKeepAliveTimeout() {
        return 60 * 1000l;
    }

    protected boolean putKeepAliveSocket(final Socket socket) throws IOException {
        /**
         * only keep-Alive sockets if
         * 
         * 1.) keepAliveEnabled, HTTP Request/Response signals Keep-Alive and
         * keep-Alive feature is enabled
         * 
         * 2.) responseCode is ok
         * 
         * 3.) socket is open/not closed/input and output open
         * 
         * 4.) used inputstream has reached valid EOF
         * 
         * 5.) available outputstream has written all data
         * 
         */
        if (socket != null && this.isKeepAlivedEnabled() && this.isOK() && socket.isConnected() && !socket.isClosed() && socket.isInputShutdown() == false && socket.isOutputShutdown() == false) {
            if (this.inputStream != null && this.inputStream instanceof StreamValidEOF && ((StreamValidEOF) this.inputStream).isValidEOF()) {
                if (!this.requiresOutputStream() || ((CountingOutputStream) this.outputStream).transferedBytes() == this.postTodoLength) {
                    socket.setKeepAlive(true);
                    synchronized (HTTPConnectionImpl.LOCK) {
                        HTTPKeepAliveSocket keepAliveSocket = HTTPConnectionImpl.KEEPALIVESOCKETS.remove(socket);
                        if (keepAliveSocket == null) {
                            final String local;
                            if (this.proxy != null && this.proxy.isDirect()) {
                                local = this.proxy.getLocalIP().getHostAddress();
                            } else {
                                local = "";
                            }
                            String remote = this.httpURL.getHost().toLowerCase(Locale.ENGLISH);
                            final String ID = this.httpURL.getProtocol() + ":" + local + "->" + remote + ":" + socket.getPort();
                            final String connectionResponse = this.getHeaderField("Keep-Alive");
                            final String maxKeepAliveTimeoutString = new Regex(connectionResponse, "timeout\\s*?=\\s*?(\\d+)").getMatch(0);
                            final String maxKeepAliveRequestsString = new Regex(connectionResponse, "max\\s*?=\\s*?(\\d+)").getMatch(0);
                            final long maxKeepAliveTimeout;
                            if (maxKeepAliveTimeoutString != null) {
                                maxKeepAliveTimeout = Long.parseLong(maxKeepAliveTimeoutString) * 1000l;
                            } else {
                                maxKeepAliveTimeout = this.getDefaultKeepAliveTimeout();
                            }
                            final long maxKeepAliveRequests;
                            if (maxKeepAliveRequestsString != null) {
                                maxKeepAliveRequests = Long.parseLong(maxKeepAliveRequestsString);
                            } else {
                                maxKeepAliveRequests = this.getDefaultKeepAliveMaxRequests();
                            }
                            keepAliveSocket = new HTTPKeepAliveSocket(ID, socket, maxKeepAliveTimeout, maxKeepAliveRequests);
                        }
                        keepAliveSocket.increaseRequests();
                        if (keepAliveSocket.getRequestsLeft() > 0) {
                            final String ID = keepAliveSocket.getID();
                            LinkedList<HTTPKeepAliveSocket> sockets = HTTPConnectionImpl.KEEPALIVEPOOL.get(ID);
                            if (sockets == null) {
                                sockets = new LinkedList<HTTPKeepAliveSocket>();
                                HTTPConnectionImpl.KEEPALIVEPOOL.put(ID, sockets);
                            }
                            keepAliveSocket.keepAlive();
                            sockets.add(keepAliveSocket);
                            HTTPConnectionImpl.keepAliveCleanup.resetAndStart();
                            final long maxKeepAlive = this.getMaxKeepAliveSockets();
                            if (sockets.size() > maxKeepAlive) {
                                Iterator<HTTPKeepAliveSocket> it = sockets.iterator();
                                while (it.hasNext() && sockets.size() > maxKeepAlive) {
                                    HTTPKeepAliveSocket next = it.next();
                                    try {
                                        next.getSocket().close();
                                    } catch (final Throwable ignore) {
                                    }
                                    it.remove();
                                }
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    protected Socket getKeepAliveSocket() {
        final String local;
        if (this.proxy != null && this.proxy.isDirect()) {
            local = this.proxy.getLocalIP().getHostAddress();
        } else {
            local = "";
        }
        int port = this.httpURL.getPort();
        if (port == -1) {
            port = this.httpURL.getDefaultPort();
        }
        String remote = this.httpURL.getHost().toLowerCase(Locale.ENGLISH);
        final String ID = this.httpURL.getProtocol() + ":" + local + "->" + remote + ":" + port;
        HTTPKeepAliveSocket ret = null;
        synchronized (HTTPConnectionImpl.LOCK) {
            LinkedList<HTTPKeepAliveSocket> sockets = HTTPConnectionImpl.KEEPALIVEPOOL.get(ID);
            if (sockets != null) {
                Iterator<HTTPKeepAliveSocket> it = sockets.descendingIterator();
                while (it.hasNext()) {
                    HTTPKeepAliveSocket next = it.next();
                    if (next.getSocket().isClosed() || next.getKeepAliveTimestamp() <= System.currentTimeMillis()) {
                        try {
                            next.getSocket().close();
                        } catch (final Throwable ignore) {
                        }
                        it.remove();
                    } else {
                        ret = next;
                        it.remove();
                        HTTPConnectionImpl.KEEPALIVESOCKETS.put(next.getSocket(), next);
                        break;
                    }
                }
                if (sockets.isEmpty()) {
                    HTTPConnectionImpl.KEEPALIVEPOOL.remove(ID);
                }
            }
        }
        if (ret != null) {
            return ret.getSocket();
        } else {
            return null;
        }
    }

    /* this will add Host header at the beginning */
    protected void addHostHeader() {
        final int defaultPort = this.httpURL.getDefaultPort();
        final int usedPort = this.httpURL.getPort();
        String port = "";
        if (usedPort != -1 && defaultPort != -1 && usedPort != defaultPort) {
            port = ":" + usedPort;
        }
        this.requestProperties.put("Host", this.httpURL.getHost() + port);
    }

    protected void resetConnection() {
        this.inputStreamConnected = false;
        this.httpResponseCode = -1;
        this.httpResponseMessage = "";
        this.postTodoLength = -1;
        this.outputStream = null;
        this.inputStream = null;
        this.convertedInputStream = null;
        this.requestTime = -1;
        this.headers.clear();
        this.ranges = null;
    }

    public void connect() throws IOException {
        boolean sslSNIWorkAround = false;
        boolean sslV3Workaround = false;
        InetAddress hosts[] = null;
        connect: while (true) {
            if (this.isConnectionSocketValid()) { return;/* oder fehler */
            }
            this.resetConnection();
            this.connectionSocket = this.getKeepAliveSocket();
            if (this.connectionSocket == null) {
                if (hosts == null) {
                    hosts = this.resolvHostIP(this.httpURL.getHost());
                }
                /* try all different ip's until one is valid and connectable */
                IOException ee = null;
                for (final InetAddress host : hosts) {
                    this.connectionSocket = new Socket(Proxy.NO_PROXY);
                    this.resetConnection();
                    int port = this.httpURL.getPort();
                    if (port == -1) {
                        port = this.httpURL.getDefaultPort();
                    }
                    final long startTime = System.currentTimeMillis();
                    if (this.proxy != null && this.proxy.isDirect()) {
                        /* bind socket to given interface */
                        InetSocketAddress proxyInetSocketAddress = null;
                        try {
                            if (this.proxy.getLocalIP() == null) { throw new IOException("Invalid localIP"); }
                            proxyInetSocketAddress = new InetSocketAddress(this.proxy.getLocalIP(), 0);
                            this.connectionSocket.bind(proxyInetSocketAddress);
                        } catch (final IOException e) {
                            this.connectExceptions.add(proxyInetSocketAddress + "|" + e.getMessage());
                            throw new ProxyConnectException(e, this.proxy);
                        }
                    } else if (this.proxy != null && this.proxy.isNone()) {
                        /* none is also allowed here */
                    } else if (this.proxy != null) { throw new RuntimeException("Invalid Direct Proxy"); }
                    SSLSocket sslSocket = null;
                    InetSocketAddress connectedInetSocketAddress = null;
                    try {
                        /* try to connect to given host now */
                        connectedInetSocketAddress = new InetSocketAddress(host, port);
                        this.connectionSocket.connect(connectedInetSocketAddress, this.connectTimeout);
                        if (this.httpURL.getProtocol().startsWith("https")) {
                            if (sslSNIWorkAround) {
                                /* wrong configured SNI at serverSide */
                                sslSocket = (SSLSocket) TrustALLSSLFactory.getSSLFactoryTrustALL().createSocket(this.connectionSocket, "", port, true);
                            } else {
                                sslSocket = (SSLSocket) TrustALLSSLFactory.getSSLFactoryTrustALL().createSocket(this.connectionSocket, this.httpURL.getHost(), port, true);
                            }
                            if (sslV3Workaround && sslSocket != null) {
                                /* workaround for SSLv3 only hosts */
                                sslSocket.setEnabledProtocols(new String[] { "SSLv3" });
                            }
                            sslSocket.startHandshake();
                            this.connectionSocket = sslSocket;
                        }
                        this.requestTime = System.currentTimeMillis() - startTime;
                        ee = null;
                        break;
                    } catch (final IOException e) {
                        this.connectExceptions.add(connectedInetSocketAddress + "|" + e.getMessage());
                        this.disconnect();
                        if (sslSNIWorkAround == false && e.getMessage().contains("unrecognized_name")) {
                            sslSNIWorkAround = true;
                            continue connect;
                        } else if (sslV3Workaround == false && e.getMessage().contains("bad_record_mac")) {
                            sslV3Workaround = true;
                            continue connect;
                        }
                        ee = e;
                    }
                }
                if (ee != null) { throw ee; }
            }
            this.setReadTimeout(this.readTimeout);
            /* now send Request */
            try {
                this.sendRequest();
                return;
            } catch (final javax.net.ssl.SSLException e) {
                this.connectExceptions.add(this.connectionSocket.getInetAddress() + "|" + e.getMessage());
                this.disconnect();
                if (sslSNIWorkAround == false && e.getMessage().contains("unrecognized_name")) {
                    sslSNIWorkAround = true;
                    continue connect;
                } else if (sslV3Workaround == false && e.getMessage().contains("bad_record_mac")) {
                    sslV3Workaround = true;
                    continue connect;
                }
                throw e;
            } catch (final HTTPKeepAliveSocketException e) {
                if (KEEPALIVE.EXTERNAL_EXCEPTION.equals(this.getKeepAlive())) {
                    //
                    throw e;
                }
            }
        }
    }

    protected boolean isKeepAlivedEnabled() {
        final KEEPALIVE keepAlive = this.getKeepAlive();
        if (!KEEPALIVE.DISABLED.equals(keepAlive)) {
            final String connectionRequest = this.getRequestProperty(HTTPConstants.HEADER_REQUEST_CONNECTION);
            final String connectionResponse = this.getHeaderField(HTTPConstants.HEADER_REQUEST_CONNECTION);
            final boolean tryKeepAlive = (!this.requiresOutputStream() || KEEPALIVE.EXTERNAL_EXCEPTION.equals(keepAlive)) && (connectionResponse == null || StringUtils.containsIgnoreCase(connectionResponse, "Keep-Alive")) && (connectionRequest == null || !StringUtils.containsIgnoreCase(connectionRequest, "close"));
            return tryKeepAlive;
        } else {
            return false;
        }
    }

    protected synchronized void connectInputStream() throws IOException {
        final Socket connectionSocket = this.getConnectionSocket();
        try {
            if (this.requiresOutputStream()) {
                final long done = ((CountingOutputStream) this.getOutputStream()).transferedBytes();
                if (done != this.postTodoLength) { throw new IllegalStateException("Content-Length " + this.postTodoLength + " does not match send " + done + " bytes"); }
            }
            if (this.inputStreamConnected) { return; }
            if (this.requiresOutputStream()) {
                /* flush outputstream in case some buffers are not flushed yet */
                this.getOutputStream().flush();
            }
            this.inputStreamConnected = true;
            /* first read http header */
            ByteBuffer header = HTTPConnectionUtils.readheader(connectionSocket.getInputStream(), true);
            if (header.limit() == 0) { throw new EOFException("empty HTTP-Response"); }
            if (header.hasArray()) {
                this.httpHeader = new String(header.array(), 0, header.limit(), "ISO-8859-1").trim();
            } else {
                final byte[] bytes = new byte[header.limit()];
                header.get(bytes);
                this.httpHeader = new String(bytes, "ISO-8859-1").trim();
            }
            /* parse response code/message */
            if (this.httpHeader.startsWith("HTTP")) {
                final String code = new Regex(this.httpHeader, "HTTP.*? (\\d+)").getMatch(0);
                if (code != null) {
                    this.httpResponseCode = Integer.parseInt(code);
                }
                this.httpResponseMessage = new Regex(this.httpHeader, "HTTP.*? \\d+ (.+)").getMatch(0);
                if (this.httpResponseMessage == null) {
                    this.httpResponseMessage = "";
                }
            } else {
                this.invalidHttpHeader = this.httpHeader;
                this.httpHeader = HTTPConnectionImpl.UNKNOWN_HTTP_RESPONSE;
                // Unknown HTTP Response: 999!
                this.httpResponseCode = 999;
                this.httpResponseMessage = HTTPConnectionImpl.UNKNOWN_HTTP_RESPONSE;
                if (header.limit() > 0) {
                    /*
                     * push back the data that got read because no http header
                     * exists
                     */
                    final PushbackInputStream pushBackInputStream;
                    if (header.hasArray()) {
                        pushBackInputStream = new PushbackInputStream(connectionSocket.getInputStream(), header.limit());
                        pushBackInputStream.unread(header.array(), 0, header.limit());
                    } else {
                        final byte[] bytes = new byte[header.limit()];
                        header.get(bytes);
                        pushBackInputStream = new PushbackInputStream(connectionSocket.getInputStream(), bytes.length);
                        pushBackInputStream.unread(bytes);
                    }
                    this.inputStream = pushBackInputStream;
                } else {
                    /* nothing to push back */
                    this.inputStream = connectionSocket.getInputStream();
                }
                return;
            }
            /* read rest of http headers */
            header = HTTPConnectionUtils.readheader(connectionSocket.getInputStream(), false);
            final String temp;
            if (header.hasArray()) {
                temp = new String(header.array(), 0, header.limit(), "UTF-8");
            } else {
                final byte[] bytes = new byte[header.limit()];
                header.get(bytes);
                temp = new String(bytes, "UTF-8");
            }
            /*
             * split header into single strings, use RN or N(buggy fucking non
             * rfc)
             */
            String[] headerStrings = temp.split("(\r\n)|(\n)");
            for (final String line : headerStrings) {
                String key = null;
                String value = null;
                int index = 0;
                if ((index = line.indexOf(": ")) > 0) {
                    key = line.substring(0, index);
                    value = line.substring(index + 2);
                } else if ((index = line.indexOf(":")) > 0) {
                    /* buggy servers that don't have :space ARG */
                    key = line.substring(0, index);
                    value = line.substring(index + 1);
                } else {
                    key = null;
                    value = line;
                }
                if (key != null) {
                    key = key.trim();
                }
                if (value != null) {
                    value = value.trim();
                }
                List<String> list = this.headers.get(key);
                if (list == null) {
                    list = new ArrayList<String>();
                    this.headers.put(key, list);
                }
                list.add(value);
            }
            headerStrings = null;
            InputStream wrappedInputStream;
            if (this.isKeepAlivedEnabled()) {
                /* keep-alive-> do not close the inputstream! */
                wrappedInputStream = new FilterInputStream(connectionSocket.getInputStream()) {

                    @Override
                    public void close() throws IOException {
                        /* do not close, keep-Alive */
                    }
                };
            } else {
                wrappedInputStream = connectionSocket.getInputStream();
            }
            final boolean isChunked = StringUtils.containsIgnoreCase(this.getHeaderField(HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING), HTTPConstants.HEADER_RESPONSE_TRANSFER_ENCODING_CHUNKED);
            if (isChunked) {
                /* wrap chunkedInputStream */
                wrappedInputStream = new ChunkedInputStream(wrappedInputStream);
            } else {
                final long contentLength = this.getContentLength();
                if (contentLength >= 0) {
                    /* wrap limitedInputStream */
                    wrappedInputStream = new LimitedInputStream(wrappedInputStream, contentLength);
                }
            }
            this.inputStream = wrappedInputStream;
        } catch (final IOException e) {
            this.disconnect();
            synchronized (HTTPConnectionImpl.LOCK) {
                if (HTTPConnectionImpl.KEEPALIVESOCKETS.containsKey(connectionSocket)) {
                    throw new HTTPKeepAliveSocketException(e, connectionSocket);
                } else {
                    throw e;
                }
            }
        }
    }

    public void disconnect() {
        Socket connectionSocket = null;
        try {
            connectionSocket = this.getConnectionSocket();
            if (connectionSocket != null && !this.putKeepAliveSocket(connectionSocket)) {
                connectionSocket.close();
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            try {
                if (connectionSocket != null) {
                    connectionSocket.close();
                }
            } catch (final Throwable ignore) {
            }
        } finally {
            this.connectionSocket = null;
        }
    }

    @Override
    public void finalizeConnect() throws IOException {
        this.connect();
        this.connectInputStream();
    }

    @Override
    public int[] getAllowedResponseCodes() {
        return this.allowedResponseCodes;
    }

    public String getCharset() {
        if (this.customcharset != null) { return this.customcharset; }
        String charSet = this.getContentType();
        if (charSet != null) {
            final int charSetIndex = this.getContentType().toLowerCase().indexOf("charset=");
            if (charSetIndex > 0) {
                charSet = this.getContentType().substring(charSetIndex + 8).trim();
                if (charSet.length() > 2) {
                    if (charSet.startsWith("\"")) {
                        charSet = charSet.substring(1);
                        final int indexLast = charSet.lastIndexOf("\"");
                        if (indexLast > 0) {
                            charSet = charSet.substring(0, indexLast);
                        }
                    }
                    return charSet;
                }
            }
        }
        return null;

    }

    @Override
    public long getCompleteContentLength() {
        final long[] ranges = this.getRange();
        if (ranges != null) { return ranges[2]; }
        return this.getContentLength();
    }

    public long getContentLength() {
        final String length = this.getHeaderField("Content-Length");
        if (length != null) { return Long.parseLong(length.trim()); }
        return -1;
    }

    public String getContentType() {
        final String type = this.getHeaderField("Content-Type");
        if (type == null) { return "unknown"; }
        return type;
    }

    public String getHeaderField(final String string) {
        final List<String> ret = this.headers.get(string);
        if (ret == null || ret.size() == 0) { return null; }
        return ret.get(0);
    }

    public Map<String, List<String>> getHeaderFields() {
        return this.headers;
    }

    public List<String> getHeaderFields(final String string) {
        final List<String> ret = this.headers.get(string);
        if (ret == null || ret.size() == 0) { return null; }
        return ret;
    }

    public InputStream getInputStream() throws IOException {
        this.connect();
        this.connectInputStream();
        final int code = this.getResponseCode();
        if (this.isOK() || code == 404 || code == 403 || code == 416) {
            if (this.convertedInputStream != null) { return this.convertedInputStream; }
            if (this.contentDecoded) {
                final String encodingTransfer = this.getHeaderField("Content-Transfer-Encoding");
                if ("base64".equalsIgnoreCase(encodingTransfer)) {
                    /* base64 encoded content */
                    this.inputStream = new Base64InputStream(this.inputStream);
                }
                /* we convert different content-encodings to normal inputstream */
                final String encoding = this.getHeaderField("Content-Encoding");
                if (encoding == null || encoding.length() == 0 || "none".equalsIgnoreCase(encoding)) {
                    /* no encoding */
                    this.convertedInputStream = this.inputStream;
                } else if ("gzip".equalsIgnoreCase(encoding)) {
                    /* gzip encoding */
                    this.convertedInputStream = new GZIPInputStream(this.inputStream);
                } else if ("deflate".equalsIgnoreCase(encoding)) {
                    /* deflate encoding */
                    this.convertedInputStream = new java.util.zip.InflaterInputStream(this.inputStream, new java.util.zip.Inflater(true));
                } else {
                    /* unsupported */
                    this.contentDecoded = false;
                    this.convertedInputStream = this.inputStream;
                }
            } else {
                /* use original inputstream */
                this.convertedInputStream = this.inputStream;
            }
            return this.convertedInputStream;
        } else {
            throw new IOException(this.getResponseCode() + " " + this.getResponseMessage());
        }
    }

    public OutputStream getOutputStream() throws IOException {
        this.connect();
        if (this.outputStream == null) { throw new IOException("OutputStream no longer available"); }
        return this.outputStream;
    }

    public long[] getRange() {
        if (this.ranges != null) { return this.ranges; }
        String contentRange = this.getHeaderField("Content-Range");
        if ((contentRange = this.getHeaderField("Content-Range")) == null) { return null; }
        String[] range = null;
        if (contentRange != null) {
            if ((range = new Regex(contentRange, ".*?(\\d+).*?-.*?(\\d+).*?/.*?(\\d+)").getRow(0)) != null) {
                /* RFC-2616 */
                /* START-STOP/SIZE */
                /* Content-Range=[133333332-199999999/200000000] */
                final long gotSB = Long.parseLong(range[0]);
                final long gotEB = Long.parseLong(range[1]);
                final long gotS = Long.parseLong(range[2]);
                this.ranges = new long[] { gotSB, gotEB, gotS };
            } else if ((range = new Regex(contentRange, ".*?(\\d+).*?-/.*?(\\d+)").getRow(0)) != null && this.getResponseCode() != 416) {
                /* only parse this when we have NO 416 (invalid range request) */
                /* NON RFC-2616! STOP is missing */
                /*
                 * this happend for some stupid servers, seems to happen when
                 * request is bytes=9500- (x till end)
                 */
                /* START-/SIZE */
                /* content-range: bytes 1020054729-/1073741824 */
                final long gotSB = Long.parseLong(range[0]);
                final long gotS = Long.parseLong(range[1]);
                this.ranges = new long[] { gotSB, gotS - 1, gotS };
            } else if (this.getResponseCode() == 416 && (range = new Regex(contentRange, ".*?\\*/.*?(\\d+)").getRow(0)) != null) {
                /* a 416 may respond with content-range * | content.size answer */
                this.ranges = new long[] { -1, -1, Long.parseLong(range[0]) };
            } else if (this.getResponseCode() == 206 && (range = new Regex(contentRange, "[ \\*]+/(\\d+)").getRow(0)) != null) {
                /* RFC-2616 */
                /* a nginx 206 may respond with */
                /* content-range: bytes * / 554407633 */
                /*
                 * A response with status code 206 (Partial Content) MUST NOT
                 * include a Content-Range field with a byte-range- resp-spec of
                 * "*".
                 */
                this.ranges = new long[] { -1, Long.parseLong(range[0]), Long.parseLong(range[0]) };
            } else {
                /* unknown range header format! */
                System.out.println(contentRange + " format is unknown!");
            }
        }
        return this.ranges;
    }

    protected String getRequestInfo() {
        final StringBuilder sb = new StringBuilder();
        sb.append("----------------Request Information-------------\r\n");
        sb.append("URL: ").append(this.getURL()).append("\r\n");
        final Socket lhttpSocket = this.connectionSocket;
        if (lhttpSocket != null && lhttpSocket.isConnected()) {
            sb.append("HostIP: ").append(lhttpSocket.getInetAddress()).append("\r\n");
        } else {
            sb.append("Host: ").append(this.getURL().getHost()).append("\r\n");
        }
        if (this.proxy != null && this.proxy.isDirect()) {
            sb.append("LocalIP: ").append(this.proxy.getLocalIP()).append("\r\n");
        }
        sb.append("Connection-Timeout: ").append(this.connectTimeout + "ms").append("\r\n");
        sb.append("Read-Timeout: ").append(this.readTimeout + "ms").append("\r\n");
        if (this.connectExceptions.size() > 0) {
            sb.append("----------------ConnectionExceptions-------------------------\r\n");
            int index = 0;
            for (String connectException : this.connectExceptions) {
                sb.append(index++).append(":").append(connectException).append("\r\n");
            }
        }
        sb.append("----------------Request-------------------------\r\n");
        if (this.inputStream != null) {
            sb.append(this.httpMethod.toString()).append(' ').append(this.httpPath).append(" HTTP/1.1\r\n");

            final Iterator<Entry<String, String>> it = this.getRequestProperties().entrySet().iterator();
            while (it.hasNext()) {
                final Entry<String, String> next = it.next();
                if (next.getValue() == null) {
                    continue;
                }
                sb.append(next.getKey());
                sb.append(": ");
                sb.append(next.getValue());
                sb.append("\r\n");
            }
        } else {
            sb.append("-------------Not Connected Yet!-----------------\r\n");
        }
        return sb.toString();
    }

    public RequestMethod getRequestMethod() {
        return this.httpMethod;
    }

    public Map<String, String> getRequestProperties() {
        return this.requestProperties;
    }

    public String getRequestProperty(final String string) {
        return this.requestProperties.get(string);
    }

    public long getRequestTime() {
        return this.requestTime;
    }

    public int getResponseCode() {
        return this.httpResponseCode;
    }

    protected String getResponseInfo() {
        final StringBuilder sb = new StringBuilder();
        sb.append("----------------Response Information------------\r\n");
        try {
            if (this.inputStream != null) {
                final long lrequestTime = this.requestTime;
                if (lrequestTime >= 0) {
                    sb.append("Connection-Time: ").append(lrequestTime + "ms").append("\r\n");
                } else {
                    sb.append("Connection-Time: keep-Alive\r\n");
                }
                sb.append("----------------Response------------------------\r\n");
                this.connectInputStream();
                sb.append(this.httpHeader).append("\r\n");
                if (this.invalidHttpHeader != null) {
                    sb.append("InvalidHTTPHeader: ").append(this.invalidHttpHeader).append("\r\n");
                }
                for (final Entry<String, List<String>> next : this.getHeaderFields().entrySet()) {
                    for (int i = 0; i < next.getValue().size(); i++) {
                        if (next.getKey() == null) {
                            sb.append(next.getValue().get(i));
                            sb.append("\r\n");
                        } else {
                            sb.append(next.getKey());
                            sb.append(": ");
                            sb.append(next.getValue().get(i));
                            sb.append("\r\n");
                        }
                    }
                }
                sb.append("------------------------------------------------\r\n");
            } else {
                sb.append("-------------Not Connected Yet!------------------\r\n");
            }
        } catch (final IOException nothing) {
            sb.append("----------No InputStream Available!--------------\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }

    public String getResponseMessage() {
        return this.httpResponseMessage;
    }

    public URL getURL() {
        return this.httpURL;
    }

    public boolean isConnected() {
        final Socket connectionSocket = this.getConnectionSocket();
        if (connectionSocket != null && connectionSocket.isConnected()) { return true; }
        return false;
    }

    protected boolean isConnectionSocketValid() {
        final Socket connectionSocket = this.getConnectionSocket();
        return connectionSocket != null && connectionSocket.isConnected() && !connectionSocket.isClosed();
    }

    @Override
    public boolean isContentDecoded() {
        return this.contentDecoded;
    }

    public boolean isContentDisposition() {
        return this.getHeaderField("Content-Disposition") != null;
    }

    public boolean isOK() {
        final int code = this.getResponseCode();
        if (code >= 200 && code < 400) { return true; }
        if (this.isResponseCodeAllowed(code)) { return true; }
        return false;
    }

    protected boolean isResponseCodeAllowed(final int code) {
        for (final int c : this.allowedResponseCodes) {
            if (c == code) { return true; }
        }
        return false;
    }

    protected void putHostToTop(final Map<String, String> oldRequestProperties) {
        final HTTPHeaderMap<String> newRet = new HTTPHeaderMap<String>();
        final String host = oldRequestProperties.remove("Host");
        if (host != null) {
            newRet.put("Host", host);
        }
        newRet.putAll(oldRequestProperties);
        oldRequestProperties.clear();
        oldRequestProperties.putAll(newRet);
    }

    protected boolean requiresOutputStream() {
        final RequestMethod method = this.getRequestMethod();
        return method == RequestMethod.POST || method == RequestMethod.PUT;
    }

    public InetAddress[] resolvHostIP(final String host) throws IOException {
        return HTTPConnectionUtils.resolvHostIP(host);
    }

    protected void sendRequest() throws UnsupportedEncodingException, IOException {
        /* now send Request */
        final Socket connectionSocket = this.getConnectionSocket();
        final StringBuilder sb = new StringBuilder();
        sb.append(this.httpMethod.name()).append(' ').append(this.httpPath).append(" HTTP/1.1\r\n");
        boolean hostSet = false;
        /* check if host entry does exist */
        for (final String key : this.requestProperties.keySet()) {
            if ("Host".equalsIgnoreCase(key)) {
                hostSet = true;
                break;
            }
        }
        if (hostSet == false) {
            /* host entry does not exist,lets add it */
            this.addHostHeader();
        }
        this.putHostToTop(this.requestProperties);
        final Iterator<Entry<String, String>> it = this.requestProperties.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, String> next = it.next();
            if (next.getValue() == null) {
                continue;
            }
            if ("Content-Length".equalsIgnoreCase(next.getKey())) {
                /* content length to check if we send out all data */
                this.postTodoLength = Long.parseLong(next.getValue().trim());
            }
            sb.append(next.getKey()).append(": ").append(next.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        try {
            connectionSocket.getOutputStream().write(sb.toString().getBytes("ISO-8859-1"));
            connectionSocket.getOutputStream().flush();
            if (this.requiresOutputStream()) {
                final boolean isKeepAliveSocket;
                synchronized (HTTPConnectionImpl.LOCK) {
                    isKeepAliveSocket = HTTPConnectionImpl.KEEPALIVESOCKETS.containsKey(connectionSocket);
                }
                this.outputStream = new CountingOutputStream(connectionSocket.getOutputStream()) {

                    @Override
                    public void close() throws IOException {
                        if (!isKeepAliveSocket) {
                            super.close();
                        }
                    }

                    @Override
                    public void flush() throws IOException {
                        try {
                            super.flush();
                        } catch (final IOException e) {
                            if (isKeepAliveSocket) {
                                throw new HTTPKeepAliveSocketException(e, connectionSocket);
                            } else {
                                throw e;
                            }
                        }
                    }

                    @Override
                    public void write(byte[] b) throws IOException {
                        try {
                            super.write(b);
                        } catch (final IOException e) {
                            if (isKeepAliveSocket) {
                                throw new HTTPKeepAliveSocketException(e, connectionSocket);
                            } else {
                                throw e;
                            }
                        }
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        try {
                            super.write(b, off, len);
                        } catch (final IOException e) {
                            if (isKeepAliveSocket) {
                                throw new HTTPKeepAliveSocketException(e, connectionSocket);
                            } else {
                                throw e;
                            }
                        }
                    }

                    @Override
                    public void write(int b) throws IOException {
                        try {
                            super.write(b);
                        } catch (final IOException e) {
                            if (isKeepAliveSocket) {
                                throw new HTTPKeepAliveSocketException(e, connectionSocket);
                            } else {
                                throw e;
                            }
                        }
                    }

                };
            } else {
                this.connectInputStream();
            }
        } catch (final HTTPKeepAliveSocketException e) {
            throw e;
        } catch (final IOException e) {
            this.disconnect();
            synchronized (HTTPConnectionImpl.LOCK) {
                if (HTTPConnectionImpl.KEEPALIVESOCKETS.containsKey(connectionSocket)) {
                    throw new HTTPKeepAliveSocketException(e, connectionSocket);
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    public void setAllowedResponseCodes(final int[] codes) {
        if (codes == null) { throw new IllegalArgumentException("codes==null"); }
        this.allowedResponseCodes = codes;
    }

    public void setCharset(final String Charset) {
        this.customcharset = Charset;
    }

    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = Math.max(0, connectTimeout);
    }

    @Override
    public void setContentDecoded(final boolean b) {
        if (this.convertedInputStream != null) { throw new IllegalStateException("InputStream already in use!"); }
        this.contentDecoded = b;
    }

    public void setReadTimeout(final int readTimeout) {
        try {
            this.readTimeout = Math.max(0, readTimeout);
            Socket connectionSocket = this.getConnectionSocket();
            if (connectionSocket != null) {
                connectionSocket.setSoTimeout(this.readTimeout);
            }
        } catch (final Throwable ignore) {
        }
    }

    public void setRequestMethod(final RequestMethod method) {
        this.httpMethod = method;
    }

    public void setRequestProperty(final String key, final String value) {
        this.requestProperties.put(key, value);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getRequestInfo());
        sb.append(this.getResponseInfo());
        return sb.toString();
    }

}
