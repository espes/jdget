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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jd.PluginWrapper;
import jd.config.Property;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.nutils.encoding.Encoding;
import jd.parser.html.Form;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.locale.JDL;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "yahoo.com" }, urls = { "http://(www\\.)?de\\.groups\\.decryptedhahoo\\.com/group/[a-z0-9]+/photos/album/\\d+/pic/\\d+/view\\?.*?yahoolink" }, flags = { 2 })
public class YahooCom extends PluginForHost {

    private static Object       LOCK     = new Object();

    private static final String MAINPAGE = "www.yahoo.com";

    private static final String USERTEXT = "Only downloadable for registered users!";

    public YahooCom(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("https://na.edit.yahoo.com/registration?");
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("decryptedhahoo", "yahoo").replace("yahoolink", ""));
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
        ai.setStatus("Registered (free) User");
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://info.yahoo.com/legal/de/yahoo/tos.html";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return -1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.yahoocom.only4registered", USERTEXT));
    }

    @Override
    public void handlePremium(DownloadLink link, Account account) throws Exception {
        requestFileInformation(link);
        login(account, false);
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("login\\.yahoo\\.com/login")) {
            logger.warning("Cookies not valid anymore, retrying!");
            account.setProperty("cookies", Property.NULL);
            throw new PluginException(LinkStatus.ERROR_RETRY);
        }
        String finallink = br.getRegex("class=\"ygrp-photos-body-image\" style=\"height: \\d+px;\"><img src=\"(http://.*?)\"").getMatch(0);
        if (finallink == null) finallink = br.getRegex("\"(http://xa\\.yimg\\.com/kq/groups/\\d+/.*?)\"").getMatch(0);
        if (finallink == null) {
            logger.warning("finallink is null...");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, finallink, true, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            logger.warning("The final dllink seems not to be a file!");
            br.followConnection();
            if (br.containsHTML("(We are sorry, you can not display images hosted by Yahoo|Groups on non Yahoo\\! Groups pages\\.)")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, JDL.L("plugins.hoster.yahoocom.servererror", "Server error"), 10 * 60 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        // We HAVE to do this, server doesn't send filename correctly!
        final String getExt = dl.getConnection().getContentType();
        if (getExt.contains("jpeg"))
            link.setFinalFileName(link.getName() + ".jpeg");
        else
            link.setFinalFileName(getFileNameFromHeader(dl.getConnection()));
        dl.startDownload();
    }

    @SuppressWarnings("unchecked")
    private void login(Account account, final boolean force) throws Exception {
        synchronized (LOCK) {
            // Load cookies
            br.setCookiesExclusive(true);
            final Object ret = account.getProperty("cookies", null);
            boolean acmatch = Encoding.urlEncode(account.getUser()).equals(account.getStringProperty("name", Encoding.urlEncode(account.getUser())));
            if (acmatch) acmatch = Encoding.urlEncode(account.getPass()).equals(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
            if (acmatch && ret != null && ret instanceof HashMap<?, ?> && !force) {
                final HashMap<String, String> cookies = (HashMap<String, String>) ret;
                if (cookies.containsKey("SSL") && account.isValid()) {
                    for (final Map.Entry<String, String> cookieEntry : cookies.entrySet()) {
                        final String key = cookieEntry.getKey();
                        final String value = cookieEntry.getValue();
                        this.br.setCookie(MAINPAGE, key, value);
                    }
                    return;
                }
            }
            br.getPage("https://login.yahoo.com/config/login?");
            Form loginForm = br.getFormbyProperty("name", "login_form");
            if (loginForm == null) {
                logger.warning("Login broken!");
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
            }
            loginForm.put("login", Encoding.urlEncode(account.getUser()));
            loginForm.put("passwd", Encoding.urlEncode(account.getPass()));
            br.submitForm(loginForm);
            if (br.getCookie(MAINPAGE, "PH") == null || br.getCookie(MAINPAGE, "SSL") == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
            // Save cookies
            final HashMap<String, String> cookies = new HashMap<String, String>();
            final Cookies add = this.br.getCookies(MAINPAGE);
            for (final Cookie c : add.getCookies()) {
                cookies.put(c.getKey(), c.getValue());
            }
            account.setProperty("name", Encoding.urlEncode(account.getUser()));
            account.setProperty("pass", Encoding.urlEncode(account.getPass()));
            account.setProperty("cookies", cookies);
        }
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        link.getLinkStatus().setStatusText(JDL.L("plugins.hoster.yahoocom.only4registered", USERTEXT));
        return AvailableStatus.TRUE;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}