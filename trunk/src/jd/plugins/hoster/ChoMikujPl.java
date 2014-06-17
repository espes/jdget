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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.config.Property;
import jd.http.Browser;
import jd.http.Cookie;
import jd.http.Cookies;
import jd.http.Request;
import jd.http.URLConnectionAdapter;
import jd.nutils.JDHash;
import jd.nutils.encoding.Encoding;
import jd.plugins.Account;
import jd.plugins.Account.AccountType;
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

//This plugin gets all its links from a decrypter!
@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "chomikuj.pl" }, urls = { "http://chomikujdecrypted\\.pl/.*?,\\d+$" }, flags = { 2 })
public class ChoMikujPl extends PluginForHost {

    private String              DLLINK              = null;

    private static final String PREMIUMONLY         = "(Aby pobrać ten plik, musisz być zalogowany lub wysłać jeden SMS\\.|Właściciel tego chomika udostępnia swój transfer, ale nie ma go już w wystarczającej|wymaga opłacenia kosztów transferu z serwerów Chomikuj\\.pl)";
    private static final String PREMIUMONLYUSERTEXT = "Download is only available for registered/premium users!";
    private static final String ACCESSDENIED        = "Nie masz w tej chwili uprawnień do tego pliku lub dostęp do niego nie jest w tej chwili możliwy z innych powodów.";
    private static final String MAINPAGE            = "http://chomikuj.pl/";
    // private static final String FILEIDREGEX = "\\&id=(.*?)\\&";
    private boolean             videolink           = false;
    private static Object       LOCK                = new Object();
    public static final String  DECRYPTFOLDERS      = "DECRYPTFOLDERS";

    private static boolean      pluginloaded        = false;
    private Browser             cbr                 = null;

    public ChoMikujPl(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://chomikuj.pl/Create.aspx");
        setConfigElements();
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replace("chomikujdecrypted.pl/", "chomikuj.pl/"));
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws Exception {
        this.setBrowserExclusive();
        // Offline from decrypter
        if (link.getBooleanProperty("offline", false)) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        if (!getDllink(link, br.cloneBrowser(), false)) {
            link.getLinkStatus().setStatusText(JDL.L("plugins.hoster.chomikujpl.only4registered", PREMIUMONLYUSERTEXT));
            return AvailableStatus.TRUE;
        }
        if (cbr.containsHTML("Najprawdopodobniej plik został w miedzyczasie usunięty z konta")) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        if (cbr.containsHTML(PREMIUMONLY)) {
            link.getLinkStatus().setStatusText(JDL.L("plugins.hoster.chomikujpl.only4registered", PREMIUMONLYUSERTEXT));
            return AvailableStatus.TRUE;
        }
        if (DLLINK == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        // In case the link redirects to the finallink
        br.setFollowRedirects(true);
        URLConnectionAdapter con = null;
        try {
            con = br.openGetConnection(DLLINK);
            if (!con.getContentType().contains("html")) {
                link.setDownloadSize(con.getLongContentLength());
                // Only set final filename if it wasn't set before as video and
                // audio streams can have bad filenames
                if (link.getFinalFileName() == null) {
                    link.setFinalFileName(Encoding.htmlDecode(getFileNameFromHeader(con)));
                }
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

    private static synchronized String unescape(final String s) {
        /* we have to make sure the youtube plugin is loaded */
        if (pluginloaded == false) {
            final PluginForHost plugin = JDUtilities.getPluginForHost("youtube.com");
            if (plugin == null) {
                throw new IllegalStateException("youtube plugin not found!");
            }
            pluginloaded = true;
        }
        return jd.plugins.hoster.Youtube.unescape(s);
    }

    @SuppressWarnings("deprecation")
    @Override
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        try {
            login(account, true);
        } catch (PluginException e) {
            account.setValid(false);
            throw e;
        }
        account.setValid(true);
        final String remainingTraffic = br.getRegex("<strong>([^<>\"]*?)</strong>[\t\n\r ]+transferu").getMatch(0);
        if (remainingTraffic != null) {
            ai.setTrafficLeft(SizeFormatter.getSize(remainingTraffic.replace(",", ".")));
        } else {
            ai.setUnlimitedTraffic();
        }
        ai.setStatus("Premium User");
        try {
        } catch (final Throwable e) {
            account.setType(AccountType.PREMIUM);
            /* Not available in old 0.9.581 Stable */
        }
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://chomikuj.pl/Regulamin.aspx";
    }

    public boolean getDllink(final DownloadLink theLink, final Browser br, final boolean premium) throws Exception {
        final boolean redirectsSetting = br.isFollowingRedirects();
        br.setFollowRedirects(false);
        final String fid = theLink.getStringProperty("fileid");
        // Set by the decrypter if the link is password protected
        String savedLink = theLink.getStringProperty("savedlink");
        String savedPost = theLink.getStringProperty("savedpost");
        if (savedLink != null && savedPost != null) {
            br.postPage(savedLink, savedPost);
        }
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        // Premium users can always download the original file
        if (theLink.getBooleanProperty("video") && !premium) {
            br.setFollowRedirects(true);
            videolink = true;
            getPage(br, "http://chomikuj.pl/ShowVideo.aspx?id=" + fid);
            if (br.getURL().contains("chomikuj.pl/Error404.aspx") || cbr.containsHTML("(Nie znaleziono|Strona, której szukasz nie została odnaleziona w portalu\\.<|>Sprawdź czy na pewno posługujesz się dobrym adresem)")) {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            br.setFollowRedirects(false);
            br.getPage("http://chomikuj.pl/Video.ashx?id=" + fid + "&type=1&ts=" + new Random().nextInt(1000000000) + "&file=video&start=0");
            DLLINK = br.getRedirectLocation();
            theLink.setFinalFileName(theLink.getName());
        } else if ((theLink.getName().endsWith(".mp3") || theLink.getName().endsWith(".MP3")) && !premium) {
            getPage(br, "http://chomikuj.pl/Audio.ashx?id=" + fid + "&type=2&tp=mp3");
            DLLINK = br.getRedirectLocation();
            theLink.setFinalFileName(theLink.getName());
        } else {
            getPage(br, "http://chomikuj.pl/action/fileDetails/Index/" + fid);
            final String filesize = br.getRegex("<p class=\"fileSize\">([^<>\"]*?)</p>").getMatch(0);
            if (filesize != null) {
                theLink.setDownloadSize(SizeFormatter.getSize(filesize.replace(",", ".")));
            }
            if (br.getRedirectLocation() != null && br.getRedirectLocation().contains("fileDetails/Unavailable")) {
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            String requestVerificationToken = theLink.getStringProperty("requestverificationtoken");
            if (requestVerificationToken == null) {
                br.setFollowRedirects(true);
                br.getPage(theLink.getDownloadURL());
                br.setFollowRedirects(false);
                requestVerificationToken = br.getRegex("<div id=\"content\">[\t\n\r ]+<input name=\"__RequestVerificationToken\" type=\"hidden\" value=\"([^<>\"]*?)\"").getMatch(0);
            }
            if (requestVerificationToken == null) {
                requestVerificationToken = theLink.getStringProperty("__RequestVerificationToken_Lw__", null);
            }
            if (requestVerificationToken == null) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            br.setCookie("http://chomikuj.pl/", "__RequestVerificationToken_Lw__", requestVerificationToken);

            final String chomikID = theLink.getStringProperty("chomikID");

            if (chomikID != null) {
                final String folderPassword = theLink.getStringProperty("password");

                if (folderPassword != null) {
                    br.setCookie("http://chomikuj.pl/", "FoldersAccess", String.format("%s=%s", chomikID, folderPassword));
                } else {
                    logger.warning("Failed to set FoldersAccess cookie inside getDllink");
                    // this link won't work without password
                    return false;
                }
            }

            postPage(br, "http://chomikuj.pl/action/License/Download", "fileId=" + fid + "&__RequestVerificationToken=" + Encoding.urlEncode(requestVerificationToken));
            if (cbr.containsHTML(PREMIUMONLY)) {
                return false;
            }
            if (cbr.containsHTML(ACCESSDENIED)) {
                return false;
            }
            DLLINK = br.getRegex("redirectUrl\":\"(http://.*?)\"").getMatch(0);
            if (DLLINK == null) {
                DLLINK = br.getRegex("\\\\u003ca href=\\\\\"([^\"]*?)\\\\\" title").getMatch(0);
            }
            if (DLLINK == null) {
                DLLINK = br.getRegex("\"(http://[A-Za-z0-9\\-_\\.]+\\.chomikuj\\.pl/File\\.aspx[^<>\"]*?)\\\\\"").getMatch(0);
            }
            if (DLLINK != null) {
                DLLINK = Encoding.htmlDecode(DLLINK);
            }
        }
        if (DLLINK != null) {
            DLLINK = unescape(DLLINK);
        }
        br.setFollowRedirects(redirectsSetting);
        return true;
    }

    public boolean getDllink_premium(final DownloadLink theLink, final Browser br, final boolean premium) throws Exception {
        final boolean redirectsSetting = br.isFollowingRedirects();
        br.setFollowRedirects(false);
        final String fid = theLink.getStringProperty("fileid");
        /* Set by the decrypter if the link is password protected */
        String savedLink = theLink.getStringProperty("savedlink");
        String savedPost = theLink.getStringProperty("savedpost");
        if (savedLink != null && savedPost != null) {
            br.postPage(savedLink, savedPost);
        }
        br.getHeaders().put("X-Requested-With", "XMLHttpRequest");
        /* Premium users can always download the original file */
        getPage(br, "http://chomikuj.pl/action/fileDetails/Index/" + fid);
        final String filesize = br.getRegex("<p class=\"fileSize\">([^<>\"]*?)</p>").getMatch(0);
        if (filesize != null) {
            theLink.setDownloadSize(SizeFormatter.getSize(filesize.replace(",", ".")));
        }
        if (br.getRedirectLocation() != null && br.getRedirectLocation().contains("fileDetails/Unavailable")) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        String requestVerificationToken = theLink.getStringProperty("requestverificationtoken");
        if (requestVerificationToken == null) {
            br.setFollowRedirects(true);
            getPage(br, theLink.getDownloadURL());
            br.setFollowRedirects(false);
            requestVerificationToken = cbr.getRegex("<div id=\"content\">[\t\n\r ]+<input name=\"__RequestVerificationToken\" type=\"hidden\" value=\"([^<>\"]*?)\"").getMatch(0);
        }
        if (requestVerificationToken == null) {
            requestVerificationToken = theLink.getStringProperty("__RequestVerificationToken_Lw__", null);
        }
        if (requestVerificationToken == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        br.setCookie("http://chomikuj.pl/", "__RequestVerificationToken_Lw__", requestVerificationToken);

        final String chomikID = theLink.getStringProperty("chomikID");

        if (chomikID != null) {
            final String folderPassword = theLink.getStringProperty("password");

            if (folderPassword != null) {
                br.setCookie("http://chomikuj.pl/", "FoldersAccess", String.format("%s=%s", chomikID, folderPassword));
            } else {
                logger.warning("Failed to set FoldersAccess cookie inside getDllink");
                // this link won't work without password
                return false;
            }
        }

        br.getHeaders().put("Referer", theLink.getDownloadURL());
        postPage(br, "http://chomikuj.pl/action/License/DownloadContext", "fileId=" + fid + "&__RequestVerificationToken=" + Encoding.urlEncode(requestVerificationToken));
        if (cbr.containsHTML(ACCESSDENIED)) {
            return false;
        }
        /* Low traffic warning */
        if (cbr.containsHTML("action=\"/action/License/DownloadWarningAccept\"")) {
            final String serializedUserSelection = cbr.getRegex("name=\"SerializedUserSelection\" type=\"hidden\" value=\"([^<>\"]*?)\"").getMatch(0);
            final String serializedOrgFile = cbr.getRegex("name=\"SerializedOrgFile\" type=\"hidden\" value=\"([^<>\"]*?)\"").getMatch(0);
            if (serializedUserSelection == null || serializedOrgFile == null) {
                logger.warning("Failed to pass low traffic warning!");
                return false;
            }
            postPage(br, "http://chomikuj.pl/action/License/DownloadWarningAccept", "FileId=" + fid + "&SerializedUserSelection=" + Encoding.urlEncode(serializedUserSelection) + "&SerializedOrgFile=" + Encoding.urlEncode(serializedOrgFile) + "&__RequestVerificationToken=" + Encoding.urlEncode(requestVerificationToken));
        }
        DLLINK = br.getRegex("redirectUrl\":\"(http://.*?)\"").getMatch(0);
        if (DLLINK == null) {
            DLLINK = br.getRegex("\\\\u003ca href=\\\\\"([^\"]*?)\\\\\" title").getMatch(0);
        }
        if (DLLINK == null) {
            DLLINK = br.getRegex("\"(http://[A-Za-z0-9\\-_\\.]+\\.chomikuj\\.pl/File\\.aspx[^<>\"]*?)\\\\\"").getMatch(0);
        }
        if (DLLINK != null) {
            DLLINK = unescape(DLLINK);
            DLLINK = Encoding.htmlDecode(DLLINK);
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
        if (cbr.containsHTML(PREMIUMONLY)) {
            try {
                throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_ONLY);
            } catch (final Throwable e) {
                if (e instanceof PluginException) {
                    throw (PluginException) e;
                }
            }
            throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.chomikujpl.only4registered", PREMIUMONLYUSERTEXT));
        }
        if (!videolink) {
            if (!getDllink(downloadLink, br, false)) {
                try {
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_ONLY);
                } catch (final Throwable e) {
                    if (e instanceof PluginException) {
                        throw (PluginException) e;
                    }
                }
                throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.hoster.chomikujpl.only4registered", PREMIUMONLYUSERTEXT));
            }
        }
        if (DLLINK == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, DLLINK, false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            logger.warning("The final dllink seems not to be a file!");
            br.followConnection();
            handleServerErrors();
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
        getDllink_premium(link, br, true);
        if (cbr.containsHTML("\"BuyAdditionalTransfer")) {
            logger.info("Disabling chomikuj.pl account: Not enough traffic available");
            throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
        }
        if (DLLINK == null) {
            String argh1 = br.getRegex("orgFile\\\\\" value=\\\\\"(.*?)\\\\\"").getMatch(0);
            String argh2 = br.getRegex("userSelection\\\\\" value=\\\\\"(.*?)\\\\\"").getMatch(0);
            if (argh1 == null || argh2 == null) {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            // For some files they ask
            // "Do you really want to download this file", so we have to confirm
            // it with "YES" here ;)
            if (cbr.containsHTML("Właściciel tego chomika udostępnia darmowy transfer, ale jego ilość jest obecnie zbyt mała, aby można było pobrać plik")) {
                br.postPage("http://chomikuj.pl/action/License/AcceptOwnTransfer?fileId=" + link.getStringProperty("fileid"), "orgFile=" + Encoding.urlEncode(argh1) + "&userSelection=" + Encoding.urlEncode(argh2) + "&__RequestVerificationToken=" + Encoding.urlEncode(link.getStringProperty("requestverificationtoken")));
            } else {
                br.postPage("http://chomikuj.pl/action/License/acceptLargeTransfer?fileId=" + link.getStringProperty("fileid"), "orgFile=" + Encoding.urlEncode(argh1) + "&userSelection=" + Encoding.urlEncode(argh2) + "&__RequestVerificationToken=" + Encoding.urlEncode(link.getStringProperty("requestverificationtoken")));
            }
            DLLINK = br.getRegex("redirectUrl\":\"(http://.*?)\"").getMatch(0);
            if (DLLINK == null) {
                DLLINK = br.getRegex("\\\\u003ca href=\\\\\"([^\"]*?)\\\\\" title").getMatch(0);
            }
            if (DLLINK != null) {
                DLLINK = Encoding.htmlDecode(DLLINK);
            }
            if (DLLINK == null) {
                getDllink(link, br, true);
            }
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
            handleServerErrors();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    private void handleServerErrors() throws PluginException {
        if (br.getURL().contains("Error.aspx")) {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 15 * 60 * 1000l);
        }
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
                if (acmatch) {
                    acmatch = Encoding.urlEncode(account.getPass()).equals(account.getStringProperty("pass", Encoding.urlEncode(account.getPass())));
                }
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
                getPage(this.br, MAINPAGE);
                final String lang = System.getProperty("user.language");
                final String requestVerificationToken = br.getRegex("<input name=\"__RequestVerificationToken\" type=\"hidden\" value=\"([^<>\"\\']+)\"").getMatch(0);
                if (requestVerificationToken == null) {
                    if ("de".equalsIgnoreCase(lang)) {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nPlugin defekt, bitte den JDownloader Support kontaktieren!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    } else {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nPlugin broken, please contact the JDownloader Support!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    }
                }
                postPageRaw(this.br, "http://chomikuj.pl/action/Login/TopBarLogin", "rememberLogin=true&rememberLogin=false&topBar_LoginBtn=Zaloguj&ReturnUrl=%2F" + Encoding.urlEncode(account.getUser()) + "&Login=" + Encoding.urlEncode(account.getUser()) + "&Password=" + Encoding.urlEncode(account.getPass()) + "&__RequestVerificationToken=" + Encoding.urlEncode(requestVerificationToken));
                if (br.getCookie(MAINPAGE, "RememberMe") == null) {
                    if ("de".equalsIgnoreCase(lang)) {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nUngültiger Benutzername oder ungültiges Passwort!\r\nSchnellhilfe: \r\nDu bist dir sicher, dass dein eingegebener Benutzername und Passwort stimmen?\r\nFalls dein Passwort Sonderzeichen enthält, ändere es und versuche es erneut!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    } else {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nInvalid username/password!\r\nQuick help:\r\nYou're sure that the username and password you entered are correct?\r\nIf your password contains special characters, change it (remove them) and try again!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    }
                }

                br.setCookie(MAINPAGE, "cookiesAccepted", "1");
                br.setCookie(MAINPAGE, "spt", "0");
                br.setCookie(MAINPAGE, "rcid", "1");

                postPageRaw(this.br, "http://chomikuj.pl/" + Encoding.urlEncode(account.getUser()), "ReturnUrl=%2F" + Encoding.urlEncode(account.getUser()) + "&Login=" + Encoding.urlEncode(account.getUser()) + "&Password=" + Encoding.urlEncode(account.getPass()) + "&rememberLogin=true&rememberLogin=false&topBar_LoginBtn=Zaloguj");
                getPage(this.br, "http://chomikuj.pl/" + Encoding.urlEncode(account.getUser()));

                /* Save cookies */
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

    private void getPage(final Browser br, final String url) throws Exception {
        br.getPage(url);
        cbr = br.cloneBrowser();
        cleanupBrowser(cbr, correctBR(br.toString()));
    }

    private void postPage(final Browser br, final String url, final String postData) throws Exception {
        br.postPage(url, postData);
        cbr = br.cloneBrowser();
        cleanupBrowser(cbr, correctBR(br.toString()));
    }

    private void postPageRaw(final Browser br, final String url, final String postData) throws Exception {
        br.postPageRaw(url, postData);
        cbr = br.cloneBrowser();
        cleanupBrowser(cbr, correctBR(br.toString()));
    }

    private String correctBR(final String input) {
        return input.replace("\\", "");
    }

    /**
     * This allows backward compatibility for design flaw in setHtmlCode(), It injects updated html into all browsers that share the same
     * request id. This is needed as request.cloneRequest() was never fully implemented like browser.cloneBrowser().
     * 
     * @param ibr
     *            Import Browser
     * @param t
     *            Provided replacement string output browser
     * @author raztoki
     * */
    private void cleanupBrowser(final Browser ibr, final String t) throws Exception {
        String dMD5 = JDHash.getMD5(ibr.toString());
        // preserve valuable original request components.
        final String oURL = ibr.getURL();
        final URLConnectionAdapter con = ibr.getRequest().getHttpConnection();

        Request req = new Request(oURL) {
            {
                boolean okay = false;
                try {
                    final Field field = this.getClass().getSuperclass().getDeclaredField("requested");
                    field.setAccessible(true);
                    field.setBoolean(this, true);
                    okay = true;
                } catch (final Throwable e2) {
                    e2.printStackTrace();
                }
                if (okay == false) {
                    try {
                        requested = true;
                    } catch (final Throwable e) {
                        e.printStackTrace();
                    }
                }

                httpConnection = con;
                setHtmlCode(t);
            }

            public long postRequest() throws IOException {
                return 0;
            }

            public void preRequest() throws IOException {
            }
        };

        ibr.setRequest(req);
        if (ibr.isDebug()) {
            logger.info("\r\ndirtyMD5sum = " + dMD5 + "\r\ncleanMD5sum = " + JDHash.getMD5(ibr.toString()) + "\r\n");
            System.out.println(ibr.toString());
        }
    }

    /* NO OVERRIDE!! We need to stay 0.9*compatible */
    public boolean allowHandle(final DownloadLink downloadLink, final PluginForHost plugin) {
        return downloadLink.getHost().equalsIgnoreCase(plugin.getHost());
    }

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), ChoMikujPl.DECRYPTFOLDERS, JDL.L("plugins.hoster.chomikujpl.decryptfolders", "Decrypt subfolders in folders")).setDefaultValue(true));
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}