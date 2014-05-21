//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.http;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.CharacterCodingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import jd.nutils.encoding.Encoding;
import jd.parser.Regex;

import org.appwork.exceptions.WTFException;
import org.appwork.utils.Application;
import org.appwork.utils.StringUtils;
import org.appwork.utils.net.HTTPHeader;
import org.appwork.utils.net.httpconnection.HTTPProxy;

public abstract class Request {
    // public static int MAX_REDIRECTS = 30;

    public static String getCookieString(final Cookies cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return null;
        }
        final StringBuilder buffer = new StringBuilder();
        for (final Cookie cookie : cookies.getCookies()) {
            // Pfade sollten verarbeitet werden...TODO
            if (cookie.isExpired()) {
                continue;
            }
            if (buffer.length() > 0) {
                buffer.append("; ");
            }
            buffer.append(cookie.getKey());
            buffer.append("=");
            buffer.append(cookie.getValue());
        }
        return buffer.toString();
    }

    /**
     * Gibt eine Hashmap mit allen key:value pairs im query zurück
     * 
     * @param query
     *            kann ein reines query ein (&key=value) oder eine url mit query
     * @return
     * @throws MalformedURLException
     */

    public static LinkedHashMap<String, String> parseQuery(String query) throws MalformedURLException {
        if (query == null) {
            return null;
        }
        final LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
        if (query.toLowerCase().trim().startsWith("http")) {
            query = new URL(query).getQuery();
        }

        if (query == null) {
            return ret;
        }
        final String[][] split = new Regex(query.trim(), "&?(.*?)=(.*?)($|&(?=.*?=.+))").getMatches();
        if (split != null) {
            final int splitLength = split.length;
            for (int i = 0; i < splitLength; i++) {
                ret.put(split[i][0], split[i][1]);
            }
        }
        return ret;
    }

    public static byte[] read(final URLConnectionAdapter con, int readLimit) throws IOException {
        readLimit = Math.max(0, readLimit);
        final InputStream is = con.getInputStream();
        byte[] ret = null;
        if (is == null) {
            // TODO: check if we have to close con here
            return null;
        }
        ByteArrayOutputStream tmpOut = null;
        final long contentLength = con.getLongContentLength();
        if (contentLength != -1) {
            final int length = contentLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) contentLength;
            tmpOut = new ByteArrayOutputStream(length);
        } else {
            tmpOut = new ByteArrayOutputStream(32767);
        }
        boolean okay = false;
        /* added "Corrupt GZIP trailer" for CamWinsCom */
        try {
            int len;
            final byte[] buffer = new byte[32767];
            while ((len = is.read(buffer)) != -1) {
                if (len > 0) {
                    if (tmpOut.size() + len > readLimit) {
                        throw new IOException("Content-length too big " + tmpOut.size() + len + " >= " + readLimit);
                    }
                    tmpOut.write(buffer, 0, len);
                }
            }
            okay = true;
        } catch (final EOFException e) {
            e.printStackTrace();
            okay = true;
        } catch (final IOException e) {
            if (e.toString().contains("end of ZLIB") || e.toString().contains("Premature") || e.toString().contains("Corrupt GZIP trailer")) {
                System.out.println("Try workaround for " + e);
                okay = true;
            } else {
                throw e;
            }
        } finally {
            try {
                is.close();
            } catch (final Exception e) {
            }
            try {
                /* disconnect connection */
                con.disconnect();
            } catch (final Exception e) {
            }
            if (okay) {
                ret = tmpOut.toByteArray();
            }
        }
        return ret;
    }

    /*
     * default timeouts, because 0 is infinite and BAD, if we need 0 then we have to set it manually
     */
    protected int                  connectTimeout = 30000;
    protected int                  readTimeout    = 60000;
    protected Cookies              cookies        = null;
    protected RequestHeader        headers;

    protected String               htmlCode;

    protected URLConnectionAdapter httpConnection;
    protected long                 readTime       = -1;

    protected boolean              requested      = false;
    protected int                  readLimit      = 1 * 1024 * 1024;

    protected HTTPProxy            proxy;

    protected String               orgURL;

    protected String               customCharset  = null;

    protected byte[]               byteArray      = null;

    protected boolean              contentDecoded = true;

    protected boolean              keepByteArray  = false;

    protected Request(final Request cloneRequest) {
        this.orgURL = cloneRequest.getUrl();
        this.setCustomCharset(cloneRequest.getCustomCharset());
        this.setReadTimeout(cloneRequest.getReadTimeout());
        this.setConnectTimeout(cloneRequest.getConnectTimeout());
        if (cloneRequest.hasCookies()) {
            this.setCookies(new Cookies(cloneRequest.getCookies()));
        }
        this.setReadLimit(cloneRequest.getReadLimit());
        this.setProxy(cloneRequest.getProxy());
        this.setContentDecoded(cloneRequest.isContentDecodedSet());
        if (cloneRequest.getHeaders() != null) {
            this.setHeaders(new RequestHeader(cloneRequest.getHeaders()));
        }
    }

    public Request(final String url) throws MalformedURLException {
        this.setURL(Browser.correctURL(url));
        this.setHeaders(this.getDefaultRequestHeader());
        final String basicAuth = Browser.getBasicAuthfromURL(url);
        if (basicAuth != null) {
            this.getHeaders().put("Authorization", "Basic " + basicAuth);
        }
    }

    public Request(final URLConnectionAdapter con) {
        this.httpConnection = con;
        this.requested = true;
        this.collectCookiesFromConnection();
    }

    public Request cloneRequest() {
        throw new WTFException("Not Implemented");
    }

    private void collectCookiesFromConnection() {
        final List<String> cookieHeaders = this.httpConnection.getHeaderFields("Set-Cookie");
        if (cookieHeaders == null || cookieHeaders.size() == 0) {
            return;
        }
        final String date = this.httpConnection.getHeaderField("Date");
        final String host = Browser.getHost(this.httpConnection.getURL());
        for (int i = 0; i < cookieHeaders.size(); i++) {
            final String header = cookieHeaders.get(i);
            this.getCookies().add(Cookies.parseCookies(header, host, date));
        }
    }

    /**
     * DO NEVER call this method directly... use browser.connect
     */
    protected Request connect() throws IOException {
        try {
            this.openConnection();
            this.postRequest();
            try {
                this.httpConnection.finalizeConnect();
            } finally {
                try {
                    this.collectCookiesFromConnection();
                } catch (final NullPointerException e) {
                    throw new IOException("Malformed url?", e);
                }
            }
        } finally {
            this.requested = true;
        }
        return this;
    }

    public boolean containsHTML(final String html) throws CharacterCodingException {
        return this.getHtmlCode() == null ? false : this.getHtmlCode().contains(html);
    }

    public void disconnect() {
        try {
            if (this.httpConnection != null) {
                this.httpConnection.disconnect();
            }
        } catch (final Throwable e) {
        }
    }

    public String getCharsetFromMetaTags() {
        String parseFrom = null;
        if (this.htmlCode == null && this.byteArray != null) {
            parseFrom = new String(this.byteArray);
        } else if (this.htmlCode != null) {
            parseFrom = this.htmlCode;
        }
        if (parseFrom == null) {
            return null;
        }
        String charSetMetaTag = new Regex(parseFrom, "http-equiv=\"Content-Type\"[^<>]+content=\"[^\"]+charset=(.*?)\"").getMatch(0);
        if (charSetMetaTag == null) {
            charSetMetaTag = new Regex(parseFrom, "meta charset=\"(.*?)\"").getMatch(0);
        }
        return charSetMetaTag;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public long getContentLength() {
        return this.httpConnection == null ? -1 : this.httpConnection.getLongContentLength();
    }

    public Cookies getCookies() {
        if (this.cookies == null) {
            this.cookies = new Cookies();
        }
        return this.cookies;
    }

    public String getCookieString() {
        return Request.getCookieString(this.cookies);
    }

    public String getCustomCharset() {
        return this.customCharset;
    }

    protected RequestHeader getDefaultRequestHeader() {
        final RequestHeader headers = new RequestHeader();
        headers.put("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.10) Gecko/2009042523 Ubuntu/9.04 (jaunty) Firefox/3.0.10");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.put("Accept-Language", "de, en-gb;q=0.9, en;q=0.8");

        if (Application.getJavaVersion() >= Application.JAVA16) {
            /* deflate only java >=1.6 */
            headers.put("Accept-Encoding", "gzip,deflate");
        } else {
            headers.put("Accept-Encoding", "gzip");
        }
        headers.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");

        headers.put("Cache-Control", "no-cache");
        headers.put("Pragma", "no-cache");
        headers.put("Connection", "close");
        return headers;
    }

    public RequestHeader getHeaders() {
        return this.headers;
    }

    public String getHtmlCode() throws CharacterCodingException {
        if (this.htmlCode != null) {
            return this.htmlCode;
        }
        String ct = null;
        if (this.httpConnection != null) {
            ct = this.httpConnection.getContentType();
        }
        final boolean keepBytes = this.isKeepByteArray();
        /* check for image content type */
        if (ct != null && Pattern.compile("images?/\\w*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(ct).matches()) {
            throw new IllegalStateException("Content-Type: " + ct);
        }
        if (this.htmlCode == null && this.byteArray != null) {
            /* use custom charset or charset from httpconnection */
            String useCS = this.customCharset;
            if (StringUtils.isEmpty(useCS)) {
                useCS = this.httpConnection.getCharset();
            }
            if (StringUtils.isEmpty(useCS)) {
                useCS = this.getCharsetFromMetaTags();
            }
            try {
                try {
                    try {
                        if (useCS != null) {
                            /* try to use wanted charset */
                            useCS = useCS.toUpperCase(Locale.ENGLISH);
                            this.htmlCode = new String(this.byteArray, useCS);
                            if (!keepBytes) {
                                this.byteArray = null;
                            }
                            this.httpConnection.setCharset(useCS);
                            return this.htmlCode;
                        }
                    } catch (final Exception e) {
                    }
                    this.htmlCode = new String(this.byteArray, "ISO-8859-1");
                    if (!keepBytes) {
                        this.byteArray = null;
                    }
                    this.httpConnection.setCharset("ISO-8859-1");
                    return this.htmlCode;
                } catch (final Exception e) {
                    System.out.println("could neither charset: " + useCS + " nor default charset");
                    /* fallback to default charset in error case */
                    this.htmlCode = new String(this.byteArray);
                    if (!keepBytes) {
                        this.byteArray = null;
                    }
                    return this.htmlCode;
                }
            } catch (final Exception e) {
                /* in case of error we do not reset byteArray */
                this.httpConnection.setCharset(null);
            }
        }
        return this.htmlCode;
    }

    protected String getHTMLSource() {
        if (!this.requested) {
            return "Request not sent yet";
        }
        try {
            this.getHtmlCode();
            if (StringUtils.isEmpty(this.htmlCode)) {
                final String location = this.getLocation();
                if (location != null) {
                    return "Not HTML Code. Redirect to: " + location;
                }
                return "No htmlCode read";
            }
        } catch (final Throwable e) {
            return "NOTEXT: " + e.getMessage();
        }
        return this.htmlCode;
    }

    public URLConnectionAdapter getHttpConnection() {
        return this.httpConnection;
    }

    public String getLocation() {
        if (this.httpConnection == null) {
            return null;
        }
        String red = this.httpConnection.getHeaderField("Location");
        if (StringUtils.isEmpty(red)) {
            /* check if we have an old-school refresh header */
            red = this.httpConnection.getHeaderField("refresh");
            if (red != null) {
                // we need to filter the time count from the url
                red = new Regex(red, "url=(.+);?").getMatch(0);
            }
            if (StringUtils.isEmpty(red)) {
                return null;
            }
        }
        final String encoding = this.httpConnection.getHeaderField("Content-Type");
        if (encoding != null && encoding.contains("UTF-8")) {
            red = Encoding.UTF8Decode(red, "ISO-8859-1");
        }
        try {
            new URL(red);
        } catch (final Exception e) {
            String path = this.getHttpConnection().getURL().getFile();
            if (!path.endsWith("/")) {
                /*
                 * path does not end with / we have to find latest valid path
                 * 
                 * \/test.rar should result in empty path
                 * 
                 * \/test/test.rar should result in \/test/
                 */
                final String validPath = new Regex(path, "(/.*?/.*?)(\\?|$)").getMatch(0);
                if (validPath != null && validPath.length() > 0) {
                    path = validPath;
                } else {
                    path = "";
                }
            }
            final int port = this.getHttpConnection().getURL().getPort();
            final int defaultport = this.getHttpConnection().getURL().getDefaultPort();
            String proto = "http://";
            if (this.getHttpConnection().getURL().toString().startsWith("https")) {
                proto = "https://";
            }
            String addPort = "";
            if (defaultport > 0 && port > 0 && defaultport != port) {
                addPort = ":" + port;
            }
            red = proto + this.getHttpConnection().getURL().getHost() + addPort + (red.charAt(0) == '/' ? red : path + "/" + red);
        }
        return Browser.correctURL(Encoding.urlEncode_light(red));

    }

    public HTTPProxy getProxy() {
        return this.proxy;
    }

    public int getReadLimit() {
        return this.readLimit;
    }

    public long getReadTime() {
        return this.readTime;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public long getRequestTime() {
        return this.httpConnection == null ? -1 : this.httpConnection.getRequestTime();
    }

    /**
     * @return the byteArray
     */
    public byte[] getResponseBytes() {
        return this.byteArray;
    }

    public String getResponseHeader(final String key) {
        return this.httpConnection == null ? null : this.httpConnection.getHeaderField(key);
    }

    public Map<String, List<String>> getResponseHeaders() {
        return this.httpConnection == null ? null : this.httpConnection.getHeaderFields();
    }

    /**
     * Will replace #getHtmlCode() with next release
     */
    public String getResponseText() throws CharacterCodingException {
        return this.getHtmlCode();
    }

    public String getUrl() {
        return this.orgURL;
    }

    protected boolean hasCookies() {
        return this.cookies != null && !this.cookies.isEmpty();
    }

    public boolean isContentDecoded() {
        return this.httpConnection == null ? this.isContentDecodedSet() : this.httpConnection.isContentDecoded();
    }

    public boolean isContentDecodedSet() {
        return this.contentDecoded;
    }

    public boolean isKeepByteArray() {
        return this.keepByteArray;
    }

    public boolean isRequested() {
        return this.requested;
    }

    private void openConnection() throws IOException {
        this.httpConnection = HTTPConnectionFactory.createHTTPConnection(new URL(this.getUrl()), this.getProxy());
        this.httpConnection.setRequest(this);
        this.httpConnection.setReadTimeout(this.getReadTimeout());
        this.httpConnection.setConnectTimeout(this.getConnectTimeout());
        this.httpConnection.setContentDecoded(this.isContentDecodedSet());

        final RequestHeader headers = this.getHeaders();
        if (headers != null) {
            for (final HTTPHeader header : headers) {
                if (StringUtils.isEmpty(header.getValue())) {
                    continue;
                }
                this.httpConnection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        this.preRequest();
        if (this.hasCookies()) {
            final String cookieString = this.getCookieString();
            if (StringUtils.isNotEmpty(cookieString)) {
                this.httpConnection.setRequestProperty("Cookie", cookieString);
            }
        }
    }

    abstract public long postRequest() throws IOException;

    abstract public void preRequest() throws IOException;

    public String printHeaders() {
        if (this.httpConnection == null) {
            return null;
        }
        return this.httpConnection.toString();
    }

    public Request read(final boolean keepByteArray) throws IOException {
        this.keepByteArray = keepByteArray;
        final long tima = System.currentTimeMillis();
        this.httpConnection.setCharset(this.getCustomCharset());
        this.byteArray = Request.read(this.getHttpConnection(), this.getReadLimit());
        this.readTime = System.currentTimeMillis() - tima;
        return this;
    }

    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setContentDecoded(final boolean c) {
        this.contentDecoded = c;
    }

    public void setCookies(final Cookies cookies) {
        this.cookies = cookies;
    }

    public void setCustomCharset(final String charset) {
        this.customCharset = charset;
    }

    /**
     * DO NOT USE in 09581 Stable
     */
    public void setHeaders(final RequestHeader headers) {
        this.headers = headers;
    }

    public void setHtmlCode(final String htmlCode) {
        this.byteArray = null;
        this.htmlCode = htmlCode;
        this.requested = true;
    }

    public void setProxy(final HTTPProxy proxy) {
        if (proxy instanceof ClonedProxy) {
            this.proxy = proxy;
        } else {
            this.proxy = new ClonedProxy(proxy);
        }
    }

    public void setProxy(final ClonedProxy proxy) {
        this.proxy = proxy;
    }

    public void setReadLimit(final int readLimit) {
        this.readLimit = Math.max(0, readLimit);
    }

    public void setReadTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
        final URLConnectionAdapter con = this.httpConnection;
        if (con != null) {
            con.setReadTimeout(readTimeout);
        }
    }

    public void setURL(final String url) {
        this.orgURL = url;
    }

    @Override
    public String toString() {
        if (!this.requested) {
            return "Request not sent yet";
        }
        final StringBuilder sb = new StringBuilder();
        try {
            if (this.httpConnection != null) {
                sb.append(this.httpConnection.toString());
                sb.append("\r\n");
                this.getHtmlCode();
                sb.append(this.getHTMLSource());
            } else {
                return this.getHTMLSource();
            }
        } catch (final Exception e) {
            return "NOTEXT: " + e.getMessage();
        }
        return sb.toString();
    }

}
