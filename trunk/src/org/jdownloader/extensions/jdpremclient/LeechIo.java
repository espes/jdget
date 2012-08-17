package org.jdownloader.extensions.jdpremclient;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.http.Browser;
import jd.nutils.Formatter;
import jd.nutils.encoding.Encoding;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.plugins.download.DownloadInterface;

import org.appwork.utils.Regex;

public class LeechIo extends PluginForHost implements JDPremInterface {

    private boolean                  proxyused    = false;
    private String                   infostring   = null;
    private PluginForHost            plugin       = null;
    private static boolean           enabled      = false;
    private static java.util.List<String> premiumHosts = new ArrayList<String>();
    private static final Object      LOCK         = new Object();

    public LeechIo(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("http://leech.io/");
        infostring = "leech.io @ " + wrapper.getLazy().getDisplayName();
    }

    @Override
    public String getAGBLink() {
        if (plugin == null) return "http://leech.io/";
        return plugin.getAGBLink();
    }

    @Override
    public long getVersion() {
        if (plugin == null) return Formatter.getRevision("$Revision: 14598 $");
        return plugin.getVersion();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (plugin != null) {
            plugin.actionPerformed(e);
        } else {
            super.actionPerformed(e);
        }
    }

    @Override
    public String getHost() {
        if (plugin == null) return "leech.io";
        return plugin.getHost();
    }

    @Override
    public ConfigContainer getConfig() {
        if (plugin == null) return super.getConfig();
        return plugin.getConfig();
    }

    @Override
    public String getBuyPremiumUrl() {
        if (plugin == null) return "http://leech.io/";
        return plugin.getBuyPremiumUrl();
    }

    @Override
    public void handle(final DownloadLink downloadLink, final Account account) throws Exception {
        if (plugin == null) {
            super.handle(downloadLink, account);
            return;
        }
        proxyused = false;
        /* copied from PluginForHost */
        try {
            while (waitForNextStartAllowed(downloadLink)) {
            }
        } catch (InterruptedException e) {
            return;
        }
        putLastTimeStarted(System.currentTimeMillis());

        if (proxyused = true) {
            /* failed, now try normal */
            proxyused = false;

        }
        plugin.handle(downloadLink, account);
    }

    @Override
    public void handleFree(DownloadLink link) throws Exception {
        if (plugin == null) return;
        proxyused = false;

        plugin.handleFree(link);
    }

    @Override
    public void handlePremium(DownloadLink downloadLink, Account account) throws Exception {
        if (plugin == null) return;
        proxyused = false;

        plugin.handlePremium(downloadLink, account);
    }

    @Override
    public void setBrowser(Browser br) {
        this.br = br;
        if (plugin != null) plugin.setBrowser(br);
    }

    @Override
    public Browser getBrowser() {
        if (plugin != null) return plugin.getBrowser();
        return this.br;
    }

    @Override
    public void clean() {
        super.clean();
        if (plugin != null) plugin.clean();
    }

    private boolean handleLeechio(DownloadLink link) throws Exception {
        Account acc = null;
        synchronized (LOCK) {
            /* jdpremium enabled */

            /* premium available for this host */
            if (!premiumHosts.contains(link.getHost())) return false;
            // acc =
            // AccountController.getInstance().getValidAccount("leech.io");
            /* enabled account found? */
            if (acc == null || !acc.isEnabled()) return false;
        }
        proxyused = true;
        requestFileInformation(link);
        if (link.isAvailabilityStatusChecked() && !link.isAvailable()) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);

        boolean dofollow = br.isFollowingRedirects();
        try {
            br.setFollowRedirects(true);
            br.setConnectTimeout(90 * 1000);
            br.setReadTimeout(90 * 1000);
            br.setDebug(true);
            dl = null;
            String user = Encoding.urlEncode(acc.getUser());
            String pw = Encoding.urlEncode(acc.getPass());
            // String url = link.getDownloadURL().replaceFirst("https?://", "");
            String url = link.getDownloadURL();
            url = Encoding.urlEncode(url);
            br.setHeader("X-Requested-With", "XMLHttpRequest");
            br.postPageRaw("http://leech.io/users/login", "email=" + user + "&password=" + pw);
            if (br.getCookie("http://leech.io", "LEECHIO") == null) {
                resetAvailablePremium();
                acc.setValid(false);
                return false;
            }
            br.setFollowRedirects(true);
            dl = jd.plugins.BrowserAdapter.openDownload(br, link, "http://leech.io/leech/get/?d=" + url, true, 1);
            if (dl.getConnection().getResponseCode() == 404) {
                /* file offline */
                dl.getConnection().disconnect();
                throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
            }
            if (!dl.getConnection().isContentDisposition()) {
                br.followConnection();
                logger.severe("leech.io(Error): " + br.toString());
                /*
                 * after x retries we disable this host and retry with normal plugin
                 */
                if (link.getLinkStatus().getRetryCount() >= 3) {
                    synchronized (LOCK) {
                        premiumHosts.remove(link.getHost());
                    }
                    /* reset retrycounter */
                    link.getLinkStatus().setRetryCount(0);
                    return false;
                }
                String msg = "(" + link.getLinkStatus().getRetryCount() + 1 + "/" + 3 + ")";
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Retry in few secs" + msg, 10 * 1000l);
            }
            dl.startDownload();
        } finally {
            br.setFollowRedirects(dofollow);
        }
        return true;
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink parameter) throws Exception {
        if (plugin == null) return AvailableStatus.UNCHECKABLE;
        return plugin.requestFileInformation(parameter);
    }

    @Override
    public void reset() {
        if (plugin != null) {
            plugin.reset();
        }
    }

    @Override
    public void init() {
        if (plugin != null) {
            plugin.init();
        } else {
            super.init();
        }
    }

    private void resetAvailablePremium() {
        synchronized (LOCK) {
            premiumHosts.clear();
        }
    }

    @Override
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        if (plugin == null) {
            String restartReq = enabled == false ? "(Restart required) " : "";
            AccountInfo ac = new AccountInfo();
            br.setConnectTimeout(60 * 1000);
            br.setReadTimeout(60 * 1000);
            br.setDebug(true);
            String username = Encoding.urlEncode(account.getUser());
            String pass = Encoding.urlEncode(account.getPass());
            // String page = null;
            String hosts = null;
            String traffic = null;
            br.setFollowRedirects(true);
            br.setHeader("X-Requested-With", "XMLHttpRequest");
            br.setAcceptLanguage("en, en-gb;q=0.8");
            try {
                br.postPageRaw("http://leech.io/users/login", "email=" + username + "&password=" + pass);
                if (br.getCookie("http://leech.io", "LEECHIO") != null) {
                    traffic = br.getPage("http://leech.io/users/approxtraffic").trim() + " Points";
                    hosts = br.getPage("http://leech.io/starts/hosters");
                }
            } catch (Exception e) {
                account.setTempDisabled(true);
                account.setValid(true);
                resetAvailablePremium();
                ac.setStatus("Leech.io Server Error, temp disabled" + restartReq);
                return ac;
            }
            if (br.getCookie("http://leech.io", "LEECHIO") == null) {
                resetAvailablePremium();
                account.setValid(false);
                return ac;
            }
            ac.setTrafficLeft(traffic);
            // String date = new Regex(page, "\"d\":\"(.*?)\",").getMatch(0);
            account.setValid(true);
            /* expire date does currently not work */
            // ac.setValidUntil(TimeFormatter.getMilliSeconds(date,
            // "dd MMM yyyy", null));
            synchronized (LOCK) {
                premiumHosts.clear();
                String hoster[] = new Regex(hosts.trim(), "(.+?)(;|$)").getColumn(0);
                if (hoster != null) {
                    for (String host : hoster) {
                        if (hosts == null || host.length() == 0) continue;
                        premiumHosts.add(host.trim());
                    }
                }
            }
            if (account.isValid()) {
                if (premiumHosts.size() == 0) {
                    ac.setStatus(restartReq + "Account valid: 0 Hosts via leech.io available");
                } else {
                    ac.setStatus(restartReq + "Account valid: " + premiumHosts.size() + " Hosts via leech.io available");
                }
            } else {
                account.setTempDisabled(false);
                account.setValid(false);
                resetAvailablePremium();
                ac.setStatus("Account invalid");
            }
            return ac;
        } else
            return plugin.fetchAccountInfo(account);
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
        if (plugin != null) plugin.resetDownloadlink(link);
    }

    @Override
    public void correctDownloadLink(DownloadLink link) throws Exception {
        if (plugin != null) plugin.correctDownloadLink(link);
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        if (plugin != null) return plugin.getMaxSimultanFreeDownloadNum();
        return super.getMaxSimultanFreeDownloadNum();
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        if (plugin != null) return plugin.getMaxSimultanPremiumDownloadNum();
        return super.getMaxSimultanPremiumDownloadNum();
    }

    @Override
    public boolean checkLinks(DownloadLink[] urls) {
        if (plugin == null) return false;
        return plugin.checkLinks(urls);
    }

    public void setReplacedPlugin(PluginForHost plugin) {
        this.plugin = plugin;
    }

    public void enablePlugin() {
        enabled = true;
    }

    @Override
    public int getTimegapBetweenConnections() {
        if (plugin != null) return plugin.getTimegapBetweenConnections();
        return super.getTimegapBetweenConnections();
    }

    @Override
    public boolean rewriteHost(DownloadLink link) {
        if (plugin != null) return plugin.rewriteHost(link);
        return false;
    }

    @Override
    public void setDownloadInterface(DownloadInterface dl) {
        this.dl = dl;
        if (plugin != null) plugin.setDownloadInterface(dl);
    }

    @Override
    public String getCustomFavIconURL() {
        if (proxyused) return "leech.io";
        if (plugin != null) return plugin.getCustomFavIconURL();
        return null;
    }

}
