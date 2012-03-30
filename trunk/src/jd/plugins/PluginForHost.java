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

package jd.plugins;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import jd.PluginWrapper;
import jd.captcha.JACMethod;
import jd.config.SubConfiguration;
import jd.controlling.AccountController;
import jd.controlling.IOPermission;
import jd.controlling.JDLogger;
import jd.controlling.JDPluginLogger;
import jd.controlling.captcha.CaptchaController;
import jd.controlling.downloadcontroller.SingleDownloadController;
import jd.gui.UserIF;
import jd.gui.swing.jdgui.views.settings.panels.accountmanager.NewAction;
import jd.http.Browser;
import jd.nutils.Formatter;
import jd.nutils.encoding.Encoding;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.download.DownloadInterface;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Regex;
import org.appwork.utils.images.IconIO;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;
import org.jdownloader.DomainInfo;
import org.jdownloader.plugins.controller.host.LazyHostPlugin;
import org.jdownloader.settings.GeneralSettings;
import org.jdownloader.translate._JDT;

/**
 * Dies ist die Oberklasse fuer alle Plugins, die von einem Anbieter Dateien
 * herunterladen koennen
 * 
 * @author astaldo
 */
public abstract class PluginForHost extends Plugin {
    private static Pattern[] PATTERNS     = new Pattern[] {
                                          // multipart rar archives
            Pattern.compile("(.*)(\\.pa?r?t?\\.?[0-9]+.*?\\.rar$)"),
            // normal files with extension
            Pattern.compile("(.*)\\.(.*?$)") };
    private IOPermission     ioPermission = null;

    private LazyHostPlugin   lazyP        = null;

    public LazyHostPlugin getLazyP() {
        return lazyP;
    }

    public void setLazyP(LazyHostPlugin lazyP) {
        this.lazyP = lazyP;
    }

    @Deprecated
    public PluginForHost(final PluginWrapper wrapper) {
        super(wrapper);
        /* defaultPlugin does not need any Browser instance */
        br = null;
        dl = null;
        /* defaultPlugins do not have any working logger */
        /* workaround for all the lazy init issues */
        this.lazyP = (LazyHostPlugin) wrapper.getLazy();
    }

    public void setLogger(JDPluginLogger logger) {
        this.logger = logger;
    }

    public JDPluginLogger getLogger() {

        return (JDPluginLogger) logger;
    }

    public void setBrowser(Browser brr) {
        br = brr;
    }

    protected String getCaptchaCode(final String captchaAddress, final DownloadLink downloadLink) throws IOException, PluginException {
        return getCaptchaCode(getHost(), captchaAddress, downloadLink);
    }

    @Override
    public long getVersion() {
        return lazyP.getVersion();
    }

    @Override
    public Pattern getSupportedLinks() {
        return lazyP.getPattern();
    }

    protected String getCaptchaCode(final String method, final String captchaAddress, final DownloadLink downloadLink) throws IOException, PluginException {
        if (captchaAddress == null) {
            logger.severe("Captcha Adresse nicht definiert");
            throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        }
        final File captchaFile = getLocalCaptchaFile();
        try {
            Browser.download(captchaFile, br.cloneBrowser().openGetConnection(captchaAddress));
        } catch (Exception e) {
            logger.severe("Captcha Download fehlgeschlagen: " + captchaAddress);
            throw new PluginException(LinkStatus.ERROR_CAPTCHA);
        }
        final String captchaCode = getCaptchaCode(method, captchaFile, downloadLink);
        captchaFile.delete();
        return captchaCode;
    }

    protected String getCaptchaCode(final File captchaFile, final DownloadLink downloadLink) throws PluginException {
        return getCaptchaCode(getHost(), captchaFile, downloadLink);
    }

    protected String getCaptchaCode(final String methodname, final File captchaFile, final DownloadLink downloadLink) throws PluginException {

        return getCaptchaCode(methodname, captchaFile, 0, downloadLink, null, null);
    }

    protected String getCaptchaCode(final String method, final File file, final int flag, final DownloadLink link, final String defaultValue, final String explain) throws PluginException {
        final LinkStatus linkStatus = link.getLinkStatus();
        final String status = linkStatus.getStatusText();
        try {
            linkStatus.addStatus(LinkStatus.WAITING_USERIO);
            linkStatus.setStatusText(_JDT._.gui_downloadview_statustext_jac());
            try {
                final BufferedImage img = ImageIO.read(file);

                linkStatus.setStatusIcon(new ImageIcon(IconIO.getScaledInstance(img, 16, 16)));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            final String cc = new CaptchaController(ioPermission, method, file, defaultValue, explain, this).getCode(flag);
            if (cc == null) throw new PluginException(LinkStatus.ERROR_CAPTCHA);
            return cc;
        } finally {
            linkStatus.removeStatus(LinkStatus.WAITING_USERIO);
            linkStatus.setStatusText(status);
            linkStatus.setStatusIcon(null);
        }
    }

    protected DownloadInterface                dl                     = null;

    private static final HashMap<String, Long> LAST_CONNECTION_TIME   = new HashMap<String, Long>();
    private static final HashMap<String, Long> LAST_STARTED_TIME      = new HashMap<String, Long>();

    private Long                               WAIT_BETWEEN_STARTS    = 0L;

    private boolean                            enablePremium          = false;

    private boolean                            accountWithoutUsername = false;

    private String                             premiumurl             = null;

    private DownloadLink                       link                   = null;

    public boolean checkLinks(final DownloadLink[] urls) {
        return false;
    }

    @Override
    public String getHost() {
        return lazyP.getDisplayName();
    }

    @Override
    public SubConfiguration getPluginConfig() {
        return SubConfiguration.getConfig(lazyP.getHost());
    }

    @Override
    public void clean() {
        dl = null;
        br = null;
        super.clean();
    }

    public void setDownloadInterface(DownloadInterface dl) {
        this.dl = dl;
    }

    protected void setBrowserExclusive() {
        if (br == null) return;
        br.setCookiesExclusive(true);
        br.clearCookies(getHost());
    }

    public PluginForHost getNewInstance() {
        if (lazyP == null) return null;
        return lazyP.newInstance();
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final int eID = e.getID();
        if (eID == 1) {
            if (hasConfig()) {
                UserIF.getInstance().requestPanel(UserIF.Panels.CONFIGPANEL, getConfig());
            }
            return;
        }
        if (eID == 2) {
            UserIF.getInstance().requestPanel(UserIF.Panels.PREMIUMCONFIG, null);
            new NewAction().actionPerformed(new ActionEvent(this, 0, "addaccount"));
            return;
        }
        if (eID == 3) {
            UserIF.getInstance().requestPanel(UserIF.Panels.PREMIUMCONFIG, null);
            CrossSystem.openURLOrShowMessage(getBuyPremiumUrl());
            return;
        }
        final ArrayList<Account> accounts = getPremiumAccounts();
        if (eID >= 200) {
            final int accountID = eID - 200;
            final Account account = accounts.get(accountID);
            UserIF.getInstance().requestPanel(UserIF.Panels.PREMIUMCONFIG, account);

        } else if (eID >= 100) {
            final int accountID = eID - 100;
            Account account = accounts.get(accountID);
            account.setEnabled(!account.isEnabled());
        }
    }

    /** default fetchAccountInfo, set account valid to true */
    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        AccountInfo ai = new AccountInfo();
        account.setValid(true);
        return ai;
    }

    public boolean getAccountwithoutUsername() {
        return accountWithoutUsername;
    }

    public void setAccountwithoutUsername(boolean b) {
        accountWithoutUsername = b;
    }

    // @Override
    // public ArrayList<MenuAction> createMenuitems() {
    // final ArrayList<MenuAction> menuList = new ArrayList<MenuAction>();
    // if (!enablePremium) return menuList;
    // MenuAction account;
    // MenuAction m;
    //
    // if (hasConfig()) {
    // m = new MenuAction(_GUI._.action_plugin_config(), "plugins.configs", 1) {
    //
    // private static final long serialVersionUID = -5376242428242330373L;
    //
    // @Override
    // protected String createMnemonic() {
    // return _GUI._.action_plugin_config_mnemonic();
    // }
    //
    // @Override
    // protected String createAccelerator() {
    // return _GUI._.action_plugin_config_accelerator();
    // }
    //
    // @Override
    // protected String createTooltip() {
    // return _GUI._.action_plugin_config_tooltip();
    // }
    //
    // };
    // m.setActionListener(this);
    // menuList.add(m);
    // menuList.add(new MenuAction(Types.SEPARATOR) {
    //
    // private static final long serialVersionUID = -5071061048221401102L;
    //
    // @Override
    // protected String createMnemonic() {
    // return null;
    // }
    //
    // @Override
    // protected String createAccelerator() {
    // return null;
    // }
    //
    // @Override
    // protected String createTooltip() {
    // return null;
    // }
    // });
    // }
    //
    // MenuAction premiumAction = new
    // MenuAction(_GUI._.action_plugin_accounts(), "accounts", 0) {
    //
    // private static final long serialVersionUID = -1987064249424203910L;
    //
    // @Override
    // protected String createMnemonic() {
    // return _GUI._.action_plugin_accounts_mnemonic();
    // }
    //
    // @Override
    // protected String createAccelerator() {
    // return _GUI._.action_plugin_accounts_accelerator();
    // }
    //
    // @Override
    // protected String createTooltip() {
    // return _GUI._.action_plugin_accounts_tooltip();
    // }
    //
    // };
    // premiumAction.setType(Types.CONTAINER);
    // ArrayList<Account> accounts = getPremiumAccounts();
    //
    // int i = 1;
    // int c = 0;
    // for (final Account a : accounts) {
    // if (a != null) {
    // try {
    // c++;
    // if (getAccountwithoutUsername()) {
    // if (a.getPass() == null || a.getPass().trim().length() == 0) continue;
    // account = new MenuAction(i++ + ". " +
    // _JDT._.jd_plugins_PluginsForHost_account()) {
    //
    // private static final long serialVersionUID = 8808632091567875643L;
    //
    // @Override
    // protected String createMnemonic() {
    // return null;
    // }
    //
    // @Override
    // protected String createAccelerator() {
    // return null;
    // }
    //
    // @Override
    // protected String createTooltip() {
    // return null;
    // }
    // };
    // account.setType(Types.CONTAINER);
    // } else {
    // if (a.getUser() == null || a.getUser().trim().length() == 0) continue;
    // account = new MenuAction(i++ + ". " + a.getUser()) {
    //
    // private static final long serialVersionUID = -8277393315361677608L;
    //
    // @Override
    // protected String createMnemonic() {
    // return null;
    // }
    //
    // @Override
    // protected String createAccelerator() {
    // return null;
    // }
    //
    // @Override
    // protected String createTooltip() {
    // return null;
    // }
    // };
    // account.setType(Types.CONTAINER);
    // }
    // m = AccountMenuItemSyncer.getInstance().get(a);
    //
    // if (m == null) {
    // m = new MenuAction(_GUI._.action_plugin_enable_premium(),
    // "plugins.PluginForHost.enable_premium", 100 + c - 1) {
    //
    // private static final long serialVersionUID = 1487783746694078208L;
    //
    // @Override
    // protected String createMnemonic() {
    // return _GUI._.action_plugin_enable_premium_mnemonic();
    // }
    //
    // @Override
    // protected String createAccelerator() {
    // return _GUI._.action_plugin_enable_premium_accelerator();
    // }
    //
    // @Override
    // protected String createTooltip() {
    // return _GUI._.action_plugin_enable_premium_tooltip();
    // }
    // };
    // }
    // m.setActionID(100 + c - 1);
    // m.setSelected(a.isEnabled());
    // m.setActionListener(this);
    // account.addMenuItem(m);
    //
    // AccountMenuItemSyncer.getInstance().map(a, m);
    //
    // m = new MenuAction(_GUI._.action_plugin_premium_info(),
    // "plugins.PluginForHost.premiumInfo", 200 + c - 1) {
    //
    // private static final long serialVersionUID = -9129239333353281936L;
    //
    // @Override
    // protected String createMnemonic() {
    // return _GUI._.action_plugin_premium_info_mnemonic();
    // }
    //
    // @Override
    // protected String createAccelerator() {
    // return _GUI._.action_plugin_premium_info_accelerator();
    // }
    //
    // @Override
    // protected String createTooltip() {
    // return _GUI._.action_plugin_premium_info_tooltip();
    // }
    // };
    // m.setActionListener(this);
    // account.addMenuItem(m);
    // premiumAction.addMenuItem(account);
    //
    // } catch (Exception e) {
    // JDLogger.exception(e);
    // }
    // }
    // }
    //
    // if (premiumAction.getSize() != 0) {
    // menuList.add(premiumAction);
    // } else {
    // menuList.add(m = new
    // MenuAction(_GUI._.action_plugin_premium_noAccounts(),
    // "plugins.menu.noaccounts", 2) {
    //
    // private static final long serialVersionUID = -2329091953907925997L;
    //
    // @Override
    // protected String createMnemonic() {
    // return _GUI._.action_plugin_premium_noAccounts_mnemonic();
    // }
    //
    // @Override
    // protected String createAccelerator() {
    // return _GUI._.action_plugin_premium_noAccounts_accelerator();
    // }
    //
    // @Override
    // protected String createTooltip() {
    // return _GUI._.action_plugin_premium_noAccounts_tooltip();
    // }
    // });
    // m.setActionListener(this);
    // }
    // menuList.add(m = new
    // MenuAction(_GUI._.action_plugin_premium_buyAccount(),
    // "plugins.menu.buyaccount", 3) {
    //
    // private static final long serialVersionUID = 4684046655398621492L;
    //
    // @Override
    // protected String createMnemonic() {
    // return _GUI._.action_plugin_premium_buyAccount_mnemonic();
    // }
    //
    // @Override
    // protected String createAccelerator() {
    // return _GUI._.action_plugin_premium_buyAccount_accelerator();
    // }
    //
    // @Override
    // protected String createTooltip() {
    // return _GUI._.action_plugin_premium_buyAccount_tooltip();
    // }
    // });
    // m.setActionListener(this);
    //
    // return menuList;
    // }

    public abstract String getAGBLink();

    protected void enablePremium() {
        enablePremium(null);
    }

    protected void enablePremium(final String url) {
        premiumurl = url;
        enablePremium = true;
    }

    /**
     * Hier werden Treffer fuer Downloadlinks dieses Anbieters in diesem Text
     * gesucht. Gefundene Links werden dann in einem ArrayList zurueckgeliefert
     * 
     * @param data
     *            Ein Text mit beliebig vielen Downloadlinks dieses Anbieters
     * @return Ein ArrayList mit den gefundenen Downloadlinks
     */
    public ArrayList<DownloadLink> getDownloadLinks(final String data, final FilePackage fp) {
        ArrayList<DownloadLink> links = null;

        final String[] hits = new Regex(data, getSupportedLinks()).getColumn(-1);
        if (hits != null && hits.length > 0) {
            links = new ArrayList<DownloadLink>(hits.length);
            for (String file : hits) {
                /* remove newlines... */
                file = file.trim();
                /*
                 * this removes the " from HTMLParser.ArrayToString
                 */
                /* only 1 " at start */
                while (file.charAt(0) == '"') {
                    file = file.substring(1);
                }
                /* can have several " at the end */
                while (file.charAt(file.length() - 1) == '"') {
                    file = file.substring(0, file.length() - 1);
                }

                try {
                    /*
                     * use this REGEX to cut of following http links,
                     * (?=https?:|$|\r|\n|)
                     */
                    final DownloadLink link = new DownloadLink(this, null, getHost(), file, true);
                    links.add(link);
                } catch (Throwable e) {
                    JDLogger.exception(e);
                }
            }
        }
        if (links != null && fp != null && fp != FilePackage.getDefaultFilePackage()) {
            fp.addLinks(links);
        }
        return links;
    }

    /*
     * OVERRIDE this function if you need to modify the link, ATTENTION: you
     * have to use new browser instances, this plugin might not have one!
     */
    public void correctDownloadLink(final DownloadLink link) throws Exception {
    }

    /**
     * Holt Informationen zu einem Link. z.B. dateigroeße, Dateiname,
     * verfuegbarkeit etc.
     * 
     * @param parameter
     * @return true/false je nach dem ob die Datei noch online ist (verfuegbar)
     * @throws IOException
     */
    public abstract AvailableStatus requestFileInformation(DownloadLink parameter) throws Exception;

    /**
     * Gibt einen String mit den Dateiinformationen zurueck. Die Defaultfunktion
     * gibt nur den dateinamen zurueck. Allerdings Sollte diese Funktion
     * ueberschrieben werden. So kann ein Plugin zusatzinfos zu seinen Links
     * anzeigen (Nach dem aufruf von getFileInformation()
     * 
     * @param downloadLink
     * @return
     */
    public String getFileInformationString(final DownloadLink downloadLink) {
        return downloadLink.getName() + " (" + Formatter.formatReadable(downloadLink.getDownloadSize()) + ")";
    }

    public int getMaxRetries() {
        return JsonConfig.create(GeneralSettings.class).getMaxPluginRetries();
    }

    public int getMaxSimultanFreeDownloadNum() {
        return 1;
    }

    public int getMaxSimultanPremiumDownloadNum() {
        return -1;
    }

    public int getMaxSimultanDownload(final Account account) {
        int max;
        if (account == null) {
            max = getMaxSimultanFreeDownloadNum();
        } else {
            max = account.getMaxSimultanDownloads();
            if (max < 0) {
                return Integer.MAX_VALUE;
            } else if (max == 0) {
                max = getMaxSimultanPremiumDownloadNum();
            } else {
                return max;
            }
        }
        if (max <= 0) return Integer.MAX_VALUE;
        if (max == Integer.MIN_VALUE) return 0;
        return max;
    }

    /*
     * Override this if you want special handling for DownloadLinks to bypass
     * MaxSimultanDownloadNum
     */
    public boolean bypassMaxSimultanDownloadNum(DownloadLink link, Account acc) {
        return false;
    }

    /* TODO: remove with next major update */
    @Deprecated
    public boolean isPremiumDownload() {
        return true;
    }

    public synchronized long getLastTimeStarted() {
        if (!LAST_STARTED_TIME.containsKey(getHost())) { return 0; }
        return Math.max(0, (LAST_STARTED_TIME.get(getHost())));
    }

    public synchronized void putLastTimeStarted(long time) {
        LAST_STARTED_TIME.put(getHost(), time);
    }

    public synchronized long getLastConnectionTime() {
        if (!LAST_CONNECTION_TIME.containsKey(getHost())) { return 0; }
        return Math.max(0, (LAST_CONNECTION_TIME.get(getHost())));
    }

    public synchronized void putLastConnectionTime(long time) {
        LAST_CONNECTION_TIME.put(getHost(), time);
    }

    public void handlePremium(final DownloadLink link, final Account account) throws Exception {
        final LinkStatus linkStatus = link.getLinkStatus();
        linkStatus.addStatus(LinkStatus.ERROR_PLUGIN_DEFECT);
        linkStatus.setErrorMessage(_JDT._.plugins_hoster_nopremiumsupport());
    }

    public abstract void handleFree(DownloadLink link) throws Exception;

    /**
     * By overriding this method, a plugin is able to return a
     * HostPluginInfoGenerator. <br>
     * <b>Attention: Until next stable update, we have to return Object
     * here.</b>
     * 
     * @return
     */
    // @Override DO NEVER USE OVERRIDE ON THIS METHOD BEFORE NEXT STABLE UPDATE.
    public Object getInfoGenerator(Account account) {
        AccountInfo ai = account.getAccountInfo();
        HashMap<String, Object> props = null;
        if (ai == null) return null;
        props = ai.getProperties();
        if (props == null || props.size() == 0) return null;
        KeyValueInfoGenerator ret = new KeyValueInfoGenerator(_JDT._.pluginforhost_infogenerator_title(account.getUser(), account.getHoster()));
        for (Entry<String, Object> es : ai.getProperties().entrySet()) {
            String key = es.getKey();
            Object value = es.getValue();

            if (value != null) {
                ret.addPair(key, value.toString());
            }
        }
        return ret;
    }

    public void handle(final DownloadLink downloadLink, final Account account) throws Exception {
        try {
            while (waitForNextStartAllowed(downloadLink)) {
            }
        } catch (InterruptedException e) {
            return;
        }
        putLastTimeStarted(System.currentTimeMillis());

        if (account != null) {
            /* with account */
            final long before = downloadLink.getDownloadCurrent();
            boolean blockAccount = false;
            try {
                handlePremium(downloadLink, account);
            } catch (PluginException e) {
                e.fillLinkStatus(downloadLink.getLinkStatus());
                if (e.getLinkStatus() == LinkStatus.ERROR_PLUGIN_DEFECT) logger.info(JDLogger.getStackTrace(e));
                logger.info(downloadLink.getLinkStatus().getLongErrorMessage());
                if (downloadLink.getLinkStatus().hasStatus(LinkStatus.ERROR_IP_BLOCKED) || downloadLink.getLinkStatus().hasStatus(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE)) {
                    blockAccount = true;
                }
            } finally {
                try {
                    dl.getConnection().disconnect();
                } catch (Throwable e) {
                }
                try {
                    br.getHttpConnection().disconnect();
                } catch (Throwable e) {
                }
                try {
                    downloadLink.getDownloadLinkController().getConnectionHandler().removeConnectionHandler(dl.getManagedConnetionHandler());
                } catch (final Throwable e) {
                }
                setDownloadInterface(null);
            }

            final long traffic = Math.max(0, downloadLink.getDownloadCurrent() - before);
            final AccountInfo accountInfo = account.getAccountInfo();
            synchronized (AccountController.ACCOUNT_LOCK) {
                final AccountInfo ai = accountInfo;
                /* check traffic of account (eg traffic limit reached) */
                if (traffic > 0 && ai != null && !ai.isUnlimitedTraffic()) {
                    long left = Math.max(0, ai.getTrafficLeft() - traffic);
                    ai.setTrafficLeft(left);
                    if (left == 0 && ai.isSpecialTraffic()) {
                        logger.severe("Premium Account " + account.getUser() + ": Traffic Limit could be reached, but SpecialTraffic might be available!");
                    } else if (left == 0) {
                        logger.severe("Premium Account " + account.getUser() + ": Traffic Limit reached");
                        account.setTempDisabled(true);
                    }
                }
                /* check blocked account(eg free user accounts with waittime) */
                if (blockAccount) {
                    logger.severe("Account: " + account.getUser() + " is blocked, temp. disabling it!");
                    AccountController.getInstance().addAccountBlocked(account);
                }
            }
            if (downloadLink.getLinkStatus().hasStatus(LinkStatus.ERROR_PREMIUM)) {
                if (downloadLink.getLinkStatus().getValue() == PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE) {
                    logger.severe("Premium Account " + account.getUser() + ": Traffic Limit reached");
                    account.setTempDisabled(true);
                    account.getAccountInfo().setTrafficLeft(0);
                    if (accountInfo != null) {
                        accountInfo.setStatus(downloadLink.getLinkStatus().getErrorMessage());
                    }
                } else if (downloadLink.getLinkStatus().getValue() == PluginException.VALUE_ID_PREMIUM_DISABLE) {
                    account.setEnabled(false);
                    if (accountInfo != null) accountInfo.setStatus(downloadLink.getLinkStatus().getErrorMessage());
                    logger.severe("Premium Account " + account.getUser() + ": expired:" + downloadLink.getLinkStatus().getLongErrorMessage());
                } else {
                    account.setEnabled(false);
                    if (accountInfo != null) accountInfo.setStatus(downloadLink.getLinkStatus().getErrorMessage());
                    logger.severe("Premium Account " + account.getUser() + ":" + downloadLink.getLinkStatus().getLongErrorMessage());
                }
            } else {
                if (accountInfo != null) {
                    accountInfo.setStatus(_JDT._.plugins_hoster_premium_status_ok());
                }
            }
        } else {
            /* without account */
            try {
                handleFree(downloadLink);
            } catch (PluginException e) {
                e.fillLinkStatus(downloadLink.getLinkStatus());
                if (e.getLinkStatus() == LinkStatus.ERROR_PLUGIN_DEFECT) logger.info(JDLogger.getStackTrace(e));
                logger.info(downloadLink.getLinkStatus().getLongErrorMessage());
            } finally {
                try {
                    dl.getConnection().disconnect();
                } catch (Throwable e) {
                }
                try {
                    br.getHttpConnection().disconnect();
                } catch (Throwable e) {
                }
                try {
                    downloadLink.getDownloadLinkController().getConnectionHandler().removeConnectionHandler(dl.getManagedConnetionHandler());
                } catch (final Throwable e) {
                }
                setDownloadInterface(null);
            }
        }
        return;
    }

    /**
     * Stellt das Plugin in den Ausgangszustand zurueck (variablen intialisieren
     * etc)
     */
    public abstract void reset();

    public abstract void resetDownloadlink(DownloadLink link);

    public void resetPluginGlobals() {
    }

    public int getTimegapBetweenConnections() {
        return 50;
    }

    public void setStartIntervall(long interval) {
        WAIT_BETWEEN_STARTS = interval;
    }

    public boolean waitForNextStartAllowed(final DownloadLink downloadLink) throws InterruptedException {
        final long time = Math.max(0, WAIT_BETWEEN_STARTS - (System.currentTimeMillis() - getLastTimeStarted()));
        if (time > 0) {
            try {
                sleep(time, downloadLink);
            } catch (PluginException e) {
                throw new InterruptedException();
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean waitForNextConnectionAllowed() throws InterruptedException {
        final long time = Math.max(0, getTimegapBetweenConnections() - (System.currentTimeMillis() - getLastConnectionTime()));
        if (time > 0) {
            Thread.sleep(time);
            return true;
        } else {
            return false;
        }
    }

    public void sleep(final long i, final DownloadLink downloadLink) throws PluginException {
        sleep(i, downloadLink, "");
    }

    public void sleep(long i, DownloadLink downloadLink, String message) throws PluginException {
        SingleDownloadController dlc = downloadLink.getDownloadLinkController();
        try {
            while (i > 0 && dlc != null && !dlc.isAborted()) {
                i -= 1000;
                downloadLink.getLinkStatus().setStatusText(message + _JDT._.gui_download_waittime_status2(Formatter.formatSeconds(i / 1000)));
                synchronized (this) {
                    this.wait(1000);
                }
            }
        } catch (InterruptedException e) {
            throw new PluginException(LinkStatus.TODO);
        }
        downloadLink.getLinkStatus().setStatusText(null);
    }

    /**
     * may only be used from within the plugin, because it only works when
     * SingleDownloadController is still running
     */
    protected boolean isAborted(DownloadLink downloadLink) {
        SingleDownloadController dlc = downloadLink.getDownloadLinkController();
        return (dlc != null && dlc.isAborted());
    }

    public Browser getBrowser() {
        return br;
    }

    /**
     * Gibt die Url zurueck, unter welcher ein PremiumAccount gekauft werden
     * kann
     * 
     * @return
     */
    public String getBuyPremiumUrl() {
        if (premiumurl != null) return "http://jdownloader.org/r.php?u=" + Encoding.urlEncode(premiumurl);
        return premiumurl;
    }

    public boolean isPremiumEnabled() {
        return enablePremium;
    }

    public ArrayList<Account> getPremiumAccounts() {
        return AccountController.getInstance().getAllAccounts(this);
    }

    /**
     * returns hosterspecific infos. for example the downloadserver
     * 
     * @return
     */
    public String getSessionInfo() {
        return getHost();
    }

    public void setDownloadLink(DownloadLink link) {
        this.link = link;
    }

    public DownloadLink getDownloadLink() {
        return link;
    }

    /* override this if you want to change a link to use this plugin */
    /* dont forget to change host with setHost */
    /* must return true if changing was successful */
    /* if this function needs a browser, it must create an instance on its own */
    public boolean rewriteHost(DownloadLink link) {
        return false;
    }

    public String getCustomFavIconURL() {
        return getHost();
    }

    /**
     * @param ioPermission
     *            the ioPermission to set
     */
    public void setIOPermission(IOPermission ioPermission) {
        this.ioPermission = ioPermission;
    }

    /**
     * @return the ioPermission
     */
    public IOPermission getIOPermission() {
        return ioPermission;
    }

    public DomainInfo getDomainInfo() {
        String host = getCustomFavIconURL();
        if (host == null) host = getHost();
        return DomainInfo.getInstance(host);
    }

    public boolean hasCaptcha() {
        return false;
    }

    public boolean hasAutoCaptcha() {
        return JACMethod.hasMethod(getHost());
    }

    /**
     * plugins may change the package identifier used for auto package matching.
     * some hosters replace chars, shorten filenames...
     * 
     * @param packageIdentifier
     * @return
     */
    public String filterPackageID(String packageIdentifier) {
        return packageIdentifier;
    }

    /**
     * Some hosters have bad filenames. Rapidshare for example replaces all
     * special chars and spaces with _. Plugins can try to autocorrect this
     * based on other downloadlinks
     * 
     * @param orgiginalfilename
     * 
     * @param downloadLink
     * 
     * @param dlinks
     */
    // public String autoFilenameCorrection(String orgiginalfilename,
    // DownloadLink downloadLink, ArrayList<DownloadLink> dlinks) {
    // return null;
    // }

    public String autoFilenameCorrection(String originalFilename, DownloadLink downloadLink, ArrayList<DownloadLink> dlinks) {
        try {

            String MD5 = downloadLink.getMD5Hash();
            String SHA1 = downloadLink.getSha1Hash();
            // auto partname correction

            String[] multiPart = null;
            Pattern pattern = null;
            // find first match
            for (Pattern p : PATTERNS) {
                multiPart = new Regex(originalFilename, p).getRow(0);
                if (multiPart != null) {
                    pattern = p;
                    break;
                }
            }

            if (multiPart == null) {
                multiPart = new String[] { originalFilename, "" };
            }
            String filteredName = filterPackageID(multiPart[0]);
            String prototypesplit;
            String newName;
            for (DownloadLink next : dlinks) {
                if (downloadLink == next) continue;
                if (next.getHost().equals(getHost())) continue;
                String prototypeName = next.getNameSetbyPlugin();
                if (prototypeName.equals(originalFilename)) continue;
                if (prototypeName.equalsIgnoreCase(originalFilename)) {
                    newName = fixCase(originalFilename, prototypeName);
                    if (newName != null) return newName;
                }
                if (pattern != null) {
                    prototypesplit = new Regex(prototypeName, pattern).getMatch(0);
                } else {
                    prototypesplit = prototypeName;
                }
                if (isHosterManipulatesFilenames() && multiPart[0].length() == prototypesplit.length() && filteredName.equals(filterPackageID(prototypesplit))) {
                    newName = getFixedFileName(originalFilename, prototypesplit);
                    if (newName != null) {

                    return newName + multiPart[1]; }
                }

                if ((MD5 != null && MD5.equalsIgnoreCase(next.getMD5Hash())) || (SHA1 != null && SHA1.equalsIgnoreCase(next.getSha1Hash()))) {
                    // 100% mirror! ok and now? these files should have the
                    // same filename!!
                    return next.getName();
                }
            }

        } catch (Throwable e) {
            Log.exception(e);
        }

        return null;
    }

    protected String fixCase(String originalFilename, String prototypeName) {
        StringBuilder sb = new StringBuilder(prototypeName.length());
        for (int i = 0; i < prototypeName.length(); i++) {
            char c = originalFilename.charAt(i);
            char correctc = prototypeName.charAt(i);
            if (Character.toLowerCase(c) == Character.toLowerCase(correctc)) {
                sb.append(Character.isUpperCase(c) ? c : correctc);
            } else {
                return null;
            }
        }
        return sb.toString();
    }

    protected String getFixedFileName(String originalFilename, String prototypeName) {
        throw new RuntimeException("not implemented");
    }

    /**
     * Some hoster manipulate the filename after upload. rapidshare for example,
     * replaces special chars and spaces with _
     * 
     * @return
     */
    public boolean isHosterManipulatesFilenames() {
        return false;
    }

}