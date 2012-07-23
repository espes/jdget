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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import jd.PluginWrapper;
import jd.http.Browser;
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

@HostPlugin(revision = "$Revision$", interfaceVersion = 3, names = { "premiumize.me" }, urls = { "REGEX_NOT_POSSIBLE_RANDOM-asdfasdfsadfsfs2133" }, flags = { 2 })
public class PremiumizeMe extends PluginForHost {

    private static final HashMap<Account, HashMap<String, Long>> hostUnavailableMap = new HashMap<Account, HashMap<String, Long>>();

    public PremiumizeMe(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("https://premiumize.me");
    }

    @Override
    public String getAGBLink() {
        return "https://premiumize.me/?show=tos";
    }

    public Browser newBrowser() {
        br = new Browser();
        br.setCookiesExclusive(true);
        // define custom browser headers and language settings.
        br.getHeaders().put("Accept-Language", "en-gb, en;q=0.9, de;q=0.8");
        br.setCookie("https://premiumize.me", "lang", "english");
        br.getHeaders().put("User-Agent", "JDownloader");
        br.setCustomCharset("utf-8");
        br.setConnectTimeout(60 * 1000);
        br.setReadTimeout(60 * 1000);
        return br;
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws PluginException {
        return AvailableStatus.UNCHECKABLE;
    }

    @Override
    public boolean canHandle(DownloadLink downloadLink, Account account) {
        if (account == null) {
            /* without account its not possible to download the link */
            return false;
        }
        synchronized (hostUnavailableMap) {
            HashMap<String, Long> unavailableMap = hostUnavailableMap.get(account);
            if (unavailableMap != null) {
                Long lastUnavailable = unavailableMap.get(downloadLink.getHost());
                if (lastUnavailable != null && System.currentTimeMillis() < lastUnavailable) {
                    return false;
                } else if (lastUnavailable != null) {
                    unavailableMap.remove(downloadLink.getHost());
                    if (unavailableMap.size() == 0) hostUnavailableMap.remove(account);
                }
            }
        }
        return true;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 0;
    }

    @Override
    public void handlePremium(DownloadLink link, Account account) throws Exception {
        /* handle premium should never be called */
        throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
    }

    private void handleDL(Account account, DownloadLink link, String dllink) throws Exception {
        /* we want to follow redirects in final stage */
        br.setFollowRedirects(true);
        dl = jd.plugins.BrowserAdapter.openDownload(br, link, dllink, true, 0);
        if (dl.getConnection().isContentDisposition()) {
            /* contentdisposition, lets download it */
            dl.startDownload();
            return;
        } else if (dl.getConnection().getContentType() != null && !dl.getConnection().getContentType().contains("html") && !dl.getConnection().getContentType().contains("text")) {
            /* no content disposition, but api says that some hoster might not have one */
            dl.startDownload();
            return;
        } else {
            /* download is not contentdisposition, so remove this host from premiumHosts list */
            br.followConnection();
            handleAPIErrors(br, account, link);
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
    }

    @Override
    public void handleMultiHost(DownloadLink link, Account account) throws Exception {
        br = newBrowser();
        showMessage(link, "Task 1: Generating Link");
        /* request Download */
        br.getPage("https://api.premiumize.me/pm-api/v1.php?method=directdownloadlink&params[login]=" + Encoding.urlEncode(account.getUser()) + "&params[pass]=" + Encoding.urlEncode(account.getPass()) + "&params[link]=" + Encoding.urlEncode(link.getDownloadURL()));
        handleAPIErrors(br, account, link);
        String dllink = br.getRegex("location\":\"(http[^\"]+)").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dllink = dllink.replaceAll("\\\\/", "/");
        showMessage(link, "Task 2: Download begins!");
        handleDL(account, link, dllink);
    }

    @Override
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        login(account);
        account.setValid(true);
        account.setConcurrentUsePossible(true);
        account.setMaxSimultanDownloads(-1);
        String status = br.getRegex("type\":\"(.*?)\"").getMatch(0);
        if (status == null) status = "Unknown Account Type";
        ai.setStatus(status);
        String expire = br.getRegex("\"expires\":(\\d+)").getMatch(0);
        if (expire != null) {
            ai.setValidUntil((Long.parseLong(expire)) * 1000);
        }
        String fairUse = br.getRegex("fairuse_left\":([\\d\\.]+)").getMatch(0);
        if (fairUse != null) {
            // 7 day rolling average
            // AVERAGE = way to display percentage value. prevent controlling from using figure. Just a GUI display for the user.
            // "fairuse_left":0.99994588120502,
            // ai.setTrafficLeft(AVERAGE(Integer.parseInt(fairUse.trim()) * 100));
        }
        String hostsSup = br.getPage("https://api.premiumize.me/pm-api/v1.php?method=hosterlist&params[login]=" + Encoding.urlEncode(account.getUser()) + "&params[pass]=" + Encoding.urlEncode(account.getPass()));
        handleAPIErrors(br, account, null);
        String HostsJSON = new Regex(hostsSup, "\"tldlist\":\\[([^\\]]+)\\]").getMatch(0);
        String[] hosts = new Regex(HostsJSON, "\"([a-zA-Z0-9\\.\\-]+)\"").getColumn(0);
        ArrayList<String> supportedHosts = new ArrayList<String>(Arrays.asList(hosts));
        ai.setProperty("multiHostSupport", supportedHosts);
        return ai;
    }

    private void login(Account account) throws Exception {
        br = newBrowser();
        br.getPage("https://api.premiumize.me/pm-api/v1.php?method=accountstatus&params[login]=" + Encoding.urlEncode(account.getUser()) + "&params[pass]=" + Encoding.urlEncode(account.getPass()));
        handleAPIErrors(br, account, null);
        if (br.containsHTML("type\":\"free\"")) { throw new PluginException(LinkStatus.ERROR_PREMIUM, "Free accounts are not supported", PluginException.VALUE_ID_PREMIUM_DISABLE); }
    }

    private void showMessage(DownloadLink link, String message) {
        link.getLinkStatus().setStatusText(message);
    }

    private void tempUnavailableHoster(Account account, DownloadLink downloadLink, long timeout) throws PluginException {
        if (downloadLink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT, "Unable to handle this errorcode!");
        synchronized (hostUnavailableMap) {
            HashMap<String, Long> unavailableMap = hostUnavailableMap.get(account);
            if (unavailableMap == null) {
                unavailableMap = new HashMap<String, Long>();
                hostUnavailableMap.put(account, unavailableMap);
            }
            /* wait 30 mins to retry this host */
            unavailableMap.put(downloadLink.getHost(), (System.currentTimeMillis() + timeout));
        }
        throw new PluginException(LinkStatus.ERROR_RETRY);
    }

    private void handleAPIErrors(Browser br, Account account, DownloadLink downloadLink) throws PluginException {
        String statusCode = br.getRegex("\"status\":(\\d+)").getMatch(0);
        if (statusCode == null) return;
        String statusMessage = br.getRegex("\"statusmessage\":\"([^\"]+)").getMatch(0);
        try {
            int status = Integer.parseInt(statusCode);
            switch (status) {
            case 200:
                /* all okay */
                return;
            case 400:
                /* not a valid link, do not try again with this multihoster */
                if (statusMessage == null) statusMessage = "Invalid DownloadLink";
                throw new PluginException(LinkStatus.ERROR_FATAL);
            case 401:
                /* not logged in, disable account. */
                if (statusMessage == null) statusMessage = "Login error";
                throw new PluginException(LinkStatus.ERROR_PREMIUM, statusMessage, PluginException.VALUE_ID_PREMIUM_DISABLE);
            case 402:
                /* account with outstanding payment,disable account */
                if (statusMessage == null) statusMessage = "Account payment required in order to download";
                throw new PluginException(LinkStatus.ERROR_PREMIUM, statusMessage, PluginException.VALUE_ID_PREMIUM_DISABLE);
            case 403:
                /* forbidden, banned ip , temp disable account */
                // additional info provided to the user for this error message.
                String statusMessage1 = "Login prevented by MultiHoster! Please contact them for resolution";
                if (statusMessage == null)
                    statusMessage = statusMessage1;
                else
                    statusMessage += statusMessage + " :: " + statusMessage1;
                throw new PluginException(LinkStatus.ERROR_PREMIUM, statusMessage, PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE);
            case 404:
                /* file offline */
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            case 428:
                /* hoster currently not possible,block host for 30 mins */
                if (statusMessage == null) statusMessage = "Hoster currently not possible";
                tempUnavailableHoster(account, downloadLink, 30 * 60 * 1000);
                break;
            case 502:
                /* unknown technical error, block host for 3 mins */
                if (statusMessage == null) statusMessage = "Unknown technical error";
                // tempUnavailableHoster(account, downloadLink, 3 * 60 * 1000);
                /* only disable plugin for this link */
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, statusMessage, 3 * 60 * 1000l);
            case 503:
                /* temp multihoster issue, maintenance period, block host for 3 mins */
                if (statusMessage == null) statusMessage = "Hoster temporarily not possible";
                // tempUnavailableHoster(account, downloadLink, 3 * 60 * 1000);
                /* only disable plugin for this link */
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, statusMessage, 3 * 60 * 1000l);
            case 509:
                /* fair use limit reached ,block host for 10 mins */
                if (statusMessage == null) statusMessage = "Fair use limit reached!";
                tempUnavailableHoster(account, downloadLink, 10 * 60 * 1000);
                break;
            default:
                /* unknown error, do not try again with this multihoster */
                if (statusMessage == null) statusMessage = "Unknown error code, please inform JDownloader Development Team";
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
        } catch (final PluginException e) {
            logger.info("PremiumizeMe Exception: statusCode: " + statusCode + " statusMessage: " + statusMessage);
            throw e;
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}