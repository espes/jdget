//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
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
import java.lang.reflect.Field;

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

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "mangatraders.com" }, urls = { "http://(www\\.)*?mangatraders\\.com/(download|view)/file/\\d+" }, flags = { 2 })
public class MangaTradersCom extends PluginForHost {

    private boolean             weAreAlreadyLoggedIn = false;

    private static final String COOKIENAME           = "SMFCookie232";
    private static final String ACCESSBLOCK          = "<p>You have attempted to download this file within the last 10 seconds.</p>";
    private static final String FILEOFFLINE          = ">Download Manager Error - Invalid Fileid";

    public MangaTradersCom(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://www.mangatraders.com/register/");
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) throws Exception {
        link.setUrlDownload(link.getDownloadURL().replace("/view/file/", "/download/file/"));
    }

    public boolean checkLinks(DownloadLink[] urls) {
        br.setFollowRedirects(false);
        if (urls == null || urls.length == 0) {
            return false;
        }
        try {
            Account aa = AccountController.getInstance().getValidAccount(this);
            if (aa == null || !aa.isValid()) {
                logger.info("The user didn't enter account data even if they're needed to check the links for this host.");
                return false;
            }
            login(aa);
            for (DownloadLink dl : urls) {
                String addedlink = dl.getDownloadURL();
                br.getPage(addedlink);
                if (br.getRedirectLocation() == null) {
                    dl.setAvailable(false);
                } else {
                    URLConnectionAdapter con = br.openGetConnection(br.getRedirectLocation());
                    dl.setFinalFileName(getFileNameFromHeader(con));
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
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        try {
            login(account);
        } catch (PluginException e) {
            account.setValid(false);
            return ai;
        }
        account.setValid(true);
        ai.setStatus("Registered (free) User");
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://www.mangatraders.com/register/";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return 1;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        throw new PluginException(LinkStatus.ERROR_FATAL, "Download does only work with account");
    }

    /**
     * JD 2 Code. DO NOT USE OVERRIDE FOR COMPATIBILITY REASONS
     */
    public boolean isProxyRotationEnabledForLinkChecker() {
        return false;
    }

    @Override
    public void handlePremium(DownloadLink downloadLink, Account account) throws Exception {
        // Don't check the links because the download will then fail ;)
        // requestFileInformation(downloadLink);
        // Usually JD is already logged in after the linkcheck so if JD is
        // logged in we don't have to log in again here
        if (!weAreAlreadyLoggedIn || br.getCookie("http://www.mangatraders.com/", COOKIENAME) == null) {
            login(account);
        }
        br.getPage(downloadLink.getDownloadURL());
        String dllink = br.getRedirectLocation();
        if (dllink == null) {
            if (br.containsHTML(FILEOFFLINE)) {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            if (br.containsHTML(ACCESSBLOCK)) {
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error, wait some minutes!", 5 * 60 * 1999l);
            }
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, true, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML(ACCESSBLOCK)) {
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error, wait some minutes!", 5 * 60 * 1000l);
            }
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    public void login(Account account) throws Exception {
        this.setBrowserExclusive();
        // Clear the Referer or the download could start here which then causes
        // an exception
        br.getHeaders().put("Referer", "");
        br.setFollowRedirects(false);
        br.postPage("http://www.mangatraders.com/login/processlogin", "login-user=" + Encoding.urlEncode(account.getUser()) + "&login-pass=" + Encoding.urlEncode(account.getPass()) + "&rememberme=on");
        if (br.getCookie("http://www.mangatraders.com/", COOKIENAME) == null) {
            throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
        }
        weAreAlreadyLoggedIn = true;
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink downloadLink) throws IOException {
        if (checkLinks(new DownloadLink[] { downloadLink }) == false) {
            downloadLink.setAvailableStatus(AvailableStatus.UNCHECKABLE);
        } else if (!downloadLink.isAvailabilityStatusChecked()) {
            downloadLink.setAvailableStatus(AvailableStatus.UNCHECKABLE);
        }
        return getAvailableStatus(downloadLink);
    }

    private AvailableStatus getAvailableStatus(DownloadLink link) {
        try {
            final Field field = link.getClass().getDeclaredField("availableStatus");
            field.setAccessible(true);
            Object ret = field.get(link);
            if (ret != null && ret instanceof AvailableStatus) {
                return (AvailableStatus) ret;
            }
        } catch (final Throwable e) {
        }
        return AvailableStatus.UNCHECKED;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}