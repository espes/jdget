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

import jd.PluginWrapper;
import jd.controlling.AccountController;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

@HostPlugin(revision = "$Revision: 15422 $", interfaceVersion = 2, names = { "shurload.es" }, urls = { "http://(www\\.)?shurload\\.es/(download|view)\\.php\\?den=[a-z0-9]{32}" }, flags = { 2 })
public class ShurLoadEs extends PluginForHost {

    // DEV NOTES - by raztoki
    // modified mangatrader plugin
    // they only allow one download connection.

    private boolean             weAreAlreadyLoggedIn = false;
    private static final String COOKIENAME           = "shursession";

    public ShurLoadEs(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://www.shurload.es/register.php");
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) throws Exception {
        link.setUrlDownload(link.getDownloadURL().replace("://shurload.", "://www.shurload.").replace("/view.php", "/download.php"));
    }

    public boolean checkLinks(DownloadLink[] urls) {
        br.setFollowRedirects(false);
        if (urls == null || urls.length == 0) { return false; }
        try {
            Account aa = AccountController.getInstance().getValidAccount(this);
            if (aa == null || !aa.isValid()) { return false; }
            login(aa);
            for (DownloadLink dl : urls) {
                URLConnectionAdapter con = null;
                try {
                    con = br.openGetConnection(dl.getDownloadURL());
                    if (!con.isContentDisposition()) {
                        br.followConnection();
                        if (br.getRedirectLocation() != null && br.getRedirectLocation().contains("login.php")) {
                            aa.setValid(false);
                            aa.setEnabled(false);
                            return false;
                        } else {
                            dl.setAvailable(false);
                        }
                    }
                    dl.setFinalFileName(getFileNameFromHeader(con));
                    dl.setDownloadSize(con.getLongContentLength());
                    dl.setAvailable(true);
                } finally {
                    try {
                        con.disconnect();
                    } catch (final Throwable e) {
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        try {
            login(account);
        } catch (PluginException e) {
            account.setValid(false);
            return ai;
        }
        account.setValid(true);
        ai.setStatus("Registered User");
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://www.shurload.es/";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return 10;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        throw new PluginException(LinkStatus.ERROR_FATAL, "Download only works with an account");
    }

    @Override
    public void handlePremium(DownloadLink downloadLink, Account account) throws Exception {
        if (!weAreAlreadyLoggedIn || br.getCookie("http://www.shurload.es/", COOKIENAME) == null) login(account);
        String dllink = downloadLink.getDownloadURL();
        if (dllink == null) { throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    public void login(Account account) throws Exception {
        this.setBrowserExclusive();
        br.getHeaders().put("Referer", "");
        br.setFollowRedirects(false);
        br.postPage("http://www.shurload.es/jd.php", "username=" + account.getUser() + "&password=" + account.getPass() + "&key=" + Encoding.Base64Decode("cU5UYjlqYUVXaTg5"));
        if (br.getCookie("http://www.shurload.es/", COOKIENAME) == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
        weAreAlreadyLoggedIn = true;
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
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}