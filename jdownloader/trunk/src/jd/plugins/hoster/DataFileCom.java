//jDownloader - Downloadmanager
//Copyright (C) 2009  JD-Team support@jdownloader.org
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.hoster;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jd.PluginWrapper;
import jd.config.Property;
import jd.http.Browser;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;
import org.appwork.utils.os.CrossSystem;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "datafile.com" }, urls = { "https?://(www\\.)?datafile\\.com/d/[A-Za-z0-9]+" }, flags = { 2 })
public class DataFileCom extends PluginForHost {

    public DataFileCom(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://www.datafile.com/getpremium.html");
    }

    @Override
    public String getAGBLink() {
        return "http://www.datafile.com/terms.html";
    }

    private final String               PREMIUMONLY                  = "(\"Sorry\\. Only premium users can download this file\"|>This file can be downloaded only by users with<br />Premium account!<)";
    private final boolean              SKIPRECONNECTWAITTIME        = true;
    private final boolean              SKIPWAITTIME                 = true;
    private final String               DAILYLIMIT                   = ">You exceeded your free daily download limit";

    // Connection Management
    // note: CAN NOT be negative or zero! (ie. -1 or 0) Otherwise math sections fail. .:. use [1-20]
    private static final AtomicInteger totalMaxSimultanFreeDownload = new AtomicInteger(1);

    /**
     * They have a linkchecker but it doesn't show filenames if they're not included in the URL: http://www.datafile.com/linkchecker.html
     */
    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        // Offline links should also have nice filenames
        link.setName(new Regex(link.getDownloadURL(), "datafile\\.com/d/([A-Za-z0-9]+)").getMatch(0));
        this.setBrowserExclusive();
        prepBrowser(br);
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        br.setFollowRedirects(false);
        String filesize = null;
        // Limit reached -> Let's use their linkchecker to at least find the filesize and onlinestatus
        if (br.containsHTML(DAILYLIMIT) || br.getURL().contains("error.html?code=7")) {
            final Browser br2 = br.cloneBrowser();
            br2.postPage("http://www.datafile.com/linkchecker.html", "btn=&links=" + Encoding.urlEncode(link.getDownloadURL()));
            filesize = br2.getRegex("title=\"File size\">([^<>\"]*?)</td>").getMatch(0);
            if (filesize != null) {
                link.getLinkStatus().setStatusText("Cannot show filename when the daily limit is reached");
                link.setDownloadSize(SizeFormatter.getSize(filesize));
                return AvailableStatus.TRUE;
            } else if (!br2.containsHTML(">Link<") && !br2.containsHTML(">Status<") && !br2.containsHTML(">File size<")) {
                // Maybe no table --> Link offline
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            // No results -> Unckeckable because if the limit
            link.getLinkStatus().setStatusText("Cannot check available status when the daily limit is reached");
            return AvailableStatus.UNCHECKABLE;
        }
        // Invalid link
        if (br.containsHTML("<div class=\"error\\-msg\">")) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        // Deleted file
        if (br.containsHTML(">Sorry but this file has been deleted")) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        if (br.containsHTML("ErrorCode 7: Download file count limit")) {
            return AvailableStatus.UNCHECKABLE;
        }
        final String filename = br.getRegex("class=\"file\\-name\">([^<>\"]*?)</div>").getMatch(0);
        filesize = br.getRegex(">Filesize:<span class=\"lime\">([^<>\"]*?)</span>").getMatch(0);
        if (filesize == null) {
            filesize = br.getRegex(">Filesize: <span class=\"lime\">([^<>\"]*?)</span>").getMatch(0);
        }
        if (filename == null || filesize == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        link.setFinalFileName(Encoding.htmlDecode(filename.trim()));
        link.setDownloadSize(SizeFormatter.getSize(filesize));
        if (br.containsHTML(PREMIUMONLY)) {
            link.getLinkStatus().setStatusText("This file can only be downloaded by premium users");
        }
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        doFree(downloadLink, null);
    }

    private void doFree(final DownloadLink downloadLink, final Account account) throws Exception {
        if (br.containsHTML(DAILYLIMIT) || br.getURL().contains("error.html?code=7")) {
            throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 2 * 60 * 60 * 1000l);
        }
        if (br.containsHTML("ErrorCode 7: Download file count limit")) {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Download file count limit", 10 * 60 * 1000l);
        }
        if (br.containsHTML(PREMIUMONLY)) {
            // not possible to download under handleFree!
            try {
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_ONLY);
            } catch (final Throwable e) {
                if (e instanceof PluginException) {
                    throw (PluginException) e;
                }
            }
            throw new PluginException(LinkStatus.ERROR_FATAL, "This file can only be downloaded by premium users");
        }
        String dllink = checkDirectLink(downloadLink, "directlink");
        if (dllink == null) {
            final String fid = new Regex(downloadLink.getDownloadURL(), "([A-Za-z0-9]+)$").getMatch(0);
            br.setFollowRedirects(false);
            final Regex waitTime = br.getRegex("class=\"time\">(\\d+):(\\d+):(\\d+)</span>");
            int tmphrs = 0, tmpmin = 0, tmpsecs = 0;
            String tempHours = waitTime.getMatch(0);
            if (tempHours != null) {
                tmphrs = Integer.parseInt(tempHours);
            }
            String tempMinutes = waitTime.getMatch(1);
            if (tempMinutes != null) {
                tmpmin = Integer.parseInt(tempMinutes);
            }
            String tempSeconds = waitTime.getMatch(2);
            if (tempSeconds != null) {
                tmpsecs = Integer.parseInt(tempSeconds);
            }
            final long wait = (tmphrs * 60 * 60 * 1000) + (tmpmin * 60 * 1000) + (tmpsecs * 1001);
            if (wait == 0) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            if (!SKIPRECONNECTWAITTIME && wait > 3601800) {
                throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, wait);
            }
            long timeBefore = System.currentTimeMillis();
            final String rcID = br.getRegex("api/challenge\\?k=([^<>\"]*?)\"").getMatch(0);
            if (rcID == null) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
            final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
            final jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
            rc.setId(rcID);
            rc.load();
            for (int i = 1; i <= 5; i++) {
                final File cf = rc.downloadCaptcha(getLocalCaptchaFile());
                final String c = getCaptchaCode(cf, downloadLink);
                if (!SKIPWAITTIME || i > 1) {
                    waitTime(timeBefore, downloadLink, wait);
                }
                postPage("http://www.datafile.com/files/ajax.html", "doaction=getFileDownloadLink&recaptcha_challenge_field=" + rc.getChallenge() + "&recaptcha_response_field=" + Encoding.urlEncode(c) + "&fileid=" + fid);
                if (br.containsHTML("\"The two words is not valid")) {
                    rc.reload();
                    continue;
                }
                break;
            }
            if (br.containsHTML("\"The two words is not valid")) {
                throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            }
            if (br.containsHTML("\"errorType\":null")) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT, "Unknown error...");
            }
            dllink = br.getRegex("\"link\":\"(http:[^<>\"]*?)\"").getMatch(0);
            if (dllink == null) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
        }
        try {
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 1);
            if (dl.getConnection().getContentType().contains("html")) {
                if (dl.getConnection().getResponseCode() == 404) {
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error 404", 30 * 60 * 1000l);
                }
                br.followConnection();
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            String finalname = Encoding.htmlDecode(getFileNameFromHeader(dl.getConnection()));
            String finalFixedName = new Regex(finalname, "([^<>\"]*?)\"; creation\\-date=").getMatch(0);
            if (finalFixedName != null) {
                finalname = finalFixedName;
            }
            downloadLink.setFinalFileName(finalFixedName);
            downloadLink.setProperty("directlink", dllink);
            // add download slot
            controlSlot(+1, account);
            try {
                dl.startDownload();
            } finally {
                // remove download slot
                controlSlot(-1, account);
            }
        } catch (final PluginException e) {
            if (e.getLinkStatus() == LinkStatus.ERROR_DOWNLOAD_INCOMPLETE) {
                logger.info("Retry on ERROR_DOWNLOAD_INCOMPLETE");
                throw new PluginException(LinkStatus.ERROR_RETRY);
            }
            throw e;
        }
    }

    private void waitTime(long timeBefore, final DownloadLink downloadLink, long wait) throws PluginException {
        long passedTime = (System.currentTimeMillis() - timeBefore) - 1000;
        /** Ticket Time */
        wait -= passedTime;
        logger.info("Waittime detected, waiting " + wait + " - " + passedTime + " milliseconds from now on...");
        if (wait > 0) {
            sleep(wait, downloadLink);
        }
    }

    private String checkDirectLink(final DownloadLink downloadLink, final String property) {
        String dllink = downloadLink.getStringProperty(property);
        if (dllink != null) {
            try {
                Browser br2 = br.cloneBrowser();
                URLConnectionAdapter con = br2.openGetConnection(dllink);
                if (con.getContentType().contains("html") || con.getLongContentLength() == -1) {
                    downloadLink.setProperty(property, Property.NULL);
                    dllink = null;
                }
                con.disconnect();
            } catch (Exception e) {
                downloadLink.setProperty(property, Property.NULL);
                dllink = null;
            }
        }
        return dllink;
    }

    private static final String MAINPAGE = "http://datafile.com";
    private static Object       LOCK     = new Object();

    @SuppressWarnings("unchecked")
    private void login(final Account account, final boolean force) throws Exception {
        synchronized (LOCK) {
            try {
                // Load cookies
                br.setCookiesExclusive(true);
                prepBrowser(br);
                final Object ret = account.getProperty("cookies", null);
                boolean acmatch = Encoding.urlEncode(account.getUser()).equals(account.getStringProperty("name", Encoding.urlEncode(account.getUser())));
                if (acmatch) {
                    acmatch = Encoding.urlEncode(account.getPass()).equals(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
                }
                if (acmatch && ret != null && ret instanceof HashMap<?, ?> && !force) {
                    final HashMap<String, String> cookies = (HashMap<String, String>) ret;
                    if (account.isValid()) {
                        for (final Map.Entry<String, String> cookieEntry : cookies.entrySet()) {
                            final String key = cookieEntry.getKey();
                            final String value = cookieEntry.getValue();
                            this.br.setCookie(MAINPAGE, key, value);
                        }
                        return;
                    }
                }
                br.setFollowRedirects(true);
                // https is forced here anyways
                String protocol = "https://";
                if (isJava7nJDStable()) {
                    if (!stableSucks.get()) {
                        showSSLWarning(this.getHost());
                    }
                    // https is forced here anyways
                    protocol = "https://";
                }
                br.postPage(protocol + "www.datafile.com/login.html", "login=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()) + "&remember_me=0&remember_me=1&btn=");
                if (br.getCookie(MAINPAGE, "hash") == null || br.getCookie(MAINPAGE, "user") == null) {
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nInvalid username/password!\r\nUngültiger Benutzername oder ungültiges Passwort!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                }
                // Save cookies
                final HashMap<String, String> cookies = new HashMap<String, String>();
                final Cookies add = this.br.getCookies(MAINPAGE);
                for (final Cookie c : add.getCookies()) {
                    cookies.put(c.getKey(), c.getValue());
                }
                account.setProperty("name", Encoding.urlEncode(account.getUser()));
                account.setProperty("pass", Encoding.urlEncode(account.getPass()));
                account.setProperty("cookies", cookies);
            } catch (final PluginException e) {
                account.setProperty("cookies", Property.NULL);
                throw e;
            }
        }
    }

    @Override
    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        if (useAPI.get()) {
            return fetchAccountInfo_API(account, ai);
        } else {
            try {
                login(account, true);
            } catch (PluginException e) {
                account.setValid(false);
                throw e;
            }
            br.getPage("/profile.html");
            final String filesNum = br.getRegex(">Files: <span class=\"lime\">(\\d+)</span>").getMatch(0);
            if (filesNum != null) {
                ai.setFilesNum(Long.parseLong(filesNum));
            }
            final String space = br.getRegex(">Storage: <span class=\"lime\">([^<>\"]*?)</span>").getMatch(0);
            if (space != null) {
                ai.setUsedSpace(space.trim());
            }
            ai.setUnlimitedTraffic();
            String expire = br.getRegex("([a-zA-Z]{3} \\d{1,2}, \\d{4} \\d{1,2}:\\d{1,2})").getMatch(0);
            if (expire == null) {
                logger.info("JD could not detect account expire time, your account has been determined as a free account");
                account.setProperty("free", true);
                ai.setStatus("Free User");
            } else {
                account.setProperty("free", false);
                ai.setValidUntil(TimeFormatter.getMilliSeconds(expire, "MMM dd, yyyy HH:mm", Locale.ENGLISH));
                ai.setStatus("Premium User");
            }
            account.setValid(true);
        }
        return ai;

    }

    @Override
    public void handlePremium(final DownloadLink downloadLink, final Account account) throws Exception {
        if (useAPI.get() && !account.getBooleanProperty("free", false)) {
            handlePremium_API(downloadLink, account);
        } else {
            requestFileInformation(downloadLink);
            login(account, false);
            if (account.getBooleanProperty("free")) {
                br.getPage(downloadLink.getDownloadURL());
                // if the cached cookie expired, relogin.
                if (br.getCookie(MAINPAGE, "hash") == null || br.getCookie(MAINPAGE, "user") == null) {
                    synchronized (LOCK) {
                        account.setProperty("cookies", Property.NULL);
                        // if you retry, it can use another account...
                        throw new PluginException(LinkStatus.ERROR_RETRY);
                    }
                }
                handleGeneralErrors(account);
                doFree(downloadLink, account);
            }
            br.setFollowRedirects(true);
            dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, downloadLink.getDownloadURL(), true, 0);
            if (dl.getConnection().getContentType().contains("html")) {
                if (dl.getConnection().getResponseCode() == 404) {
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error 404", 30 * 60 * 1000l);
                }
                logger.warning("The final dllink seems not to be a file!");
                br.followConnection();
                handleGeneralErrors(account);
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            // add download slot
            controlSlot(+1, account);
            try {
                dl.startDownload();
            } finally {
                // remove download slot
                controlSlot(-1, account);
            }
        }
    }

    private void handleGeneralErrors(final Account account) throws PluginException {
        final String redirect = br.getRedirectLocation();
        String errorCode = br.getRegex("ErrorCode (\\d+):").getMatch(0);
        if ((redirect != null && redirect.contains("error.html?code=")) || errorCode != null) {
            if (errorCode == null) {
                errorCode = new Regex(redirect, "error\\.html\\?code=(\\d+)").getMatch(0);
            }
            if ("6".endsWith(errorCode)) {
                logger.info("Trafficlimit reached");
                final AccountInfo ac = new AccountInfo();
                ac.setTrafficLeft(0);
                account.setAccountInfo(ac);
                throw new PluginException(LinkStatus.ERROR_PREMIUM, "Trafficlimit reached", PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
            } else if ("7".endsWith(errorCode)) {
                // reached daily download quota
                logger.info("You've reached daily download quota for " + account.getUser() + " account");
                final AccountInfo ac = new AccountInfo();
                ac.setTrafficLeft(0);
                account.setAccountInfo(ac);
                throw new PluginException(LinkStatus.ERROR_PREMIUM, "Trafficlimit reached", PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
            }
            logger.warning("Unknown error");
            throw new PluginException(LinkStatus.ERROR_PREMIUM, "Unknown error", PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
        }
    }

    private void postPage(String url, final String postData) throws IOException {
        if (isJava7nJDStable() && url.toLowerCase().startsWith("https://")) {
            if (!stableSucks.get()) {
                showSSLWarning(this.getHost());
            }
            url = url.replaceFirst("https://", "http://");
        }
        br.postPage(url, postData);
        br.getRequest().setHtmlCode(br.toString().replace("\\", ""));
    }

    private void prepBrowser(final Browser br) {
        br.getHeaders().put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        br.getHeaders().put("Accept-Language", "en-gb, en;q=0.9");
        br.setCookie(MAINPAGE, "lang", "en");
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return maxPrem.get();
    }

    @Override
    public void reset() {
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return maxFree.get();
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

    /* NO OVERRIDE!! We need to stay 0.9*compatible */
    public boolean hasCaptcha(DownloadLink link, jd.plugins.Account acc) {
        if (acc == null) {
            /* no account, yes we can expect captcha */
            return true;
        }
        if (Boolean.TRUE.equals(acc.getBooleanProperty("free"))) {
            /* free accounts also have captchas */
            return true;
        }
        return false;
    }

    private static AtomicBoolean useAPI = new AtomicBoolean(true);
    private final String         apiURL = "https://api.datafile.com";

    private Browser prepApiBrowser(final Browser ibr) {
        return ibr;
    }

    private synchronized void getPage(final Browser ibr, final String url, final Account account) throws Exception {
        if (account != null) {
            String apiToken = getApiToken(account);
            ibr.getPage(url + (url.matches("(" + apiURL + ")?/[a-zA-Z0-9]+/[a-zA-Z0-9]+\\?[a-zA-Z0-9]+.+") ? "&" : "?") + "token=" + apiToken);
            if (sessionTokenInValid(account, ibr)) {
                apiToken = getApiToken(account);
                if (apiToken != null) {
                    // can't sessionKeyInValid because getApiKey/loginKey return String, and loginKey uses a new Browser.
                    ibr.getPage(url + (url.matches("(" + apiURL + ")?/[a-zA-Z0-9]+/[a-zA-Z0-9]+\\?[a-zA-Z0-9]+.+") ? "&" : "?") + "token=" + apiToken);
                } else {
                    // failure occurred.
                    throw new PluginException(LinkStatus.ERROR_FATAL);
                }
            }
            // account specific errors which could happen at any point in time!
            if (sessionTokenInValid(account, ibr)) {
                throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\n" + errorMsg(ibr), PluginException.VALUE_ID_PREMIUM_DISABLE);
            }
        } else {
            ibr.getPage(url);
        }
        // error handling for generic errors which could occur at any point in time!
        if (("718".equalsIgnoreCase(getJson(ibr, "code")))) {
            // 718 ERR_API_IP_SUSPENDED The IP Address initiating the request has been suspended
            throw new PluginException(LinkStatus.ERROR_FATAL, "\r\n" + errorMsg(ibr));
        }
    }

    private String errorMsg(final Browser ibr) {
        final String message = getJson(ibr, "message");
        logger.warning(message);
        return message;
    }

    private synchronized String getApiToken(final Account account) throws Exception {
        String apiToken = account.getStringProperty("apiToken", null);
        if (apiToken == null) {
            apiToken = loginToken(account);
        }
        return apiToken;
    }

    private boolean sessionTokenInValid(final Account account, final Browser ibr) {
        final String code = getJson(ibr, "code");
        if (("909".equalsIgnoreCase(code) || "910".equalsIgnoreCase(code))) {
            // 909 Token not valid
            // 910 Token Expired
            account.setProperty("apiToken", Property.NULL);
            return true;
        } else {
            return false;
        }
    }

    private AccountInfo fetchAccountInfo_API(final Account account, final AccountInfo ai) throws Exception {
        try {
            loginToken(account);
        } catch (PluginException e) {
            account.setValid(false);
            return ai;
        }
        return account.getAccountInfo();
    }

    private void handlePremium_API(final DownloadLink downloadLink, final Account account) throws Exception {
        // No API method for linkchecking, but can done based on this request response!
        getPage(br, apiURL + "/files/download?file=" + Encoding.urlEncode(downloadLink.getDownloadURL()), account);
        final String ddlink = getJson("download_url");
        if (ddlink == null) {
            final String code = getJson("code");
            if ("700".equalsIgnoreCase(code) || "701".equalsIgnoreCase(code)) {
                // 700 File url not valid
                // 701 File removed
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            } else if ("702".equalsIgnoreCase(code) || "703".equalsIgnoreCase(code)) {
                // 702 File blocked
                // 703 File download prohibited
                /*
                 * not sure about this either, blocked..why ?? download prohibited..why ??
                 */
                // set this for now...
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE);
            } else if ("704".equalsIgnoreCase(code)) {
                // 704 Not enough traffic
                /*
                 * This is a problem under current JD frame work.. For example: the file downloaded could be 10GiB, the hoster prevents
                 * because user has only 5GiB traffic left... yet we can't disable the account due to this, because they have 5GB traffic
                 * left. Also could be due to out of sync account stats? we don't have an exception to handle this...
                 */
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE);
            } else {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT, "Unhandled Error code or 'download_url' could not be found");
            }
        }
        br.setFollowRedirects(true);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, ddlink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            if (dl.getConnection().getResponseCode() == 404) {
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error 404", 30 * 60 * 1000l);
            }
            logger.warning("The final dllink seems not to be a file!");
            br.followConnection();
            handleGeneralErrors(account);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        // add download slot
        controlSlot(+1, account);
        try {
            dl.startDownload();
        } finally {
            // remove download slot
            controlSlot(-1, account);
        }

    }

    private String loginToken(final Account account) throws Exception {
        final Browser nbr = new Browser();
        prepApiBrowser(nbr);
        nbr.getPage(apiURL + "/users/auth?login=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()));
        final String apiToken = getJson(nbr, "token");
        final String code = getJson(nbr, "code");
        if (apiToken != null) {
            account.setProperty("apiToken", apiToken);
            // all is good! lets update account info whilst we are at it. It's all here!
            AccountInfo ai = new AccountInfo();
            final String traffic_left = getJson(nbr, "traffic_left");
            final String primium_till = getJson(nbr, "premium_till");
            final String space_left = getJson(nbr, "space_left");
            if (primium_till != null) {
                // premium_till - time when user premium had expired, unix_timestamp, int (0 -no premium access)
                ai.setValidUntil(Long.parseLong(primium_till + "000"));
                if (!"0".equalsIgnoreCase(primium_till) || !ai.isExpired()) {
                    account.setProperty("free", false);
                    account.setProperty("totalMaxSim", 20);
                    ai.setStatus("Premium Account");
                } else {
                    account.setProperty("free", true);
                    account.setProperty("totalMaxSim", 1);
                    ai.setStatus("Free Account");
                    ai.setUnlimitedTraffic();
                    ai.setValidUntil(-1);
                }
            }
            if (traffic_left != null) {
                // traffic_left - user traffic left bytes for download files, int (-1 unlimited, 0 no traffic left)
                ai.setTrafficLeft(Long.parseLong(traffic_left));
            }
            if (space_left != null) {
                // space_left - user storage space left bytes for upload files, int ( -1 unlimited, 0 no space)
                // we only have space used not space left...
                // ai.setUsedSpace(Long.parseLong(space_left));
            }
            account.setAccountInfo(ai);
        } else if ("900".equalsIgnoreCase(code) || "901".equalsIgnoreCase(code) || "902".equalsIgnoreCase(code) || "903".equalsIgnoreCase(code)) {
            // 900 User not found
            // 901 Login can not be empty
            // 902 Password can not be empty
            // 903 User inactive
            throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\n" + errorMsg(nbr), PluginException.VALUE_ID_PREMIUM_DISABLE);
        }
        return apiToken;
    }

    /**
     * Tries to return value of key from JSon response, from String source.
     * 
     * @author raztoki
     * */
    private String getJson(final String source, final String key) {
        String result = new Regex(source, "\"" + key + "\":(-?\\d+(\\.\\d+)?|true|false|null)").getMatch(0);
        if (result == null) {
            result = new Regex(source, "\"" + key + "\":\"([^\"]+)\"").getMatch(0);
        }
        if (result != null) {
            result = result.replaceAll("\\\\/", "/");
        }
        return result;
    }

    /**
     * Tries to return value of key from JSon response, from default 'br' Browser.
     * 
     * @author raztoki
     * */
    private String getJson(final String key) {
        return getJson(br.toString(), key);
    }

    /**
     * Tries to return value of key from JSon response, from provided Browser.
     * 
     * @author raztoki
     * */
    private String getJson(final Browser ibr, final String key) {
        return getJson(ibr.toString(), key);
    }

    private static Object        CTRLLOCK = new Object();

    private static AtomicInteger maxPrem  = new AtomicInteger(1);
    private static AtomicInteger maxFree  = new AtomicInteger(1);

    /**
     * Prevents more than one free download from starting at a given time. One step prior to dl.startDownload(), it adds a slot to maxFree
     * which allows the next singleton download to start, or at least try.
     * 
     * This is needed because xfileshare(website) only throws errors after a final dllink starts transferring or at a given step within pre
     * download sequence. But this template(XfileSharingProBasic) allows multiple slots(when available) to commence the download sequence,
     * this.setstartintival does not resolve this issue. Which results in x(20) captcha events all at once and only allows one download to
     * start. This prevents wasting peoples time and effort on captcha solving and|or wasting captcha trading credits. Users will experience
     * minimal harm to downloading as slots are freed up soon as current download begins.
     * 
     * @param controlSlot
     *            (+1|-1)
     * @author raztoki
     * */
    private void controlSlot(final int num, final Account account) {
        synchronized (CTRLLOCK) {
            if (account == null) {
                int was = maxFree.get();
                maxFree.set(Math.min(Math.max(1, maxFree.addAndGet(num)), totalMaxSimultanFreeDownload.get()));
                logger.info("maxFree was = " + was + " && maxFree now = " + maxFree.get());
            } else {
                int was = maxPrem.get();
                maxPrem.set(Math.min(Math.max(1, maxPrem.addAndGet(num)), account.getIntegerProperty("totalMaxSim", 20)));
                logger.info("maxPrem was = " + was + " && maxPrem now = " + maxPrem.get());
            }
        }
    }

    private boolean isJava7nJDStable() {
        if (System.getProperty("jd.revision.jdownloaderrevision") == null && System.getProperty("java.version").matches("1\\.[7-9].+")) {
            return true;
        } else {
            return false;
        }
    }

    private static AtomicBoolean stableSucks = new AtomicBoolean(false);

    public static void showSSLWarning(final String domain) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    try {
                        String lng = System.getProperty("user.language");
                        String message = null;
                        String title = null;
                        boolean xSystem = CrossSystem.isOpenBrowserSupported();
                        if ("de".equalsIgnoreCase(lng)) {
                            title = domain + " :: Java 7+ && HTTPS Post Requests.";
                            message = "Wegen einem Bug in in Java 7+ in dieser JDownloader version koennen wir keine HTTPS Post Requests ausfuehren.\r\n";
                            message += "Wir haben eine Notloesung ergaenzt durch die man weiterhin diese JDownloader Version nutzen kann.\r\n";
                            message += "Bitte bedenke, dass HTTPS Post Requests als HTTP gesendet werden. Nutzung auf eigene Gefahr!\r\n";
                            message += "Falls du keine unverschluesselten Daten versenden willst, update bitte auf JDownloader 2!\r\n";
                            if (xSystem) {
                                message += "JDownloader 2 Installationsanleitung und Downloadlink: Klicke -OK- (per Browser oeffnen)\r\n ";
                            } else {
                                message += "JDownloader 2 Installationsanleitung und Downloadlink:\r\n" + new URL("http://board.jdownloader.org/showthread.php?t=37365") + "\r\n";
                            }
                        } else if ("es".equalsIgnoreCase(lng)) {
                            title = domain + " :: Java 7+ && HTTPS Solicitudes Post.";
                            message = "Debido a un bug en Java 7+, al utilizar esta versión de JDownloader, no se puede enviar correctamente las solicitudes Post en HTTPS\r\n";
                            message += "Por ello, hemos añadido una solución alternativa para que pueda seguir utilizando esta versión de JDownloader...\r\n";
                            message += "Tenga en cuenta que las peticiones Post de HTTPS se envían como HTTP. Utilice esto a su propia discreción.\r\n";
                            message += "Si usted no desea enviar información o datos desencriptados, por favor utilice JDownloader 2!\r\n";
                            if (xSystem) {
                                message += " Las instrucciones para descargar e instalar Jdownloader 2 se muestran a continuación: Hacer Click en -Aceptar- (El navegador de internet se abrirá)\r\n ";
                            } else {
                                message += " Las instrucciones para descargar e instalar Jdownloader 2 se muestran a continuación, enlace :\r\n" + new URL("http://board.jdownloader.org/showthread.php?t=37365") + "\r\n";
                            }
                        } else {
                            title = domain + " :: Java 7+ && HTTPS Post Requests.";
                            message = "Due to a bug in Java 7+ when using this version of JDownloader, we can not successfully send HTTPS Post Requests.\r\n";
                            message += "We have added a work around so you can continue to use this version of JDownloader...\r\n";
                            message += "Please be aware that HTTPS Post Requests are sent as HTTP. Use at your own discretion.\r\n";
                            message += "If you do not want to send unecrypted data, please upgrade to JDownloader 2!\r\n";
                            if (xSystem) {
                                message += "Jdownloader 2 install instructions and download link: Click -OK- (open in browser)\r\n ";
                            } else {
                                message += "JDownloader 2 install instructions and download link:\r\n" + new URL("http://board.jdownloader.org/showthread.php?t=37365") + "\r\n";
                            }
                        }
                        int result = JOptionPane.showConfirmDialog(jd.gui.swing.jdgui.JDGui.getInstance().getMainFrame(), message, title, JOptionPane.CLOSED_OPTION, JOptionPane.CLOSED_OPTION);
                        if (xSystem && JOptionPane.OK_OPTION == result) {
                            CrossSystem.openURL(new URL("http://board.jdownloader.org/showthread.php?t=37365"));
                        }
                        stableSucks.set(true);
                    } catch (Throwable e) {
                    }
                }
            });
        } catch (Throwable e) {
        }
    }

}