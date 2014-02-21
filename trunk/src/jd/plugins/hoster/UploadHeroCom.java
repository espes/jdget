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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import jd.PluginWrapper;
import jd.config.Property;
import jd.http.Browser;
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

import org.appwork.utils.formatter.SizeFormatter;

/**
 * They have a linkchecker but it's only available for registered users (well maybe it also works so) but it doesn't show the filenames of
 * the links: http://uploadhero.com/api/linktester.php postvalues: "linktest=" + links to check
 */
@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "uploadhero.co", "uploadhero.com" }, urls = { "http://(www\\.)?uploadhero\\.com?/(dl|v)/[A-Za-z0-9]+", "http://NULL_REGEX_BLAAAH12312321" }, flags = { 2, 0 })
public class UploadHeroCom extends PluginForHost {

    private static final String  MAINPAGE = "http://uploadhero.co";
    private static Object        LOCK     = new Object();
    private static AtomicInteger maxPrem  = new AtomicInteger(1);

    public UploadHeroCom(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium(MAINPAGE + "/premium");
    }

    @Override
    public String getAGBLink() {
        return MAINPAGE + "/tos";
    }

    public void correctDownloadLink(DownloadLink link) {
        link.setUrlDownload(link.getDownloadURL().replaceAll("http://(www\\.)?uploadhero\\.com?/(dl|v)/", MAINPAGE + "/dl/"));
    }

    // They got a linkchecker but it's only available for registered users: http://uploadhero.co/link-checker
    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws IOException, PluginException {
        this.setBrowserExclusive();
        br.setFollowRedirects(true);
        br.setCookie(MAINPAGE, "lang", "en");
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("(>The following download is not available on our server|>The file link is invalid|>The uploader has deleted the file|>The file was illegal and was deleted|<title>UploadHero \\- File Sharing made easy\\!</title>)")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        String filename = br.getRegex("<div class=\"nom_de_fichier\">([^<>\"/]+)</div>").getMatch(0);
        if (filename == null) filename = br.getRegex("<title>(.*?) \\- UploadHero</title>").getMatch(0);
        String filesize = br.getRegex("<span class=\"noir\">Filesize: </span><strong>([^<>\"\\'/]+)</strong>").getMatch(0);
        if (filename == null || filesize == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        link.setName(Encoding.htmlDecode(filename.trim()));
        link.setDownloadSize(SizeFormatter.getSize(filesize));
        return AvailableStatus.TRUE;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        doFree(downloadLink);
    }

    public void doFree(final DownloadLink downloadLink) throws Exception, PluginException {
        final Regex blockRegex = br.getRegex("/lightbox_block_download\\.php\\?min=(\\d+)\\&sec=(\\d+)\"");
        final String blockmin = blockRegex.getMatch(0);
        final String blocksec = blockRegex.getMatch(1);
        if (blockmin != null && blocksec != null) throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, ((Integer.parseInt(blockmin) + 5) * 60 + Integer.parseInt(blocksec)) * 1001l);
        final String captchaLink = br.getRegex("\"(/captchadl\\.php\\?[a-z0-9]+)\"").getMatch(0);
        if (captchaLink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        String code = getCaptchaCode(MAINPAGE + captchaLink, downloadLink);
        br.getPage(downloadLink.getDownloadURL() + "?code=" + code);
        if (!br.containsHTML("\"dddl\"")) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        String dllink = getDllink();
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dl = jd.plugins.BrowserAdapter.openDownload(br, downloadLink, Encoding.htmlDecode(dllink), false, 1);
        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            if (br.containsHTML("404 Not Found")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 60 * 60 * 1000l);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        dl.startDownload();
    }

    private String getDllink() {
        String dllink = br.getRegex("var magicomfg = \\'<a href=\"(http://[^<>\"]*?)\"").getMatch(0);
        if (dllink == null) dllink = br.getRegex("\"(http://storage\\d+\\.uploadhero\\.com?/\\?d=[A-Za-z0-9]+/[^<>\"/]+)\"").getMatch(0);
        return dllink;
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
                if (acmatch && ret != null && ret instanceof Map<?, ?> && !force) {
                    final Map<String, String> cookies = (Map<String, String>) ret;
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
                br.setCookie(MAINPAGE, "lang", "en");
                br.getPage("http://uploadhero.co/home");
                br.postPage(MAINPAGE + "/lib/connexion.php", "pseudo_login=" + Encoding.urlEncode(account.getUser()) + "&password_login=" + Encoding.urlEncode(account.getPass()));
                final String cookie = br.getRegex("id=\"cookietransitload\" style=\"display:none;\">([^<>\"]*?)</div>").getMatch(0);
                if (cookie == null) throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                br.setCookie(MAINPAGE, "uh", cookie);
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
    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        maxPrem.set(1);
        final AccountInfo ai = new AccountInfo();
        try {
            login(account, true);
        } catch (PluginException e) {
            account.setValid(false);
            return ai;
        }
        br.getPage(MAINPAGE + "/my-account");
        final Regex otherStuff = br.getRegex("\">Used storage</div><div class=\"champdeux\">([^<>\"]*?) \\- (\\d+) Files</div></div>");
        String space = otherStuff.getMatch(0);
        if (space != null) ai.setUsedSpace(space.trim());
        String filesNum = otherStuff.getMatch(1);
        if (filesNum != null) ai.setFilesNum(Integer.parseInt(filesNum));
        ai.setUnlimitedTraffic();
        final String expire = br.getRegex("<td><b>Days:</b></td>[\t\n\r ]+<td>(\\d+) days</td>").getMatch(0);
        if (expire == null) {
            try {
                maxPrem.set(1);
                // free accounts can still have captcha.
                account.setMaxSimultanDownloads(maxPrem.get());
                account.setConcurrentUsePossible(false);
            } catch (final Throwable e) {
                // not available in old Stable 0.9.581
            }
            account.setProperty("nopremium", true);
            ai.setStatus("Registered (free) user");
        } else {
            ai.setValidUntil(System.currentTimeMillis() + (Integer.parseInt(expire) + 1) * 24 * 60 * 60 * 1000l);
            try {
                maxPrem.set(20);
                account.setMaxSimultanDownloads(maxPrem.get());
                account.setConcurrentUsePossible(true);
            } catch (final Throwable e) {
                // not available in old Stable 0.9.581
            }
            account.setProperty("nopremium", false);
            ai.setStatus("Premium User");
        }
        account.setValid(true);
        return ai;
    }

    @Override
    public void handlePremium(final DownloadLink link, final Account account) throws Exception {
        requestFileInformation(link);
        login(account, false);
        br.setFollowRedirects(false);
        br.getPage(link.getDownloadURL());
        if (account.getBooleanProperty("nopremium", false)) {
            doFree(link);
        } else {
            String dllink = br.getRedirectLocation();
            if (dllink == null) dllink = getDllink();
            if (dllink == null) {
                logger.warning("Final downloadlink (String is \"dllink\") regex didn't match!");
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            dl = jd.plugins.BrowserAdapter.openDownload(br, link, Encoding.htmlDecode(dllink), true, 0);
            if (dl.getConnection().getContentType().contains("html")) {
                logger.warning("The final dllink seems not to be a file!");
                br.followConnection();
                if (br.containsHTML("File not found")) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                if (br.containsHTML("404 Not Found")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 60 * 60 * 1000l);
                /* check if cookie is outdated */
                synchronized (LOCK) {
                    Object ret = account.getProperty("cookies", null);
                    if (ret == null) {
                        /* cookie already got deleted */
                        throw new PluginException(LinkStatus.ERROR_RETRY);
                    }
                    Browser brc = br.cloneBrowser();
                    brc.getPage(MAINPAGE + "/my-account");
                    String expire = brc.getRegex("<td><b>Days:</b></td>[\t\n\r ]+<td>(\\d+) days</td>").getMatch(0);
                    if (expire == null) {
                        logger.info("try to refresh cookie, as it seems to be invalid!");
                        account.setProperty("cookies", Property.NULL);
                        throw new PluginException(LinkStatus.ERROR_RETRY);
                    }
                }
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            dl.startDownload();
        }
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        return maxPrem.get();
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