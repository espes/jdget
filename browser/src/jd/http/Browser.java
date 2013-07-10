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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jd.http.requests.FormData;
import jd.http.requests.GetRequest;
import jd.http.requests.PostFormDataRequest;
import jd.http.requests.PostRequest;
import jd.http.requests.RequestVariable;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.parser.html.InputField;

import org.appwork.utils.logging2.LogSource;
import org.appwork.utils.net.httpconnection.HTTPProxy;

public class Browser {
    // we need this class in here due to jdownloader stable 0.9 compatibility
    public class BrowserException extends IOException {

        private static final long    serialVersionUID = 1509988898224037320L;
        private URLConnectionAdapter connection;
        private Exception            e                = null;

        public BrowserException(final String string) {
            super(string);
        }

        public BrowserException(final String string, final Exception e) {
            this(string);
            this.e = e;
        }

        public BrowserException(final String message, final URLConnectionAdapter con) {
            this(message);
            this.connection = con;
        }

        public BrowserException(final String message, final URLConnectionAdapter con, final Exception e) {
            this(message, con);
            this.e = e;
        }

        /**
         * Returns the connection adapter that caused the browserexception
         * 
         * @return
         */
        public URLConnectionAdapter getConnection() {
            return this.connection;
        }

        public Exception getException() {
            return this.e;
        }

    }

    private static final HashMap<String, Cookies> COOKIES         = new HashMap<String, Cookies>();
    private static HTTPProxy                      GLOBAL_PROXY    = null;
    private static Logger                         LOGGER          = null;

    // added proxy map to find proxy passwords.

    private static HashMap<String, Integer>       REQUEST_INTERVAL_LIMIT_MAP;

    private static HashMap<String, Long>          REQUESTTIME_MAP;

    private static int                            TIMEOUT_CONNECT = 30000;

    private static int                            TIMEOUT_READ    = 30000;

    public static HTTPProxy _getGlobalProxy() {
        return Browser.GLOBAL_PROXY;
    }

    public static int getGlobalReadTimeout() {
        return Browser.TIMEOUT_READ;
    }

    public static String getHost(final String url) {
        return Browser.getHost(url, false);
    }

    public static String getHost(final String url, final boolean includeSubDomains) {
        if (url == null) { return null; }
        /* direct ip */
        String ret = new Regex(url, "(^[a-z0-9]+://|^)(\\d+\\.\\d+\\.\\d+\\.\\d+)(/|$|:)").getMatch(1);
        if (ret != null) { return ret; }
        /* normal url */
        if (includeSubDomains) {
            ret = new Regex(url, "^[a-z0-9]+://(.*?@)?(.*?)(/|$|:)").getMatch(1);
        }
        if (ret == null) {
            ret = new Regex(url, ".*?([^.:/]+\\.[^.:/]+)(/|$|:)").getMatch(0);
        }
        if (ret != null) { return ret.toLowerCase(Locale.ENGLISH); }
        return url;
    }

    /**
     * Returns the host for url. input: http://srv2.bluehost.to/dsdsf ->out bluehost.to
     * 
     * @param url
     * @return
     * @throws MalformedURLException
     */

    public static String getHost(final URL url) {
        return Browser.getHost(url.getHost());
    }

    /**
     * Sets the global connect timeout
     * 
     * @param valueMS
     */
    public static void setGlobalConnectTimeout(final int valueMS) {
        Browser.TIMEOUT_CONNECT = valueMS;
    }

    public static void setGlobalLogger(final Logger logger) {
        Browser.LOGGER = logger;
    }

    public static void setGlobalProxy(final HTTPProxy p) {
        Browser.GLOBAL_PROXY = p;
    }

    /**
     * Sets the global readtimeout in ms
     * 
     * @param valueMS
     */
    public static void setGlobalReadTimeout(final int valueMS) {
        Browser.TIMEOUT_READ = valueMS;
    }

    public static void setGlobalVerbose(final boolean b) {
        Browser.VERBOSE = b;
    }

    public static synchronized void setRequestIntervalLimitGlobal(final String host, final int i) {
        final String domain = Browser.getHost(host);
        if (domain == null) { return; }
        if (Browser.REQUEST_INTERVAL_LIMIT_MAP == null) {
            Browser.REQUEST_INTERVAL_LIMIT_MAP = new HashMap<String, Integer>();
            Browser.REQUESTTIME_MAP = new HashMap<String, Long>();
        }
        Browser.REQUEST_INTERVAL_LIMIT_MAP.put(domain, i);
    }

    private static synchronized void waitForPageAccess(final Browser browser, final Request request) throws InterruptedException {
        final String host = Browser.getHost(request.getUrl());
        try {
            Integer localLimit = null;
            Integer globalLimit = null;
            Long localLastRequest = null;
            Long globalLastRequest = null;

            if (browser.requestIntervalLimitMap != null) {
                localLimit = browser.requestIntervalLimitMap.get(host);
                localLastRequest = browser.requestTimeMap.get(host);
            }
            if (Browser.REQUEST_INTERVAL_LIMIT_MAP != null) {
                globalLimit = Browser.REQUEST_INTERVAL_LIMIT_MAP.get(host);
                globalLastRequest = Browser.REQUESTTIME_MAP.get(host);
            }

            if (localLimit == null && globalLimit == null) { return; }
            if (localLastRequest == null && globalLastRequest == null) { return; }
            if (localLimit != null && localLastRequest == null) { return; }
            if (globalLimit != null && globalLastRequest == null) { return; }

            if (globalLimit == null) {
                globalLimit = 0;
            }
            if (localLimit == null) {
                localLimit = 0;
            }
            if (localLastRequest == null) {
                localLastRequest = System.currentTimeMillis();
            }
            if (globalLastRequest == null) {
                globalLastRequest = System.currentTimeMillis();
            }
            final long dif = Math.max(localLimit - (System.currentTimeMillis() - localLastRequest), globalLimit - (System.currentTimeMillis() - globalLastRequest));

            if (dif > 0) {
                // System.out.println("Sleep " + dif + " before connect to " +
                // request.getUrl().getHost());
                Thread.sleep(dif);
                // waitForPageAccess(request);
            }
        } finally {
            if (browser.requestTimeMap != null) {
                browser.requestTimeMap.put(host, System.currentTimeMillis());
            }
            if (Browser.REQUESTTIME_MAP != null) {
                Browser.REQUESTTIME_MAP.put(host, System.currentTimeMillis());
            }
        }
    }

    private boolean        keepResponseContentBytes = false;

    private int[]          allowedResponseCodes     = new int[0];

    private static boolean VERBOSE                  = false;

    public static String correctURL(final String url) {
        return Browser.correctURL(url, false);
    }

    /**
     * Returns a corrected url, where multiple / and ../. are removed
     * 
     * @param url
     * @return
     */
    public static String correctURL(String url, final boolean replaceDoubleSlash) {
        if (url == null) { return url; }
        /* check if we need to correct url */
        int begin = url.indexOf("://");
        if (begin > 0 && url.indexOf("/", begin + 3) < 0) {
            /* check for missing first / in url */
            url = url + "/";
        }
        if (begin > 0 && !url.substring(begin + 3).contains("//") && !url.contains("./")) { return url; }
        String ret = url;
        String end = null;
        String tmp = null;
        boolean endisslash = false;
        if (url.startsWith("http://")) {
            begin = 8;
        } else if (url.startsWith("https://")) {
            begin = 9;
        } else {
            begin = 0;
        }
        final int first = url.indexOf("/", begin);
        if (first < 0) { return ret; }
        ret = url.substring(0, first);
        final int endp = url.indexOf("?", first);
        if (endp > 0) {
            end = url.substring(endp);
            tmp = url.substring(first, endp);
        } else {
            tmp = url.substring(first);
        }
        /* is the end of url a / */
        endisslash = tmp.endsWith("/");

        /* filter multiple / */
        /*
         * NOTE: http://webmasters.stackexchange.com/questions/8354/what-does-the-double-slash-mean-in-urls
         * 
         * http://svn.jdownloader.org/issues/5610
         */
        if (replaceDoubleSlash) {
            tmp = tmp.replaceAll("/{2,}", "/");
        } else {
            tmp = tmp.replaceAll("/{3,}", "/");
        }

        /* filter .. and . */
        final String parts[] = tmp.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equalsIgnoreCase(".")) {
                parts[i] = "";
            } else if (parts[i].equalsIgnoreCase("..")) {
                if (i > 0) {
                    int j = i - 1;
                    while (true && j > 0) {
                        if (parts[j].length() > 0) {
                            parts[j] = "";
                            break;
                        }
                        j--;
                    }
                }
                parts[i] = "";
            } else if (i > 0 && parts[i].length() == 0) {
                parts[i] = "/";
            }
        }
        tmp = "";
        for (final String part : parts) {
            if (part.length() > 0) {
                if ("/".equals(part)) {
                    tmp = tmp + "/";
                } else {
                    tmp = tmp + "/" + part;
                }
            }
        }
        if (endisslash) {
            tmp = tmp + "/";
        }
        return ret + tmp + (end != null ? end : "");
    }

    /**
     * Downloads url to file.
     * 
     * @param file
     * @param urlString
     * @return Erfolg true/false
     * @throws IOException
     */
    public static void download(final File file, final String url) throws IOException {
        new Browser().getDownload(file, url);
    }

    /**
     * Lädt über eine URLConnection eine Datei herunter. Zieldatei ist file.
     * 
     * @param file
     * @param con
     * @return Erfolg true/false
     * @throws IOException
     */
    public static void download(final File file, final URLConnectionAdapter con) throws IOException {
        if (file.isFile()) {
            if (!file.delete()) {
                System.out.println("Konnte Datei nicht löschen " + file);
                throw new IOException("Could not overwrite file: " + file);
            }
        }

        final File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        file.createNewFile();
        FileOutputStream fos = null;
        InputStream input = null;
        boolean okay = false;
        try {
            fos = new FileOutputStream(file, false);
            input = con.getInputStream();
            final byte[] b = new byte[32767];
            int len;
            while ((len = input.read(b)) != -1) {
                fos.write(b, 0, len);
            }
            okay = true;
        } finally {
            try {
                input.close();
            } catch (final Throwable e) {
            }
            try {
                fos.close();
            } catch (final Throwable e) {
            }
            if (okay == false) {
                file.delete();
            }
        }
    }

    public static String getBasicAuthfromURL(final String url) {
        if (url == null) { return null; }
        final String basicauth = new Regex(url, "http.*?/([^/]{1}.*?)@").getMatch(0);
        if (basicauth != null && basicauth.contains(":")) { return Encoding.Base64Encode(basicauth); }
        return null;
    }

    public static int getGlobalConnectTimeout() {
        return Browser.TIMEOUT_CONNECT;
    }

    public static Logger getGlobalLogger() {
        // TODO Auto-generated method stub
        return Browser.LOGGER;
    }

    private String                   acceptLanguage      = "de, en-gb;q=0.9, en;q=0.8";

    /*
     * -1 means use default Timeouts
     * 
     * 0 means infinite (DO NOT USE if not needed)
     */
    private int                      connectTimeout      = -1;

    private HashMap<String, Cookies> cookies             = new HashMap<String, Cookies>();

    private boolean                  cookiesExclusive    = true;

    private String                   currentURL          = null;

    private String                   customCharset       = null;

    private boolean                  debug               = false;
    private boolean                  doRedirects         = false;
    private RequestHeader            headers;
    private int                      limit               = 1 * 1024 * 1024;
    private Logger                   logger              = null;
    private HTTPProxy                proxy;
    private int                      readTimeout         = -1;
    private int                      redirectLoopCounter = 0;
    private Request                  request;
    private HashMap<String, Integer> requestIntervalLimitMap;
    private HashMap<String, Long>    requestTimeMap;

    private boolean                  verbose             = false;

    public Browser() {
        final Thread currentThread = Thread.currentThread();
        /**
         * use BrowserSettings from current thread if available
         */
        if (currentThread != null && currentThread instanceof BrowserSettings) {
            final BrowserSettings settings = (BrowserSettings) currentThread;
            this.proxy = settings.getCurrentProxy();
            this.debug = settings.isDebug();
            this.verbose = settings.isVerbose();
            this.logger = settings.getLogger();
        }
    }

    /**
     * Assures that the browser does not download any binary files in textmode
     * 
     * @param request
     * @throws BrowserException
     */
    private void checkContentLengthLimit(final Request request) throws BrowserException {
        long length = -1;
        if (request == null || request.getHttpConnection() == null || (length = request.getHttpConnection().getLongContentLength()) < 0) {
            return;
        } else if (length > this.limit) {
            final Logger llogger = this.getLogger();
            if (llogger != null) {
                llogger.severe(request.printHeaders());
            }
            throw new BrowserException("Content-length too big", request.getHttpConnection());
        }
    }

    /**
     * Clears all cookies for the given URL. URL has to be a valid. if (url == null), all cookies are cleared.
     * 
     * @param url
     */
    public void clearCookies(final String url) {
        if (url == null) {
            this.cookies.clear();
        }
        final String host = Browser.getHost(url);
        final Iterator<String> it = this.getCookies().keySet().iterator();
        String check = null;
        while (it.hasNext()) {
            check = it.next();
            if (check.contains(host)) {
                this.cookies.get(check).clear();
                break;
            }
        }
    }

    public Browser cloneBrowser() {
        final Browser br = new Browser();
        br.requestIntervalLimitMap = this.requestIntervalLimitMap;
        br.requestTimeMap = this.requestTimeMap;
        br.acceptLanguage = this.acceptLanguage;
        br.connectTimeout = this.connectTimeout;
        br.currentURL = this.currentURL;
        br.doRedirects = this.doRedirects;
        br.setCustomCharset(this.customCharset);
        br.getHeaders().putAll(this.getHeaders());
        br.limit = this.limit;
        br.readTimeout = this.readTimeout;
        br.request = this.request;
        br.cookies = this.cookies;
        br.cookiesExclusive = this.cookiesExclusive;
        br.debug = this.debug;
        br.verbose = this.verbose;
        br.logger = this.logger;
        br.proxy = this.proxy;
        br.keepResponseContentBytes = this.keepResponseContentBytes;
        br.allowedResponseCodes = this.allowedResponseCodes;
        return br;
    }

    /**
     * Connects a request. and sets the requests as the browsers latest request
     * 
     * @param request
     * @throws IOException
     */
    public void connect(final Request request) throws IOException {
        // sets request BEVOR connection. this enhables to find the request in
        // the protocol handlers
        this.request = request;
        try {
            Browser.waitForPageAccess(this, request);
        } catch (final InterruptedException e) {
            throw new IOException("requestIntervalTime Exception");
        }
        try {
            request.connect();
        } finally {
            if (this.isDebug()) {
                final Logger llogger = this.getLogger();
                if (llogger != null) {
                    try {
                        llogger.finest("\r\n" + request.printHeaders());
                    } catch (final Throwable e) {
                        LogSource.exception(llogger, e);
                    }
                }
            }
        }
    }

    public boolean containsHTML(final String regex) {
        return new Regex(this, regex).matches();
    }

    /**
     * Creates a new Request object based on a form
     * 
     * @param form
     * @return
     * @throws Exception
     */
    public Request createFormRequest(final Form form) throws Exception {
        String base = null;
        String action = null;
        if (this.request != null) {
            /* take current url as base url */
            base = this.request.getUrl().toString();
        }
        try {
            final String sourceBase = this.getRegex("<base.*?href=\"(.+?)\"").getMatch(0);
            if (sourceBase != null) {
                /* take baseURL in case we've found one in current request */
                new URL(sourceBase.trim());
                base = sourceBase;
            }
        } catch (final Throwable e) {
        }
        action = form.getAction(base);
        if (action == null) { throw new NullPointerException("no valid action url"); }
        // action = action;
        switch (form.getMethod()) {
        case GET:
            final String varString = form.getPropertyString();
            if (varString != null && !varString.matches("[\\s]*")) {
                if (action.matches(".*\\?.+")) {
                    action += "&";
                } else if (action.matches("[^\\?]*")) {
                    action += "?";
                }
                action += varString;
            }
            return this.createGetRequest(action);

        case POST:
            if (form.getEncoding() == null || !form.getEncoding().toLowerCase().endsWith("form-data")) {
                return this.createPostRequest(action, form.getRequestVariables(), form.getEncoding());
            } else {
                final PostFormDataRequest request = (PostFormDataRequest) this.createPostFormDataRequest(action);
                if (form.getEncoding() != null) {
                    request.setEncodeType(form.getEncoding());
                }
                final int size = form.getInputFields().size();
                for (int i = 0; i < size; i++) {
                    final InputField entry = form.getInputFields().get(i);

                    if (entry.getValue() == null) {
                        // continue;
                    } else if (entry.getType() != null && entry.getType().equalsIgnoreCase("image")) {
                        request.addFormData(new FormData(entry.getKey() + ".x", entry.getProperty("x", (int) (Math.random() * 100) + "")));
                        request.addFormData(new FormData(entry.getKey() + ".y", entry.getProperty("y", (int) (Math.random() * 100) + "")));
                    } else if (entry.getType() != null && entry.getType().equalsIgnoreCase("file")) {
                        request.addFormData(new FormData(entry.getKey(), entry.getFileToPost().getName(), entry.getFileToPost()));
                    } else if (entry.getKey() != null && entry.getValue() != null) {
                        request.addFormData(new FormData(entry.getKey(), entry.getValue()));
                    }
                }
                return request;
            }
        }
        return null;

    }

    /**
     * Creates a new GET request.
     * 
     * @param string
     *            a string including an URL
     * 
     * @return the created GET request
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public Request createGetRequest(final String string) throws IOException {
        return this.createGetRequest(string, null);
    }

    /**
     * Creates a new GET request.
     * 
     * @param string
     *            a string including an URL
     * @param oldRequest
     *            the old request for forwarding cookies to the new request. Can be null, to ignore old cookies.
     * 
     * @return the created GET request
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public Request createGetRequest(String string, final Request oldRequest) throws IOException {
        string = this.getURL(string);
        boolean sendref = true;
        if (this.currentURL == null) {
            sendref = false;
            this.currentURL = string;
        }

        final GetRequest request = new GetRequest(string);
        request.setCustomCharset(this.customCharset);
        if (this.selectProxy() != null) {
            request.setProxy(this.selectProxy());
        }

        // if old request is set, use it's cookies for the new request
        if (oldRequest != null && oldRequest.hasCookies()) {
            request.setCookies(oldRequest.getCookies());
        }

        // doAuth(request);
        /* set Timeouts */
        request.setConnectTimeout(this.getConnectTimeout());
        request.setReadTimeout(this.getReadTimeout());

        request.getHeaders().put("Accept-Language", this.acceptLanguage);
        // request.setFollowRedirects(doRedirects);
        this.forwardCookies(request);
        if (sendref) {
            request.getHeaders().put("Referer", this.currentURL.toString());
        }
        if (this.headers != null) {
            this.mergeHeaders(request);
        }

        // if (this.doRedirects && request.getLocation() != null) {
        // this.openGetConnection(null);
        // } else {
        //
        // currentURL = new URL(string);
        // }
        // return this.request.getHttpConnection();
        return request;
    }

    /* this is buggy as we must set correct referer! */
    @Deprecated
    public Request createGetRequestRedirectedRequest(final Request oldRequest) throws IOException {
        return this.createGetRequest(oldRequest.getLocation(), oldRequest);
    }

    public Request createPostFormDataRequest(String url) throws IOException {
        url = this.getURL(url);
        boolean sendref = true;
        if (this.currentURL == null) {
            sendref = false;
            this.currentURL = url;
        }

        final PostFormDataRequest request = new PostFormDataRequest(url);
        request.setCustomCharset(this.customCharset);
        if (this.selectProxy() != null) {
            request.setProxy(this.selectProxy());
        }

        request.getHeaders().put("Accept-Language", this.acceptLanguage);

        /* set Timeouts */
        request.setConnectTimeout(this.getConnectTimeout());
        request.setReadTimeout(this.getReadTimeout());
        this.forwardCookies(request);
        if (sendref) {
            request.getHeaders().put("Referer", this.currentURL.toString());
        }

        if (this.headers != null) {
            this.mergeHeaders(request);
        }
        return request;
    }

    /**
     * Creates a new postrequest based an an requestVariable ArrayList
     */
    private Request createPostRequest(String url, final java.util.List<RequestVariable> post, final String encoding) throws IOException {
        url = this.getURL(url);
        boolean sendref = true;
        if (this.currentURL == null) {
            sendref = false;
            this.currentURL = url;
        }

        final PostRequest request = new PostRequest(url);
        request.setCustomCharset(this.customCharset);
        if (this.selectProxy() != null) {
            request.setProxy(this.selectProxy());
        }
        // doAuth(request);
        request.getHeaders().put("Accept-Language", this.acceptLanguage);
        // request.setFollowRedirects(doRedirects);
        /* set Timeouts */
        request.setConnectTimeout(this.getConnectTimeout());
        request.setReadTimeout(this.getReadTimeout());
        this.forwardCookies(request);
        if (sendref) {
            request.getHeaders().put("Referer", this.currentURL.toString());
        }
        if (post != null) {
            request.addAll(post);
        }
        /* check browser/call for content type encoding, or set to default */
        String brContentType = null;
        if (this.headers != null) {
            brContentType = this.headers.remove("Content-Type");
        }
        if (brContentType == null) {
            brContentType = encoding;
        }
        if (brContentType == null) {
            brContentType = "application/x-www-form-urlencoded";
        }
        request.setContentType(brContentType);
        if (this.headers != null) {
            this.mergeHeaders(request);
        }
        return request;
    }

    /**
     * Creates a new POstrequest based on a variable HashMap
     */
    public Request createPostRequest(final String url, final LinkedHashMap<String, String> post) throws IOException {
        return this.createPostRequest(url, PostRequest.variableMaptoArray(post), null);
    }

    /**
     * Creates a postrequest based on a querystring
     */
    public Request createPostRequest(final String url, final String post) throws MalformedURLException, IOException {
        return this.createPostRequest(url, Request.parseQuery(post));
    }

    @Deprecated
    /* this is buggy as we must set correct referer! */
    public Request createPostRequestfromRedirectedRequest(final Request oldrequest, final String postdata) throws IOException {
        final String url = this.getURL(oldrequest.getLocation());
        boolean sendref = true;
        if (this.currentURL == null) {
            sendref = false;
            this.currentURL = url;
        }
        final HashMap<String, String> post = Request.parseQuery(postdata);

        final PostRequest request = new PostRequest(url);
        request.setCustomCharset(this.customCharset);
        if (this.selectProxy() != null) {
            request.setProxy(this.selectProxy());
        }
        if (oldrequest.hasCookies()) {
            request.setCookies(oldrequest.getCookies());
        }
        // doAuth(request);
        request.getHeaders().put("Accept-Language", this.acceptLanguage);
        // request.setFollowRedirects(doRedirects);
        /* set Timeouts */
        request.setConnectTimeout(this.getConnectTimeout());
        request.setReadTimeout(this.getReadTimeout());
        this.forwardCookies(request);
        if (sendref) {
            request.getHeaders().put("Referer", this.currentURL.toString());
        }
        if (post != null) {
            request.addAll(post);
        }
        if (this.headers != null) {
            this.mergeHeaders(request);
        }
        return request;
    }

    public Request createRequest(final Form form) throws Exception {
        return this.createFormRequest(form);
    }

    public Request createRequest(final String downloadURL) throws Exception {
        return this.createGetRequest(downloadURL);
    }

    public void disconnect() {
        try {
            this.getRequest().getHttpConnection().disconnect();
        } catch (final Throwable e) {
        }
    }

    /**
     * Downloads the contents behind con to file. if(con ==null), the latest request is downloaded. Useful for redirects
     * 
     * @param file
     * @param con
     * @throws IOException
     */
    public void downloadConnection(final File file, URLConnectionAdapter con) throws IOException {
        if (con == null) {
            con = this.request.getHttpConnection();
        }
        Browser.download(file, con);
    }

    public String followConnection() throws IOException {
        final Logger llogger = this.getLogger();
        if (this.request.getHtmlCode() != null) {
            if (llogger != null) {
                llogger.warning("Request has already been read");
            }
            return null;
        }
        try {
            this.checkContentLengthLimit(this.request);
            /* we update allowedResponseCodes here */
            this.request.getHttpConnection().setAllowedResponseCodes(this.allowedResponseCodes);
            this.request.read(this.isKeepResponseContentBytes());
        } catch (final BrowserException e) {
            throw e;
        } catch (final IOException e) {
            throw new BrowserException(e.getMessage(), this.request.getHttpConnection(), e);
        } finally {
            this.request.disconnect();
        }
        if (this.isVerbose()) {
            if (llogger != null) {
                llogger.finest("\r\n" + this.request + "\r\n");
            }
        }
        return this.request.getHtmlCode();
    }

    /**
     * Zeigt debuginformationen auch im Hauptprogramm an
     * 
     * @param b
     */
    public void forceDebug(final boolean b) {
        this.debug = b;
    }

    public void forwardCookies(final Request request) {
        if (request == null) { return; }
        final String host = Browser.getHost(request.getUrl());
        final Cookies cookies = this.getCookies().get(host);
        if (cookies == null) { return; }

        for (final Cookie cookie : cookies.getCookies()) {
            // Pfade sollten verarbeitet werden...TODO
            if (cookie.isExpired()) {
                continue;
            }
            request.getCookies().add(cookie);
        }
    }

    public void forwardCookies(final URLConnectionAdapter con) {
        if (con == null) { return; }
        final String host = Browser.getHost(con.getURL().toString());
        final Cookies cookies = this.getCookies().get(host);
        final String cs = Request.getCookieString(cookies);
        if (cs != null && cs.trim().length() > 0) {
            con.setRequestProperty("Cookie", cs);
        }
    }

    public String getAcceptLanguage() {
        return this.acceptLanguage;
    }

    /**
     * @return the allowedResponseCodes
     */
    public int[] getAllowedResponseCodes() {
        return this.allowedResponseCodes;
    }

    private String getBase(final String string) {
        if (string == null) { return ""; }

        final String base = this.getRegex("<base\\s*href=\"(.*?)\"").getMatch(0);
        if (base != null) { return base; }

        final URL url = this.request.getHttpConnection().getURL();
        final String host = url.getHost();
        String portUse = "";
        if (url.getDefaultPort() > 0 && url.getPort() > 0 && url.getDefaultPort() != url.getPort()) {
            portUse = ":" + url.getPort();
        }
        String proto = "http://";
        if (url.toString().startsWith("https")) {
            proto = "https://";
        }
        String path = url.getPath();
        int id;
        if ((id = path.lastIndexOf('/')) >= 0) {
            path = path.substring(0, id);
        }
        return proto + host + portUse + path + "/";
    }

    public String getBaseURL() {
        if (this.request == null) { return null; }

        final String base = this.request.getUrl().toString();
        // if (base.matches("http://.*/.*")) {
        // return base.substring(0, base.lastIndexOf("/")) + "/";
        // } else {
        // return base + "/";
        // }
        return base.matches("https?://.*/.*") ? base.substring(0, base.lastIndexOf("/")) + "/" : base + "/";
    }

    /**
     * returns current ConnectTimeout
     * 
     * @return
     */
    public int getConnectTimeout() {
        return this.connectTimeout < 0 ? Browser.TIMEOUT_CONNECT : this.connectTimeout;
    }

    public String getCookie(final String url, final String key) {
        final String host = Browser.getHost(url);
        final Cookies cookies = this.getCookies(host);
        final Cookie cookie = cookies.get(key);

        return cookie != null ? cookie.getValue() : null;
    }

    private HashMap<String, Cookies> getCookies() {
        return this.cookiesExclusive ? this.cookies : Browser.COOKIES;
    }

    public Cookies getCookies(final String url) {
        final String host = Browser.getHost(url);
        Cookies cookies2 = this.getCookies().get(host);
        if (cookies2 == null) {
            this.getCookies().put(host, cookies2 = new Cookies());
        }
        return cookies2;
    }

    public void getDownload(final File file, final String urlString) throws IOException {
        final URLConnectionAdapter con = this.openGetConnection(URLDecoder.decode(urlString, "UTF-8"));
        Browser.download(file, con);
    }

    public Form getForm(final int i) {
        final Form[] forms = this.getForms();
        return forms.length <= i ? null : forms[i];
    }

    /**
     * Returns the first form that has an input filed with name key
     * 
     * @param key
     * @return
     */
    public Form getFormbyKey(final String key) {
        for (final Form f : this.getForms()) {
            if (f.hasInputFieldByName(key)) { return f; }
        }
        return null;
    }

    /**
     * Returns the first form that has a 'key' that equals 'value'.
     * 
     * NOTE: JDownloader 2 dependent
     * 
     * @param key
     * @param value
     * @return
     */
    public Form getFormbyKey(final String key, final String value) {
        for (final Form f : this.getForms()) {
            for (final InputField field : f.getInputFields()) {
                if (key != null && key.equals(field.getKey())) {
                    if (value == null && field.getValue() == null) { return f; }
                    if (value != null && value.equals(field.getValue())) { return f; }
                }
            }
        }
        return null;
    }

    public Form getFormbyProperty(final String property, final String name) {
        for (final Form form : this.getForms()) {
            if (form.getStringProperty(property) != null && form.getStringProperty(property).equalsIgnoreCase(name)) { return form; }
        }
        return null;
    }

    /**
     * Returns the first form with an Submitvalue of name
     * 
     * @param name
     * @return
     */
    public Form getFormBySubmitvalue(final String name) {
        for (final Form form : this.getForms()) {
            try {
                form.setPreferredSubmit(name);
                return form;
            } catch (final IllegalArgumentException e) {
            }
        }
        return null;
    }

    public Form[] getForms() {
        return Form.getForms(this);
    }

    public Form[] getForms(final String downloadURL) throws IOException {
        this.getPage(downloadURL);
        return this.getForms();
    }

    public RequestHeader getHeaders() {
        if (this.headers == null) {
            this.headers = new RequestHeader();
        }
        return this.headers;
    }

    public String getHost() {
        return this.request == null ? null : Browser.getHost(this.request.getUrl(), false);
    }

    public URLConnectionAdapter getHttpConnection() {
        if (this.request == null) { return null; }
        return this.request.getHttpConnection();
    }

    public Logger getLogger() {
        final Logger llogger = this.logger;
        if (llogger != null) { return llogger; }
        return Browser.LOGGER;
    }

    public String getMatch(final String string) {
        return this.getRegex(string).getMatch(0);
    }

    public String getPage(final String string) throws IOException {
        this.openRequestConnection(this.createGetRequest(string));
        return this.loadConnection(null).getHtmlCode();
    }

    public String getPage(final URL url) throws IOException {
        return this.getPage(url + "");
    }

    public HTTPProxy getProxy() {
        return this.proxy;
    }

    /**
     * returns current ReadTimeout
     * 
     * @return
     */
    public int getReadTimeout() {
        return this.readTimeout < 0 ? Browser.TIMEOUT_READ : this.readTimeout;
    }

    /**
     * If automatic redirectfollowing is disabled, you can get the redirect URL if there is any.
     * 
     * @return
     */
    public String getRedirectLocation() {
        if (this.request == null) { return null; }
        return this.request.getLocation();
    }

    public Regex getRegex(final Pattern compile) {
        return new Regex(this, compile);
    }

    public Regex getRegex(final String string) {
        return new Regex(this, string);
    }

    /**
     * Gets the latest request
     * 
     * @return
     */
    public Request getRequest() {
        return this.request;
    }

    public HTTPProxy getThreadProxy() {
        final Thread currentThread = Thread.currentThread();
        /**
         * return BrowserSettings from current thread if available
         */
        if (currentThread != null && currentThread instanceof BrowserSettings) {
            final BrowserSettings settings = (BrowserSettings) currentThread;
            return settings.getCurrentProxy();
        }
        return null;
    }

    public String getURL() {
        return this.request == null ? null : this.request.getUrl().toString();
    }

    /**
     * Tries to get a full URL out of string
     * 
     * @throws BrowserException
     */
    public String getURL(String string) throws BrowserException {
        if (string == null) {
            string = this.getRedirectLocation();
        }
        if (string == null) { throw new BrowserException("Null URL"); }
        try {
            new URL(string);
        } catch (final Exception e) {
            if (this.request == null || this.request.getHttpConnection() == null) { return string; }
            final String base = this.getBase(string);
            if (string.startsWith("/") || string.startsWith("\\")) {
                try {
                    final URL bUrl = new URL(base);
                    String proto = "http://";
                    if (base.startsWith("https")) {
                        proto = "https://";
                    }
                    String portUse = "";
                    if (bUrl.getDefaultPort() > 0 && bUrl.getPort() > 0 && bUrl.getDefaultPort() != bUrl.getPort()) {
                        portUse = ":" + bUrl.getPort();
                    }
                    string = proto + new URL(base).getHost() + portUse + string;
                } catch (final MalformedURLException e1) {
                    e1.printStackTrace();
                }
            } else {
                string = base + string;
            }
        }
        return Browser.correctURL(Encoding.urlEncode_light(string));
    }

    public boolean isCookiesExclusive() {
        return this.cookiesExclusive;
    }

    public boolean isDebug() {
        return this.debug || this.isVerbose();
    }

    public boolean isFollowingRedirects() {
        return this.doRedirects;
    }

    public boolean isKeepResponseContentBytes() {
        return this.keepResponseContentBytes;
    }

    public boolean isVerbose() {
        return Browser.VERBOSE || this.verbose;
    }

    /**
     * Reads the content behind a con and returns them. Note: if con==null, the current request is read. This is useful for redirects. Note
     * #2: if a connection is loaded, data is not stored in the browser instance.
     * 
     * @param con
     * @return
     * @throws IOException
     */
    public Request loadConnection(URLConnectionAdapter con) throws IOException {

        Request requ;
        if (con == null) {
            requ = this.request;
        } else {
            requ = new Request(con) {
                {
                    this.requested = true;
                }

                @Override
                public long postRequest() throws IOException {
                    return 0;
                }

                @Override
                public void preRequest() throws IOException {
                }
            };
        }
        try {
            this.checkContentLengthLimit(requ);
            con = requ.getHttpConnection();
            /* we update allowedResponseCodes here */
            con.setAllowedResponseCodes(this.allowedResponseCodes);
            requ.read(this.isKeepResponseContentBytes());
        } catch (final BrowserException e) {
            throw e;
        } catch (final IOException e) {
            throw new BrowserException(e.getMessage(), con, e);
        } finally {
            try {
                con.disconnect();
            } catch (final Throwable e) {
            }
        }
        if (this.isVerbose()) {
            final Logger llogger = this.getLogger();
            if (llogger != null) {
                llogger.finest("\r\n" + requ + "\r\n");
            }
        }
        return requ;
    }

    private void mergeHeaders(final Request request) {
        if (this.headers.isDominant()) {
            request.getHeaders().clear();
        }
        final int size = this.headers.size();
        String value;
        for (int i = 0; i < size; i++) {
            value = this.headers.getValue(i);
            if (value == null) {
                request.getHeaders().remove(this.headers.getKey(i));
            } else {
                request.getHeaders().put(this.headers.getKey(i), value);
            }
        }
    }

    /**
     * Opens a new connection based on a Form
     * 
     * @param form
     * @return
     * @throws Exception
     */
    public URLConnectionAdapter openFormConnection(final Form form) throws Exception {
        return this.openRequestConnection(this.createFormRequest(form));
    }

    public URLConnectionAdapter openFormConnection(final int i) throws Exception {
        return this.openFormConnection(this.getForm(i));
    }

    /**
     * Opens a new get connection
     * 
     * @param string
     * @return
     * @throws IOException
     */
    public URLConnectionAdapter openGetConnection(final String string) throws IOException {
        return this.openRequestConnection(this.createGetRequest(string));

    }

    /**
     * Opens a Post COnnection based on a variable HashMap
     */
    public URLConnectionAdapter openPostConnection(final String url, final LinkedHashMap<String, String> post) throws IOException {
        return this.openRequestConnection(this.createPostRequest(url, post));
    }

    /**
     * OPens a new POst connection based on a query string
     */
    public URLConnectionAdapter openPostConnection(final String url, final String post) throws IOException {
        return this.openPostConnection(url, Request.parseQuery(post));
    }

    /**
     * Opens a connection based on the request object
     */
    public URLConnectionAdapter openRequestConnection(final Request request) throws IOException {
        this.connect(request);
        this.updateCookies(request);
        this.request = request;
        if (this.doRedirects && request.getLocation() != null) {
            if (request.getLocation().toLowerCase().startsWith("ftp://")) { throw new BrowserException("Cannot redirect to FTP"); }
            final String org = request.getUrl();
            final String red = request.getLocation();
            if (org.equalsIgnoreCase(red) && this.redirectLoopCounter >= 20) {
                final Logger llogger = this.getLogger();
                if (llogger != null) {
                    llogger.severe("20 Redirects!!!");
                }
            } else if (!org.equalsIgnoreCase(red) || this.redirectLoopCounter < 20) {
                if (org.equalsIgnoreCase(red)) {
                    this.redirectLoopCounter++;
                } else {
                    this.redirectLoopCounter = 0;
                }
                /* prevent buggy redirect loops */
                /* source==dest */
                try {
                    /* close old connection, because we follow redirect */
                    request.httpConnection.disconnect();
                } catch (final Throwable e) {
                }
                this.openGetConnection(null);
            }
        } else {
            this.currentURL = request.getUrl();
        }
        return this.request.getHttpConnection();
    }

    /**
     * loads a new page (post)
     */
    public String postPage(final String url, final LinkedHashMap<String, String> post) throws IOException {
        this.openPostConnection(url, post);
        return this.loadConnection(null).getHtmlCode();
    }

    /**
     * loads a new page (POST)
     */
    public String postPage(final String url, final String post) throws IOException {
        return this.postPage(url, Request.parseQuery(post));
    }

    public String postPageRaw(final String url, final byte[] post) throws IOException {
        final PostRequest request = (PostRequest) this.createPostRequest(url, new ArrayList<RequestVariable>(), null);
        request.setCustomCharset(this.customCharset);
        if (post != null) {
            request.setPostBytes(post);
        }
        this.openRequestConnection(request);
        return this.loadConnection(null).getHtmlCode();
    }

    /**
     * loads a new page (post) the postdata is given by the poststring. It will be sent as is
     */
    public String postPageRaw(final String url, final String post) throws IOException {
        final PostRequest request = (PostRequest) this.createPostRequest(url, new ArrayList<RequestVariable>(), null);
        request.setCustomCharset(this.customCharset);
        if (post != null) {
            request.setPostDataString(post);
        }
        this.openRequestConnection(request);
        return this.loadConnection(null).getHtmlCode();
    }

    private HTTPProxy selectProxy() {
        if (this.proxy != null) {
            if (this.proxy == HTTPProxy.NONE) { return HTTPProxy.NONE; }
            return this.proxy;
        }
        return Browser.GLOBAL_PROXY;
    }

    public void setAcceptLanguage(final String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    /**
     * @param allowedResponseCodes
     *            the allowedResponseCodes to set
     */
    public void setAllowedResponseCodes(final int... allowedResponseCodes) {
        this.allowedResponseCodes = allowedResponseCodes;
    }

    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setCookie(final String url, final String key, final String value) {
        final String host = Browser.getHost(url);
        Cookies cookies;
        if (!this.getCookies().containsKey(host) || (cookies = this.getCookies().get(host)) == null) {
            cookies = new Cookies();
            this.getCookies().put(host, cookies);
        }
        cookies.add(new Cookie(host, key, value));
    }

    public void setCookiesExclusive(final boolean b) {
        if (this.cookiesExclusive == b) { return; }
        this.cookiesExclusive = b;
        if (b) {
            this.cookies.clear();
            for (final Entry<String, Cookies> next : Browser.COOKIES.entrySet()) {
                Cookies tmp;
                this.cookies.put(next.getKey(), tmp = new Cookies());
                tmp.add(next.getValue());
            }
        } else {
            this.cookies.clear();
        }
    }

    /**
     * sets current URL, if null we don't send referer!
     * 
     * @param string
     * @since JD2
     * */
    public void setCurrentURL(final String string) throws MalformedURLException {
        if (string == null || string.length() == 0) {
            this.currentURL = null;
        } else {
            this.currentURL = string;
        }
    }

    public void setCustomCharset(final String charset) {
        this.customCharset = charset;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public void setFollowRedirects(final boolean b) {
        this.doRedirects = b;
    }

    /**
     * do not below revision 10000
     * 
     * @since JD2
     * */
    public void setHeader(final String field, final String value) {
        this.getHeaders().put(field, value);
    }

    public void setHeaders(final RequestHeader h) {
        this.headers = h;
    }

    public void setKeepResponseContentBytes(final boolean keepResponseContentBytes) {
        this.keepResponseContentBytes = keepResponseContentBytes;
    }

    public void setLoadLimit(final int i) {
        this.limit = i;
    }

    public void setLogger(final Logger logger) {
        this.logger = logger;
    }

    public void setProxy(HTTPProxy proxy) {
        final HTTPProxy wished = proxy;
        if (proxy == null) {
            proxy = this.getThreadProxy();
        }
        this.proxy = proxy;
        if (this.debug) {
            final Logger llogger = this.getLogger();
            if (llogger != null) {
                llogger.info("Use local proxy: " + proxy + " wished: " + wished);
            }
        }
    }

    public void setReadTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setRequest(final Request request) {
        if (request == null) { return; }
        this.updateCookies(request);
        this.request = request;
        this.currentURL = request.getUrl();
    }

    public void setRequestIntervalLimit(final String host, final int i) {
        final String domain = Browser.getHost(host);
        if (domain == null) { return; }
        if (this.requestIntervalLimitMap == null) {
            this.requestTimeMap = new HashMap<String, Long>();
            this.requestIntervalLimitMap = new HashMap<String, Integer>();
        }
        this.requestIntervalLimitMap.put(domain, i);

    }

    public void setVerbose(final boolean b) {
        this.verbose = b;
    }

    public String submitForm(final Form form) throws Exception {
        this.openFormConnection(form);
        return this.followConnection();
    }

    @Override
    public String toString() {
        if (this.request == null) { return "Browser. no request yet"; }
        return this.request.getHTMLSource();
    }

    public void updateCookies(final Request request) {
        if (request == null) { return; }
        final String host = Browser.getHost(request.getUrl());
        Cookies cookies = this.getCookies().get(host);
        if (cookies == null) {
            cookies = new Cookies();
            this.getCookies().put(host, cookies);
        }
        cookies.add(request.getCookies());
    }
}
