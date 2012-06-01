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
import jd.http.Cookie;
import jd.http.Cookies;
import jd.nutils.encoding.Encoding;
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

@HostPlugin(revision = "$Revision: 16733 $", interfaceVersion = 2, names = { "superupload.com" }, urls = { "http://(www\\.)?superupload\\.com/\\?d=[A-Z0-9]{8}" }, flags = { 2 })
public class SuperUploadCom extends PluginForHost {

    public SuperUploadCom(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://superupload.com/#p=premium");
    }

    @Override
    public String getAGBLink() {
        return "http://www.superupload.com/#p=terms";
    }

    private static final Object LOCK     = new Object();
    private static final String MAINPAGE = "http://superupload.com";

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.setCookie("http://superupload.com/", "lang", "en");
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("File not found\\!")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("title=\"Download ([^<>\"]*?)\"").getMatch(0);
        String filesize = br.getRegex("<div class=\"vmid_c\">([^<>\"]*?)</div>").getMatch(0);
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(Encoding.htmlDecode(filename.trim()));
        link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        final String rcID = br.getRegex("Recaptcha\\.create\\([\t\n\r ]+\"([^<>\"]*?)\"").getMatch(0);
        if (rcID == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.getPage(downloadLink.getDownloadURL() + "&act=r&rnd=" + System.currentTimeMillis());
        PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
        jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
        rc.setId(rcID);
        rc.load();
        for (int i = 0; i <= 5; i++) {
            File cf = rc.downloadCaptcha(getLocalCaptchaFile());
            final String c = getCaptchaCode(cf, downloadLink);
            br.postPage(downloadLink.getDownloadURL() + "&act=t&rnd=" + System.currentTimeMillis(), "captcha=" + c + "&challenge=" + rc.getChallenge());
            if (br.containsHTML(">Incorrect captcha<")) {
                rc.reload();
                continue;
            }
            break;
        }
        if (br.containsHTML(">Incorrect captcha<")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        br.getPage(downloadLink.getDownloadURL() + "&act=t&rnd=" + System.currentTimeMillis());
        final String waittime = br.getRegex("var iEnd = new Date\\(\\)\\.setTime\\(new Date\\(\\)\\.getTime\\(\\) \\+ (\\d+) \\* 1000\\)").getMatch(0);
        int wait = 30;
        if (waittime != null) wait = Integer.parseInt(waittime);
        sleep(wait * 1001l, downloadLink);
        br.getPage(downloadLink.getDownloadURL() + "&act=t&rnd=" + System.currentTimeMillis());
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, downloadLink.getDownloadURL() + "&act=start", false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
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
                br.setFollowRedirects(false);
                // br.getPage("");
                br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
                br.postPage("http://superupload.com/?login", "login=" + Encoding.urlEncode(account.getUser()) + "&pass=" + Encoding.urlEncode(account.getPass()));
                if (br.getCookie(MAINPAGE, "auth") == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
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
        br.getPage("http://superupload.com/?account&page=panel");
        ai.setUnlimitedTraffic();
        String expire = br.getRegex("<BR>Exp.: (\\d{2}/\\d{2}/\\d{2}  \\-  \\d{2}:\\d{2}:\\d{2})").getMatch(0);
        if (expire == null) {
            account.setValid(false);
            return ai;
        } else {
            ai.setValidUntil(TimeFormatter.getMilliSeconds(expire, "dd/MM/yy  -  hh:mm:ss", null));
        }
        account.setValid(true);
        ai.setStatus("Premium User");
        return ai;
    }

    @Override
    public void handlePremium(DownloadLink link, Account account) throws Exception {
        requestFileInformation(link);
        login(account, false);
        br.setFollowRedirects(false);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, link.getDownloadURL() + "&act=start", true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            logger.warning("The final dllink seems not to be a file!");
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
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
        return 1;
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}