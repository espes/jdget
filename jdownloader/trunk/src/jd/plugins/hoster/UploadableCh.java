//jDownloader - Downloadmanager
//Copyright (C) 2010  JD-Team support@jdownloader.org
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "uploadable.ch" }, urls = { "http://(www\\.)?uploadable\\.ch/file/[A-Za-z0-9]+" }, flags = { 2 })
public class UploadableCh extends PluginForHost {

    public UploadableCh(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://www.uploadable.ch/extend.php");
    }

    @Override
    public String getAGBLink() {
        return "http://www.uploadable.ch/terms.php";
    }

    private static AtomicInteger maxPrem        = new AtomicInteger(1);

    private static final long    FREE_SIZELIMIT = 1073741824;

    // TODO: Maybe implement mass linkchecker: http://www.uploadable.ch/check.php
    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getHeaders().put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0");
        br.getPage(link.getDownloadURL());
        if (br.containsHTML(">File not available<|>This file is no longer available.<")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        final String filename = br.getRegex("id=\"file_name\" title=\"([^<>\"]*?)\"").getMatch(0);
        final String filesize = br.getRegex("class=\"filename_normal\">\\(([^<>\"]*?)\\)</span>").getMatch(0);
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(Encoding.htmlDecode(filename.trim()));
        final long fsize = SizeFormatter.getSize(filesize);
        link.setDownloadSize(fsize);
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        doFree(downloadLink);
    }

    private void doFree(final DownloadLink downloadLink) throws Exception {
        br.setFollowRedirects(false);
        String dllink = checkDirectLink(downloadLink, "uploadabledirectlink");
        if (dllink == null) {
            final String fid = new Regex(downloadLink.getDownloadURL(), "([A-Za-z0-9]+)$").getMatch(0);
            final String postLink = br.getURL();
            br.getHeaders().put("Accept", "application/json, text/javascript, */*");
            br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
            br.cloneBrowser().getPage("http://www.uploadable.ch/now.php");
            br.postPage(postLink, "downloadLink=wait");
            int wait = 90;
            final String waittime = br.getRegex("\"waitTime\":(\\d+)").getMatch(0);
            if (waittime != null) wait = Integer.parseInt(waittime);
            sleep(wait * 1001l, downloadLink);
            br.postPage(postLink, "checkDownload=check");
            if (br.containsHTML("\"fail\":\"timeLimit\"")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 20 * 60 * 1001l);
            final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
            final jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
            rc.setId("6LdlJuwSAAAAAPJbPIoUhyqOJd7-yrah5Nhim5S3");
            rc.load();
            for (int i = 1; i <= 5; i++) {
                final File cf = rc.downloadCaptcha(getLocalCaptchaFile());
                final String c = getCaptchaCode(cf, downloadLink);
                br.postPage("http://www.uploadable.ch/checkReCaptcha.php", "recaptcha_challenge_field=" + rc.getChallenge() + "&recaptcha_response_field=" + Encoding.urlEncode(c) + "&recaptcha_shortencode_field=" + fid);
                if (br.containsHTML("\"success\":0")) {
                    rc.reload();
                    continue;
                }
                break;
            }
            if (br.containsHTML("\"success\":0")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);

            br.getHeaders().put("Accept", "*/*");
            br.getHeaders().put("Accept-Language", "de,en-us;q=0.7,en;q=0.3");
            br.getHeaders().put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            br.getHeaders().put("Referer", postLink);
            br.postPage("http://www.uploadable.ch/file/" + fid, "downloadLink=show");
            if (br.containsHTML("fail")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 5 * 60 * 1000l);
            br.postPage("http://www.uploadable.ch/file/" + fid, "download=normal");
            dllink = br.getRedirectLocation();
            if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        downloadLink.setProperty("uploadabledirectlink", dllink);
        dl.startDownload();
    }

    private String checkDirectLink(final DownloadLink downloadLink, final String property) {
        String dllink = downloadLink.getStringProperty(property);
        if (dllink != null) {
            try {
                final Browser br2 = br.cloneBrowser();
                URLConnectionAdapter con = br2.openGetConnection(dllink);
                if (con.getContentType().contains("html") || con.getLongContentLength() == -1) {
                    downloadLink.setProperty(property, Property.NULL);
                    dllink = null;
                }
                con.disconnect();
            } catch (final Exception e) {
                downloadLink.setProperty(property, Property.NULL);
                dllink = null;
            }
        }
        return dllink;
    }

    private static final String MAINPAGE = "http://uploadable.ch";
    private static Object       LOCK     = new Object();

    @SuppressWarnings("unchecked")
    private void login(final Account account, final boolean force) throws Exception {
        synchronized (LOCK) {
            try {
                // Load cookies
                br.setCookiesExclusive(true);
                final Object ret = account.getProperty("cookies", null);
                boolean acmatch = Encoding.urlEncode(account.getUser()).equals(account.getStringProperty("name", Encoding.urlEncode(account.getUser())));
                if (acmatch) acmatch = Encoding.urlEncode(account.getPass()).equals(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
                if (acmatch && ret != null && ret instanceof HashMap<?, ?> && !force) {
                    final HashMap<String, String> cookies = (HashMap<String, String>) ret;
                    if (account.isValid()) {
                        for (final Map.Entry<String, String> cookieEntry : cookies.entrySet()) {
                            final String key = cookieEntry.getKey();
                            final String value = cookieEntry.getValue();
                            br.setCookie(MAINPAGE, key, value);
                        }
                        return;
                    }
                }
                br.setFollowRedirects(false);
                br.postPage("http://www.uploadable.ch/login.php", "autoLogin=on&action__login=normalLogin&userName=" + Encoding.urlEncode(account.getUser()) + "&userPassword=" + Encoding.urlEncode(account.getPass()));
                if (br.getCookie(MAINPAGE, "autologin") == null || !br.containsHTML("class=\"icon logout\"")) {
                    if ("de".equalsIgnoreCase(System.getProperty("user.language"))) {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nUngültiger Benutzername oder ungültiges Passwort!\r\nSchnellhilfe: \r\nDu bist dir sicher, dass dein eingegebener Benutzername und Passwort stimmen?\r\nFalls dein Passwort Sonderzeichen enthält, ändere es und versuche es erneut!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    } else {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nInvalid username/password!\r\nQuick help:\r\nYou're sure that the username and password you entered are correct?\r\nIf your password contains special characters, change it (remove them) and try again!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    }
                }
                // Save cookies
                final HashMap<String, String> cookies = new HashMap<String, String>();
                final Cookies add = br.getCookies(MAINPAGE);
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
        try {
            login(account, true);
        } catch (PluginException e) {
            account.setValid(false);
            throw e;
        }
        br.getPage("http://www.uploadable.ch/indexboard.php");
        final String space = br.getRegex(">Storage</div>[\t\r\n ]+<div class=\"b_blue_type\">([^\"/]*?)</span>").getMatch(0);
        if (space != null) ai.setUsedSpace(space.trim().replace("<span>", ""));
        final String expiredate = br.getRegex("lass=\"grey_type\">[\r\n\t ]+Until([^<>\"]*?)</div>").getMatch(0);
        if (expiredate == null) {
            try {
                maxPrem.set(1);
                // free accounts can still have captcha.
                account.setMaxSimultanDownloads(maxPrem.get());
                account.setConcurrentUsePossible(false);
            } catch (final Throwable e) {
                // not available in old Stable 0.9.581
            }
            account.setProperty("nopremium", true);
            ai.setStatus("Registered (free) user");
        } else {
            ai.setValidUntil(TimeFormatter.getMilliSeconds(expiredate.trim(), "dd MMM yyyy", Locale.ENGLISH));
            try {
                maxPrem.set(20);
                account.setMaxSimultanDownloads(maxPrem.get());
                account.setConcurrentUsePossible(true);
            } catch (final Throwable e) {
                // not available in old Stable 0.9.581
            }
            account.setProperty("nopremium", false);
            ai.setStatus("Premium user");
        }
        ai.setUnlimitedTraffic();
        account.setValid(true);
        return ai;
    }

    @Override
    public void handlePremium(final DownloadLink link, final Account account) throws Exception {
        requestFileInformation(link);
        login(account, false);
        if (account.getBooleanProperty("nopremium", false)) {
            doFree(link);
        } else {
            br.setFollowRedirects(false);
            /* This way we don't have to care about the users' "instant download" setting */
            br.postPage(link.getDownloadURL(), "download=premium");
            final String dllink = br.getRedirectLocation();
            if (dllink == null) {
                logger.warning("Final link is null");
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            dl = jd.plugins.BrowserAdapter.openDownload(br, link, dllink, true, 0);
            if (dl.getConnection().getContentType().contains("html")) {
                br.followConnection();
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            dl.startDownload();
        }
    }

    public boolean canHandle(DownloadLink downloadLink, Account account) {
        if ((account == null || account.getBooleanProperty("free", false)) && downloadLink.getDownloadSize() > FREE_SIZELIMIT)
            return false;
        else
            return true;
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return 1;
    }

    @Override
    public void reset() {
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
    }

}