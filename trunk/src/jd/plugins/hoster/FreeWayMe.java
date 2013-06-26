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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.config.Property;
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

@HostPlugin(revision = "$Revision$", interfaceVersion = 3, names = { "free-way.me" }, urls = { "REGEX_NOT_POSSIBLE_RANDOM-asdfasdfsadfsdgfd32423" }, flags = { 2 })
public class FreeWayMe extends PluginForHost {

    private static HashMap<Account, HashMap<String, Long>> hostUnavailableMap = new HashMap<Account, HashMap<String, Long>>();
    private static AtomicInteger                           maxPrem            = new AtomicInteger(1);
    private final String                                   USEBETAENCODING    = "USEBETAENCODING";

    public FreeWayMe(PluginWrapper wrapper) {
        super(wrapper);
        setStartIntervall(1 * 1000l);
        setConfigElements();
        this.enablePremium("https://www.free-way.me/premium");
    }

    public void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), USEBETAENCODING, "Use beta encoding").setDefaultValue(false));
    }

    @Override
    public String getAGBLink() {
        return "https://www.free-way.me/agb";
    }

    @Override
    public int getMaxSimultanDownload(final DownloadLink link, final Account account) {
        return maxPrem.get();
    }

    @Override
    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        boolean betaEncoding = this.getPluginConfig().getBooleanProperty(USEBETAENCODING, false);

        AccountInfo ac = new AccountInfo();
        /* reset maxPrem workaround on every fetchaccount info */
        maxPrem.set(1);
        br.setConnectTimeout(60 * 1000);
        br.setReadTimeout(60 * 1000);
        String username = (betaEncoding) ? Encoding.urlTotalEncode(account.getUser()) : Encoding.urlEncode(account.getUser());
        String pass = (betaEncoding) ? Encoding.urlTotalEncode(account.getPass()) : Encoding.urlEncode(account.getPass());
        String hosts[] = null;
        ac.setProperty("multiHostSupport", Property.NULL);
        // check if account is valid
        br.getPage("https://www.free-way.me/ajax/jd.php?id=1&user=" + username + "&pass=" + pass + ((betaEncoding) ? "&encoded" : ""));
        // "Invalid login" / "Banned" / "Valid login"
        if (br.toString().equalsIgnoreCase("Valid login")) {
            account.setValid(true);
        } else if (br.toString().equalsIgnoreCase("Invalid login")) {
            account.setValid(false);
            throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nInvalid username/password!\r\nFalscher Benutzername/Passwort!", PluginException.VALUE_ID_PREMIUM_DISABLE);
        } else if (br.toString().equalsIgnoreCase("Banned")) {
            account.setValid(false);
            account.setEnabled(false);
            throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nAccount banned!\r\nAccount gesperrt!", PluginException.VALUE_ID_PREMIUM_DISABLE);
        } else {
            // unknown error
            account.setValid(false);
            throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nUnknown account status (deactivated)!\r\nUnbekannter Accountstatus (deaktiviert)!", PluginException.VALUE_ID_PREMIUM_DISABLE);
        }
        // account should be valid now, let's get account information:
        br.getPage("https://www.free-way.me/ajax/jd.php?id=4&user=" + username + "&pass=" + pass + ((betaEncoding) ? "&encoded" : ""));

        int maxPremi = 1;
        final String maxPremApi = getJson("parallel", br.toString());
        if (maxPremApi != null) {
            maxPremi = Integer.parseInt(maxPremApi);
            account.setProperty("parallel", maxPremApi);
        }
        maxPrem.set(maxPremi);
        try {
            Long guthaben = Long.parseLong(getRegexTag(br.toString(), "guthaben").getMatch(0));
            ac.setTrafficLeft(guthaben * 1024 * 1024);
        } catch (Exception e) {
            ac.setUnlimitedTraffic(); // workaround
        }
        try {
            account.setMaxSimultanDownloads(maxPrem.get());
            account.setConcurrentUsePossible(true);
        } catch (final Throwable e) {
            // not available in old Stable 0.9.581
        }
        String accountType = getRegexTag(br.toString(), "premium").getMatch(0);
        ac.setValidUntil(-1);
        if (accountType != null) {
            if (accountType.equalsIgnoreCase("Flatrate")) {
                ac.setUnlimitedTraffic();
                long validUntil = Long.parseLong(getRegexTag(br.toString(), "Flatrate").getMatch(0));
                ac.setValidUntil(validUntil * 1000);
            } else if (accountType.equalsIgnoreCase("Spender")) {
                ac.setUnlimitedTraffic();
            }
        }
        ac.setProperty("acctype", accountType);
        // now let's get a list of all supported hosts:
        br.getPage("https://www.free-way.me/ajax/jd.php?id=3");
        hosts = br.getRegex("\"([^\"]*)\"").getColumn(0);
        ArrayList<String> supportedHosts = new ArrayList<String>();
        for (String host : hosts) {
            if (!host.isEmpty()) {
                supportedHosts.add(host.trim());
            }
        }

        if (supportedHosts.size() == 0) {
            ac.setStatus("Account valid: 0 Hosts via free-way.me available");
        } else {
            ac.setStatus("Account valid: " + supportedHosts.size() + " Hosts via free-way.me available");
            ac.setProperty("multiHostSupport", supportedHosts);
        }
        return ac;
    }

    private String getJson(final String parameter, final String source) {
        String result = new Regex(source, "\"" + parameter + "\":(\\d+)").getMatch(0);
        if (result == null) result = new Regex(source, "\"" + parameter + "\":\"([^<>\"]*?)\"").getMatch(0);
        return result;
    }

    private Regex getRegexTag(String content, String tag) {
        return new Regex(content, "\"" + tag + "\":\"([^\"]*)\"");
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return 0;
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_ONLY);
    }

    /** no override to keep plugin compatible to old stable */
    public void handleMultiHost(final DownloadLink link, final Account acc) throws Exception {
        boolean betaEncoding = this.getPluginConfig().getBooleanProperty(USEBETAENCODING, false);
        String user = (betaEncoding) ? Encoding.urlTotalEncode(acc.getUser()) : Encoding.urlEncode(acc.getUser());
        String pw = (betaEncoding) ? Encoding.urlTotalEncode(acc.getPass()) : Encoding.urlEncode(acc.getPass());
        final String url = (betaEncoding) ? Encoding.urlTotalEncode(link.getDownloadURL()) : Encoding.urlEncode(link.getDownloadURL());

        String dllink = "https://www.free-way.me/load.php?multiget=2&user=" + user + "&pw=" + pw + "&url=" + url + ((betaEncoding) ? "&encoded" : "");

        if (betaEncoding) {
            /* Begin workaround for wrong encoding while redirect */
            br.setFollowRedirects(false);
            br.getPage(dllink);
            String location = br.getRedirectLocation();
            dllink = location.substring(0, location.indexOf("?")) + dllink.substring(dllink.indexOf("?"), dllink.length()) + "&s=" + location.substring(location.length() - 1, location.length());
            /* end workaround for wrong encoding while redirect */
        }

        dl = jd.plugins.BrowserAdapter.openDownload(br, link, dllink, false, 1);

        if (dl.getConnection().getContentType().contains("html")) {
            br.followConnection();
            String error = "";
            try {
                error = (new Regex(br.toString(), "<p id=\\'error\\'>([^<]*)</p>")).getMatch(0);
            } catch (Exception e) {
                // we handle this few lines later
            }
            if (error.equalsIgnoreCase("Ungültiger Login")) {
                acc.setTempDisabled(true);
                throw new PluginException(LinkStatus.ERROR_RETRY);
            } else if (error.equalsIgnoreCase("Ungültige URL")) {
                tempUnavailableHoster(acc, link, 2 * 60 * 1000l);
            } else if (error.equalsIgnoreCase("Sie haben nicht genug Traffic, um diesen Download durchzuführen.")) {
                tempUnavailableHoster(acc, link, 10 * 60 * 1000l);
            } else if (error.startsWith("Sie können nicht mehr parallele Downloads durchführen")) {
                throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, "Too many simultan downloads", 1 * 60 * 1000l);
            } else if (error.startsWith("Ung&uuml;ltiger Hoster")) {
                tempUnavailableHoster(acc, link, 5 * 60 * 60 * 1000l);
            } else if (error.equalsIgnoreCase("Dieser Hoster ist aktuell leider nicht aktiv.")) {
                tempUnavailableHoster(acc, link, 5 * 60 * 60 * 1000l);
            } else if (error.equalsIgnoreCase("Diese Datei wurde nicht gefunden.")) {
                tempUnavailableHoster(acc, link, 1 * 60 * 1000l);
            } else if (error.equalsIgnoreCase("Unbekannter Fehler #2") || error.equals("Es ist ein unbekannter Fehler aufgetreten (#1)")) {
                /*
                 * after x retries we disable this host and retry with normal plugin
                 */
                if (link.getLinkStatus().getRetryCount() >= 2) {
                    /* reset retrycounter */
                    link.getLinkStatus().setRetryCount(0);
                    tempUnavailableHoster(acc, link, 3 * 60 * 60 * 1000l);
                }
                String msg = "(" + link.getLinkStatus().getRetryCount() + 1 + "/" + 3 + ")";
                throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Error: Retry in few secs" + msg, 20 * 1000l);
            } else if (error.startsWith("Die Datei darf maximal")) {
                tempUnavailableHoster(acc, link, 2 * 60 * 1000l);
            } else if (error.equalsIgnoreCase("Mehrere Computer haben in letzter Zeit diesen Account genutzt")) {
                acc.setTempDisabled(true);
                throw new PluginException(LinkStatus.ERROR_RETRY);
            }
            logger.info("Unhandled download error on free-way.me: " + br.toString());
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);

        }
        dl.startDownload();
    }

    public void showAccountDetailsDialog(final Account account) {
        final AccountInfo ai = account.getAccountInfo();
        if (ai != null) {
            final Object supported = ai.getProperty("multiHostSupport", Property.NULL);
            if (supported != null) {
                final HashMap<String, Long> unavailableMap = hostUnavailableMap.get(account);
                ArrayList<String> unavailableHosts = new ArrayList<String>();
                if (unavailableMap != null) {
                    unavailableHosts = new ArrayList<String>(unavailableMap.keySet());
                }
                String windowTitleLangText = null;
                ArrayList<String> supportedHosts = (ArrayList) supported;
                final String lang = System.getProperty("user.language");
                final String accType = ai.getStringProperty("acctype", null);
                final String maxSimultanDls = account.getStringProperty("parallel", null);
                String message = "";
                if ("de".equalsIgnoreCase(lang)) {
                    windowTitleLangText = "Account Zusatzinformationen für " + account.getUser();
                    message += "Account Typ: " + accType + "\r\n";
                    if (maxSimultanDls != null) message += "Max. Anzahl gleichzeitiger Downloads: " + maxSimultanDls + "\r\n";
                    message += "Unterstützte Hoster [" + supportedHosts.size() + "]:\r\n\r\n";
                    for (final String host : supportedHosts) {
                        message += host + "\r\n";
                    }
                    if (unavailableHosts != null && unavailableHosts.size() != 0) {
                        message += "\r\nTemporär deaktivierte Hoster [" + unavailableHosts.size() + "]:\r\n\r\n";
                        for (final String tempDeactivatedHost : unavailableHosts) {
                            message += tempDeactivatedHost + "\r\n";
                        }
                    }
                } else {
                    windowTitleLangText = "extended account information for " + account.getUser();
                    message += "Account type: " + accType + "\r\n";
                    if (maxSimultanDls != null) message += "Max. number of simultan downloads: " + maxSimultanDls + "\r\n";
                    message += "Supported hosts[" + supportedHosts.size() + "]:\r\n\r\n";
                    for (final String host : supportedHosts) {
                        message += host + "\r\n";
                    }
                    if (unavailableHosts != null && unavailableHosts.size() != 0) {
                        message += "\r\nTemporarily deactivated hosts [" + unavailableHosts.size() + "]:\r\n\r\n";
                        for (final String tempDeactivatedHost : unavailableHosts) {
                            message += tempDeactivatedHost + "\r\n";
                        }
                    }
                }
                jd.gui.UserIO.getInstance().requestMessageDialog(this.getHost() + " " + windowTitleLangText, message);
            }
        }

    }

    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws Exception {
        return AvailableStatus.UNCHECKABLE;
    }

    private void tempUnavailableHoster(final Account account, final DownloadLink downloadLink, long timeout) throws PluginException {
        if (downloadLink == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT, "Unable to handle this errorcode!");
        synchronized (hostUnavailableMap) {
            HashMap<String, Long> unavailableMap = hostUnavailableMap.get(account);
            if (unavailableMap == null) {
                unavailableMap = new HashMap<String, Long>();
                hostUnavailableMap.put(account, unavailableMap);
            }
            /* wait to retry this host */
            unavailableMap.put(downloadLink.getHost(), (System.currentTimeMillis() + timeout));
            account.setProperty("unavailablemap", unavailableMap);
        }
        throw new PluginException(LinkStatus.ERROR_RETRY);
    }

    @Override
    public boolean canHandle(final DownloadLink downloadLink, final Account account) {
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
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}