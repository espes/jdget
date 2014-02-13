package org.appwork.utils.net.httpconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
        this.httpURL = url;
        this.proxy = p;
        this.requestProperties = new HTTPHeaderMap<String>();
        this.headers = new HTTPHeaderMap<List<String>>();
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

    public void connect() throws IOException {
        if (this.isConnected()) { return;/* oder fehler */
        }
        final InetAddress hosts[] = this.resolvHostIP(this.httpURL.getHost());
        /* try all different ip's until one is valid and connectable */
        IOException ee = null;
        for (final InetAddress host : hosts) {
            if (this.httpURL.getProtocol().toLowerCase(Locale.ENGLISH).startsWith("https")) {
                /* https */
                this.httpSocket = TrustALLSSLFactory.getSSLFactoryTrustALL().createSocket();
                if (this.sslException != null && this.sslException.getMessage().contains("bad_record_mac")) {
                    /* workaround for SSLv3 only hosts */
                    ((SSLSocket) this.httpSocket).setEnabledProtocols(new String[] { "SSLv3" });
                }
                /*
                 * http://twelve-programmers.blogspot.de/2010/01/limitations-in-jsse
                 * -tls-implementation.html
                 */
                /*
                 * http://stackoverflow.com/questions/6851461/java-why-does-ssl-
                 * handshake-give-could-not-generate-dh-keypair-exception
                 */
                // final List<String> limited = new LinkedList<String>();
                // for (final String suite : ((SSLSocket)
                // this.httpSocket).getEnabledCipherSuites()) {
                // if (!suite.contains("_DHE_")) {
                // limited.add(suite);
                // }
                // }
                // ((SSLSocket)
                // this.httpSocket).setEnabledCipherSuites(limited.toArray(new
                // String[limited.size()]));
            } else {
                /* http */
                this.httpSocket = new Socket();
            }
            this.httpSocket.setSoTimeout(this.readTimeout);
            this.httpResponseCode = -1;
            int port = this.httpURL.getPort();
            if (port == -1) {
                port = this.httpURL.getDefaultPort();
            }
            final long startTime = System.currentTimeMillis();
            if (this.proxy != null && this.proxy.isDirect()) {
                /* bind socket to given interface */
                try {
                    if (this.proxy.getLocalIP() == null) { throw new IOException("Invalid localIP"); }
                    this.httpSocket.bind(this.proxyInetSocketAddress = new InetSocketAddress(this.proxy.getLocalIP(), 0));
                } catch (final IOException e) {
                    this.proxyInetSocketAddress = null;
                    throw new ProxyConnectException(e, this.proxy);
                }
            } else if (this.proxy != null && this.proxy.isNone()) {
                /* none is also allowed here */
            } else if (this.proxy != null) { throw new RuntimeException("Invalid Direct Proxy"); }

            try {
                /* try to connect to given host now */
                this.httpSocket.connect(this.connectedInetSocketAddress = new InetSocketAddress(host, port), this.connectTimeout);
                this.requestTime = System.currentTimeMillis() - startTime;
                ee = null;
                break;
            } catch (final IOException e) {
                this.connectedInetSocketAddress = null;
                try {
                    this.httpSocket.close();
                } catch (final Throwable nothing) {
                }
                ee = e;
            }
        }
        if (ee != null) { throw ee; }
        this.httpPath = new org.appwork.utils.Regex(this.httpURL.toString(), "https?://.*?(/.+)").getMatch(0);
        if (this.httpPath == null) {
            this.httpPath = "/";
        }
        /* now send Request */
        try {
            this.sendRequest();
        } catch (final javax.net.ssl.SSLException e) {
            if (this.sslException != null) {
                throw e;
            } else {
                this.disconnect(true);
                this.sslException = e;
                this.connect();
            }
        }
    }

    protected synchronized void connectInputStream() throws IOException {
        if (this.httpMethod == RequestMethod.POST) {
            this.shutDownOutput();
            final long done = ((CountingOutputStream) this.getOutputStream()).transferedBytes();
            if (done != this.postTodoLength) { throw new IOException("Content-Length " + this.postTodoLength + " does not match send " + done + " bytes"); }
        }
        if (this.inputStreamConnected) { return; }
        if (this.httpMethod == RequestMethod.POST) {
            /* flush outputstream in case some buffers are not flushed yet */
            this.getOutputStream().flush();
        }
        this.inputStreamConnected = true;
        /* first read http header */
        ByteBuffer header = HTTPConnectionUtils.readheader(this.httpSocket.getInputStream(), true);
        byte[] bytes = new byte[header.limit()];
        header.get(bytes);
        this.httpHeader = new String(bytes, "ISO-8859-1").trim();
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
            this.httpHeader = HTTPConnectionImpl.UNKNOWN_HTTP_RESPONSE;
            this.httpResponseCode = 200;
            this.httpResponseMessage = HTTPConnectionImpl.UNKNOWN_HTTP_RESPONSE;
            if (bytes.length > 0) {
                this.inputStream = new PushbackInputStream(this.httpSocket.getInputStream(), bytes.length);
                /*
                 * push back the data that got read because no http header
                 * exists
                 */
                ((PushbackInputStream) this.inputStream).unread(bytes);
            } else {
                /* nothing to push back */
                this.inputStream = this.httpSocket.getInputStream();
            }
            return;
        }
        /* read rest of http headers */
        header = HTTPConnectionUtils.readheader(this.httpSocket.getInputStream(), false);
        bytes = new byte[header.limit()];
        header.get(bytes);
        String temp = new String(bytes, "UTF-8");
        /* split header into single strings, use RN or N(buggy fucking non rfc) */
        String[] headerStrings = temp.split("(\r\n)|(\n)");
        temp = null;
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
        final List<String> chunked = this.headers.get("Transfer-Encoding");
        if (chunked != null && chunked.size() > 0 && "chunked".equalsIgnoreCase(chunked.get(0))) {
            this.inputStream = new ChunkedInputStream(this.httpSocket.getInputStream());
        } else {
            this.inputStream = this.httpSocket.getInputStream();
        }
    }

    public void disconnect() {
        this.disconnect(false);
    }

    public void disconnect(final boolean freeConnection) {
        try {
            try {
                this.shutDownOutput();
            } finally {
                this.httpSocket.close();
            }
        } catch (final Throwable e) {
        } finally {
            if (freeConnection) {
                this.httpSocket = null;
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
        this.connect();
        this.connectInputStream();
    }

    @Override
    public int[] getAllowedResponseCodes() {
        return this.allowedResponseCodes;
    }

    public String getCharset() {
        int i;
        if (this.customcharset != null) { return this.customcharset; }
        return this.getContentType() != null && (i = this.getContentType().toLowerCase().indexOf("charset=")) > 0 ? this.getContentType().substring(i + 8).trim() : null;
    }

    @Override
    public long getCompleteContentLength() {
        this.getRange();
        if (this.ranges != null) { return this.ranges[2]; }
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
            final boolean chunked = "chunked".equalsIgnoreCase(this.getHeaderField("Transfer-Encoding"));
            final boolean keepAlive = "Keep-Alive".equalsIgnoreCase(this.getRequestProperty("connection"));
            final String connectionResponse = this.getHeaderField("connection");
            if (chunked == false && ("Keep-Alive".equalsIgnoreCase(connectionResponse) || keepAlive && connectionResponse == null)) {
                /* RFC2616, 4.4 Transfer-Encoding and Content-Length */
                /*
                 * wrap inputStream into limitedInputStream to avoid readTimeout
                 * on keep-alive connections
                 */
                final long contentLength = this.getContentLength();
                if (contentLength >= 0) {
                    this.inputStream = new LimitedInputStream(this.inputStream, contentLength);
                }
            }
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
                    this.convertedInputStream = new java.util.zip.DeflaterInputStream(this.inputStream);
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
        if (this.outputStream == null || this.outputClosed) { throw new IOException("OutputStream no longer available"); }
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
                return this.ranges;
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
                return this.ranges;
            } else if (this.getResponseCode() == 416 && (range = new Regex(contentRange, ".*?\\*/.*?(\\d+)").getRow(0)) != null) {
                /* a 416 may respond with content-range * | content.size answer */
                this.ranges = new long[] { -1, -1, Long.parseLong(range[0]) };
                return this.ranges;
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
                return this.ranges;
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
        sb.append("URL: ").append(this.getURL()).append("\r\n");
        sb.append("Host: ").append(this.getURL().getHost()).append("\r\n");
        if (this.connectedInetSocketAddress != null && this.connectedInetSocketAddress.getAddress() != null) {
            sb.append("HostIP: ").append(this.connectedInetSocketAddress.getAddress().getHostAddress()).append("\r\n");
        }
        if (this.proxyInetSocketAddress != null && this.proxyInetSocketAddress.getAddress() != null) {
            sb.append("LocalIP: ").append(this.proxyInetSocketAddress.getAddress().getHostAddress()).append("\r\n");
        }
        sb.append("Connection-Timeout: ").append(this.connectTimeout + "ms").append("\r\n");
        sb.append("Read-Timeout: ").append(this.readTimeout + "ms").append("\r\n");
        sb.append("----------------Request-------------------------\r\n");
        if (this.isConnected()) {
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
            if (this.isConnected()) {
                sb.append("Connection-Time: ").append(this.requestTime + "ms").append("\r\n");
                sb.append("----------------Response------------------------\r\n");
                this.connectInputStream();
                sb.append(this.httpHeader).append("\r\n");
                for (final Entry<String, List<String>> next : this.getHeaderFields().entrySet()) {
                    // Achtung cookie reihenfolge ist wichtig!!!
                    for (int i = next.getValue().size() - 1; i >= 0; i--) {
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
        if (this.httpSocket != null && this.httpSocket.isConnected()) { return true; }
        return false;
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

    public InetAddress[] resolvHostIP(final String host) throws IOException {
        return HTTPConnectionUtils.resolvHostIP(host);
    }

    protected void sendRequest() throws UnsupportedEncodingException, IOException {
        /* now send Request */
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
        this.httpSocket.getOutputStream().write(sb.toString().getBytes("ISO-8859-1"));
        this.httpSocket.getOutputStream().flush();
        if (this.httpMethod != RequestMethod.POST) {
            this.outputStream = this.httpSocket.getOutputStream();
            this.outputClosed = true;
            this.connectInputStream();
        } else {
            this.outputStream = new CountingOutputStream(this.httpSocket.getOutputStream());
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
        this.connectTimeout = connectTimeout;
    }

    @Override
    public void setContentDecoded(final boolean b) {
        if (this.convertedInputStream != null) { throw new IllegalStateException("InputStream already in use!"); }
        this.contentDecoded = b;

    }

    public void setReadTimeout(final int readTimeout) {
        try {
            if (this.isConnected()) {
                this.httpSocket.setSoTimeout(readTimeout);
            }
            this.readTimeout = readTimeout;
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    public void setRequestMethod(final RequestMethod method) {
        this.httpMethod = method;
    }

    public void setRequestProperty(final String key, final String value) {
        this.requestProperties.put(key, value);
    }

    protected void shutDownOutput() throws IOException {
        if (this.httpURL.getProtocol().toLowerCase(Locale.ENGLISH).startsWith("https")) {
            /* we cannot close Output on SSL socket */
            return;
        }
        if (this.isConnected() && !this.httpSocket.isOutputShutdown()) {
            this.httpSocket.shutdownOutput();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getRequestInfo());
        sb.append(this.getResponseInfo());
        return sb.toString();
    }

}
