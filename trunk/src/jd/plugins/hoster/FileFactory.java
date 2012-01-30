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

package jd.plugins.hoster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.http.Browser;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.Plugin;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "filefactory.com" }, urls = { "http://[\\w\\.]*?filefactory\\.com(/|//)file/[\\w]+/?" }, flags = { 2 })
public class FileFactory extends PluginForHost {

    private static final String  FILESIZE         = "id=\"info\" class=\"metadata\">[\t\n\r ]+<span>(.*?) file uploaded";
    private static AtomicInteger maxPrem          = new AtomicInteger(1);
    private static final String  NO_SLOT          = ">All free download slots are in use";
    private static final String  NO_SLOT_USERTEXT = "No free slots available";
    private static final String  NOT_AVAILABLE    = "class=\"box error\"";
    private static final String  SERVERFAIL       = "(<p>Your download slot has expired\\.|>Unfortunately the file you have requested cannot be downloaded at this time)";
    private static final String  LOGIN_ERROR      = "The email or password you have entered is incorrect";
    private static final String  SERVER_DOWN      = "server hosting the file you are requesting is currently down";
    private static final String  CAPTCHALIMIT     = "<p>We have detected several recent attempts to bypass our free download restrictions originating from your IP Address";
    private static final Object  LOCK             = new Object();
    private static final String  COOKIE_HOST      = "http://filefactory.com/";
    private String               dlUrl            = null;

    public FileFactory(final PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://www.filefactory.com/info/premium.php");
    }

    public void checkErrors() throws PluginException {
        if (this.br.containsHTML("This file is only available to Premium Members")) { throw new PluginException(LinkStatus.ERROR_FATAL, "This file is only available to Premium Members"); }
        if (br.containsHTML(CAPTCHALIMIT)) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 10 * 60 * 1000l);
        if (this.br.containsHTML(FileFactory.SERVERFAIL)) { throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error"); }
        if (this.br.containsHTML(NO_SLOT)) { throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, NO_SLOT_USERTEXT, 10 * 60 * 1000l); }
        if (this.br.containsHTML(FileFactory.NOT_AVAILABLE)) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        if (this.br.containsHTML(FileFactory.SERVER_DOWN) || this.br.containsHTML(FileFactory.NO_SLOT)) { throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 20 * 60 * 1000l); }
        if (this.br.getRegex("Please wait (\\d+) minutes to download more files, or").getMatch(0) != null) { throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, Integer.parseInt(this.br.getRegex("Please wait (\\d+) minutes to download more files, or").getMatch(0)) * 60 * 1001l); }
    }

    @Override
    public boolean checkLinks(final DownloadLink[] urls) {
        if (urls == null || urls.length == 0) { return false; }
        try {
            final Browser br = new Browser();
            this.br.getHeaders().put("Accept-Encoding", "");
            br.forceDebug(true);
            final StringBuilder sb = new StringBuilder();
            br.setCookiesExclusive(true);
            final ArrayList<DownloadLink> links = new ArrayList<DownloadLink>();
            int index = 0;
            while (true) {
                br.getPage("http://filefactory.com/tool/links.php");
                links.clear();
                while (true) {
                    if (index == urls.length || links.size() > 25) {
                        break;
                    }
                    links.add(urls[index]);
                    index++;
                }
                sb.delete(0, sb.capacity());
                sb.append("func=links&links=");
                for (final DownloadLink dl : links) {
                    sb.append(Encoding.urlEncode(dl.getDownloadURL()));
                    sb.append("%0D%0A");
                }
                br.postPage("http://filefactory.com/tool/links.php", sb.toString());
                for (final DownloadLink dl : links) {
                    final String size = br.getRegex("div class=\"metadata\".*?" + dl.getDownloadURL() + ".*?</div>.*?</td>.*?<td>(.*?)</td>").getMatch(0);
                    final String name = br.getRegex("<a href=.*?" + dl.getDownloadURL() + ".*?\">(.*?)<").getMatch(0);
                    if (name != null && size != null) {
                        dl.setName(name.trim());
                        dl.setDownloadSize(SizeFormatter.getSize(size.trim()));
                        dl.setAvailable(true);
                    } else {
                        dl.setAvailable(false);
                    }
                }
                if (index == urls.length) {
                    break;
                }
            }
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replaceAll(".com//", ".com/"));
        link.setUrlDownload(link.getDownloadURL().replaceAll("http://filefactory", "http://www.filefactory"));
    }

    @Override
    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        final AccountInfo ai = new AccountInfo();
        /* reset maxPrem workaround on every fetchaccount info */
        maxPrem.set(1);
        try {
            this.login(account, true);
        } catch (final PluginException e) {
            account.setValid(false);
            return ai;
        }
        this.br.getPage("http://www.filefactory.com/member/");
        Regex someDetails = br.getRegex(">Files:</span>[\t\n\r ]+<span style=\"\">(\\d+) \\((.*?)\\)</span>");
        String filesNum = someDetails.getMatch(0);
        String usedSpace = someDetails.getMatch(1);
        if (filesNum != null) ai.setFilesNum(Integer.parseInt(filesNum));
        if (usedSpace != null) ai.setUsedSpace(SizeFormatter.getSize(usedSpace.trim()));
        if (br.containsHTML(">Membership:</span>[\t\n\r ]+<span>Free <a")) {
            ai.setStatus("Registered (free) User");
            ai.setUnlimitedTraffic();
            account.setProperty("freeAccount", "true");
            try {
                maxPrem.set(1);
                account.setMaxSimultanDownloads(1);
            } catch (final Throwable e) {
            }
        } else {
            account.setProperty("freeAccount", null);
            String expire = this.br.getMatch("Your account is valid until the <strong>(.*?)</strong>");
            if (expire == null) {
                account.setValid(false);
                return ai;
            }
            expire = expire.replaceFirst("([^\\d].*?) ", " ");
            ai.setValidUntil(TimeFormatter.getMilliSeconds(expire, "dd MMMM, yyyy", Locale.UK));
            final String loaded = this.br.getRegex("You have downloaded(.*?)out").getMatch(0);
            String max = this.br.getRegex("limit of(.*?\\. )").getMatch(0);
            if (max != null && loaded != null) {
                ai.setTrafficMax(SizeFormatter.getSize(max));
                ai.setTrafficLeft(ai.getTrafficMax() - SizeFormatter.getSize(loaded));
            } else {
                max = this.br.getRegex("You can now download up to(.*?)in").getMatch(0);
                if (max != null) ai.setTrafficLeft(SizeFormatter.getSize(max));
            }
            try {
                this.br.getPage("http://www.filefactory.com/reward/summary.php");
                final String points = this.br.getMatch("Available reward points.*?class=\"amount\">(.*?) points");
                if (points != null) {
                    /* not always enough info available to calculate points */
                    ai.setPremiumPoints(Long.parseLong(points.replaceAll("\\,", "").trim()));
                }
            } catch (final Throwable e) {
            }
            ai.setStatus("Premium User");
            try {
                maxPrem.set(-1);
                account.setMaxSimultanDownloads(-1);
            } catch (final Throwable e) {
            }
        }
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://www.filefactory.com/legal/terms.php";
    }

    @Override
    public int getMaxRetries() {
        return 20;
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        /* workaround for free/premium issue on stable 09581 */
        return maxPrem.get();
    }

    @Override
    public int getTimegapBetweenConnections() {
        return 200;
    }

    public String getUrl() throws IOException, PluginException {
        String url = this.br.getRegex("<div.*?id=\"downloadLink\".*?>.*?<a .*?href=\"(.*?)\".*?\"downloadLinkTarget").getMatch(0);
        if (url == null) url = this.br.getRegex("greyDownload\".*?<div.*?id=\"free\".*?>.*?<a .*?href=\"(http.*?)\".*?\"downloadLinkTarget").getMatch(0);
        if (url == null) {
            Context cx = null;
            try {
                cx = ContextFactory.getGlobal().enterContext();
                final Scriptable scope = cx.initStandardObjects();
                final String[] eval = this.br.getRegex("var (.*?) = (.*?), (.*?) = (.*?)+\"(.*?)\", (.*?) = (.*?), (.*?) = (.*?), (.*?) = (.*?), (.*?) = (.*?), (.*?) = (.*?);").getRow(0);
                if (eval != null) {
                    // first load js
                    Object result = cx.evaluateString(scope, "function g(){return " + eval[1] + "} g();", "<cmd>", 1, null);
                    final String link = "/file" + result + eval[4];
                    this.br.getPage("http://www.filefactory.com" + link);
                    final String[] row = this.br.getRegex("var (.*?) = '';(.*;) (.*?)=(.*?)\\(\\);").getRow(0);
                    result = cx.evaluateString(scope, row[1] + row[3] + " ();", "<cmd>", 1, null);
                    if (result.toString().startsWith("http")) {
                        url = result + "";
                    } else {
                        url = "http://www.filefactory.com" + result;
                    }
                } else {
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }
            } finally {
                if (cx != null) Context.exit();
            }
        }
        return url;

    }

    @Override
    public void handleFree(final DownloadLink parameter) throws Exception {
        this.requestFileInformation(parameter);
        this.handleFree0(parameter);
    }

    public void handleFree0(final DownloadLink parameter) throws Exception {
        try {
            long waittime;
            if (br.getRedirectLocation() != null) br.getPage(br.getRedirectLocation());
            if (dlUrl != null) {
                logger.finer("DIRECT free-download");
                this.br.setFollowRedirects(true);
                this.dl = jd.plugins.BrowserAdapter.openDownload(this.br, parameter, dlUrl, true, 1);
            } else {
                this.checkErrors();
                String urlWithFilename = null;
                if (this.br.containsHTML("recaptcha_ajax\\.js")) {
                    urlWithFilename = this.handleRecaptcha(this.br, parameter);
                } else {
                    urlWithFilename = this.getUrl();
                }
                if (urlWithFilename == null) {
                    logger.warning("getUrl is broken!");
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }
                this.br.setFollowRedirects(true);
                this.br.getPage(urlWithFilename);
                String wait = this.br.getRegex("class=\"countdown\">(\\d+)</span>").getMatch(0);
                if (wait != null) {
                    waittime = Long.parseLong(wait) * 1000l;
                    if (waittime > 60000) { throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, waittime); }
                }
                this.checkErrors();
                String downloadUrl = this.getUrl();
                if (downloadUrl == null) {
                    logger.warning("getUrl is broken!");
                    throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                }

                wait = this.br.getRegex("class=\"countdown\">(\\d+)</span>").getMatch(0);
                waittime = 60 * 1000l;
                if (wait != null) {
                    waittime = Long.parseLong(wait) * 1000l;
                }
                if (waittime > 60000l) { throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, waittime); }
                waittime += 1000;
                this.sleep(waittime, parameter);
                this.br.setFollowRedirects(true);
                this.dl = jd.plugins.BrowserAdapter.openDownload(this.br, parameter, downloadUrl);
            }
            // Prüft ob content disposition header da sind
            if (this.dl.getConnection().isContentDisposition()) {
                this.dl.startDownload();
            } else {
                this.br.followConnection();
                if (this.br.containsHTML("have exceeded the download limit")) {
                    waittime = 10 * 60 * 1000l;
                    try {
                        waittime = Long.parseLong(this.br.getRegex("Please wait (\\d+) minutes to download more files").getMatch(0)) * 1000l;
                    } catch (final Exception e) {
                    }
                    if (waittime > 0) { throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, waittime); }
                }
                if (this.br.containsHTML("You are currently downloading too many files at once")) { throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 10 * 60 * 1000l); }
                this.checkErrors();
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
        } catch (final PluginException e4) {
            throw e4;
        } catch (final InterruptedException e2) {
            return;
        } catch (final IOException e) {
            logger.log(java.util.logging.Level.SEVERE, "Exception occurred", e);
            if (e.getMessage() != null && e.getMessage().contains("502")) {
                logger.severe("Filefactory returned Bad gateway.");
                Thread.sleep(1000);
                throw new PluginException(LinkStatus.ERROR_RETRY);
            } else if (e.getMessage() != null && e.getMessage().contains("503")) {
                logger.severe("Filefactory returned Bad gateway.");
                Thread.sleep(1000);
                throw new PluginException(LinkStatus.ERROR_RETRY);
            } else {
                throw e;
            }
        }
    }

    @Override
    public void handlePremium(final DownloadLink downloadLink, final Account account) throws Exception {
        this.requestFileInformation(downloadLink);
        this.login(account, false);
        this.br.setFollowRedirects(false);
        this.br.getPage(downloadLink.getDownloadURL());
        if (account.getProperty("freeAccount") != null) {
            this.handleFree0(downloadLink);
        } else {
            this.br.setFollowRedirects(true);
            this.dl = jd.plugins.BrowserAdapter.openDownload(this.br, downloadLink, this.br.getRedirectLocation(), true, 0);
            if (!this.dl.getConnection().isContentDisposition()) {
                this.br.followConnection();
                if (this.br.containsHTML(FileFactory.NOT_AVAILABLE)) {
                    throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                } else if (this.br.containsHTML(FileFactory.SERVER_DOWN)) {
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 20 * 60 * 1000l);
                } else {
                    String red = this.br.getRegex(Pattern.compile("10px 0;\">.*<a href=\"(.*?)\">Download with FileFactory Premium", Pattern.DOTALL)).getMatch(0);
                    if (red == null) red = this.br.getRegex("subPremium.*?ready.*?<a href=\"(.*?)\"").getMatch(0);
                    logger.finer("Indirect download");
                    this.br.setFollowRedirects(true);
                    this.dl = jd.plugins.BrowserAdapter.openDownload(this.br, downloadLink, red, true, 0);
                    if (!this.dl.getConnection().isContentDisposition()) {
                        this.br.followConnection();
                        if (br.containsHTML("Unfortunately we have encountered a problem locating your file")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                        throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                    }
                }
            } else {
                logger.finer("DIRECT download");
            }
            this.dl.startDownload();
        }
    }

    public String handleRecaptcha(final Browser br, final DownloadLink link) throws Exception {
        final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
        final jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
        final String id = br.getRegex("Recaptcha\\.create\\(\"(.*?)\"").getMatch(0);
        rc.setId(id);
        final Form form = new Form();
        form.setAction("/file/checkCaptcha.php");
        final String check = br.getRegex("check:'(.*?)'").getMatch(0);
        form.put("check", check);
        rc.setForm(form);
        rc.load();
        final File cf = rc.downloadCaptcha(this.getLocalCaptchaFile());
        final String c = this.getCaptchaCode(cf, link);
        rc.setCode(c);
        if (br.containsHTML(CAPTCHALIMIT)) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 10 * 60 * 1000l);
        if (!br.containsHTML("status:\"ok")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        final String url = br.getRegex("path:\"(.*?)\"").getMatch(0);
        if (url == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        if (url.startsWith("http")) { return url; }
        return "http://www.filefactory.com" + url;
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasAutoCaptcha() {
        return true;
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasCaptcha() {
        return true;
    }

    private void login(final Account account, final boolean force) throws Exception {
        synchronized (LOCK) {
            // Load cookies
            try {
                this.setBrowserExclusive();
                final Object ret = account.getProperty("cookies", null);
                boolean acmatch = Encoding.urlEncode(account.getUser()).matches(account.getStringProperty("name", Encoding.urlEncode(account.getUser())));
                if (acmatch) acmatch = Encoding.urlEncode(account.getPass()).matches(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
                if (acmatch && ret != null && ret instanceof HashMap<?, ?> && !force) {
                    final HashMap<String, String> cookies = (HashMap<String, String>) ret;
                    if (account.isValid()) {
                        for (final Map.Entry<String, String> cookieEntry : cookies.entrySet()) {
                            final String key = cookieEntry.getKey();
                            final String value = cookieEntry.getValue();
                            this.br.setCookie(COOKIE_HOST, key, value);
                        }
                        return;
                    }
                }
                this.br.getHeaders().put("Accept-Encoding", "gzip");
                this.br.setFollowRedirects(true);
                this.br.getPage("http://filefactory.com");
                this.br.postPage("http://filefactory.com/member/login.php", "redirect=%2F&email=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()));
                if (this.br.containsHTML(FileFactory.LOGIN_ERROR)) { throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE); }
                // Save cookies
                final HashMap<String, String> cookies = new HashMap<String, String>();
                final Cookies add = this.br.getCookies(COOKIE_HOST);
                for (final Cookie c : add.getCookies()) {
                    cookies.put(c.getKey(), c.getValue());
                }
                account.setProperty("name", Encoding.urlEncode(account.getUser()));
                account.setProperty("pass", Encoding.urlEncode(account.getPass()));
                account.setProperty("cookies", cookies);
            } catch (final PluginException e) {
                account.setProperty("cookies", null);
                throw e;
            }
        }
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink downloadLink) throws Exception {
        this.setBrowserExclusive();
        this.br.getHeaders().put("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.24) Gecko/20111107 Ubuntu/10.10 (maverick) Firefox/3.6.24");
        this.br.getHeaders().put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        this.br.getHeaders().put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        this.br.getHeaders().put("Accept-Language", "de,de-de;q=0.7,en;q=0.3");
        this.br.getHeaders().put("Cache-Control", null);
        this.br.getHeaders().put("Pragma", null);
        this.br.setFollowRedirects(true);
        for (int i = 0; i < 4; i++) {
            try {
                Thread.sleep(200);
            } catch (final Exception e) {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            URLConnectionAdapter con = null;
            try {
                dlUrl = null;
                con = this.br.openGetConnection(downloadLink.getDownloadURL());
                if (con.isContentDisposition()) {
                    downloadLink.setFinalFileName(Plugin.getFileNameFromHeader(con));
                    downloadLink.setDownloadSize(con.getLongContentLength());
                    con.disconnect();
                    dlUrl = downloadLink.getDownloadURL();
                    return AvailableStatus.TRUE;
                } else {
                    br.followConnection();
                }
                break;
            } catch (final Exception e) {
                if (i == 3) { throw e; }
            } finally {
                try {
                    con.disconnect();
                } catch (final Throwable e) {
                }
            }
        }
        if (this.br.containsHTML("This file has been deleted\\.")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        if (this.br.containsHTML(FileFactory.NOT_AVAILABLE) && !this.br.containsHTML(NO_SLOT)) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        } else if (this.br.containsHTML(FileFactory.SERVER_DOWN)) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        } else {
            if (this.br.containsHTML("This file is only available to Premium Members")) {
                downloadLink.getLinkStatus().setErrorMessage("This file is only available to Premium Members");
                downloadLink.getLinkStatus().setStatusText("This file is only available to Premium Members");
            } else if (this.br.containsHTML(NO_SLOT)) {
                downloadLink.getLinkStatus().setErrorMessage(JDL.L("plugins.hoster.filefactorycom.errors.nofreeslots", NO_SLOT_USERTEXT));
                downloadLink.getLinkStatus().setStatusText(JDL.L("plugins.hoster.filefactorycom.errors.nofreeslots", NO_SLOT_USERTEXT));
            } else {
                if (this.br.containsHTML("File Not Found")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
                final String fileName = this.br.getRegex("<title>(.*?) - download now for free").getMatch(0);
                final String fileSize = this.br.getRegex(FileFactory.FILESIZE).getMatch(0);
                if (fileName == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
                downloadLink.setName(fileName.trim());
                if (fileSize != null) downloadLink.setDownloadSize(SizeFormatter.getSize(fileSize));
            }

        }
        this.br.setFollowRedirects(false);
        return AvailableStatus.TRUE;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
    }

}