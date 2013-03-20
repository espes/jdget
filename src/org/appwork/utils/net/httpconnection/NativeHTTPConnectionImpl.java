package org.appwork.utils.net.httpconnection;

/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.httpconnection
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.appwork.utils.LowerCaseHashMap;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.net.CountingOutputStream;
import org.appwork.utils.net.NullOutputStream;

/**
 * @author daniel
 * 
 */
public class NativeHTTPConnectionImpl implements HTTPConnection {
    protected URL                                 httpURL              = null;
    protected HTTPProxy                           proxy                = null;
    protected LinkedHashMap<String, String>       requestProperties    = null;
    protected LowerCaseHashMap<List<String>>      headers              = null;
    private HttpURLConnection                     con;
    protected int                                 readTimeout          = 30000;
    protected int                                 connectTimeout       = 30000;
    private int[]                                 allowedResponseCodes = new int[0];
    protected long                                postTodoLength       = -1;
    protected RequestMethod                       httpMethod           = RequestMethod.GET;
    protected OutputStream                        outputStream         = null;
    protected InputStream                         inputStream          = null;
    protected boolean                             inputStreamConnected = false;
    protected boolean                             outputClosed         = false;
    protected int                                 httpResponseCode     = -1;
    protected String                              httpResponseMessage  = "";
    protected String                              customcharset        = null;
    protected long                                requestTime          = -1;
    protected long[]                              ranges;
    private final boolean                         contentDecoded       = false;
    private Proxy                                 nativeProxy;
    private boolean                               connected            = false;
    private boolean                               wasConnected         = false;

    private static WeakHashMap<Thread, HTTPProxy> availableProxies     = new WeakHashMap<Thread, HTTPProxy>();

    static {
        try {
            Authenticator.setDefault(new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    HTTPProxy foundProxy = null;
                    synchronized (NativeHTTPConnectionImpl.availableProxies) {
                        foundProxy = NativeHTTPConnectionImpl.availableProxies.remove(Thread.currentThread());
                    }
                    if (foundProxy != null) {
                        String user = foundProxy.getUser();
                        String pass = foundProxy.getPass();
                        if (user != null || pass != null) {
                            if (StringUtils.isEmpty(user)) {
                                user = "";
                            }
                            if (StringUtils.isEmpty(pass)) {
                                pass = "";
                            }
                            return new PasswordAuthentication(user, pass.toCharArray());
                        }
                    }
                    return null;
                }

            });
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    public NativeHTTPConnectionImpl(final URL url) {
        this.httpURL = url;
        this.proxy = null;
        this.requestProperties = new LinkedHashMap<String, String>();
        this.headers = new LowerCaseHashMap<List<String>>();
    }

    public NativeHTTPConnectionImpl(final URL url, final HTTPProxy p) {
        this.httpURL = url;
        this.proxy = p;
        this.requestProperties = new LinkedHashMap<String, String>();
        this.headers = new LowerCaseHashMap<List<String>>();
        if (this.proxy != null) {
            switch (this.proxy.getType()) {
            case HTTP:
                this.setRequestProperty("Proxy-Connection", "close");
                break;
            }
        }
    }

    @Override
    public void connect() throws IOException {
        if (this.isConnected()) { return;/* oder fehler */
        }
        this.wasConnected = false;
        final long startTime = System.currentTimeMillis();
        if (this.proxy != null) {
            switch (this.proxy.getType()) {
            case HTTP:
                this.nativeProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxy.getHost(), this.proxy.getPort()));
                break;
            case SOCKS4:
            case SOCKS5:
                this.nativeProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(this.proxy.getHost(), this.proxy.getPort()));
                break;
            case DIRECT:
                this.nativeProxy = null;
                break;
            default:
                throw new IOException("Unsupported ProxyType " + this.proxy.getType());
            }
        }
        if (this.nativeProxy != null) {
            synchronized (NativeHTTPConnectionImpl.availableProxies) {
                NativeHTTPConnectionImpl.availableProxies.put(Thread.currentThread(), this.proxy);
            }
            try {
                /** http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6626700 **/
                /**
                 * Request for ability to turn off authentication caching in
                 * HttpURLConnection
                 **/
                sun.net.www.protocol.http.AuthCacheValue.setAuthCache(new sun.net.www.protocol.http.AuthCacheImpl());
            } catch (final Throwable e) {
                /* sun/oracle java only? */
            }
            this.con = (HttpURLConnection) this.httpURL.openConnection(this.nativeProxy);
        } else {
            synchronized (NativeHTTPConnectionImpl.availableProxies) {
                NativeHTTPConnectionImpl.availableProxies.remove(Thread.currentThread());
            }
            this.con = (HttpURLConnection) this.httpURL.openConnection();
        }

        this.con.setConnectTimeout(this.connectTimeout);
        this.con.setReadTimeout(this.readTimeout);
        this.con.setRequestMethod(this.httpMethod.name());
        this.con.setAllowUserInteraction(false);
        this.con.setInstanceFollowRedirects(false);
        if (RequestMethod.POST.equals(this.httpMethod)) {
            this.con.setDoOutput(true);
        } else {
            this.outputClosed = true;
            this.con.setDoOutput(false);
        }
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
            this.con.setRequestProperty(next.getKey(), next.getValue());
        }
        this.con.connect();
        this.connected = true;
        this.wasConnected = true;
        this.requestTime = System.currentTimeMillis() - startTime;
        if (this.httpMethod != RequestMethod.POST) {
            this.outputStream = new NullOutputStream();
            this.outputClosed = true;
            this.connectInputStream();
        } else {
            this.outputStream = new CountingOutputStream(this.con.getOutputStream());
        }
    }

    protected synchronized void connectInputStream() throws IOException {
        if (this.httpMethod == RequestMethod.POST) {
            final long done = ((CountingOutputStream) this.outputStream).transferedBytes();
            if (done != this.postTodoLength) { throw new IOException("Content-Length " + this.postTodoLength + " does not match send " + done + " bytes"); }
        }
        if (this.inputStreamConnected) { return; }
        if (this.httpMethod == RequestMethod.POST) {
            /* flush outputstream in case some buffers are not flushed yet */
            this.outputStream.flush();
        }
        IOException inputException = null;
        try {
            this.inputStream = this.con.getInputStream();
        } catch (final IOException e) {
            inputException = e;
            this.inputStream = this.con.getErrorStream();
        }
        this.inputStreamConnected = true;
        this.httpResponseCode = this.con.getResponseCode();
        this.httpResponseMessage = this.con.getResponseMessage();

        final Iterator<Entry<String, List<String>>> it = this.con.getHeaderFields().entrySet().iterator();
        while (it.hasNext()) {
            final Entry<String, List<String>> next = it.next();
            final String key = next.getKey();
            final List<String> value = next.getValue();
            List<String> list = this.headers.get(key);
            if (list == null) {
                list = new ArrayList<String>();
                this.headers.put(key, list);
            }
            list.addAll(value);
        }
        if (this.inputStream == null && inputException != null) {
            if (this.getContentLength() == 0) {
                this.inputStream = new InputStream() {

                    @Override
                    public int read() throws IOException {
                        return -1;
                    }
                };
            } else {
                /* in case we dont have an error Stream */
                throw inputException;
            }
        }
    }

    @Override
    public void disconnect() {
        this.disconnect(false);
    }

    public void disconnect(final boolean freeConnection) {
        try {
            this.con.disconnect();
        } catch (final Throwable e) {
        } finally {
            this.connected = false;
            if (freeConnection) {
                this.con = null;
            }
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

    @Override
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

    @Override
    public long getContentLength() {
        final String length = this.getHeaderField("Content-Length");
        if (length != null) { return Long.parseLong(length.trim()); }
        return -1;
    }

    @Override
    public String getContentType() {
        final String type = this.getHeaderField("Content-Type");
        if (type == null) { return "unknown"; }
        return type;
    }

    @Override
    public String getHeaderField(final String string) {
        final List<String> ret = this.headers.get(string);
        if (ret == null || ret.size() == 0) { return null; }
        return ret.get(0);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return this.headers;
    }

    @Override
    public List<String> getHeaderFields(final String string) {
        final List<String> ret = this.headers.get(string);
        if (ret == null || ret.size() == 0) { return null; }
        return ret;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        this.connect();
        this.connectInputStream();
        return this.inputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        this.connect();
        if (this.outputClosed) { throw new IOException("OutputStream no longer available"); }
        return this.outputStream;
    }

    @Override
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
            } else {
                /* unknown range header format! */
                System.out.println(contentRange + " format is unknown!");
            }
        }
        return null;
    }

    protected String getRequestInfo() {
        final StringBuilder sb = new StringBuilder();
        sb.append("----------------(Native)Request Information-------------\r\n");
        sb.append("URL: ").append(this.getURL()).append("\r\n");
        sb.append("Host: ").append(this.getURL().getHost()).append("\r\n");

        if (this.nativeProxy != null) {
            final SocketAddress proxyInetSocketAddress = this.nativeProxy.address();
            if (proxyInetSocketAddress != null) {
                sb.append("ProxyIP: ").append(proxyInetSocketAddress).append("\r\n");
            }
        }
        sb.append("Connection-Timeout: ").append(this.connectTimeout + "ms").append("\r\n");
        sb.append("Read-Timeout: ").append(this.readTimeout + "ms").append("\r\n");
        sb.append("----------------(Native)Request-------------------------\r\n");
        if (this.isConnected() || this.wasConnected()) {
            sb.append(this.httpMethod.toString()).append(' ').append(this.getURL().getPath()).append(" HTTP/1.1\r\n");

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

    @Override
    public RequestMethod getRequestMethod() {
        return this.httpMethod;
    }

    @Override
    public Map<String, String> getRequestProperties() {
        return this.requestProperties;
    }

    @Override
    public String getRequestProperty(final String string) {
        return this.requestProperties.get(string);
    }

    @Override
    public long getRequestTime() {
        return this.requestTime;
    }

    @Override
    public int getResponseCode() {
        return this.httpResponseCode;
    }

    protected String getResponseInfo() {
        final StringBuilder sb = new StringBuilder();
        sb.append("----------------Response Information------------\r\n");
        try {
            if (this.isConnected() || this.wasConnected()) {
                sb.append("Connection-Time: ").append(this.requestTime + "ms").append("\r\n");
                sb.append("----------------Response------------------------\r\n");
                this.connectInputStream();
                sb.append(this.getRequestMethod());
                sb.append(" ");
                sb.append(this.con.getURL());
                sb.append(" ");
                sb.append(this.getResponseCode());
                sb.append(" ");
                sb.append(this.getResponseMessage());
                sb.append("\r\n");
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

    @Override
    public String getResponseMessage() {
        return this.httpResponseMessage;
    }

    @Override
    public URL getURL() {
        return this.httpURL;
    }

    @Override
    public boolean isConnected() {
        if (this.con != null && this.connected) { return true; }
        return false;
    }

    @Override
    public boolean isContentDecoded() {
        return this.contentDecoded;
    }

    @Override
    public boolean isContentDisposition() {
        return this.getHeaderField("Content-Disposition") != null;
    }

    @Override
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

    public InetAddress[] resolvHostIP(final String host) throws IOException {
        InetAddress hosts[] = null;
        for (int resolvTry = 0; resolvTry < 2; resolvTry++) {
            try {
                /* resolv all possible ip's */
                hosts = InetAddress.getAllByName(host);
                return hosts;
            } catch (final UnknownHostException e) {
                try {
                    Thread.sleep(500);
                } catch (final InterruptedException e1) {
                    throw new UnknownHostException("Could not resolv " + host);
                }
            }
        }
        throw new UnknownHostException("Could not resolv " + host);
    }

    @Override
    public void setAllowedResponseCodes(final int[] codes) {
        if (codes == null) { throw new IllegalArgumentException("codes==null"); }
        this.allowedResponseCodes = codes;
    }

    @Override
    public void setCharset(final String Charset) {
        this.customcharset = Charset;
    }

    @Override
    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    @Override
    public void setContentDecoded(final boolean b) {
        /* TODO: how to do this with httpurlconnection */
        // if (this.convertedInputStream != null) { throw new
        // IllegalStateException("InputStream already in use!"); }
        // this.contentDecoded = b;
    };

    @Override
    public void setReadTimeout(final int readTimeout) {
        try {
            if (this.isConnected()) {
                this.con.setReadTimeout(readTimeout);
            }
            this.readTimeout = readTimeout;
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setRequestMethod(final RequestMethod method) {
        this.httpMethod = method;
    }

    @Override
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

    /**
     * @return
     */
    private boolean wasConnected() {
        return this.wasConnected;
    }

}
