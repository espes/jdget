package jd.plugins.hoster;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.http.Browser;
import jd.http.Browser.BrowserException;
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

import org.jdownloader.logging.LogController;

@HostPlugin(revision = "$Revision: 18302 $", interfaceVersion = 4, names = { "reload.cc" }, urls = { "REGEX_NOT_POSSIBLE_RANDOM-hrfgmodeydgbgdtklzh" }, flags = { 2 })
public class ReloadCc extends PluginForHost {
    private static HashMap<Account, HashMap<String, Long>> hostUnavailableMap = new HashMap<Account, HashMap<String, Long>>();

    private static final String                            PLUGIN_VERSION_KEY = "PLUGINVERSION";
    private static final String                            PLUGIN_VERSION     = "0.2.1";

    public ReloadCc(PluginWrapper wrapper) {
        super(wrapper);
        setConfigElements();
        this.enablePremium("https://reload.cc/premium");
    }

    public void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, getPluginConfig(), PLUGIN_VERSION_KEY, "Your Reload.cc plugin version: " + PLUGIN_VERSION));
    }

    private void showMessage(DownloadLink link, String message) {
        link.getLinkStatus().setStatusText(message);
    }

    public Browser newBrowser() {
        br = new Browser();
        br.setCookiesExclusive(true);
        br.getHeaders().put("Accept-Language", "en-gb, en;q=0.9, de;q=0.8");
        br.getHeaders().put("User-Agent", "JDownloader");
        br.setCustomCharset("utf-8");
        br.setConnectTimeout(60 * 1000);
        br.setReadTimeout(60 * 1000);
        return br;
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
    public String getAGBLink() {
        return null;
    }

    @Override
    public AvailableStatus requestFileInformation(DownloadLink link) throws PluginException {
        return AvailableStatus.UNCHECKABLE;
    }

    @Override
    public void handleFree(DownloadLink downloadLink) throws Exception, PluginException {
        throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
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

    private String getPassword(final DownloadLink downloadLink) throws PluginException {
        String passCode = null;
        logger.info("pw protected link");
        if (downloadLink.getStringProperty("pass", null) == null) {
            passCode = getUserInput(null, downloadLink);
        } else {
            /* gespeicherten PassCode holen */
            passCode = downloadLink.getStringProperty("pass", null);
        }
        return passCode;
    }

    @Override
    public void handleMultiHost(DownloadLink link, Account account) throws Exception {
        br = newBrowser();
        showMessage(link, "Task 1: Generating Link");
        /* request Download */
        try {
            br.getPage("https://reload.cc/api/dl?via=jd&v=1&user=" + Encoding.urlEncode(account.getUser()) + "&" + getPasswordParam(account) + "&uri=" + Encoding.urlEncode(link.getDownloadURL()));
        } catch (BrowserException e) {
            handleAPIErrors(br, account, link, null);
        }

        String dllink = br.getRegex("link\": \"(http[^\"]+)").getMatch(0);
        if (dllink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        dllink = dllink.replaceAll("\\\\/", "/");
        showMessage(link, "Task 2: Download begins!");
        handleDL(account, link, dllink);
    }

    private void handleDL(Account account, DownloadLink link, String dllink) throws Exception {
        /* we want to follow redirects in final stage */
        br.setFollowRedirects(true);
        int maxConnections = 0;
        // if ("uploaded.to".equalsIgnoreCase(link.getHost())) maxConnections = 1;
        try {
            dl = jd.plugins.BrowserAdapter.openDownload(br, link, dllink, true, maxConnections);
            if (!handleAPIErrors(br, account, link, dllink)) return;

            if (dl.getConnection().isContentDisposition()) {
                /* contentdisposition, lets download it */
                dl.startDownload();
                return;
            } else if (dl.getConnection().getContentType() != null && !dl.getConnection().getContentType().contains("html") && !dl.getConnection().getContentType().contains("text")) {
                /* no content disposition, but api says that some hoster might not have one */
                dl.startDownload();
                return;
            } else {
                /* download is not contentdisposition */
                br.followConnection();
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
        } catch (BrowserException e) {
            if (!handleAPIErrors(br, account, link, dllink)) return;
        }
    }

    @Override
    public AccountInfo fetchAccountInfo(Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        String info = login(account);

        ai.setStatus("Premium account");
        account.setValid(true);
        try {
            account.setMaxSimultanDownloads(-1);
            account.setConcurrentUsePossible(true);
        } catch (final Throwable e) {
        }
        account.setProperty("free", false);

        String expire = br.getRegex("\"expires\": (\\d+)").getMatch(0);
        if (expire != null) {
            ai.setValidUntil((Long.parseLong(expire)) * 1000);
        }

        String hashed = br.getRegex("\"hash\": \\\"([^\\\"]*)\\\"").getMatch(0);
        if (hashed != null) {
            account.setProperty("hashed", hashed);
            account.setProperty("hashedFor", account.getPass());
        }

        ai.setUnlimitedTraffic();

        try {
            String HostsJSON = new Regex(info, ".*\"supportedHosters\": \\[([^\\]]*)\\].*").getMatch(0);
            String[] hosts = new Regex(HostsJSON, "\"([a-zA-Z0-9\\.\\-]+)\"").getColumn(0);
            ArrayList<String> supportedHosts = new ArrayList<String>(Arrays.asList(hosts));
            ai.setProperty("multiHostSupport", supportedHosts);
        } catch (NullPointerException e) {

        }

        try {
            String limitsJSON = new Regex(info, ".*\"connection_limits\": \\{([^\\}]*)\\}.*").getMatch(0);
            Regex connLimits = new Regex(limitsJSON, "\"([a-zA-Z0-9\\.\\-]+)\":[ ]*([0-9\\-]*)");

            HashMap<String, Integer> limits = new HashMap<String, Integer>();

            for (int i = 0; i < connLimits.count(); i++) {
                logger.info("Reload connection limit: " + connLimits.getRow(i)[0] + ": " + connLimits.getRow(i)[1]);
                limits.put(connLimits.getRow(i)[0], Integer.parseInt(connLimits.getRow(i)[1]));
            }

            ai.setProperty("connectionLimits", limits);
        } catch (NullPointerException e) {

        }

        return ai;
    }

    private String login(Account account) throws Exception {
        br = newBrowser();
        String ret = null;
        try {
            ret = br.getPage("https://reload.cc/api/login?via=jd&v=1&get_supported=true&get_connection_limits=1&user=" + Encoding.urlEncode(account.getUser()) + "&" + getPasswordParam(account));
        } catch (BrowserException e) {
            handleAPIErrors(br, account, null, null);
        } catch (ConnectException e) {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "No connection possible", 3 * 60 * 1000l);
        }

        return ret;
    }

    private String getPasswordParam(Account account) {
        String pwd = "pwd=" + Encoding.urlEncode(account.getPass());
        String hash = (String) account.getProperty("hashed", null);
        String hashedFor = (String) account.getProperty("hashedFor", null);

        if (hash != null) {
            if (!account.getPass().equals(hashedFor)) {
                // User changed password
                account.setProperty("hashed", null);
                account.setProperty("hashedFor", null);
                return pwd;
            }
            pwd = "hash=" + Encoding.urlEncode(hash);
        }

        return pwd;
    }

    private boolean handleAPIErrors(Browser br, Account account, DownloadLink downloadLink, String ddl) throws Exception {
        int status = br.getHttpConnection().getResponseCode();
        String statusMessage = br.getRegex("\"msg\":\"([^\"]+)").getMatch(0);
        if (status >= 200 && status <= 299) return true;

        try {
            switch (status) {
            case 400:
                /* not a valid link, do not try again with this multihoster */
                if (statusMessage == null) statusMessage = "Invalid DownloadLink";
                throw new PluginException(LinkStatus.ERROR_FATAL);
            case 401:
                /* not logged in, disable account. */
                if (statusMessage == null) statusMessage = "Wrong username/password";
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
            case 409:
                /* fair use limit reached ,block host for 10 mins */
                if (statusMessage == null) statusMessage = "Fair use limit reached!";
                tempUnavailableHoster(account, downloadLink, 10 * 60 * 1000);
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, statusMessage, 10 * 60 * 1000l);
            case 412:
            case 417:
                String pwd = this.getPassword(downloadLink);
                ddl += "&filepwd=" + Encoding.urlEncode(pwd);
                handleDL(account, downloadLink, ddl);
                return false;
            case 428:
                /* hoster currently not possible,block host for 30 mins */
                if (statusMessage == null) statusMessage = "Hoster currently not possible";
                tempUnavailableHoster(account, downloadLink, 30 * 60 * 1000);
                return false;
            case 429:
                /* Too many connections */
                if (statusMessage == null) statusMessage = "Too many concurrent connections";
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, statusMessage, 2 * 60 * 1000l);
            case 503:
                /* temp multihoster issue, maintenance period, block host for 10 mins */
                if (statusMessage == null) statusMessage = "Hoster temporarily not possible";
                tempUnavailableHoster(account, downloadLink, 3 * 60 * 1000);
                /* only disable plugin for this link */
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, statusMessage, 10 * 60 * 1000l);
            default:
                /* unknown error, do not try again with this multihoster */
                if (statusMessage == null) statusMessage = "Unknown error: " + statusMessage;
                logger.info(statusMessage);
                logger.info("HTTP status code: " + status);
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
            }
        } catch (final PluginException e) {
            logger.info("ReloadCc Exception: statusCode: " + status + " statusMessage: " + statusMessage);
            throw e;
        }
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
    }

    @Override
    public int getMaxSimultanDownload(DownloadLink link, Account account) {
        setLogger(LogController.getInstance().getLogger(this));

        if (account == null || account.getAccountInfo() == null) {
            logger.info("Could not get max connections limit for " + link.getHost() + ": AccountInfo is empty.");
            return super.getMaxSimultanDownload(link, account);
        }

        @SuppressWarnings("unchecked")
        HashMap<String, Integer> limits = (HashMap<String, Integer>) account.getAccountInfo().getProperty("connectionLimits");
        if (limits != null) {
            logger.info("Trying to get max connection limit for " + link.getHost());
            Integer connections = limits.get(link.getHost());
            logger.info("Limit is: " + connections);
            if (connections != null && connections != -1) return connections;
        } else {
            logger.info("Could not get max connections limit for " + link.getHost() + ": ConnectionLimits info for account is empty.");
        }

        return super.getMaxSimultanDownload(link, account);
    }
}
