//    jDownloader - Downloadmanager
//    Copyright (C) 2013  JD-Team support@jdownloader.org
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
import java.util.Random;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.config.Property;
import jd.gui.UserIO;
import jd.http.Browser;
import jd.http.Cookie;
import jd.http.Cookies;
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
import jd.utils.locale.JDL;

//This plugin gets all its links from a decrypter!
@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "chomikuj.pl" }, urls = { "http://chomikujdecrypted\\.pl/.*?,\\d+$" }, flags = { 2 })
public class ChoMikujPl extends PluginForHost {

    private String              DLLINK              = null;

    private static final String PREMIUMONLY         = "(Aby pobrać ten plik, musisz być zalogowany lub wysłać jeden SMS\\.|Właściciel tego chomika udostępnia swój transfer, ale nie ma go już w wystarczającej|wymaga opłacenia kosztów transferu z serwerów Chomikuj\\.pl)";
    private static final String PREMIUMONLYUSERTEXT = "Download is only available for registered/premium users!";
    private static final String MAINPAGE            = "http://chomikuj.pl/";
    // private static final String FILEIDREGEX = "\\&id=(.*?)\\&";
    private boolean             videolink           = false;
    private static Object       LOCK                = new Object();
    public static final String  DECRYPTFOLDERS      = "DECRYPTFOLDERS";

    public ChoMikujPl(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://chomikuj.pl/Create.aspx");
        setConfigElements();
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("chomikujdecrypted.pl/", "chomikuj.pl/"));
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        if (!getDllink(link, br.cloneBrowser(), false)) {
            link.getLinkStatus().setStatusText(JDL.L("plugins.hoster.chomikujpl.only4registered", PREMIUMONLYUSERTEXT));
            return AvailableStatus.TRUE;
        }
        if (br.containsHTML("Najprawdopodobniej plik został w miedzyczasie usunięty z konta")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        if (br.containsHTML(PREMIUMONLY)) {
            link.getLinkStatus().setStatusText(JDL.L("plugins.hoster.chomikujpl.only4registered", PREMIUMONLYUSERTEXT));
            return AvailableStatus.TRUE;
        }
        if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        // In case the link redirects to the finallink
        br.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            con = br.openGetConnection(DLLINK);
            if (!con.getContentType().contains("html")) {
                link.setDownloadSize(con.getLongContentLength());
                // Only set final filename if it wasn't set before as video and
                // audio streams can have bad filenames
                if (link.getFinalFileName() == null) link.setFinalFileName(Encoding.htmlDecode(getFileNameFromHeader(con)));
            } else {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            return AvailableStatus.TRUE;
        } finally {
            try {
                con.disconnect();
            } catch (Throwable e) {
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
            ai.setStatus("<Login failed/Nieprawidłowe dane>");
            UserIO.getInstance().requestMessageDialog(0, "Chomikuj.pl Premium Error", "Login failed!\r\nPlease check your Username and Password!");
            return ai;
        }
        account.setValid(true);
        ai.setUnlimitedTraffic();
        ai.setStatus("Premium User");
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://chomikuj.pl/Regulamin.aspx";
    }

    public boolean getDllink(final DownloadLink theLink, final Browser br, final boolean premium) throws NumberFormatException, PluginException, IOException {
        final boolean redirectsSetting = br.isFollowingRedirects();
        br.setFollowRedirects(false);
        final String fid = theLink.getStringProperty("fileid");
        // Set by the decrypter if the link is password protected
        String savedLink = theLink.getStringProperty("savedlink");
        String savedPost = theLink.getStringProperty("savedpost");
        if (savedLink != null && savedPost != null) br.postPage(savedLink, savedPost);
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        // Premium users can always download the original file
        if (theLink.getBooleanProperty("video") && !premium) {
            br.setFollowRedirects(true);
            videolink = true;
            br.getPage("http://chomikuj.pl/ShowVideo.aspx?id=" + fid);
            if (br.getURL().contains("chomikuj.pl/Error404.aspx") || br.containsHTML("(Nie znaleziono|Strona, której szukasz nie została odnaleziona w portalu\\.<|>Sprawdź czy na pewno posługujesz się dobrym adresem)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            br.setFollowRedirects(false);
            br.getPage("http://chomikuj.pl/Video.ashx?id=" + fid + "&type=1&ts=" + new Random().nextInt(1000000000) + "&file=video&start=0");
            DLLINK = br.getRedirectLocation();
            theLink.setFinalFileName(theLink.getName());
        } else if ((theLink.getName().endsWith(".mp3") || theLink.getName().endsWith(".MP3")) && !premium) {
            br.getPage("http://chomikuj.pl/Audio.ashx?id=" + fid + "&type=2&tp=mp3");
            DLLINK = br.getRedirectLocation();
            theLink.setFinalFileName(theLink.getName());
        } else {
            br.getPage("http://chomikuj.pl/action/fileDetails/Index/" + fid);
            br.postPage("http://chomikuj.pl/action/License/Download", "fileId=" + fid + "&__RequestVerificationToken=" + Encoding.urlEncode(theLink.getStringProperty("requestverificationtoken")));
            if (br.containsHTML(PREMIUMONLY)) return false;
            DLLINK = br.getRegex("redirectUrl\":\"(http://.*?)\"").getMatch(0);
            if (DLLINK == null) DLLINK = br.getRegex("\\\\u003ca href=\\\\\"([^\"]*?)\\\\\" title").getMatch(0);
            if (DLLINK != null) DLLINK = Encoding.htmlDecode(DLLINK);
        }
        br.setFollowRedirects(redirectsSetting);
        return true;
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
        if (br.containsHTML(PREMIUMONLY)) throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.chomikujpl.only4registered", PREMIUMONLYUSERTEXT));
        if (!videolink) {
            if (!getDllink(downloadLink, br, false)) throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.chomikujpl.only4registered", PREMIUMONLYUSERTEXT));
        }
        if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @Override
    public void handlePremium(final DownloadLink link, final Account account) throws Exception {
        requestFileInformation(link);
        login(account, false);
        br.setFollowRedirects(false);
        DLLINK = null;
        getDllink(link, br, true);
        if (br.containsHTML("Masz \\\\u003cb\\\\u003e\\d+(,\\d+)? MB\\\\u003c/b\\\\u003e")) {
            logger.info("Disabling chomikuj.pl account: Not enough traffic available");
            throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
        }
        if (DLLINK == null) {
            String argh1 = br.getRegex("orgFile\\\\\" value=\\\\\"(.*?)\\\\\"").getMatch(0);
            String argh2 = br.getRegex("userSelection\\\\\" value=\\\\\"(.*?)\\\\\"").getMatch(0);
            if (argh1 == null || argh2 == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            // For some files they ask
            // "Do you really want to download this file", so we have to confirm
            // it with "YES" here ;)
            if (br.containsHTML("Właściciel tego chomika udostępnia darmowy transfer, ale jego ilość jest obecnie zbyt mała, aby można było pobrać plik"))
                br.postPage("http://chomikuj.pl/action/License/AcceptOwnTransfer?fileId=" + link.getStringProperty("fileid"), "orgFile=" + Encoding.urlEncode(argh1) + "&userSelection=" + Encoding.urlEncode(argh2) + "&__RequestVerificationToken=" + Encoding.urlEncode(link.getStringProperty("requestverificationtoken")));
            else
                br.postPage("http://chomikuj.pl/action/License/acceptLargeTransfer?fileId=" + link.getStringProperty("fileid"), "orgFile=" + Encoding.urlEncode(argh1) + "&userSelection=" + Encoding.urlEncode(argh2) + "&__RequestVerificationToken=" + Encoding.urlEncode(link.getStringProperty("requestverificationtoken")));
            DLLINK = br.getRegex("redirectUrl\":\"(http://.*?)\"").getMatch(0);
            if (DLLINK == null) DLLINK = br.getRegex("\\\\u003ca href=\\\\\"([^\"]*?)\\\\\" title").getMatch(0);
            if (DLLINK != null) DLLINK = Encoding.htmlDecode(DLLINK);
            if (DLLINK == null) getDllink(link, br, true);
        }
        if (DLLINK == null) {
            logger.warning("Final downloadlink (String is \"dllink\") regex didn't match!");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        sleep(2 * 1000l, link);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, DLLINK, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            logger.warning("The final dllink seems not to be a file!");
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @SuppressWarnings("unchecked")
    private void login(Account account, boolean force) throws Exception {
        synchronized (LOCK) {
            try {
                /** Load cookies */
                br.setCookiesExclusive(true);
                br.setFollowRedirects(true);
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
                br.getPage("http://chomikuj.pl/");
                final String requestVerificationToken = br.getRegex("<input name=\"__RequestVerificationToken\" type=\"hidden\" value=\"([^<>\"\\']+)\"").getMatch(0);
                if (requestVerificationToken == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                br.postPage("http://chomikuj.pl/action/Login/TopBarLogin", "rememberLogin=true&rememberLogin=false&topBar_LoginBtn=Zaloguj&ReturnUrl=&Login=" + Encoding.urlEncode(account.getUser()) + "&Password=" + Encoding.urlEncode(account.getPass()) + "&__RequestVerificationToken=" + Encoding.urlEncode(requestVerificationToken));
                if (br.getURL().equals(MAINPAGE)) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                /** Save cookies */
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

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), ChoMikujPl.DECRYPTFOLDERS, JDL.L("plugins.hoster.chomikujpl.decryptfolders", "Decrypt folders and subfolders")).setDefaultValue(true));
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}