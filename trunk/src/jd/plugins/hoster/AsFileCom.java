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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jd.PluginWrapper;
import jd.http.Cookie;
import jd.http.Cookies;
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

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "asfile.com" }, urls = { "http://(www\\.)?asfile\\.com/file/[A-Za-z0-9]+" }, flags = { 2 })
public class AsFileCom extends PluginForHost {

    public AsFileCom(PluginWrapper wrapper) {
        super(wrapper);
        this.setAccountwithoutUsername(true);
        this.enablePremium("http://asfile.com/en/index/pay");
    }

    @Override
    public String getAGBLink() {
        return "http://asfile.com/en/page/offer";
    }

    private static final Object LOCK     = new Object();
    private static final String MAINPAGE = "http://asfile.com";

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("(<title>ASfile\\.com</title>|>Page not found<)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        Regex fileInfo = br.getRegex("Download: <strong>([^<>\"\\']+)</strong><br/> \\(([^<>\"\\']+)\\)");
        String filename = br.getRegex("<meta name=\"title\" content=\"Free download ([^<>\"\\']+)\"").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("<title>Free download ([^<>\"\\']+)</title>").getMatch(0);
            if (filename == null) {
                filename = fileInfo.getMatch(0);
            }
        }
        String filesize = fileInfo.getMatch(1);
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(Encoding.htmlDecode(filename.trim()));
        if (filesize != null) link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        if (!br.containsHTML("/free\\-download/")) {
            if (br.containsHTML("This file is available only to premium users")) {
                try {
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_ONLY);
                } catch (final Throwable e) {
                    if (e instanceof PluginException) throw (PluginException) e;
                    throw new PluginException(LinkStatus.ERROR_FATAL, "Premium only");
                }
            }
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        final String fileID = new Regex(downloadLink.getDownloadURL(), "asfile\\.com/file/(.+)").getMatch(0);
        br.getPage("http://asfile.com/en/free-download/file/" + fileID);
        if (br.containsHTML("You have exceeded the download limit for today")) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED);
        final String hash = br.getRegex("hash: \\'([a-z0-9]+)\\'").getMatch(0);
        final String storage = br.getRegex("storage: \\'([^<>\"\\']+)\\'").getMatch(0);
        if (hash == null || storage == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        final String waittime = br.getRegex("class=\"orange\">(\\d+)</span>").getMatch(0);
        int wait = 60;
        if (waittime != null) wait = Integer.parseInt(waittime);
        sleep(wait * 1001l, downloadLink);
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.postPage("http://asfile.com/en/index/convertHashToLink", "hash=" + hash + "&path=" + fileID + "&storage=" + Encoding.urlEncode(storage) + "&name=" + Encoding.urlEncode(downloadLink.getName()));
        final String correctedBR = br.toString().replace("\\", "");
        String dllink = new Regex(correctedBR, "\"url\":\"(http:[^<>\"\\']+)\"").getMatch(0);
        if (dllink == null) dllink = new Regex(correctedBR, "\"(http://s\\d+\\.asfile\\.com/file/free/[a-z0-9]+/\\d+/[A-Za-z0-9]+/[^<>\"\\'/]+)\"").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @SuppressWarnings("unchecked")
    private void login(Account account, boolean force) throws Exception {
        br.setReadTimeout(3 * 60 * 1000);
        synchronized (LOCK) {
            // Load cookies
            try {
                br.setCookiesExclusive(true);
                br.setFollowRedirects(true);
                String user = account.getUser();
                if (user == null || user.trim().length() == 0) {
                    /* passCode only */
                    br.setCookie(MAINPAGE, "code", account.getPass());
                    return;
                }
                final Object ret = account.getProperty("cookies", null);
                boolean acmatch = Encoding.urlEncode(account.getUser()).equals(account.getStringProperty("name", Encoding.urlEncode(account.getUser())));
                if (acmatch) acmatch = Encoding.urlEncode(account.getPass()).equals(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
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
                br.setFollowRedirects(false);
                br.postPage(MAINPAGE + "/en/login", "login=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()));
                br.getPage(MAINPAGE + "/en/");
                if (br.containsHTML(">Fail login<")) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                if (!br.containsHTML("logout\">Logout ")) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                br.getPage("http://asfile.com/en/profile");
                if (!br.containsHTML("<p>Your account:<strong> premium")) {
                    logger.info("This is no premium account!");
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
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
                account.setProperty("cookies", null);
                throw e;
            }
        }
    }

    @Override
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        try {
            login(account, true);
        } catch (PluginException e) {
            account.setValid(false);
            return ai;
        }
        ai.setUnlimitedTraffic();
        String user = account.getUser();
        if (user != null && user.trim().length() > 0) {
            String expire = br.getRegex("premium </strong>\\(to (\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2})\\)</p>").getMatch(0);
            if (expire == null) {
                account.setValid(false);
                return ai;
            } else {
                ai.setValidUntil(TimeFormatter.getMilliSeconds(expire, "yyyy/MM/dd hh:mm", null));
            }
            ai.setStatus("Premium User");
        } else {
            br.getPage("http://asfile.com/en/index/pay");
            String expire = br.getRegex("You have got the premium access to: (\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2})</p>").getMatch(0);
            if (expire != null) ai.setValidUntil(TimeFormatter.getMilliSeconds(expire, "yyyy/MM/dd hh:mm", null));
            ai.setStatus("Passcode User");
        }
        account.setValid(true);
        return ai;
    }

    @Override
    public void handlePremium(DownloadLink link, Account account) throws Exception {
        requestFileInformation(link);
        login(account, false);
        /* try direct link */
        String dllink = link.getStringProperty("direct", null);
        if (dllink != null) {
            br.setFollowRedirects(true);
            dl = jd.plugins.BrowserAdapter.openDownload(br, link, dllink, true, 0);
            if (dl.getConnection().getContentType().contains("html")) {
                /* direct link no longer valid */
                br.followConnection();
                dllink = null;
            }
            if (dllink != null) {
                /* direct link still valid */
                link.setProperty("direct", dllink);
                dl.startDownload();
                return;
            }
        }
        br.setFollowRedirects(false);
        try {
            br.getPage("http://asfile.com/en/premium-download/file/" + new Regex(link.getDownloadURL(), "asfile\\.com/file/(.+)").getMatch(0));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("500")) {
                logger.severe("500 error->account seems invalid!");
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
            }
        }
        dllink = br.getRegex("\"(http://s\\d+\\.asfile\\.com/file/premium/[a-z0-9]+/\\d+/[A-Za-z0-9]+/[^<>\"\\'/]+)\"").getMatch(0);
        if (dllink == null) dllink = br.getRegex("<p><a href=\"(http://[^<>\"\\'/]+)\"").getMatch(0);
        if (dllink == null) {
            if (br.containsHTML("You have exceeded the download limit for today")) {
                logger.info("You have exceeded the download limit for today");
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
            }
            logger.warning("Final downloadlink (String is \"dllink\") regex didn't match!");
            if (br.containsHTML("index/pay")) {
                logger.info("Seems the account is no longer valid");
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
            }
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dllink = Encoding.htmlDecode(dllink);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            link.setProperty("direct", null);
            logger.warning("The final dllink seems not to be a file!");
            br.followConnection();
            if (br.containsHTML(">404 Not Found<")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 60 * 60 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        link.setProperty("direct", dllink);
        dl.startDownload();
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return -1;
    }

    @Override
    public void reset() {
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}