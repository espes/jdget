package org.appwork.utils.net.httpconnection;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;

import org.appwork.utils.Regex;
import org.appwork.utils.net.Base64InputStream;
import org.appwork.utils.net.ChunkedInputStream;
import org.appwork.utils.net.CountingOutputStream;
import org.appwork.utils.net.LimitedInputStream;

public class HTTPConnectionImpl implements HTTPConnection {

    /**
     * 
     */
    public static final String            UNKNOWN_HTTP_RESPONSE      = "unknown HTTP response";

    protected HTTPHeaderMap<String>       requestProperties          = null;

    protected long[]                      ranges;

    protected String                      customcharset              = null;
    protected Socket                      httpSocket                 = null;
    protected URL                         httpURL                    = null;
    protected HTTPProxy                   proxy                      = null;

    protected String                      httpPath                   = null;
    protected RequestMethod               httpMethod                 = RequestMethod.GET;
    protected HTTPHeaderMap<List<String>> headers                    = null;
    protected int                         httpResponseCode           = -1;
    protected String                      httpResponseMessage        = "";
    protected int                         readTimeout                = 30000;
    protected int                         connectTimeout             = 30000;
    protected long                        requestTime                = -1;
    protected OutputStream                outputStream               = null;
    protected InputStream                 inputStream                = null;
    protected InputStream                 convertedInputStream       = null;
    protected boolean                     inputStreamConnected       = false;

    protected String                      httpHeader                 = null;
    protected boolean                     outputClosed               = false;
    private boolean                       contentDecoded             = true;
    protected long                        postTodoLength             = -1;
    private int[]                         allowedResponseCodes       = new int[0];
    private InetSocketAddress             proxyInetSocketAddress     = null;
    protected InetSocketAddress           connectedInetSocketAddress = null;

    private SSLException                  sslException               = null;

    public HTTPConnectionImpl(final URL url) {
        this(url, null);
    }

    public HTTPConnectionImpl(final URL url, final HTTPProxy p) {
        httpURL = url;
        proxy = p;
        requestProperties = new HTTPHeaderMap<String>();
        headers = new HTTPHeaderMap<List<String>>();
    }

    /* this will add Host header at the beginning */
    protected void addHostHeader() {
        final int defaultPort = httpURL.getDefaultPort();
        final int usedPort = httpURL.getPort();
        String port = "";
        if (usedPort != -1 && defaultPort != -1 && usedPort != defaultPort) {
            port = ":" + usedPort;
        }

        requestProperties.put("Host", httpURL.getHost() + port);
    }

    public void connect() throws IOException {
        if (isConnected()) { return;/* oder fehler */
        }
        final InetAddress hosts[] = resolvHostIP(httpURL.getHost());
        /* try all different ip's until one is valid and connectable */
        IOException ee = null;
        for (final InetAddress host : hosts) {
            httpSocket = new Socket(Proxy.NO_PROXY);
            httpSocket.setSoTimeout(readTimeout);
            httpResponseCode = -1;
            int port = httpURL.getPort();
            if (port == -1) {
                port = httpURL.getDefaultPort();
            }
            final long startTime = System.currentTimeMillis();
            if (proxy != null && proxy.isDirect()) {
                /* bind socket to given interface */
                try {
                    if (proxy.getLocalIP() == null) { throw new IOException("Invalid localIP"); }
                    httpSocket.bind(proxyInetSocketAddress = new InetSocketAddress(proxy.getLocalIP(), 0));
                } catch (final IOException e) {
                    proxyInetSocketAddress = null;
                    throw new ProxyConnectException(e, proxy);
                }
            } else if (proxy != null && proxy.isNone()) {
                /* none is also allowed here */
            } else if (proxy != null) { throw new RuntimeException("Invalid Direct Proxy"); }
            SSLSocket sslSocket = null;
            try {
                /* try to connect to given host now */
                httpSocket.connect(connectedInetSocketAddress = new InetSocketAddress(host, port), connectTimeout);
                if (httpURL.getProtocol().startsWith("https")) {
                    /* https */
                    sslSocket = (SSLSocket) TrustALLSSLFactory.getSSLFactoryTrustALL().createSocket(httpSocket, httpURL.getHost(), port, true);
                    if (sslException != null && sslException.getMessage().contains("bad_record_mac")) {
                        /* workaround for SSLv3 only hosts */
                        ((SSLSocket) httpSocket).setEnabledProtocols(new String[] { "SSLv3" });
                    }
                    sslSocket.startHandshake();
                    httpSocket = sslSocket;
                }
                requestTime = System.currentTimeMillis() - startTime;
                ee = null;
                break;
            } catch (final IOException e) {
                try {
                    if (sslSocket != null) {
                        sslSocket.close();
                    }
                } catch (final Throwable nothing) {
                }
                connectedInetSocketAddress = null;
                try {
                    httpSocket.close();
                } catch (final Throwable nothing) {
                }
                ee = e;
            }
        }
        if (ee != null) { throw ee; }
        httpPath = new org.appwork.utils.Regex(httpURL.toString(), "https?://.*?(/.+)").getMatch(0);
        if (httpPath == null) {
            httpPath = "/";
        }
        /* now send Request */
        try {
            sendRequest();
        } catch (final javax.net.ssl.SSLException e) {
            if (sslException != null) {
                throw e;
            } else {
                this.disconnect(true);
                sslException = e;
                connect();
            }
        }
    }

    protected synchronized void connectInputStream() throws IOException {
        if (requiresOutputStream()) {
            final long done = ((CountingOutputStream) getOutputStream()).transferedBytes();
            if (done != postTodoLength) { throw new IOException("Content-Length " + postTodoLength + " does not match send " + done + " bytes"); }
        }
        if (inputStreamConnected) { return; }
        if (requiresOutputStream()) {
            /* flush outputstream in case some buffers are not flushed yet */
            getOutputStream().flush();
        }
        inputStreamConnected = true;
        /* first read http header */
        ByteBuffer header = HTTPConnectionUtils.readheader(httpSocket.getInputStream(), true);
        if (header.hasArray()) {
            httpHeader = new String(header.array(), 0, header.limit(), "ISO-8859-1").trim();
        } else {
            final byte[] bytes = new byte[header.limit()];
            header.get(bytes);
            httpHeader = new String(bytes, "ISO-8859-1").trim();
        }
        /* parse response code/message */
        if (httpHeader.startsWith("HTTP")) {
            final String code = new Regex(httpHeader, "HTTP.*? (\\d+)").getMatch(0);
            if (code != null) {
                httpResponseCode = Integer.parseInt(code);
            }
            httpResponseMessage = new Regex(httpHeader, "HTTP.*? \\d+ (.+)").getMatch(0);
            if (httpResponseMessage == null) {
                httpResponseMessage = "";
            }
        } else {
            httpHeader = HTTPConnectionImpl.UNKNOWN_HTTP_RESPONSE;
            httpResponseCode = 200;
            httpResponseMessage = HTTPConnectionImpl.UNKNOWN_HTTP_RESPONSE;
            if (header.limit() > 0) {
                /*
                 * push back the data that got read because no http header
                 * exists
                 */
                if (header.hasArray()) {
                    inputStream = new PushbackInputStream(httpSocket.getInputStream(), header.limit());
                    ((PushbackInputStream) inputStream).unread(header.array(), 0, header.limit());
                } else {
                    final byte[] bytes = new byte[header.limit()];
                    header.get(bytes);
                    inputStream = new PushbackInputStream(httpSocket.getInputStream(), bytes.length);
                    ((PushbackInputStream) inputStream).unread(bytes);
                }
            } else {
                /* nothing to push back */
                inputStream = httpSocket.getInputStream();
            }
            return;
        }
        /* read rest of http headers */
        header = HTTPConnectionUtils.readheader(httpSocket.getInputStream(), false);
        final String temp;
        if (header.hasArray()) {
            temp = new String(header.array(), 0, header.limit(), "UTF-8");
        } else {
            final byte[] bytes = new byte[header.limit()];
            header.get(bytes);
            temp = new String(bytes, "UTF-8");
        }
        /* split header into single strings, use RN or N(buggy fucking non rfc) */
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
            List<String> list = headers.get(key);
            if (list == null) {
                list = new ArrayList<String>();
                headers.put(key, list);
            }
            list.add(value);
        }
        headerStrings = null;
        final List<String> chunked = headers.get("Transfer-Encoding");
        if (chunked != null && chunked.size() > 0 && "chunked".equalsIgnoreCase(chunked.get(0))) {
            inputStream = new ChunkedInputStream(httpSocket.getInputStream());
        } else {
            inputStream = httpSocket.getInputStream();
        }
    }

    public void disconnect() {
        this.disconnect(false);
    }

    public void disconnect(final boolean freeConnection) {
        try {
            if (httpSocket != null) {
                httpSocket.close();
            }
        } catch (final Throwable e) {
            e.printStackTrace();
        } finally {
            if (freeConnection) {
                httpSocket = null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.httpconnection.HTTPConnection#finalizeConnect()
     */
    @Override
    public void finalizeConnect() throws IOException {
        connect();
        connectInputStream();
    }

    @Override
    public int[] getAllowedResponseCodes() {
        return allowedResponseCodes;
    }

    public String getCharset() {
        if (customcharset != null) { return customcharset; }
        String charSet = getContentType();
        if (charSet != null) {
            final int charSetIndex = getContentType().toLowerCase().indexOf("charset=");
            if (charSetIndex > 0) {
                charSet = getContentType().substring(charSetIndex + 8).trim();
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
        getRange();
        if (ranges != null) { return ranges[2]; }
        return getContentLength();
    }

    public long getContentLength() {
        final String length = getHeaderField("Content-Length");
        if (length != null) { return Long.parseLong(length.trim()); }
        return -1;
    }

    public String getContentType() {
        final String type = getHeaderField("Content-Type");
        if (type == null) { return "unknown"; }
        return type;
    }

    public String getHeaderField(final String string) {
        final List<String> ret = headers.get(string);
        if (ret == null || ret.size() == 0) { return null; }
        return ret.get(0);
    }

    public Map<String, List<String>> getHeaderFields() {
        return headers;
    }

    public List<String> getHeaderFields(final String string) {
        final List<String> ret = headers.get(string);
        if (ret == null || ret.size() == 0) { return null; }

        return ret;
    }

    public InputStream getInputStream() throws IOException {
        connect();
        connectInputStream();
        final int code = getResponseCode();
        if (isOK() || code == 404 || code == 403 || code == 416) {
            if (convertedInputStream != null) { return convertedInputStream; }
            final boolean chunked = "chunked".equalsIgnoreCase(getHeaderField("Transfer-Encoding"));
            final boolean keepAlive = "Keep-Alive".equalsIgnoreCase(getRequestProperty("connection"));
            final String connectionResponse = getHeaderField("connection");
            if (chunked == false && ("Keep-Alive".equalsIgnoreCase(connectionResponse) || keepAlive && connectionResponse == null)) {
                /* RFC2616, 4.4 Transfer-Encoding and Content-Length */
                /*
                 * wrap inputStream into limitedInputStream to avoid readTimeout
                 * on keep-alive connections
                 */
                final long contentLength = getContentLength();
                if (contentLength >= 0) {
                    inputStream = new LimitedInputStream(inputStream, contentLength);
                }
            }
            if (contentDecoded) {
                final String encodingTransfer = getHeaderField("Content-Transfer-Encoding");
                if ("base64".equalsIgnoreCase(encodingTransfer)) {
                    /* base64 encoded content */
                    inputStream = new Base64InputStream(inputStream);
                }
                /* we convert different content-encodings to normal inputstream */
                final String encoding = getHeaderField("Content-Encoding");
                if (encoding == null || encoding.length() == 0 || "none".equalsIgnoreCase(encoding)) {
                    /* no encoding */
                    convertedInputStream = inputStream;
                } else if ("gzip".equalsIgnoreCase(encoding)) {
                    /* gzip encoding */
                    convertedInputStream = new GZIPInputStream(inputStream);
                } else if ("deflate".equalsIgnoreCase(encoding)) {
                    /* deflate encoding */
                    convertedInputStream = new java.util.zip.InflaterInputStream(inputStream, new java.util.zip.Inflater(true));
                } else {
                    /* unsupported */
                    contentDecoded = false;
                    convertedInputStream = inputStream;
                }
            } else {
                /* use original inputstream */
                convertedInputStream = inputStream;
            }
            return convertedInputStream;
        } else {
            throw new IOException(getResponseCode() + " " + getResponseMessage());
        }
    }

    public OutputStream getOutputStream() throws IOException {
        connect();
        if (outputStream == null || outputClosed) { throw new IOException("OutputStream no longer available"); }
        return outputStream;
    }

    public long[] getRange() {
        if (ranges != null) { return ranges; }
        String contentRange = getHeaderField("Content-Range");
        if ((contentRange = getHeaderField("Content-Range")) == null) { return null; }
        String[] range = null;
        if (contentRange != null) {
            if ((range = new Regex(contentRange, ".*?(\\d+).*?-.*?(\\d+).*?/.*?(\\d+)").getRow(0)) != null) {
                /* RFC-2616 */
                /* START-STOP/SIZE */
                /* Content-Range=[133333332-199999999/200000000] */
                final long gotSB = Long.parseLong(range[0]);
                final long gotEB = Long.parseLong(range[1]);
                final long gotS = Long.parseLong(range[2]);
                ranges = new long[] { gotSB, gotEB, gotS };
                return ranges;
            } else if ((range = new Regex(contentRange, ".*?(\\d+).*?-/.*?(\\d+)").getRow(0)) != null && getResponseCode() != 416) {
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
                ranges = new long[] { gotSB, gotS - 1, gotS };
                return ranges;
            } else if (getResponseCode() == 416 && (range = new Regex(contentRange, ".*?\\*/.*?(\\d+)").getRow(0)) != null) {
                /* a 416 may respond with content-range * | content.size answer */
                ranges = new long[] { -1, -1, Long.parseLong(range[0]) };
                return ranges;
            } else if (getResponseCode() == 206 && (range = new Regex(contentRange, "[ \\*]+/(\\d+)").getRow(0)) != null) {
                /* RFC-2616 */
                /* a nginx 206 may respond with */
                /* content-range: bytes * / 554407633 */
                /*
                 * A response with status code 206 (Partial Content) MUST NOT
                 * include a Content-Range field with a byte-range- resp-spec of
                 * "*".
                 */
                ranges = new long[] { -1, Long.parseLong(range[0]), Long.parseLong(range[0]) };
                return ranges;
            } else {
                /* unknown range header format! */
                System.out.println(contentRange + " format is unknown!");
            }
        }
        return null;
    }

    protected String getRequestInfo() {
        final StringBuilder sb = new StringBuilder();
        sb.append("----------------Request Information-------------\r\n");
        sb.append("URL: ").append(getURL()).append("\r\n");
        sb.append("Host: ").append(getURL().getHost()).append("\r\n");
        if (connectedInetSocketAddress != null && connectedInetSocketAddress.getAddress() != null) {
            sb.append("HostIP: ").append(connectedInetSocketAddress.getAddress().getHostAddress()).append("\r\n");
        }
        if (proxyInetSocketAddress != null && proxyInetSocketAddress.getAddress() != null) {
            sb.append("LocalIP: ").append(proxyInetSocketAddress.getAddress().getHostAddress()).append("\r\n");
        }
        sb.append("Connection-Timeout: ").append(connectTimeout + "ms").append("\r\n");
        sb.append("Read-Timeout: ").append(readTimeout + "ms").append("\r\n");
        sb.append("----------------Request-------------------------\r\n");
        if (isConnected()) {
            sb.append(httpMethod.toString()).append(' ').append(httpPath).append(" HTTP/1.1\r\n");

            final Iterator<Entry<String, String>> it = getRequestProperties().entrySet().iterator();
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
        return httpMethod;
    }

    public Map<String, String> getRequestProperties() {
        return requestProperties;
    }

    public String getRequestProperty(final String string) {
        return requestProperties.get(string);
    }

    public long getRequestTime() {
        return requestTime;
    }

    public int getResponseCode() {
        return httpResponseCode;
    }

    protected String getResponseInfo() {
        final StringBuilder sb = new StringBuilder();
        sb.append("----------------Response Information------------\r\n");
        try {
            if (isConnected()) {
                sb.append("Connection-Time: ").append(requestTime + "ms").append("\r\n");
                sb.append("----------------Response------------------------\r\n");
                connectInputStream();
                sb.append(httpHeader).append("\r\n");
                for (final Entry<String, List<String>> next : this.getHeaderFields().entrySet()) {               
                    for (int i = 0; i <next.getValue().size(); i++) {
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
        return httpResponseMessage;
    }

    public URL getURL() {
        return httpURL;
    }

    public boolean isConnected() {
        if (httpSocket != null && httpSocket.isConnected()) { return true; }
        return false;
    }

    @Override
    public boolean isContentDecoded() {
        return contentDecoded;
    }

    public boolean isContentDisposition() {
        return getHeaderField("Content-Disposition") != null;
    }

    public boolean isOK() {
        final int code = getResponseCode();
        if (code >= 200 && code < 400) { return true; }
        if (isResponseCodeAllowed(code)) { return true; }
        return false;
    }

    protected boolean isResponseCodeAllowed(final int code) {
        for (final int c : allowedResponseCodes) {
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
        return httpMethod == RequestMethod.POST || httpMethod == RequestMethod.PUT;
    }

    public InetAddress[] resolvHostIP(final String host) throws IOException {
        return HTTPConnectionUtils.resolvHostIP(host);
    }

    protected void sendRequest() throws UnsupportedEncodingException, IOException {
        /* now send Request */
        final StringBuilder sb = new StringBuilder();
        sb.append(httpMethod.name()).append(' ').append(httpPath).append(" HTTP/1.1\r\n");
        boolean hostSet = false;
        /* check if host entry does exist */
        for (final String key : requestProperties.keySet()) {
            if ("Host".equalsIgnoreCase(key)) {
                hostSet = true;
                break;
            }
        }
        if (hostSet == false) {
            /* host entry does not exist,lets add it */
            addHostHeader();
        }
        putHostToTop(requestProperties);
        final Iterator<Entry<String, String>> it = requestProperties.entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, String> next = it.next();
            if (next.getValue() == null) {
                continue;
            }
            if ("Content-Length".equalsIgnoreCase(next.getKey())) {
                /* content length to check if we send out all data */
                postTodoLength = Long.parseLong(next.getValue().trim());
            }
            sb.append(next.getKey()).append(": ").append(next.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        httpSocket.getOutputStream().write(sb.toString().getBytes("ISO-8859-1"));
        httpSocket.getOutputStream().flush();
        if (requiresOutputStream() == false) {
            outputStream = httpSocket.getOutputStream();
            outputClosed = true;
            connectInputStream();
        } else {
            outputStream = new CountingOutputStream(httpSocket.getOutputStream());
        }
    }

    @Override
    public void setAllowedResponseCodes(final int[] codes) {
        if (codes == null) { throw new IllegalArgumentException("codes==null"); }
        allowedResponseCodes = codes;
    }

    public void setCharset(final String Charset) {
        customcharset = Charset;
    }

    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public void setContentDecoded(final boolean b) {
        if (convertedInputStream != null) { throw new IllegalStateException("InputStream already in use!"); }
        contentDecoded = b;

    }

    public void setReadTimeout(final int readTimeout) {
        try {
            if (isConnected()) {
                httpSocket.setSoTimeout(readTimeout);
            }
            this.readTimeout = readTimeout;
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    public void setRequestMethod(final RequestMethod method) {
        httpMethod = method;
    }

    public void setRequestProperty(final String key, final String value) {
        requestProperties.put(key, value);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getRequestInfo());
        sb.append(getResponseInfo());
        return sb.toString();
    }

}
