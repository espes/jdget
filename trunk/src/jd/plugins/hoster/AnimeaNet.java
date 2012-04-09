//    jDownloader - Downloadmanager
//    Copyright (C) 2012  JD-Team support@jdownloader.org
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
import jd.parser.html.Form;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision: 15422 $", interfaceVersion = 2, names = { "vip.animea.net", "manga.animea.net" }, urls = { "REGEX_NOT_YET_KNOWN_ASDFASDFASD", "ANIMEA://.+" }, flags = { 0, 0 })
public class AnimeaNet extends PluginForHost {

    /**
     * @author raztoki
     */

    // DEV NOTES
    // other: required because jd urldecodes %23 into #. Which results in 404.
    // Impractical to use directhttp
    // other: also a place holder for vip.animea.net

    private static final String COOKIE_HOST = "http://vip.animea.net";
    private static final Object LOCK        = new Object();

    public AnimeaNet(PluginWrapper wrapper) {
        super(wrapper);
        // this.enablePremium(COOKIE_HOST.replace("http://", "https://") +
        // "/join/");
    }

    @Override
    public String getAGBLink() {
        return COOKIE_HOST + "/signup.php";
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) throws Exception {
        if (link.getDownloadURL().contains("ANIMEA://")) {
            link.setUrlDownload(link.getDownloadURL().replaceFirst("ANIMEA://", "").replace("#", "%23"));
        }
    }

    public boolean checkLinks(DownloadLink[] urls) {
        br.setFollowRedirects(false);
        if (urls == null || urls.length == 0) { return false; }
        try {
            for (DownloadLink dl : urls) {
                if (dl.getProperty("fastAdd") == "true") {
                    return true;
                } else {
                    URLConnectionAdapter con = br.openGetConnection(dl.getDownloadURL());
                    dl.setDownloadSize(con.getLongContentLength());
                    dl.setAvailable(true);
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException {
        if (checkLinks(new DownloadLink[] { downloadLink }) == false) {
            downloadLink.setAvailableStatus(AvailableStatus.UNCHECKABLE);
        } else if (!downloadLink.isAvailabilityStatusChecked()) {
            downloadLink.setAvailableStatus(AvailableStatus.UNCHECKABLE);
        }
        return downloadLink.getAvailableStatus();
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        String dllink = downloadLink.getDownloadURL();
        if (dllink == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public void handlePremium(DownloadLink downloadLink, Account account) throws Exception {
        String dllink = downloadLink.getDownloadURL();
        if (dllink == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
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
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        if (true) {
            account.setValid(false);
            return ai;
        }
        try {
            login(account, this.br);
        } catch (PluginException e) {
            account.setValid(false);
            return ai;
        }
        account.setValid(true);
        ai.setStatus("Free Registered User");
        return ai;
    }

    @SuppressWarnings("unchecked")
    public void login(Account account, Browser br) throws Exception {
        // needs changing to COOKIE_HOST..... still deviantart
        synchronized (LOCK) {
            try {
                /** Load cookies */
                br.setCookiesExclusive(true);
                final Object ret = account.getProperty("cookies", null);
                boolean acmatch = Encoding.urlEncode(account.getUser()).equals(account.getStringProperty("name", Encoding.urlEncode(account.getUser())));
                if (acmatch) acmatch = Encoding.urlEncode(account.getPass()).equals(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
                if (acmatch && ret != null && ret instanceof HashMap<?, ?>) {
                    final HashMap<String, String> cookies = (HashMap<String, String>) ret;
                    if (account.isValid()) {
                        for (final Map.Entry<String, String> cookieEntry : cookies.entrySet()) {
                            final String key = cookieEntry.getKey();
                            final String value = cookieEntry.getValue();
                            br.setCookie(COOKIE_HOST, key, value);
                        }
                        return;
                    }
                }
                br.setCookie(COOKIE_HOST, "lang", "english");
                br.getPage(COOKIE_HOST + "/login.php");
                Form loginform = br.getFormbyProperty("id", "login");
                if (loginform == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                loginform.put("username", Encoding.urlEncode(account.getUser()));
                loginform.put("password", Encoding.urlEncode(account.getPass()));
                loginform.put("remember_me", "1");
                br.submitForm(loginform);
                if (!br.getRedirectLocation().contains("/users/loggedin")) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                /** Save cookies */
                final HashMap<String, String> cookies = new HashMap<String, String>();
                final Cookies add = br.getCookies(COOKIE_HOST);
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
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}