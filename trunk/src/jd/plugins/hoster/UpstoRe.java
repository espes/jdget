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
import java.util.Map;

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
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "upsto.re" }, urls = { "http://(www\\.)?upsto\\.re/(?!faq|privacy|terms|d/|aff|login|account|dmca|imprint|message|panel|premium)[A-Za-z0-9]+" }, flags = { 2 })
public class UpstoRe extends PluginForHost {

    public UpstoRe(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://upsto.re/premium/");
    }

    @Override
    public String getAGBLink() {
        return "http://upsto.re/terms/";
    }

    private static Object LOCK     = new Object();
    private final String  MAINPAGE = "http://upsto.re";

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.setCookie("http://upsto.re/", "lang", "en");
        br.getPage(link.getDownloadURL());
        if (br.containsHTML(">File not found<")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        final Regex fileInfo = br.getRegex("<h2 style=\"margin:0\">([^<>\"]*?)</h2>[\t\n\r ]+<div class=\"comment\">([^<>\"]*?)</div>");
        String filename = fileInfo.getMatch(0);
        if (filename == null) filename = br.getRegex("<title>Download file ([^<>\"]*?) \\&mdash; Upload, store \\& share your files on Upsto\\.re</title>").getMatch(0);
        String filesize = fileInfo.getMatch(1);
        if (filename == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(Encoding.htmlDecode(filename.trim()));
        if (filesize != null) link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        String dllink = checkDirectLink(downloadLink, "freelink");
        if (dllink == null) {
            final String fid = new Regex(downloadLink.getDownloadURL(), "([A-Za-z0-9]+)$").getMatch(0);
            br.postPage(downloadLink.getDownloadURL(), "free=Slow+download&hash=" + fid);
            if (br.containsHTML(">This file is available only for Premium users<")) throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("hoster.upstore.premiumonly", "Only downloadable for premium users"));
            if (br.containsHTML(">Sorry, but server with file is overloaded")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server overloaded", 30 * 60 * 1000l);
            // Same server error (displayed differently) also exists for premium
            // users
            if (br.containsHTML(">Server with file not found<")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 60 * 60 * 1000l);
            // Waittime can be skipped
            // final long timeBefore = System.currentTimeMillis();
            final String rcID = br.getRegex("Recaptcha\\.create\\(\\'([^<>\"]*?)\\'").getMatch(0);
            if (rcID == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
            jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
            rc.setId(rcID);
            rc.load();
            File cf = rc.downloadCaptcha(getLocalCaptchaFile());
            String c = getCaptchaCode(cf, downloadLink);
            // int wait = 60;
            // String waittime = br.getRegex("var sec = (\\d+)").getMatch(0);
            // if (waittime != null) wait = Integer.parseInt(waittime);
            // int passedTime = (int) ((System.currentTimeMillis() - timeBefore)
            // / 1000) - 1;
            // wait -= passedTime;
            // if (wait > 0) sleep(wait * 1000l, downloadLink);
            br.postPage(downloadLink.getDownloadURL(), "free=Get+download+link&hash=" + fid + "&recaptcha_challenge_field=" + rc.getChallenge() + "&recaptcha_response_field=" + c);
            dllink = br.getRegex("<div style=\"margin: 10px auto 20px\" class=\"center\">[\t\n\r ]+<a href=\"(http://[^<>\"]*?)\"").getMatch(0);
            if (dllink == null) dllink = br.getRegex("\"(http://d\\d+\\.upsto\\.re/[^<>\"]*?)\"").getMatch(0);
            if (dllink == null) {
                final String reconnectWait = br.getRegex("You should wait (\\d+) minutes before downloading next file").getMatch(0);
                if (reconnectWait != null) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, Integer.parseInt(reconnectWait) * 60 * 1001l);
                if (br.containsHTML("(api\\.recaptcha\\.net|google\\.com/recaptcha/api/)")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
        } else {
            sleep(3000l, downloadLink);
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML("not found")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 30 * 60 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        downloadLink.setProperty("freelink", dllink);
        dl.startDownload();
    }

    @SuppressWarnings("unchecked")
    private void login(Account account, boolean force) throws Exception {
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
                            this.br.setCookie(MAINPAGE, key, value);
                        }
                        return;
                    }
                }
                br.setFollowRedirects(true);
                br.postPage("http://upsto.re/account/login/", "send=Login&url=http%253A%252F%252Fupsto.re%252F&email=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()));
                if (br.getCookie(MAINPAGE, "usid") == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
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
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        try {
            login(account, true);
        } catch (PluginException e) {
            account.setValid(false);
            return ai;
        }
        // Make sure that the language is correct
        br.getPage("http://upsto.re/?lang=en");
        ai.setUnlimitedTraffic();
        // Check for never-ending premium accounts
        if (!br.containsHTML("eternal premium")) {
            final String expire = br.getRegex("premium till (\\d{2}/\\d{2}/\\d{2})").getMatch(0);
            if (expire == null) {
                if (br.containsHTML("unlimited premium")) {
                    ai.setValidUntil(-1);
                    ai.setStatus("Unlimited Premium User");
                } else {
                    account.setValid(false);
                    return ai;
                }
            } else {
                ai.setValidUntil(TimeFormatter.getMilliSeconds(expire, "MM/dd/yy", null));
            }
        }
        ai.setStatus("Premium User");
        account.setValid(true);

        return ai;
    }

    @Override
    public void handlePremium(DownloadLink link, Account account) throws Exception {
        requestFileInformation(link);
        login(account, false);
        br.setFollowRedirects(false);
        // br.getPage(link.getDownloadURL());
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.postPage("http://upsto.re/load/premium/", "hash= " + new Regex(link.getDownloadURL(), "([A-Za-z0-9]+)$").getMatch(0));
        String dllink = br.getRegex("\"ok\":\"(http:[^<>\"]*?)\"").getMatch(0);
        if (dllink == null) {
            logger.warning("Final downloadlink (String is \"dllink\") regex didn't match!");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, Encoding.htmlDecode(dllink).replace("\\", ""), true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            logger.warning("The final dllink seems not to be a file!");
            br.followConnection();
            // Same server error (displayed differently) also exists for free
            // users
            if (br.containsHTML("not found")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 60 * 60 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return -1;
    }

    private String checkDirectLink(DownloadLink downloadLink, String property) {
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

    @Override
    public void reset() {
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}