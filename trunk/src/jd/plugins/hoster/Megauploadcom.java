//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.controlling.AccountController;
import jd.controlling.JDLogger;
import jd.gui.UserIO;
import jd.http.Browser;
import jd.http.Browser.BrowserException;
import jd.http.RandomUserAgent;
import jd.http.Request;
import jd.http.URLConnectionAdapter;
import jd.nutils.encoding.Encoding;
import jd.parser.html.Form;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.Plugin;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.locale.JDL;

import org.appwork.utils.formatter.SizeFormatter;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "megaupload.com" }, urls = { "http://[\\w\\.]*?(megaupload)\\.com/.*?(\\?|&)d=[0-9A-Za-z]+" }, flags = { 2 })
public class Megauploadcom extends PluginForHost {

    private static enum STATUS {
        ONLINE,
        OFFLINE,
        API,
        BLOCKED
    }

    private static final String   MU_PARAM_PORT   = "MU_PARAM_PORT_NEW1";

    private final static String[] ports           = new String[] { "80", "800", "1723" };
    private static String         wwwWorkaround   = null;
    private static final Object   LOCK            = new Object();
    private static final Object   LOGINLOCK       = new Object();

    private static int            simultanpremium = 1;

    private static void workAroundTimeOut(final Browser br) {
        try {
            if (br != null) {
                br.setConnectTimeout(30000);
                br.setReadTimeout(30000);
            }
        } catch (final Throwable e) {
        }
    }

    private boolean             directDL           = false;

    private boolean             onlyapi            = false;

    private String              wait               = null;

    private static String       agent              = RandomUserAgent.generate();

    /*
     * every jd session starts with 1=default, because no waittime does not work
     * at moment
     * 
     * try to workaround the waittime, 0=no waittime, 1 = default, other = 60
     * secs
     */
    private static int          WaittimeWorkaround = 1;

    private static final Object PREMLOCK           = new Object();

    public Megauploadcom(final PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://www.megaupload.com/premium/en/");
        this.setConfigElements();
    }

    private void antiJDBlock(final Browser br) {
        try {
            workAroundTimeOut(br);
            if (br == null) { return; }
            br.getHeaders().put("User-Agent", Megauploadcom.agent);
            br.setAcceptLanguage("en-us,en;q=0.5");
            br.setCookie("http://" + Megauploadcom.wwwWorkaround + "megaupload.com", "l", "en");
        } catch (Throwable e) {
            /* setCookie throws exception in 09580 */
        }
    }

    @Override
    public boolean checkLinks(final DownloadLink urls[]) {
        /* linkcheck seesm to be broken or blocked */
        if (urls == null || urls.length == 0) { return false; }
        if (true) {
            for (DownloadLink link : urls) {
                link.setAvailable(false);
            }
            return true;
        }
        this.checkWWWWorkaround();
        final Browser br = new Browser();
        antiJDBlock(br);
        br.getHeaders().put("Cache-Control", null);
        final LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        int i = 0;
        String id;
        for (final DownloadLink u : urls) {
            /* mark link being checked by api */
            id = "00000";
            try {
                /*
                 * reset status to unchecked because api sometimes returns false
                 * results
                 */
                u.setAvailableStatus(AvailableStatus.UNCHECKABLE);
                id = this.getDownloadID(u);
                map.put("id" + i, id);
            } catch (final Exception e) {
                logger.log(java.util.logging.Level.SEVERE, "Exception occurred", e);
            }
            i++;
        }
        /* no customized header here, because they try block jd */
        int checked = 0;
        try {
            final String[] Dls = br.postPage("http://" + Megauploadcom.wwwWorkaround + "megaupload.com/mgr_linkcheck.php", map).split("&?(?=id[\\d]+=)");
            br.getHeaders().clear();
            br.getHeaders().setDominant(false);
            for (i = 1; i < Dls.length; i++) {
                final String string = Dls[i];
                final HashMap<String, String> queryQ = Request.parseQuery(Encoding.htmlDecode(string));
                try {
                    final int d = Integer.parseInt(string.substring(2, string.indexOf('=')));
                    final String name = queryQ.get("n");
                    checked++;
                    final DownloadLink downloadLink = urls[d];
                    // idX=1 -->invalid
                    // idX=3 -->temp not available
                    if (name != null) {

                        downloadLink.setFinalFileName(name);
                        downloadLink.setDownloadSize(Long.parseLong(queryQ.get("s")));
                        downloadLink.setAvailable(true);
                    } else {
                        if (Integer.parseInt(queryQ.get("id" + d)) == 3) {
                            downloadLink.setAvailableStatus(AvailableStatus.UNCHECKABLE);
                        } else {
                            downloadLink.setAvailable(false);
                            // downloadLink.setAvailableStatus(AvailableStatus.UNCHECKABLE);
                        }

                    }
                    downloadLink.setProperty("webcheck", true);
                } catch (final Exception e) {
                }
            }
        } catch (final Exception e) {
            return false;
        }
        return checked == urls.length;
    }

    private void checkWWWWorkaround() {
        synchronized (Megauploadcom.LOCK) {
            if (Megauploadcom.wwwWorkaround != null) { return; }
            final Browser tbr = new Browser();
            this.antiJDBlock(tbr);
            try {
                tbr.setConnectTimeout(10000);
                tbr.setReadTimeout(10000);
                tbr.getPage("http://www.megaupload.com");
                Megauploadcom.wwwWorkaround = "www.";
            } catch (final BrowserException e) {
                if (e.getException() != null && e.getException() instanceof UnknownHostException) {
                    logger.info("Using Workaround for Megaupload DNS Problem!");
                    Megauploadcom.wwwWorkaround = "";
                    return;
                }
            } catch (final IOException e) {
            } finally {
                if (Megauploadcom.wwwWorkaround == null) {
                    Megauploadcom.wwwWorkaround = "";
                }
            }
        }
    }

    @Override
    public void correctDownloadLink(final DownloadLink link) throws Exception {
        link.setUrlDownload("http://www.megaupload.com/?d=" + this.getDownloadID(link));
    }

    private void doDownload(final DownloadLink link, String url, final boolean resume, final Account account) throws Exception {
        if (url == null) {
            if (this.br.containsHTML("The file you are trying to access is temporarily")) { throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, 10 * 60 * 1000l); }
            logger.severe("dlURL is null");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        url = url.replaceFirst("megaupload\\.com/", "megaupload\\.com:" + this.usePort(link) + "/");
        this.br.setFollowRedirects(true);
        if (directDL == false) {
            final String waitb = this.br.getRegex("count=(\\d+);").getMatch(0);
            logger.info("Waittime: " + waitb);
            if (waitb == null) {
                /* debug */
                try {
                    logger.severe(br.toString());
                } catch (Throwable e) {
                }
            }
            long waittime = 0;
            try {
                if (Megauploadcom.WaittimeWorkaround == 0) {
                    /* try not to wait */
                    waittime = 0;
                } else if (Megauploadcom.WaittimeWorkaround == 1) {
                    /* try normal waittime */
                    if (waitb != null) {
                        waittime = Long.parseLong(waitb);
                    }
                } else {
                    /* last try with 60 secs */
                    waittime = 60;
                }
            } catch (final Exception e) {
            }
            if (waittime > 0) {
                this.sleep(waittime * 1000, link);
            }
        }
        try {
            try {
                /* remove next major update */
                /* workaround for broken timeout in 0.9xx public */
                /*
                 * workaround for buggy megaupload servers that can freeze
                 * stable 0.9xx
                 */
                br.setConnectTimeout(30000);
                br.setReadTimeout(20000);
            } catch (final Throwable ee) {
            }
            this.dl = jd.plugins.BrowserAdapter.openDownload(this.br, link, url, resume, this.isPremium(account, this.br.cloneBrowser(), false, true) ? 0 : 1);
            try {
                /* remove next major update */
                /* workaround for broken timeout in 0.9xx public */
                dl.getRequest().setConnectTimeout(30000);
                dl.getRequest().setReadTimeout(60000);
            } catch (final Throwable ee) {
            }
            if (!this.dl.getConnection().isOK()) {
                this.dl.getConnection().disconnect();
                if (this.dl.getConnection().getResponseCode() == 416) {
                    /* server error for resume, try again */
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError(Reset might help)", 10 * 60 * 1000l);
                }
                if (this.dl.getConnection().getResponseCode() == 503) {
                    this.limitReached(link, 10 * 60, "Limit Reached (2)!");
                }
                if (this.dl.getConnection().getResponseCode() == 404) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND, "File no longer available or server error!"); }
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            if (!this.dl.getConnection().isContentDisposition()) {
                this.br.followConnection();
                handleWaittimeWorkaround(link, this.br);
                if (this.br.containsHTML("The file you are trying to access is temporarily")) { throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "File currently not available", 10 * 60 * 1000l); }
                if (this.br.getURL().contains("d=" + getDownloadID(link))) {
                    logger.info(br.toString());
                    logger.info("try to workaround loop with downloadpage again");
                    throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "ServerError: redirect to Downloadpage", 5 * 60 * 1000l);
                }
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
            try {
                /* remove next major update */
                /* workaround for broken timeout in 0.9xx public */
                /*
                 * workaround for buggy megaupload servers that can freeze
                 * stable 0.9xx
                 */
                dl.getConnection().setConnectTimeout(30000);
                dl.getConnection().setReadTimeout(20000);
            } catch (final Throwable ee) {
            }
            this.dl.startDownload();
            // Wenn ein Download Premium mit mehreren chunks angefangen wird,
            // und
            // dann versucht wird ihn free zu resumen, schlägt das fehl, weil jd
            // die
            // mehrfachchunks aus premium nicht resumen kann.
            // In diesem Fall wird der link resetted.
            if (link.getLinkStatus().hasStatus(LinkStatus.ERROR_DOWNLOAD_FAILED) && link.getLinkStatus().getErrorMessage() != null && link.getLinkStatus().getErrorMessage().contains("Limit Exceeded")) {
                link.setChunksProgress(null);
                link.getLinkStatus().setStatus(LinkStatus.ERROR_RETRY);
            }
        } finally {
            if (this.dl.getConnection() != null) {
                this.dl.getConnection().disconnect();
            }
        }
    }

    @Override
    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        final AccountInfo ai = new AccountInfo();
        if (true) {
            account.setValid(false);
            ai.setStatus("Host is offline!");
            return ai;
        }
        this.checkWWWWorkaround();
        this.setBrowserExclusive();
        synchronized (LOGINLOCK) {
            try {
                this.login(account, false);
            } catch (final PluginException e) {
                logger.info("Account got disabled(AccountCheck)");
                account.setValid(false);
                return ai;
            }
            if (!this.isPremium(account, this.br, true, false)) {
                account.setValid(true);
                ai.setStatus("Free Membership");
                try {
                    account.setMaxSimultanDownloads(1);
                    account.setConcurrentUsePossible(false);
                } catch (final Throwable e) {
                }
                return ai;
            }
        }
        String type = getAccountType(br);
        if (type != null && !type.contains("Lifetime")) {
            ai.setStatus("Premium Membership");
            String days = this.br.getRegex("class=\"account_txt\"> ([^<>/]*?) days remaining").getMatch(0);
            if (days != null) days = days.trim();
            if (days != null && !days.equalsIgnoreCase("Unlimited")) {
                /* x days left */
                ai.setValidUntil(System.currentTimeMillis() + Long.parseLong(days) * 24 * 60 * 60 * 1000);
            } else if (days == null || days.equals("0")) {
                final String hours = this.br.getRegex("class=\"account_txt\"> (\\d+) hours remaining").getMatch(0);
                if (hours == null || "0".equals(hours)) {
                    final String minutes = this.br.getRegex("class=\"account_txt\"> (\\d+) minutes remaining ").getMatch(0);
                    if (minutes != null) {
                        /* x minutes left */
                        ai.setValidUntil(System.currentTimeMillis() + Long.parseLong(minutes) * 60 * 1000);
                    } else {
                        ai.setExpired(true);
                        account.setValid(false);
                        return ai;
                    }
                } else {
                    /* x hourse left */
                    ai.setValidUntil(System.currentTimeMillis() + Long.parseLong(hours) * 60 * 60 * 1000);
                }

            }
        } else if (type != null && type.contains("Lifetime")) {
            ai.setStatus("Lifetime Membership");
        }
        try {
            account.setMaxSimultanDownloads(-1);
            account.setConcurrentUsePossible(true);
        } catch (final Throwable e) {
        }
        account.setValid(true);
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://www.megaupload.com/terms/";
    }

    public String getDownloadID(final DownloadLink link) throws MalformedURLException {
        final HashMap<String, String> p = Request.parseQuery(link.getDownloadURL());
        String ret = p.get("d");
        if (ret != null) ret = ret.toUpperCase(Locale.ENGLISH);
        return ret;
    }

    private STATUS getFileStatus(final DownloadLink link) throws MalformedURLException, PluginException {
        if (true) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        this.onlyapi = false;
        this.directDL = false;
        this.checkWWWWorkaround();
        this.setBrowserExclusive();
        this.br.setCookie("http://" + Megauploadcom.wwwWorkaround + "megaupload.com", "l", "en");
        this.websiteFileCheck(link, this.br);
        if (link.getAvailableStatus() == AvailableStatus.TRUE) { return STATUS.ONLINE; }
        if (link.getAvailableStatus() == AvailableStatus.FALSE) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        if (link.getAvailableStatus() == AvailableStatus.UNCHECKABLE) {
            if (this.onlyapi) {
                /*
                 * API only,no further check needed because its done on
                 * downloadtry anyway
                 */
                return STATUS.API;
            } else {
                return STATUS.BLOCKED;
            }
        }
        return STATUS.OFFLINE;
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return this.getPluginConfig().getIntegerProperty("MAX_FREE_PARALELL_DOWNLOADS", 1);
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        synchronized (Megauploadcom.PREMLOCK) {
            return Megauploadcom.simultanpremium;
        }
    }

    private void getRedirectforAPI(final String url, final DownloadLink downloadLink) throws PluginException, InterruptedException {
        try {
            this.br.getPage(url);
            /* file offline */
            if (this.br.getRequest().getHttpConnection().getResponseCode() == 404) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
        } catch (final IOException e) {
            try {
                /* traffic limit reached */
                if (this.br.getRequest().getHttpConnection().getResponseCode() == 503) {
                    this.limitReached(downloadLink, 10 * 60, "API Limit reached!");
                }
            } catch (final Exception e1) {
                JDLogger.exception(e1);
            }
            /*
             * debug info, can be removed when we have correct error in case of
             * pw needed
             */
            try {
                logger.info(this.br.getRequest().getHttpConnection().toString());
            } catch (final Throwable e2) {
            }
            JDLogger.exception(e);
            /* pw handling */
            try {
                String passCode;
                if (downloadLink.getStringProperty("pass", null) == null) {
                    passCode = Plugin.getUserInput(null, downloadLink);
                } else {
                    /* gespeicherten PassCode holen */
                    passCode = downloadLink.getStringProperty("pass", null);
                }
                this.br.getPage(url + "&p=" + passCode);
                /* file offline */
                if (this.br.getRequest().getHttpConnection().getResponseCode() == 404) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
                downloadLink.setProperty("pass", passCode);
                return;
            } catch (final IOException e2) {
                try {
                    /* traffic limit reached */
                    if (this.br.getRequest().getHttpConnection().getResponseCode() == 503) {
                        this.limitReached(downloadLink, 10 * 60, "API Limit reached!");
                    }
                } catch (final Exception e1) {
                    JDLogger.exception(e1);
                }
                /*
                 * debug info, can be removed when we have correct error in case
                 * of pw needed
                 */
                try {
                    logger.info(this.br.getRequest().getHttpConnection().toString());
                } catch (final Throwable e3) {
                }
                JDLogger.exception(e2);
                /* pw wrong */
                downloadLink.setProperty("pass", null);
                throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.errors.wrongpassword", "Password wrong"));
            }
        }
    }

    public void handleAPIDownload(final DownloadLink link, final Account account) throws Exception {
        String url = null;
        if (directDL == false) {
            this.setBrowserExclusive();
            this.br.setFollowRedirects(false);
            final String dlID = this.getDownloadID(link);
            this.br.setDebug(true);
            /*
             * here the customized headers are needed because without api
             * download does not work
             */
            this.br.getHeaders().clear();
            this.br.getHeaders().setDominant(true);
            this.br.getHeaders().put("Accept", "text/plain,text/html,*/*;q=0.3");
            this.br.getHeaders().put("Accept-Encoding", "*;q=0.1");
            this.br.getHeaders().put("TE", "trailers");
            this.br.getHeaders().put("Host", "" + Megauploadcom.wwwWorkaround + "megaupload.com");
            this.br.getHeaders().put("Connection", "TE");
            this.br.getHeaders().put("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)");
            String user = null;
            if (account != null) {
                user = account.getStringProperty("user", null);
            }
            if (user != null) {
                this.getRedirectforAPI("http://" + Megauploadcom.wwwWorkaround + "megaupload.com/mgr_dl.php?d=" + dlID + "&u=" + user, link);
            } else {
                this.getRedirectforAPI("http://" + Megauploadcom.wwwWorkaround + "megaupload.com/mgr_dl.php?d=" + dlID, link);
            }
            if (this.br.getRedirectLocation() == null || this.br.getRedirectLocation().toUpperCase().contains(dlID)) {
                this.limitReached(link, 10 * 60, "API Limit reached!");
            }

            url = this.br.getRedirectLocation();
            this.br.getHeaders().put("Host", new URL(url).getHost());
            this.br.getHeaders().put("Connection", "Keep-Alive,TE");
        } else {
            br.setFollowRedirects(false);
            br.getPage(link.getDownloadURL());
            url = this.br.getRedirectLocation();
        }
        this.doDownload(link, url, true, account);
    }

    @Override
    public void handleFree(final DownloadLink parameter) throws Exception {
        // usepremium = false;

        final STATUS filestatus = this.getFileStatus(parameter);
        switch (filestatus) {
        case API:
            /* api only download possible */
            this.handleAPIDownload(parameter, null);
            return;
        case ONLINE:
            /* handle download via website */
            this.handleWebsiteDownload(parameter, null);
            return;
        case OFFLINE:
            /* file offline */
            // logger.info("DebugInfo for maybe Wrong FileNotFound: " +
            // br.toString());
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        case BLOCKED:
            if (this.wait != null) {
                throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, Integer.parseInt(this.wait.trim()) * 60 * 1000l);
            } else {
                throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 25 * 60 * 1000l);
            }
        }
        logger.severe("Ooops");
        throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
    }

    @Override
    public void handlePremium(final DownloadLink parameter, final Account account) throws Exception {
        boolean free = false;
        // free = false;
        STATUS filestatus = null;
        synchronized (LOGINLOCK) {
            filestatus = this.getFileStatus(parameter);
            if (filestatus != STATUS.API && filestatus != STATUS.OFFLINE) {
                if (filestatus == STATUS.BLOCKED) {
                    /* we are blocked, so we have to login again */
                    this.login(account, false);
                } else {
                    this.login(account, true);
                }
            }
            final boolean check = filestatus == STATUS.BLOCKED && !(filestatus == STATUS.API || filestatus == STATUS.OFFLINE);
            if (!this.isPremium(account, this.br.cloneBrowser(), check, true)) {
                Megauploadcom.simultanpremium = 1;
                free = true;
            } else {
                if (Megauploadcom.simultanpremium + 1 > 20) {
                    Megauploadcom.simultanpremium = 20;
                } else {
                    Megauploadcom.simultanpremium++;
                }
            }
        }
        switch (filestatus) {
        case API:
            /* api only download possible */
            this.handleAPIDownload(parameter, account);
            return;
        case OFFLINE:
            /* file offline */
            // logger.info("DebugInfo for maybe Wrong FileNotFound: " +
            // br.toString());
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        case BLOCKED:
            /* only free users should have to wait on blocked */
            if (free) {
                if (this.wait != null) {
                    throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, Integer.parseInt(this.wait.trim()) * 60 * 1000l);
                } else {
                    throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 25 * 60 * 1000l);
                }
            }
            break;
        case ONLINE:
            if (free) {
                /* handle download via website, free user */
                this.handleWebsiteDownload(parameter, account);
                return;
            }
            break;
        }
        /* website download premium */
        String url = null;
        if (directDL == false) {
            this.br.setFollowRedirects(false);
            this.br.getPage("http://" + Megauploadcom.wwwWorkaround + "megaupload.com/?d=" + this.getDownloadID(parameter));

            if (br.containsHTML("The file has been deleted because it was violating")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
            if (br.containsHTML("Invalid link")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
            if (this.br.containsHTML("Unfortunately, the link you have clicked is not available")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }

            if (this.br.getRedirectLocation() == null) {
                if (this.br.toString().trim().length() == 0) {
                    // megupload bug. probably direct download is activated.
                    if (UserIO.isOK(UserIO.getInstance().requestConfirmDialog(0, "Direct Download Workaround", "Direct download is enabled. \r\nThis mode is not supported. \r\nDo you want to disable direct download?", null, null, null))) {
                        this.br.cloneBrowser().postPage("http://megaupload.com/?c=account", "do=directdownloads&accountupdate=1");
                        throw new PluginException(LinkStatus.ERROR_RETRY);
                    } else {
                        throw new PluginException(LinkStatus.ERROR_FATAL, "Direct download not supported");
                    }

                }
                /*
                 * check if we are still premium user, because we login via
                 * cookie
                 */
                String red = this.br.getRegex("document\\.location='(.*?)'").getMatch(0);
                if (red != null && red.contains("megavideo")) {
                    /* View on Megavideo uses the document.location stuff too */
                    red = null;
                }
                if (red != null || this.br.containsHTML("trying to download is larger than")) {
                    if (!this.isPremium(account, this.br.cloneBrowser(), true, true)) {
                        /* no longer premium retry */
                        logger.info("No longer a premiumaccount, retry as normal account!");
                        parameter.getLinkStatus().setRetryCount(parameter.getLinkStatus().getRetryCount() + 1);
                        throw new PluginException(LinkStatus.ERROR_RETRY);
                    } else {
                        /* still premium, so something went wrong */
                        throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                    }
                }
                Form form = this.br.getForm(0);
                if (form != null && form.containsHTML("logout")) {
                    form = this.br.getForm(1);
                }
                if (form != null && form.containsHTML("filepassword")) {
                    String passCode;
                    if (parameter.getStringProperty("pass", null) == null) {
                        passCode = Plugin.getUserInput("Password?", parameter);
                    } else {
                        /* gespeicherten PassCode holen */
                        passCode = parameter.getStringProperty("pass", null);
                    }
                    form.put("filepassword", passCode);
                    this.br.submitForm(form);
                    form = this.br.getForm(0);
                    if (form != null && form.containsHTML("logout")) {
                        form = this.br.getForm(1);
                    }
                    if (form != null && form.containsHTML("filepassword")) {
                        parameter.setProperty("pass", null);
                        throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.errors.wrongpassword", "Password wrong"));
                    } else {
                        parameter.setProperty("pass", passCode);
                    }
                }
                if (form != null && form.containsHTML("captchacode")) {
                    /* captcha form as premiumuser? check status again */
                    if (!this.isPremium(account, this.br.cloneBrowser(), true, true)) {
                        /* no longer premium retry */
                        logger.info("No longer a premiumaccount, retry as normal account!");
                        parameter.getLinkStatus().setRetryCount(parameter.getLinkStatus().getRetryCount() + 1);
                        throw new PluginException(LinkStatus.ERROR_RETRY);
                    } else {
                        /* still premium, so something went wrong */
                        throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
                    }
                }
                if (this.br.containsHTML("location='http://www\\.megaupload\\.com/\\?c=msg")) {
                    this.br.getPage("http://www.megaupload.com/?c=msg");
                    this.wait = this.br.getRegex("Please check back in (\\d+) minutes").getMatch(0);
                    logger.info("Megaupload blocked this IP(3): " + this.wait + " mins");
                    if (this.wait != null) {
                        throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, Integer.parseInt(this.wait.trim()) * 60 * 1000l);
                    } else {
                        throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 25 * 60 * 1000l);
                    }
                }
                url = getDownloadURL(br);
            } else {
                /* direct download */
                url = this.br.getRedirectLocation();
            }
        } else {
            br.setFollowRedirects(false);
            br.getPage(parameter.getDownloadURL());
            url = this.br.getRedirectLocation();
        }
        this.doDownload(parameter, url, true, account);
    }

    private synchronized void handleWaittimeWorkaround(final DownloadLink link, final Browser br) throws PluginException {
        if (br.containsHTML("gencap\\.php\\?")) {
            /* page contains captcha */
            if (Megauploadcom.WaittimeWorkaround == 0) {
                /*
                 * we tried to workaround the waittime, so lets try again with
                 * normal waittime
                 */
                Megauploadcom.WaittimeWorkaround = 1;
                logger.info("WaittimeWorkaround failed (1)");
                link.getLinkStatus().setRetryCount(link.getLinkStatus().getRetryCount() + 1);
                throw new PluginException(LinkStatus.ERROR_RETRY);
            } else if (Megauploadcom.WaittimeWorkaround == 1) {
                logger.info("strange servererror: we did wait(normal) but again a captcha?");
                /* no reset here for retry count */
                /* retry with longer waittime */
                Megauploadcom.WaittimeWorkaround = 2;
                throw new PluginException(LinkStatus.ERROR_RETRY);
            } else if (Megauploadcom.WaittimeWorkaround == 2) {
                logger.info("strange servererror: we did wait(longer) but again a captcha?");
                /* no reset here for retry count */
                /* retry with normal waittime again */
                Megauploadcom.WaittimeWorkaround = 1;
                throw new PluginException(LinkStatus.ERROR_RETRY);
            }
        } else {
            /* something else? */
            if (Megauploadcom.WaittimeWorkaround == 0) {
                /* lets try again with normal waittime */
                Megauploadcom.WaittimeWorkaround = 1;
                logger.info("WaittimeWorkaround failed (2)");
                link.getLinkStatus().setRetryCount(link.getLinkStatus().getRetryCount() + 1);
                throw new PluginException(LinkStatus.ERROR_RETRY);
            } else if (Megauploadcom.WaittimeWorkaround > 1) {
                logger.info("WaittimeWorkaround failed (3)");
                if (++Megauploadcom.WaittimeWorkaround > 1) {
                    Megauploadcom.WaittimeWorkaround = 1;
                }
            }
        }
    }

    private String getDownloadURL(Browser br) {
        if (br == null) return null;
        String url = br.getRegex("id=\"downloadlink\">.*?<a href=\"(.*?)\"").getMatch(0);
        if (url == null) {
            url = br.getRegex("href=\"(http://[^\"]*?)\" class=\"down_ad_butt1\">").getMatch(0);
        }
        if (url == null) {
            url = br.getRegex("href=\"(http://[^\"]*?)\" class=\"download_regular_usual\"").getMatch(0);
        }
        if (url == null) {
            url = br.getRegex("class=\"download_member_bl\".*?<a href=\"(.*?)\"").getMatch(0);
            if (url != null && !url.startsWith("http")) url = null;
        }
        if (url == null) {
            /*
             * seems free users get directdownload too sometimes, maybe for
             * special links?
             */
            url = br.getRedirectLocation();
        }
        return url;
    }

    public void handleWebsiteDownload(final DownloadLink link, final Account account) throws Exception {
        String url = null;
        if (directDL == false) {
            if (account != null) {
                this.login(account, true);
            }
            int captchTries = 10;
            Form form = null;
            String code = null;
            while (captchTries-- >= 0) {
                this.br.getPage("http://" + Megauploadcom.wwwWorkaround + "megaupload.com/?d=" + this.getDownloadID(link));
                url = getDownloadURL(br);
                if (url != null) break;
                /* check for iplimit */
                String red = this.br.getRegex("document\\.location='(.*?)'").getMatch(0);
                if (red != null && red.contains("megavideo")) {
                    /* View on Megavideo uses the document.location stuff too */
                    red = null;
                }
                if (red != null) {
                    logger.severe("Your IP got banned");
                    this.br.getPage(red);
                    final String wait = this.br.getRegex("Please check back in (\\d+) minute").getMatch(0);
                    int l = 30;
                    if (wait != null) {
                        l = Integer.parseInt(wait.trim());
                    }
                    this.limitReached(link, l * 60, "Limit Reached (1)!");
                }
                /* free users can download filesizes up to 1gb max */
                if (br.containsHTML("class=\"download_l_descr\">")) { throw new PluginException(LinkStatus.ERROR_FATAL, "File is over 1GB and needs Premium Account"); }

                form = this.br.getForm(0);
                if (form != null && form.containsHTML("logout")) {
                    form = this.br.getForm(1);
                }
                if (form != null && form.containsHTML("filepassword")) {
                    String passCode;
                    if (link.getStringProperty("pass", null) == null) {
                        passCode = Plugin.getUserInput("Password?", link);
                    } else {
                        /* gespeicherten PassCode holen */
                        passCode = link.getStringProperty("pass", null);
                    }
                    form.put("filepassword", passCode);
                    this.br.submitForm(form);
                    form = this.br.getForm(0);
                    if (form != null && form.containsHTML("logout")) {
                        form = this.br.getForm(1);
                    }
                    if (form != null && form.containsHTML("filepassword")) {
                        link.setProperty("pass", null);
                        throw new PluginException(LinkStatus.ERROR_FATAL, JDL.L("plugins.errors.wrongpassword", "Password wrong"));
                    } else {
                        link.setProperty("pass", passCode);
                    }
                }
                if (form != null && form.containsHTML("captchacode")) {
                    final String captcha = form.getRegex("Enter this.*?src=\"(.*?gencap.*?)\"").getMatch(0);
                    final File file = this.getLocalCaptchaFile();
                    final Browser c = this.br.cloneBrowser();
                    c.getHeaders().put("Accept", "image/png,image/*;q=0.8,*/*;q=0.5");
                    final URLConnectionAdapter con = c.openGetConnection(captcha);
                    Browser.download(file, con);
                    code = this.getCaptchaCode(file, link);
                    if (code == null) {
                        continue;
                    }
                    form.put("captcha", code);
                    this.br.submitForm(form);
                    form = this.br.getForm(0);
                    if (form != null && form.containsHTML("logout")) {
                        form = this.br.getForm(1);
                    }
                    if (form != null && form.containsHTML("captchacode")) {
                        continue;
                    } else {
                        break;
                    }
                }
                url = getDownloadURL(br);
                if (url != null) break;
            }
            if (form != null && form.containsHTML("captchacode")) { throw new PluginException(LinkStatus.ERROR_CAPTCHA); }
            if (this.br.containsHTML("location='http://www\\.megaupload\\.com/\\?c=msg")) {
                this.br.getPage("http://www.megaupload.com/?c=msg");
                this.wait = this.br.getRegex("Please check back in (\\d+) minutes").getMatch(0);
                if (this.wait != null) {
                    logger.info("Megaupload blocked this IP(3): " + this.wait + " mins");
                    throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, Integer.parseInt(this.wait.trim()) * 60 * 1000l);
                } else {
                    logger.severe("Waittime not found!: " + this.br.toString());
                    throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, 25 * 60 * 1000l);
                }
            }
            if (url == null) url = getDownloadURL(br);
        } else {
            br.setFollowRedirects(false);
            br.getPage(link.getDownloadURL());
            url = br.getRedirectLocation();
        }
        this.doDownload(link, url, true, account);
    }

    // do not add @Override here to keep 0.* compatibility
    public boolean hasCaptcha() {
        return true;
    }

    public boolean isPremium(final Account account, final Browser br, final boolean refresh, boolean cloneBrowser) throws IOException {
        synchronized (LOGINLOCK) {
            if (account == null) { return false; }
            if (account.getBooleanProperty("typeknown", false) == false || refresh) {
                final Browser brc;
                if (cloneBrowser) {
                    brc = br.cloneBrowser();
                } else {
                    brc = br;
                }
                this.antiJDBlock(brc);
                brc.getPage("http://" + Megauploadcom.wwwWorkaround + "megaupload.com/?c=account");
                String type = getAccountType(brc);
                if (type == null || type.equalsIgnoreCase("regular")) {
                    account.setProperty("ispremium", false);
                    if (type != null) {
                        account.setProperty("typeknown", true);
                    } else {
                        account.setProperty("typeknown", false);
                    }
                    return false;
                } else {
                    account.setProperty("ispremium", true);
                    account.setProperty("typeknown", true);
                    return true;
                }
            } else {
                return account.getBooleanProperty("ispremium", false);
            }
        }
    }

    private String getAccountType(Browser brc) {
        String type = brc.getRegex(Pattern.compile("class=\"account_txt\">(Premium)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE)).getMatch(0);
        if (type == null) type = brc.getRegex(Pattern.compile("class=\"account_txt\">(Lifetime Platinum)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE)).getMatch(0);
        return type;
    }

    /*
     * TODO: remove with next major update, DownloadWatchDog/AccountController
     * handle blocked accounts now
     */
    @Override
    public boolean isPremiumDownload() {
        /* free user accounts are no premium accounts */
        final Account acc = AccountController.getInstance().getValidAccount(this);
        if (acc != null) {
            /* acc type is known */
            if (acc.getBooleanProperty("typeknown", false)) {
                /* return true in case acc is premium */
                return acc.getBooleanProperty("ispremium", false);
            } else {
                /* unknown so lets try */
                return true;
            }
        }
        return false;
    }

    private void limitReached(final DownloadLink link, final int secs, final String message) throws PluginException {
        throw new PluginException(LinkStatus.ERROR_IP_BLOCKED, message, secs * 1000l);
    }

    public void login(final Account account, final boolean cookielogin) throws IOException, PluginException {
        synchronized (LOGINLOCK) {
            String user = account.getStringProperty("user", null);
            this.antiJDBlock(this.br);
            if (cookielogin && user != null) {
                this.br.setCookie("http://" + Megauploadcom.wwwWorkaround + "megaupload.com", "l", "en");
                this.br.setCookie("http://" + Megauploadcom.wwwWorkaround + "megaupload.com", "user", user);
                return;
            } else {
                if (account.getUser().trim().equalsIgnoreCase("cookie")) {
                    this.setBrowserExclusive();
                    this.br.setCookie("http://" + Megauploadcom.wwwWorkaround + "megaupload.com", "l", "en");
                    this.br.setCookie("http://" + Megauploadcom.wwwWorkaround + "megaupload.com", "user", account.getPass());
                    this.br.getPage("http://" + Megauploadcom.wwwWorkaround + "megaupload.com/");
                } else {
                    this.setBrowserExclusive();
                    this.br.setCookie("http://" + Megauploadcom.wwwWorkaround + "megaupload.com", "l", "en");
                    this.br.getPage("http://" + Megauploadcom.wwwWorkaround + "megaupload.com/?c=login");
                    this.br.postPage("http://" + Megauploadcom.wwwWorkaround + "megaupload.com/?c=login", "login=1&redir=1&username=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()));
                }
                user = this.br.getCookie("http://" + Megauploadcom.wwwWorkaround + "megaupload.com", "user");
                this.br.setCookie("http://" + Megauploadcom.wwwWorkaround + "megaupload.com", "user", user);
                account.setProperty("user", user);
                if (user == null) {
                    account.setProperty("ispremium", false);
                    account.setProperty("typeknown", false);
                    logger.info("Account got disabled");
                    throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
                }
            }
        }
    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink parameter) throws IOException, PluginException {
        final STATUS filestatus = this.getFileStatus(parameter);
        switch (filestatus) {
        case API:
            /* api only download possible */
            this.checkLinks(new DownloadLink[] { parameter });
            return parameter.getAvailableStatus();
        case ONLINE:
            return AvailableStatus.TRUE;
        case OFFLINE:
            /* file offline */
            return AvailableStatus.FALSE;
        case BLOCKED:
            /* ip blocked by megauploaded */
            return AvailableStatus.UNCHECKABLE;
        }
        return AvailableStatus.UNCHECKED;
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(final DownloadLink link) {
        link.setProperty(Megauploadcom.MU_PARAM_PORT, 0);
    }

    @Override
    public void resetPluginGlobals() {
    }

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_COMBOBOX_INDEX, this.getPluginConfig(), Megauploadcom.MU_PARAM_PORT, Megauploadcom.ports, JDL.L("plugins.host.megaupload.ports", "Use this port:")).setDefaultValue(0));
    }

    public int usePort(final DownloadLink link) {
        final int port = this.getPluginConfig().getIntegerProperty(Megauploadcom.MU_PARAM_PORT, 0);
        switch (port) {
        case 1:
            return 800;
        case 2:
            return 1723;
        default:
            return 80;
        }
    }

    private void websiteFileCheck(final DownloadLink l, Browser br) throws PluginException {
        if (this.onlyapi) {
            l.setAvailableStatus(AvailableStatus.UNCHECKABLE);
            return;
        }
        URLConnectionAdapter con = null;
        try {
            if (br == null) {
                br = new Browser();
                br.setCookiesExclusive(true);
                br.setCookie("http://" + Megauploadcom.wwwWorkaround + "megaupload.com", "l", "en");
            }
            this.antiJDBlock(br);
            br.setFollowRedirects(false);
            br.getPage("http://" + Megauploadcom.wwwWorkaround + "megaupload.com/?d=" + this.getDownloadID(l));
            if (br.getRedirectLocation() != null) {
                String url = br.getRedirectLocation();
                con = br.openGetConnection(url);
                if (con.isContentDisposition()) {
                    directDL = true;
                    /* direct download */
                    l.setDownloadSize(con.getLongContentLength());
                    l.setAvailableStatus(AvailableStatus.TRUE);
                    return;
                } else {
                    br.followConnection();
                }
            }
            if (br.containsHTML("location='http://www\\.megaupload\\.com/\\?c=msg")) {
                br.getPage("http://www.megaupload.com/?c=msg");
            }
            if (br.containsHTML("No htmlCode read") || br.containsHTML("This service is temporarily not available from your service area")) {
                logger.info("It seems Megaupload is blocked! Only API may work! " + br.toString());
                this.onlyapi = true;
                l.setAvailableStatus(AvailableStatus.UNCHECKABLE);
                return;
            }
            if (br.containsHTML("The file you are trying to access is temporarily unavailable")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "The file you are trying to access is temporarily unavailable", 10 * 60 * 1000l);
            if (br.containsHTML("A temporary access restriction is place") || br.containsHTML("We have detected an elevated")) {
                /* ip blocked by megauploaded */
                this.wait = br.getRegex("Please check back in (\\d+) minutes").getMatch(0);
                if (this.wait != null) {
                    logger.info("Megaupload blocked this IP(1): " + this.wait + " mins");
                } else {
                    logger.severe("Waittime not found!: " + br.toString());
                }
                l.setAvailableStatus(AvailableStatus.UNCHECKABLE);
                return;
            }
            if (br.containsHTML("The file has been deleted because it was violating")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
            if (br.containsHTML("Invalid link")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
            // coálado:outdated Regexed?
            String filename = br.getRegex(">File name:</.*?txt2\">(.*?)</span").getMatch(0);
            String filesize = br.getRegex(">File size:</.*?>(.*?)<").getMatch(0);

            if (filename == null) {
                filename = br.getRegex("<div class=\"download_file_name\">(.*?)</div>").getMatch(0);
            }
            if (filesize == null) {
                filesize = br.getRegex("<div class=\"download_file_size\">(.*?)</div>").getMatch(0);
            }
            if (br.containsHTML("class=\"download_l_descr\">")) {
                l.setAvailableStatus(AvailableStatus.TRUE);
                this.checkLinks(new DownloadLink[] { l });
                return;
            }
            if (filename == null || filesize == null) {
                l.setAvailable(false);
            } else {
                filename = filename.trim();
                filesize = filesize.trim();
                /* maybe api check failed, then set name,size here */
                if (l.getBooleanProperty("webcheck", false) == false) {
                    l.setName(filename.trim());
                    l.setDownloadSize(SizeFormatter.getSize(filesize.trim()));
                    l.setProperty("webcheck", true);
                }
                l.setAvailable(true);
            }
            return;
        } catch (final PluginException e2) {
            throw e2;
        } catch (final Exception e) {
            if (br.containsHTML("The file has been deleted because it was violating")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
            if (br.containsHTML("Invalid link")) { throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND); }
            if (br.containsHTML("The file you are trying to access is temporarily unavailable")) throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "The file you are trying to access is temporarily unavailable", 10 * 60 * 1000l);
            JDLogger.exception(e);
            logger.info("Megaupload blocked this IP(2): 25 mins");
            l.setAvailableStatus(AvailableStatus.UNCHECKABLE);
        } finally {
            try {
                con.disconnect();
            } catch (final Throwable e) {
            }
        }
        return;
    }

}