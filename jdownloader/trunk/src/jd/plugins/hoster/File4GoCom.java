//jDownloader - Downloadmanager
//Copyright (C) 2013  JD-Team support@jdownloader.org
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

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "file4go.com" }, urls = { "http://(www\\.)?file4go\\.com/(d/|download\\.php\\?id=)[a-z0-9]+" }, flags = { 2 })
public class File4GoCom extends PluginForHost {

    public File4GoCom(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium(MAINPAGE);
    }

    @Override
    public String getAGBLink() {
        return MAINPAGE;
    }

    private static final String MAINPAGE = "http://file4go.com";
    private static Object       LOCK     = new Object();

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.getHeaders().put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
        br.setCookie(MAINPAGE, "animesonline", "1");
        br.setCookie(MAINPAGE, "musicasab", "1");
        br.setCookie(MAINPAGE, "poup", "1");
        br.setCookie(MAINPAGE, "noadvtday", "0");
        br.setCookie(MAINPAGE, "hellpopab", "1");
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("Arquivo Temporariamente Indisponivel|ARQUIVO DELATADO PELO USUARIO OU REMOVIDO POR <|ARQUIVO DELATADO POR <b>INATIVIDADE") || br.getURL().contains("file4go.com/404.php")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        final String filename = br.getRegex("<b>Nome( do arquivo)?:</b>([^<>\"]*?)</span>").getMatch(1);
        final String filesize = br.getRegex("<b>Tamanho:</b>([^<>\"]*?)</span>").getMatch(0);
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(filename.trim());
        link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        final String reconnectWait = br.getRegex("var time = (\\d+)").getMatch(0);
        if (reconnectWait != null) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, Integer.parseInt(reconnectWait) * 1001l);
        final String fid = new Regex(downloadLink.getDownloadURL(), "([a-z0-9]+)$").getMatch(0);
        final String rcID = br.getRegex("\\?k=([^<>\"]*?)\"").getMatch(0);
        if (rcID == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        final PluginForHost recplug = JDUtilities.getPluginForHost("DirectHTTP");
        final jd.plugins.hoster.DirectHTTP.Recaptcha rc = ((DirectHTTP) recplug).getReCaptcha(br);
        rc.setId(rcID);
        rc.load();
        final File cf = rc.downloadCaptcha(getLocalCaptchaFile());
        final String c = getCaptchaCode(cf, downloadLink);
        br.postPage("http://www.file4go.com/getdownload.php", "recaptcha_challenge_field=" + rc.getChallenge() + "&recaptcha_response_field=" + Encoding.urlEncode(c) + "&id=" + fid);
        if (br.containsHTML("(api\\.recaptcha\\.net|google\\.com/recaptcha/api/)")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        br.setFollowRedirects(false);
        final String dllink = getDllink();
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        // Waittime can be skipped
        // int wait = 60;
        // final String waittime =
        // br.getRegex(">contador\\((\\d+)\\);").getMatch(0);
        // if (waittime != null) wait = Integer.parseInt(waittime);
        // sleep(wait * 1001l, downloadLink);
        /*
         * Skip these: br.getHeaders().put("X-Requested-With", "XMLHttpRequest"); br.postPage("http://www.file4go.com/recebe_id.php",
         * "acao=cadastrar&id=" + new Regex(downloadLink.getDownloadURL(), "([a-z0-9]+)$").getMatch(0)); String dllink =
         * br.getRegex("\"link\":\"([A-Za-z0-9]+)\"").getMatch(0); if (dllink == null) throw new
         * PluginException(LinkStatus.ERROR_PLUGIN_DEFECT); dllink = dllUrl + dllink;
         */
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, dllink, false, 1);
        /* resume no longer supported */
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML("BAIXE SIMULTANEAMENTE COM VELOCIDADE MÁXIMA")) throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, "Too many simultan downloads", 5 * 60 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    @SuppressWarnings("unchecked")
    private void login(final Account account, final boolean force) throws Exception {
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
                            if ("FREE".equalsIgnoreCase(key)) continue;
                            this.br.setCookie(MAINPAGE, key, value);
                        }
                        return;
                    }
                }
                br.setFollowRedirects(true);
                br.postPage("http://www.file4go.com/login.html", "acao=logar&login=" + Encoding.urlEncode(account.getUser()) + "&senha=" + Encoding.urlEncode(account.getPass()));
                final String lang = System.getProperty("user.language");
                if (br.getRequest().getHttpConnection().getResponseCode() == 404) {
                    if ("de".equalsIgnoreCase(lang)) {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nServer Fehler 404 - Login momentan nicht möglich!", PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
                    } else {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nIServer error 404 - login not possible at the moment!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    }
                }
                if (br.getCookie(MAINPAGE, "FILE4GO") == null) {
                    if ("de".equalsIgnoreCase(lang)) {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nUngültiger Benutzername oder ungültiges Passwort!\r\nSchnellhilfe: \r\nDu bist dir sicher, dass dein eingegebener Benutzername und Passwort stimmen?\r\nFalls dein Passwort Sonderzeichen enthält, ändere es und versuche es erneut!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    } else {
                        throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nInvalid username/password!\r\nQuick help:\r\nYou're sure that the username and password you entered are correct?\r\nIf your password contains special characters, change it (remove them) and try again!", PluginException.VALUE_ID_PREMIUM_DISABLE);
                    }
                }
                // Save cookies
                final HashMap<String, String> cookies = new HashMap<String, String>();
                final Cookies add = this.br.getCookies(MAINPAGE);
                for (final Cookie c : add.getCookies()) {
                    if ("FREE".equalsIgnoreCase(c.getKey())) continue;
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
    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        try {
            login(account, true);
        } catch (final PluginException e) {
            account.setValid(false);
            throw e;
        }
        ai.setUnlimitedTraffic();
        String expire = br.getRegex(">Premium Stop: (\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}) </b>").getMatch(0);
        account.setValid(true);
        if (expire == null) {
            final String lang = System.getProperty("user.language");
            if ("de".equalsIgnoreCase(lang)) {
                throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nNicht unterstützter Accounttyp!\r\nFalls du denkst diese Meldung sei falsch die Unterstützung dieses Account-Typs sich\r\ndeiner Meinung nach aus irgendeinem Grund lohnt,\r\nkontaktiere uns über das support Forum.", PluginException.VALUE_ID_PREMIUM_DISABLE);
            } else {
                throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nUnsupported account type!\r\nIf you think this message is incorrect or it makes sense to add support for this account type\r\ncontact us via our support forum.", PluginException.VALUE_ID_PREMIUM_DISABLE);
            }
        } else {
            ai.setValidUntil(TimeFormatter.getMilliSeconds(expire, "yyyy/MM/dd hh:mm:ss", null));
        }
        ai.setStatus("Premium User");
        return ai;
    }

    @Override
    public void handlePremium(final DownloadLink link, final Account account) throws Exception {
        requestFileInformation(link);
        login(account, false);
        br.setFollowRedirects(false);
        br.getPage(link.getDownloadURL());
        final String dllink = getDllink();
        if (dllink == null) {
            logger.warning("Final downloadlink (String is \"dllink\") regex didn't match!");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, dllink, true, 0);
        if (dl.getConnection().getContentType().contains("html")) {
            logger.warning("The final dllink seems not to be a file!");
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    private String getDllink() {
        String dllink = br.getRegex("\"(http://[a-z0-9]+\\.file4go\\.com:\\d+/[^<>\"]+/dll/[^<>\"]*?)\"").getMatch(0);
        if (dllink == null) dllink = br.getRegex("<span id=\"boton_download\" ><a href=\"(https?://[^<>\"]*?)\"").getMatch(0);
        return dllink;
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

    /* NO OVERRIDE!! We need to stay 0.9*compatible */
    public boolean hasCaptcha(DownloadLink link, jd.plugins.Account acc) {
        if (acc == null) {
            /* no account, yes we can expect captcha */
            return true;
        }
        if (Boolean.TRUE.equals(acc.getBooleanProperty("free"))) {
            /* free accounts also have captchas */
            return true;
        }
        return false;
    }
}