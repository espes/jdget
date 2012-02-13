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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.appwork.utils.LowerCaseHashMap;
import org.appwork.utils.Regex;
import org.appwork.utils.net.Base64InputStream;
import org.appwork.utils.net.ChunkedInputStream;
import org.appwork.utils.net.CountingOutputStream;

public class HTTPConnectionImpl implements HTTPConnection {

    protected LinkedHashMap<String, String>  requestProperties          = null;
    protected long[]                         ranges;

    protected String                         customcharset              = null;

    protected Socket                         httpSocket                 = null;
    protected URL                            httpURL                    = null;
    protected HTTPProxy                      proxy                      = null;
    protected String                         httpPath                   = null;

    protected RequestMethod                  httpMethod                 = RequestMethod.GET;
    protected LowerCaseHashMap<List<String>> headers                    = null;
    protected int                            httpResponseCode           = -1;
    protected String                         httpResponseMessage        = "";
    protected int                            readTimeout                = 30000;
    protected int                            connectTimeout             = 30000;
    protected long                           requestTime                = -1;
    protected OutputStream                   outputStream               = null;
    protected InputStream                    inputStream                = null;
    protected InputStream                    convertedInputStream       = null;
    protected boolean                        inputStreamConnected       = false;
    protected String                         httpHeader                 = null;

    protected boolean                        outputClosed               = false;
    private boolean                          contentDecoded             = true;
    protected long                           postTodoLength             = -1;
    private int[]                            allowedResponseCodes       = new int[0];
    private InetSocketAddress                proxyInetSocketAddress     = null;
    protected InetSocketAddress              connectedInetSocketAddress = null;

    public HTTPConnectionImpl(final URL url) {
        this(url, null);
    }

    public HTTPConnectionImpl(final URL url, final HTTPProxy p) {
        this.httpURL = url;
        this.proxy = p;
        this.requestProperties = new LinkedHashMap<String, String>();
        this.headers = new LowerCaseHashMap<List<String>>();
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
        InetAddress hosts[] = null;
        try {
            /* resolv all possible ip's */
            hosts = InetAddress.getAllByName(this.httpURL.getHost());
        } catch (final UnknownHostException e) {
            throw e;
        }
        /* try all different ip's until one is valid and connectable */
        IOException ee = null;
        for (final InetAddress host : hosts) {
            if (this.httpURL.getProtocol().startsWith("https")) {
                /* https */
                this.httpSocket = TrustALLSSLFactory.getSSLFactoryTrustALL().createSocket();
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
        this.sendRequest();
    }

    protected synchronized void connectInputStream() throws IOException {
        if (this.httpMethod == RequestMethod.POST) {
            final long done = ((CountingOutputStream) this.outputStream).transferedBytes();
            if (done != this.postTodoLength) { throw new IOException("Content-Length" + this.postTodoLength + " does not match send " + done + " bytes"); }
        }
        if (this.inputStreamConnected) { return; }
        if (this.httpMethod == RequestMethod.POST) {
            /* flush outputstream in case some buffers are not flushed yet */
            this.outputStream.flush();
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
            this.httpHeader = "unknown HTTP response";
            this.httpResponseCode = 200;
            this.httpResponseMessage = "unknown HTTP response";
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
        if (this.isConnected()) {
            try {
                this.httpSocket.close();
            } catch (final Throwable e) {
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
        if (length != null) { return Long.parseLong(length); }
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
        if (code >= 200 && code <= 400 || code == 404 || code == 403 || code == 416 || this.isResponseCodeAllowed(code)) {
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
                    this.convertedInputStream = new java.util.zip.DeflaterInputStream(this.inputStream);
                } else {
                    /* unsupported */
                    throw new UnsupportedOperationException("Encoding " + encoding + " not supported!");
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
        if (this.outputClosed) { throw new IOException("OutputStream no longer available"); }
        return this.outputStream;
    }

    public long[] getRange() {
        String range;
        if (this.ranges != null) { return this.ranges; }
        if ((range = this.getHeaderField("Content-Range")) == null) { return null; }
        // bytes 174239-735270911/735270912
        final String[] ranges = new Regex(range, ".*?(\\d+).*?-.*?(\\d+).*?/.*?(\\d+)").getRow(0);
        if (ranges == null) {
            System.err.print(this + "");
            return null;
        }
        this.ranges = new long[] { Long.parseLong(ranges[0]), Long.parseLong(ranges[1]), Long.parseLong(ranges[2]) };
        return this.ranges;
    }

    protected String getRequestInfo() {
        final StringBuilder sb = new StringBuilder();
        sb.append("-->Host:").append(this.getURL().getHost()).append("\r\n");
        if (this.connectedInetSocketAddress != null && this.connectedInetSocketAddress.getAddress() != null) {
            sb.append("-->HostIP:").append(this.connectedInetSocketAddress.getAddress().getHostAddress()).append("\r\n");
        }
        if (this.proxyInetSocketAddress != null && this.proxyInetSocketAddress.getAddress() != null) {
            sb.append("-->LocalIP:").append(this.proxyInetSocketAddress.getAddress().getHostAddress()).append("\r\n");
        }
        sb.append("----------------Request-------------------------\r\n");

        sb.append(this.httpMethod.toString()).append(' ').append(this.httpPath).append(" HTTP/1.1\r\n");

        for (final String key : this.getRequestProperties().keySet()) {
            final String v = this.getRequestProperties().get(key);
            if (v == null) {
                continue;
            }
            sb.append(key);
            sb.append(": ");
            sb.append(v);
            sb.append("\r\n");
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
        sb.append("----------------Response------------------------\r\n");
        try {
            if (this.isConnected()) {
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
                sb.append("-------------not connected yet------------------");
            }
        } catch (final IOException nothing) {
            sb.append("----------no InputStream available--------------");
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
        if (this.getResponseCode() > -2 && this.getResponseCode() < 400) { return true; }
        return false;
    }

    protected boolean isResponseCodeAllowed(final int code) {
        for (final int c : this.allowedResponseCodes) {
            if (c == code) { return true; }
        }
        return false;
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
            /* host entry does not exist, lets add it as first entry */
            this.addHostHeader();
        }
        for (final String key : this.requestProperties.keySet()) {
            if (this.requestProperties.get(key) == null) {
                continue;
            }
            if ("Content-Length".equalsIgnoreCase(key)) {
                /* content length to check if we send out all data */
                this.postTodoLength = Long.parseLong(this.requestProperties.get(key));
            }
            sb.append(key).append(": ").append(this.requestProperties.get(key)).append("\r\n");
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getRequestInfo());
        sb.append(this.getResponseInfo());
        return sb.toString();
    }

}
