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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
import jd.utils.locale.JDL;

//This plugin gets all its links from a decrypter!
@HostPlugin(revision = "$Revision: 16311 $", interfaceVersion = 2, names = { "chomikuj.pl" }, urls = { "\\&id=.*?\\&gallerylink=.*?60423fhrzisweguikipo9re.*?\\&" }, flags = { 2 })
public class ChoMikujPl extends PluginForHost {

    private String              DLLINK              = null;

    private static final String PREMIUMONLY         = "(Aby pobrać ten plik, musisz być zalogowany lub wysłać jeden SMS\\.|Właściciel tego chomika udostępnia swój transfer, ale nie ma go już w wystarczającej|wymaga opłacenia kosztów transferu z serwerów Chomikuj\\.pl)";

    private static final String PREMIUMONLYUSERTEXT = "Download is only available for registered/premium users!";
    private static final String MAINPAGE            = "http://chomikuj.pl/";
    private static final String FILEIDREGEX         = "\\&id=(.*?)\\&";
    private boolean             videolink           = false;
    private static final Object LOCK                = new Object();

    public ChoMikujPl(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://chomikuj.pl/Create.aspx");
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("60423fhrzisweguikipo9re", "chomikuj.pl"));
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
        account.setValid(true);
        ai.setUnlimitedTraffic();
        ai.setStatus("Premium User");
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://chomikuj.pl/Regulamin.aspx";
    }

    public boolean getDllink(DownloadLink theLink, Browser br2) throws NumberFormatException, PluginException, IOException {
        final boolean redirectsSetting = br.isFollowingRedirects();
        br.setFollowRedirects(false);
        final String fid = new Regex(theLink.getDownloadURL(), FILEIDREGEX).getMatch(0);
        // Set by the decrypter if the link is password protected
        String savedLink = theLink.getStringProperty("savedlink");
        String savedPost = theLink.getStringProperty("savedpost");
        if (savedLink != null && savedPost != null) br.postPage(savedLink, savedPost);
        br2.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        br.getPage("http://chomikuj.pl/action/fileDetails/Index/" + fid);
        final String videoLink = br.getRegex("\"(http://chomikuj\\.pl/Video\\.ashx\\?realId=\\d+\\&amp;id=\\d+\\&amp;type=1)\"").getMatch(0);
        if (videoLink != null) {
            br.getPage(Encoding.htmlDecode(videoLink));
            DLLINK = br.getRedirectLocation();
            if (DLLINK != null) DLLINK = Encoding.htmlDecode(DLLINK);
        } else {
            br2.postPage("http://chomikuj.pl/action/License/Download", "fileId=" + fid + "&__RequestVerificationToken=" + Encoding.urlEncode(theLink.getStringProperty("requestverificationtoken")));
            if (br2.containsHTML(PREMIUMONLY)) return false;
            DLLINK = br2.getRegex("redirectUrl\":\"(http://.*?)\"").getMatch(0);
            if (DLLINK == null) DLLINK = br2.getRegex("\\\\u003ca href=\\\\\"(.*?)\\\\\"").getMatch(0);
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
            if (!getDllink(downloadLink, br)) throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.chomikujpl.only4registered", PREMIUMONLYUSERTEXT));
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
    public void handlePremium(DownloadLink link, Account account) throws Exception {
        requestFileInformation(link);
        login(account, false);
        br.setFollowRedirects(false);
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        Browser brc = br.cloneBrowser();
        DLLINK = null;
        getDllink(link, brc);
        if (DLLINK == null) {
            Regex confirmVarsRegex = new Regex(brc.toString().replace("\\", ""), "fileId=\\d+u0027,[a-z\r\n\t ]+u0027(.*?)u0027, u0027(.*?)u0027");
            String argh1 = confirmVarsRegex.getMatch(0);
            String argh2 = confirmVarsRegex.getMatch(1);
            if (argh1 == null || argh2 == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            // For some files they ask
            // "Do you really want to download this file", so we have to confirm
            // it with "YES" here ;)
            br.postPage("http://chomikuj.pl/Chomik/License/acceptOwnTransfer?fileId=" + new Regex(link.getDownloadURL(), FILEIDREGEX).getMatch(0), "orgFile=" + Encoding.urlEncode(argh1) + "&userSelection=" + Encoding.urlEncode(argh2));
            getDllink(link, brc);
        }
        if (DLLINK == null) {
            logger.warning("Final downloadlink (String is \"dllink\") regex didn't match!");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
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

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        if (link.getStringProperty("video") != null) {
            br.setFollowRedirects(true);
            videolink = true;
            br.getPage("http://chomikuj.pl/ShowVideo.aspx?id=" + new Regex(link.getDownloadURL(), FILEIDREGEX).getMatch(0));
            if (br.getURL().contains("chomikuj.pl/Error404.aspx") || br.containsHTML("(Nie znaleziono|Strona, której szukasz nie została odnaleziona w portalu\\.<|>Sprawdź czy na pewno posługujesz się dobrym adresem)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            br.setFollowRedirects(false);
            br.getPage("http://chomikuj.pl/Video.ashx?id=" + new Regex(link.getDownloadURL(), FILEIDREGEX).getMatch(0) + "&type=1&ts=" + new Random().nextInt(1000000000) + "&file=video&start=0");
            DLLINK = br.getRedirectLocation();
        } else {
            if (!getDllink(link, br.cloneBrowser())) {
                link.getLinkStatus().setStatusText(JDL.L("plugins.hoster.chomikujpl.only4registered", PREMIUMONLYUSERTEXT));
                return AvailableStatus.TRUE;
            }
            if (br.containsHTML("Najprawdopodobniej plik został w miedzyczasie usunięty z konta")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            if (br.containsHTML(PREMIUMONLY)) {
                link.getLinkStatus().setStatusText(JDL.L("plugins.hoster.chomikujpl.only4registered", PREMIUMONLYUSERTEXT));
                return AvailableStatus.TRUE;
            }
        }
        if (DLLINK == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        // In case the link redirects to the finallink
        br.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            con = br.openGetConnection(DLLINK);
            if (!con.getContentType().contains("html")) {
                link.setDownloadSize(con.getLongContentLength());
                if (!videolink) link.setFinalFileName(Encoding.htmlDecode(getFileNameFromHeader(con)));
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
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}