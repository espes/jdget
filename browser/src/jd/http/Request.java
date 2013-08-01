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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import jd.nutils.encoding.Encoding;
import jd.parser.Regex;

import org.appwork.utils.Application;
import org.appwork.utils.StringUtils;
import org.appwork.utils.net.httpconnection.HTTPProxy;

public abstract class Request {
    // public static int MAX_REDIRECTS = 30;

    public static String getCookieString(final Cookies cookies) {
        if (cookies == null || cookies.isEmpty()) { return null; }

        final StringBuilder buffer = new StringBuilder();
        boolean first = true;
        final LinkedList<Cookie> cookies2 = new LinkedList<Cookie>(cookies.getCookies());
        for (final Cookie cookie : cookies2) {
            // Pfade sollten verarbeitet werden...TODO
            if (cookie.isExpired()) {
                continue;
            }

            if (first) {
                first = false;
            } else {
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
        if (query == null) { return null; }
        final LinkedHashMap<String, String> ret = new LinkedHashMap<String, String>();
        if (query.toLowerCase().trim().startsWith("http")) {
            query = new URL(query).getQuery();
        }

        if (query == null) { return ret; }
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
            tmpOut = new ByteArrayOutputStream(16384);
        }
        boolean okay = false;
        /* added "Corrupt GZIP trailer" for CamWinsCom */
        try {
            int len;
            final byte[] buffer = new byte[32767];
            while ((len = is.read(buffer)) != -1) {
                if (len > 0) {
                    if (tmpOut.size() + len > readLimit) { throw new IOException("Content-length too big " + tmpOut.size() + len + " >= " + readLimit); }
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

    private long                   readTime       = -1;
    protected boolean              requested      = false;
    protected int                  readLimit      = 1 * 1024 * 1024;

    protected HTTPProxy            proxy;

    protected String               orgURL;

    protected String               customCharset  = null;

    protected byte[]               byteArray      = null;

    protected boolean              contentDecoded = true;
    protected boolean              keepByteArray  = false;

    public Request(final String url) throws MalformedURLException {
        orgURL = Browser.correctURL(url);
        initDefaultHeader();
        final String basicAuth = Browser.getBasicAuthfromURL(url);
        if (basicAuth != null) {
            getHeaders().put("Authorization", "Basic " + basicAuth);
        }
    }

    public Request(final URLConnectionAdapter con) {
        httpConnection = con;
        collectCookiesFromConnection();
    }

    private void collectCookiesFromConnection() {
        final List<String> cookieHeaders = httpConnection.getHeaderFields("Set-Cookie");
        if (cookieHeaders == null || cookieHeaders.size() == 0) { return; }
        final String date = httpConnection.getHeaderField("Date");
        final String host = Browser.getHost(httpConnection.getURL());
        for (int i = 0; i < cookieHeaders.size(); i++) {
            final String header = cookieHeaders.get(i);
            getCookies().add(Cookies.parseCookies(header, host, date));
        }
    }

    /**
     * DO NEVER call this method directly... use browser.connect
     */
    protected Request connect() throws IOException {
        try {
            openConnection();
            postRequest();
            /*
             * we connect to inputstream to make sure the response headers are getting parsed first
             */
            httpConnection.finalizeConnect();
            try {
                collectCookiesFromConnection();
            } catch (final NullPointerException e) {
                throw new IOException("Malformed url?", e);
            }
        } finally {
            requested = true;
        }
        return this;
    }

    public boolean containsHTML(final String html) throws CharacterCodingException {
        return getHtmlCode() == null ? false : getHtmlCode().contains(html);
    }

    public void disconnect() {
        try {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        } catch (final Throwable e) {
        }
    }

    public String getCharsetFromMetaTags() {
        String parseFrom = null;
        if (htmlCode == null && byteArray != null) {
            parseFrom = new String(byteArray);
        } else if (htmlCode != null) {
            parseFrom = htmlCode;
        }
        if (parseFrom == null) { return null; }
        String charSetMetaTag = new Regex(parseFrom, "http-equiv=\"Content-Type\"[^<>]+content=\"[^\"]+charset=(.*?)\"").getMatch(0);
        if (charSetMetaTag == null) {
            charSetMetaTag = new Regex(parseFrom, "meta charset=\"(.*?)\"").getMatch(0);
        }
        return charSetMetaTag;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public long getContentLength() {
        return httpConnection == null ? -1 : httpConnection.getLongContentLength();
    }

    public Cookies getCookies() {
        if (cookies == null) {
            cookies = new Cookies();
        }
        return cookies;
    }

    public String getCookieString() {
        return Request.getCookieString(cookies);
    }

    public RequestHeader getHeaders() {
        return headers;
    }

    public String getHtmlCode() throws CharacterCodingException {
        if (htmlCode != null) { return htmlCode; }
        String ct = null;
        if (httpConnection != null) {
            ct = httpConnection.getContentType();
        }
        /* check for image content type */
        if (ct != null && Pattern.compile("images?/\\w*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(ct).matches()) { throw new IllegalStateException("Content-Type: " + ct); }
        if (htmlCode == null && byteArray != null) {
            /* use custom charset or charset from httpconnection */
            String useCS = customCharset;
            if (StringUtils.isEmpty(useCS)) {
                useCS = httpConnection.getCharset();
            }
            if (StringUtils.isEmpty(useCS)) {
                useCS = getCharsetFromMetaTags();
            }
            try {
                try {
                    try {
                        if (useCS != null) {
                            /* try to use wanted charset */
                            useCS = useCS.toUpperCase(Locale.ENGLISH);
                            htmlCode = new String(byteArray, useCS);
                            if (!keepByteArray) {
                                byteArray = null;
                            }
                            httpConnection.setCharset(useCS);
                            return htmlCode;
                        }
                    } catch (final Exception e) {
                    }
                    htmlCode = new String(byteArray, "ISO-8859-1");
                    if (!keepByteArray) {
                        byteArray = null;
                    }
                    httpConnection.setCharset("ISO-8859-1");
                    return htmlCode;
                } catch (final Exception e) {
                    System.out.println("could neither charset: " + useCS + " nor default charset");
                    /* fallback to default charset in error case */
                    htmlCode = new String(byteArray);
                    if (!keepByteArray) {
                        byteArray = null;
                    }
                    return htmlCode;
                }
            } catch (final Exception e) {
                /* in case of error we do not reset byteArray */
                httpConnection.setCharset(null);
            }
        }
        return htmlCode;
    }

    protected String getHTMLSource() {
        if (!requested) { return "Request not sent yet"; }
        try {
            getHtmlCode();
            if (htmlCode == null || htmlCode.length() == 0) {
                if (getLocation() != null) { return "Not HTML Code. Redirect to: " + getLocation(); }
                return "No htmlCode read";
            }
        } catch (final Exception e) {
            return "NOTEXT: " + e.getMessage();
        }
        return htmlCode;
    }

    public URLConnectionAdapter getHttpConnection() {
        return httpConnection;
    }

    public String getLocation() {
        if (httpConnection == null) { return null; }
        String red = httpConnection.getHeaderField("Location");
        if (StringUtils.isEmpty(red)) {
            /* check if we have an old-school refresh header */
            red = httpConnection.getHeaderField("refresh");
            if (red != null) {
                // we need to filter the time count from the url
                red = new Regex(red, "url=(.+);?").getMatch(0);
            }
            if (StringUtils.isEmpty(red)) { return null; }
        }
        final String encoding = httpConnection.getHeaderField("Content-Type");
        if (encoding != null && encoding.contains("UTF-8")) {
            red = Encoding.UTF8Decode(red, "ISO-8859-1");
        }
        try {
            new URL(red);
        } catch (final Exception e) {
            String path = getHttpConnection().getURL().getFile();
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
            final int port = getHttpConnection().getURL().getPort();
            final int defaultport = getHttpConnection().getURL().getDefaultPort();
            String proto = "http://";
            if (getHttpConnection().getURL().toString().startsWith("https")) {
                proto = "https://";
            }
            String addPort = "";
            if (defaultport > 0 && port > 0 && defaultport != port) {
                addPort = ":" + port;
            }
            red = proto + getHttpConnection().getURL().getHost() + addPort + (red.charAt(0) == '/' ? red : path + "/" + red);
        }
        return Browser.correctURL(Encoding.urlEncode_light(red));

    }

    public HTTPProxy getProxy() {
        return proxy;
    }

    public int getReadLimit() {
        return readLimit;
    }

    public long getReadTime() {
        return readTime;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public long getRequestTime() {
        return httpConnection == null ? -1 : httpConnection.getRequestTime();
    }

    /**
     * @return the byteArray
     */
    public byte[] getResponseBytes() {
        return byteArray;
    }

    public String getResponseHeader(final String key) {
        return httpConnection == null ? null : httpConnection.getHeaderField(key);
    }

    public Map<String, List<String>> getResponseHeaders() {
        return httpConnection == null ? null : httpConnection.getHeaderFields();
    }

    /**
     * Will replace #getHtmlCode() with next release
     */
    public String getResponseText() throws CharacterCodingException {
        return getHtmlCode();
    }

    public String getUrl() {
        return orgURL;
    }

    protected boolean hasCookies() {
        return cookies != null && !cookies.isEmpty();
    }

    protected void initDefaultHeader() {
        headers = new RequestHeader();
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
    }

    public boolean isContentDecoded() {
        return httpConnection == null ? contentDecoded : httpConnection.isContentDecoded();
    }

    public boolean isKeepByteArray() {
        return keepByteArray;
    }

    public boolean isRequested() {
        return requested;
    }

    private void openConnection() throws IOException {
        httpConnection = HTTPConnectionFactory.createHTTPConnection(new URL(orgURL), proxy);
        httpConnection.setRequest(this);
        httpConnection.setReadTimeout(readTimeout);
        httpConnection.setConnectTimeout(connectTimeout);
        httpConnection.setContentDecoded(contentDecoded);

        if (headers != null) {
            final int headersSize = headers.size();
            for (int i = 0; i < headersSize; i++) {
                httpConnection.setRequestProperty(headers.getKey(i), headers.getValue(i));
            }
        }
        preRequest();
        if (hasCookies()) {
            final String cookieString = this.getCookieString();
            if (cookieString != null) {
                httpConnection.setRequestProperty("Cookie", cookieString);
            }
        }
    }

    abstract public long postRequest() throws IOException;

    abstract public void preRequest() throws IOException;

    public String printHeaders() {
        if (httpConnection == null) { return null; }
        return httpConnection.toString();
    }

    public Request read(final boolean keepByteArray) throws IOException {
        this.keepByteArray = keepByteArray;
        final long tima = System.currentTimeMillis();
        httpConnection.setCharset(customCharset);
        byteArray = Request.read(httpConnection, getReadLimit());
        readTime = System.currentTimeMillis() - tima;
        return this;
    }

    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setContentDecoded(final boolean c) {
        contentDecoded = c;
    }

    public void setCookies(final Cookies cookies) {
        this.cookies = cookies;
    }

    public void setCustomCharset(final String charset) {
        customCharset = charset;
    }

    /**
     * DO NOT USE in 09581 Stable
     */
    public void setHeaders(final RequestHeader headers) {
        this.headers = headers;
    }

    public void setHtmlCode(final String htmlCode) {
        byteArray = null;
        this.htmlCode = htmlCode;
        requested = true;
    }

    public void setProxy(final HTTPProxy proxy) {
        this.proxy = proxy;
    }

    public void setReadLimit(final int readLimit) {
        this.readLimit = Math.max(0, readLimit);
    }

    public void setReadTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
        final URLConnectionAdapter con = httpConnection;
        if (con != null) {
            con.setReadTimeout(readTimeout);
        }
    }

    @Override
    public String toString() {
        if (!requested) { return "Request not sent yet"; }
        final StringBuilder sb = new StringBuilder();
        try {
            if (httpConnection != null) {
                sb.append(httpConnection.toString());
                sb.append("\r\n");
                getHtmlCode();
                sb.append(getHTMLSource());
            } else {
                return getHTMLSource();
            }
        } catch (final Exception e) {
            return "NOTEXT: " + e.getMessage();
        }
        return sb.toString();
    }

}
