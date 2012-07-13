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

package jd.controlling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import jd.config.Property;
import jd.config.SubConfiguration;
import jd.controlling.accountchecker.AccountChecker;
import jd.controlling.reconnect.ipcheck.BalancedWebIPCheck;
import jd.controlling.reconnect.ipcheck.IPCheckException;
import jd.controlling.reconnect.ipcheck.OfflineException;
import jd.http.Browser;
import jd.http.BrowserSettingsThread;
import jd.nutils.encoding.Encoding;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.scheduler.DelayedRunnable;
import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.StringUtils;
import org.appwork.utils.event.Eventsender;
import org.appwork.utils.logging2.LogSource;
import org.jdownloader.logging.LogController;
import org.jdownloader.settings.AccountData;
import org.jdownloader.settings.AccountSettings;

public class AccountController implements AccountControllerListener {

    private static final long                                                    serialVersionUID = -7560087582989096645L;

    private static HashMap<String, ArrayList<Account>>                           hosteraccounts   = null;

    private static HashMap<Account, Long>                                        blockedAccounts  = new HashMap<Account, Long>();

    private static AccountController                                             INSTANCE         = new AccountController();

    private final Eventsender<AccountControllerListener, AccountControllerEvent> broadcaster      = new Eventsender<AccountControllerListener, AccountControllerEvent>() {

                                                                                                      @Override
                                                                                                      protected void fireEvent(final AccountControllerListener listener, final AccountControllerEvent event) {
                                                                                                          listener.onAccountControllerEvent(event);
                                                                                                      }

                                                                                                  };

    public Eventsender<AccountControllerListener, AccountControllerEvent> getBroadcaster() {
        return broadcaster;
    }

    private AccountSettings config;

    private DelayedRunnable delayedSaver;

    private AccountController() {
        super();
        config = JsonConfig.create(AccountSettings.class);
        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {

            @Override
            public void run() {
                save();
            }

            @Override
            public String toString() {
                return "save accounts...";
            }
        });
        hosteraccounts = loadAccounts();
        final Collection<ArrayList<Account>> accsc = hosteraccounts.values();
        for (final ArrayList<Account> accs : accsc) {
            for (final Account acc : accs) {
                acc.setAccountController(this);
            }
        }
        delayedSaver = new DelayedRunnable(IOEQ.TIMINGQUEUE, 5000, 30000) {

            @Override
            public void delayedrun() {
                save();
            }
        };
        broadcaster.addListener(this);
    }

    protected void save() {
        HashMap<String, ArrayList<AccountData>> ret = new HashMap<String, ArrayList<AccountData>>();
        synchronized (hosteraccounts) {
            for (Iterator<Entry<String, ArrayList<Account>>> it = hosteraccounts.entrySet().iterator(); it.hasNext();) {
                Entry<String, ArrayList<Account>> next = it.next();
                if (next.getValue().size() > 0) {
                    ArrayList<AccountData> list = new ArrayList<AccountData>();
                    ret.put(next.getKey(), list);
                    for (Account a : next.getValue()) {
                        list.add(AccountData.create(a));
                    }
                }
            }
        }
        config.setAccounts(ret);
    }

    public AccountInfo updateAccountInfo(final Account account, final boolean forceupdate) {
        AccountInfo ai = account.getAccountInfo();
        if (!forceupdate) {
            if (account.lastUpdateTime() != 0) {
                if (ai != null && ai.isExpired()) {
                    account.setEnabled(false);
                    this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.Types.EXPIRED, account));
                    /* account is expired, no need to update */
                    return ai;
                }
                if (!account.isValid()) {
                    account.setEnabled(false);
                    this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.Types.INVALID, account));
                    /* account is invalid, no need to update */
                    return ai;
                }
            }
            if ((System.currentTimeMillis() - account.lastUpdateTime()) < account.getRefreshTimeout()) {
                /*
                 * account was checked before, timeout for recheck not reached, no need to update
                 */
                return ai;
            }
        }
        final PluginForHost plugin = JDUtilities.getNewPluginForHostInstance(account.getHoster());
        if (plugin == null) {
            LogController.CL().severe("AccountCheck: Failed because plugin " + account.getHoster() + " is missing!");
            account.setEnabled(false);
            return null;
        }
        String whoAmI = account.getUser() + "->" + account.getHoster();
        LogSource logger = LogController.getInstance().getLogger(plugin);
        logger.info("Account Update: " + whoAmI);
        logger.setAllowTimeoutFlush(false);
        plugin.setLogger(logger);
        Thread currentThread = Thread.currentThread();
        BrowserSettingsThread bThread = null;
        Logger oldLogger = null;
        if (currentThread instanceof BrowserSettingsThread) {
            bThread = (BrowserSettingsThread) currentThread;
        }
        if (bThread != null) {
            /* set logger to browserSettingsThread */
            oldLogger = bThread.getLogger();
            bThread.setLogger(logger);
        }
        try {
            Browser br = new Browser();
            br.setLogger(logger);
            plugin.setBrowser(br);
            /* not every plugin sets this info correct */
            account.setValid(true);
            /* get previous account info and resets info for new update */
            ai = account.getAccountInfo();
            if (ai != null) {
                /* reset expired and setValid */
                ai.setExpired(false);
                ai.setValidUntil(-1);
            }
            ClassLoader oldClassLoader = currentThread.getContextClassLoader();
            try {
                /*
                 * make sure the current Thread uses the PluginClassLoaderChild of the Plugin in use
                 */
                currentThread.setContextClassLoader(plugin.getLazyP().getClassLoader());
                ai = plugin.fetchAccountInfo(account);
            } finally {
                account.setUpdateTime(System.currentTimeMillis());
                currentThread.setContextClassLoader(oldClassLoader);
            }
            if (ai == null) {
                /* not every plugin has fetchAccountInfo */
                account.setAccountInfo(null);
                this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.Types.UPDATE, account));
                return null;
            }
            account.setAccountInfo(ai);
            if (ai.isExpired()) {
                logger.clear();
                LogController.CL().info("Account " + whoAmI + " is expired!");
                this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.Types.EXPIRED, account));
            } else if (!account.isValid()) {
                account.setEnabled(false);
                LogController.CL().info("Account " + whoAmI + " is invalid!");
                this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.Types.INVALID, account));
            } else {
                logger.clear();
                this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.Types.UPDATE, account));
            }
        } catch (final Throwable e) {
            if (e instanceof PluginException) {
                PluginException pe = (PluginException) e;
                ai = account.getAccountInfo();
                if (ai == null) {
                    ai = new AccountInfo();
                    account.setAccountInfo(ai);
                }
                if ((pe.getLinkStatus() == LinkStatus.ERROR_PREMIUM)) {
                    if (pe.getValue() == PluginException.VALUE_ID_PREMIUM_TEMP_DISABLE) {
                        logger.clear();
                        LogController.CL().info("Account " + whoAmI + " traffic limit reached!");
                        if (!StringUtils.isEmpty(pe.getErrorMessage())) {
                            ai.setStatus(pe.getErrorMessage());
                        } else {
                            ai.setStatus("Traffic limit reached");
                        }
                        account.setTempDisabled(true);
                        account.getAccountInfo().setTrafficLeft(0);
                        this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.Types.UPDATE, account));
                        return ai;
                    } else if (pe.getValue() == PluginException.VALUE_ID_PREMIUM_DISABLE) {
                        account.setEnabled(false);
                        account.setValid(false);
                        if (!StringUtils.isEmpty(pe.getErrorMessage())) {
                            ai.setStatus(pe.getErrorMessage());
                        } else {
                            ai.setStatus("Invalid Account!");
                        }
                        logger.clear();
                        LogController.CL().info("Account " + whoAmI + " is invalid!");
                        this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.Types.INVALID, account));
                        return ai;
                    }
                }
            } else if (e instanceof IOException) {
                /* network exception, lets temp disable the account */
                BalancedWebIPCheck onlineCheck = new BalancedWebIPCheck(true);
                try {
                    onlineCheck.getExternalIP();
                } catch (final OfflineException e2) {
                    /*
                     * we are offline, so lets just return without any account update
                     */
                    logger.clear();
                    LogController.CL().info("It seems Computer is currently offline, skipped Accountcheck for " + whoAmI);
                    return ai;
                } catch (final IPCheckException e2) {
                }
            }
            logger.severe("AccountCheck: Failed because of exception");
            logger.log(e);
            /* move download log into global log */
            account.setAccountInfo(null);
            account.setEnabled(false);
            account.setValid(false);
            this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.Types.INVALID, account));
        } finally {
            logger.close();
            if (bThread != null) {
                /* remove logger from browserSettingsThread */
                bThread.setLogger(oldLogger);
            }
        }
        return ai;
    }

    public static AccountController getInstance() {
        return INSTANCE;
    }

    private synchronized HashMap<String, ArrayList<Account>> loadAccounts() {
        HashMap<String, ArrayList<AccountData>> dat = config.getAccounts();
        if (dat == null) {
            try {
                dat = restore();
            } catch (final Throwable e) {
                LogController.CL().log(e);
            }
        }
        if (dat == null) {
            dat = new HashMap<String, ArrayList<AccountData>>();
        }
        HashMap<String, ArrayList<Account>> ret = new HashMap<String, ArrayList<Account>>();
        for (Iterator<Entry<String, ArrayList<AccountData>>> it = dat.entrySet().iterator(); it.hasNext();) {
            Entry<String, ArrayList<AccountData>> next = it.next();
            if (next.getValue().size() > 0) {
                ArrayList<Account> list = new ArrayList<Account>();
                ret.put(next.getKey(), list);
                for (AccountData ad : next.getValue()) {
                    Account acc;
                    list.add(acc = ad.toAccount());
                    /*
                     * make sure hostername is set and share same String instance
                     */
                    acc.setHoster(next.getKey());
                }
            }
        }
        return ret;
    }

    /**
     * Restores accounts from old database
     * 
     * @return
     */
    private HashMap<String, ArrayList<AccountData>> restore() {
        SubConfiguration sub = SubConfiguration.getConfig("AccountController", true);
        HashMap<String, ArrayList<AccountData>> ret = new HashMap<String, ArrayList<AccountData>>();
        Object mapRet = sub.getProperty("accountlist");
        if (mapRet != null && mapRet instanceof Map) {
            Map<String, Object> tree = (Map<String, Object>) mapRet;
            for (Iterator<Entry<String, Object>> it = tree.entrySet().iterator(); it.hasNext();) {
                Entry<String, Object> next = it.next();
                if (next.getValue() instanceof ArrayList) {
                    ArrayList<Object> accList = (ArrayList<Object>) next.getValue();
                    if (accList.size() > 0) {
                        ArrayList<AccountData> list = new ArrayList<AccountData>();
                        ret.put(next.getKey(), list);
                        if (accList.get(0) instanceof Account) {
                            ArrayList<Account> accList2 = (ArrayList<Account>) next.getValue();
                            for (Account a : accList2) {
                                AccountData ac;
                                list.add(ac = new AccountData());
                                ac.setUser(a.getUser());
                                ac.setPassword(a.getPass());
                                ac.setValid(a.isEnabled());
                                ac.setEnabled(a.isEnabled());
                            }
                        } else if (accList.get(0) instanceof Map) {
                            ArrayList<Map<String, Object>> accList2 = (ArrayList<Map<String, Object>>) next.getValue();
                            for (Map<String, Object> a : accList2) {
                                AccountData ac;
                                list.add(ac = new AccountData());
                                ac.setUser((String) a.get("user"));
                                ac.setPassword((String) a.get("pass"));
                                ac.setEnabled(a.containsKey("enabled"));
                                ac.setValid(ac.isEnabled());
                            }
                        }
                    }
                }
            }
        }
        config.setAccounts(ret);
        return ret;
    }

    @Deprecated
    public void addAccount(final PluginForHost pluginForHost, final Account account) {
        account.setHoster(pluginForHost.getHost());
        addAccount(account);
    }

    public boolean isAccountBlocked(final Account account) {
        synchronized (blockedAccounts) {
            Long ret = blockedAccounts.get(account);
            if (ret == null) return false;
            if (System.currentTimeMillis() > ret) {
                /*
                 * timeout is over, lets remove the account as it is no longer blocked
                 */
                blockedAccounts.remove(account);
                return false;
            }
            return true;
        }
    }

    public void addAccountBlocked(final Account account, final long value) {
        synchronized (blockedAccounts) {
            long blockedTime = Math.max(0, value);
            if (blockedTime == 0) {
                LogController.CL().info("Invalid AccountBlock timeout! set 30 mins!");
                blockedTime = 60 * 60 * 1000l;
            }
            blockedAccounts.put(account, System.currentTimeMillis() + blockedTime);
        }
    }

    /* remove accountblock for given account or all if account is null */
    public void removeAccountBlocked(final Account account) {
        synchronized (blockedAccounts) {
            if (account == null) {
                blockedAccounts.clear();
            } else {
                blockedAccounts.remove(account);
            }
        }
    }

    /* returns a list of all available accounts for given host */
    public List<Account> list(String host) {
        ArrayList<Account> ret = new ArrayList<Account>();
        synchronized (hosteraccounts) {
            if (StringUtils.isEmpty(host)) {
                for (String hoster : hosteraccounts.keySet()) {
                    ArrayList<Account> ret2 = hosteraccounts.get(hoster);
                    if (ret2 != null) ret.addAll(ret2);
                }
            } else {
                ArrayList<Account> ret2 = hosteraccounts.get(host);
                if (ret2 != null) ret.addAll(ret2);
            }
        }
        return ret;
    }

    /* returns a list of all available accounts */
    public List<Account> list() {
        return list(null);
    }

    /* do we have accounts for this host */
    public boolean hasAccounts(final String host) {
        ArrayList<Account> ret = null;
        synchronized (hosteraccounts) {
            ret = hosteraccounts.get(host);
            if (ret != null) {
                for (Account acc : ret) {
                    if (acc.isEnabled() && acc.isValid()) return true;
                }
            }
        }
        return false;
    }

    /* do we have multihost accounts for this host */
    public boolean hasMultiHostAccounts(final String host) {
        synchronized (hosteraccounts) {
            Iterator<Entry<String, ArrayList<Account>>> it = hosteraccounts.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, ArrayList<Account>> next = it.next();
                if (next.getKey().equalsIgnoreCase(host)) {
                    /* we dont't want account from same host */
                    continue;
                }
                for (Account acc : next.getValue()) {
                    if (!acc.isEnabled() || !acc.isValid()) {
                        /*
                         * we remove every invalid/disabled/tempdisabled/blocked account
                         */
                        continue;
                    }
                    AccountInfo ai = acc.getAccountInfo();
                    if (ai == null) continue;
                    Object supported = null;
                    synchronized (ai) {
                        /*
                         * synchronized on accountinfo because properties are not threadsafe
                         */
                        supported = ai.getProperty("multiHostSupport", Property.NULL);
                    }
                    if (Property.NULL == supported || supported == null) continue;
                    synchronized (supported) {
                        /*
                         * synchronized on list because plugins can change the list in runtime
                         */
                        if (supported instanceof ArrayList) {
                            for (String sup : (ArrayList<String>) supported) {
                                if (host.equalsIgnoreCase(sup)) { return true; }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public void addAccount(final Account account) {
        if (account == null) return;
        synchronized (hosteraccounts) {
            ArrayList<Account> accs = hosteraccounts.get(account.getHoster());
            if (accs == null) {
                accs = new ArrayList<Account>();
                hosteraccounts.put(account.getHoster(), accs);
            }
            for (final Account acc : accs) {
                if (acc.equals(account)) return;
            }
            accs.add(account);
            account.setAccountController(this);
        }
        this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.Types.ADDED, account));
    }

    public boolean removeAccount(final Account account) {
        if (account == null) { return false; }
        /* remove reference to AccountController */
        account.setAccountController(null);
        removeAccountBlocked(account);
        synchronized (hosteraccounts) {
            ArrayList<Account> accs = hosteraccounts.get(account.getHoster());
            if (accs == null || !accs.remove(account)) return false;
            if (accs.size() == 0) hosteraccounts.remove(account.getHoster());
        }
        this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.Types.REMOVED, account));
        return true;
    }

    public void onAccountControllerEvent(final AccountControllerEvent event) {
        Account acc = null;
        switch (event.getType()) {
        case ADDED:
            org.jdownloader.settings.staticreferences.CFG_GENERAL.USE_AVAILABLE_ACCOUNTS.setValue(true);
            delayedSaver.run();
            break;
        case REMOVED:
            delayedSaver.run();
            return;
        }
        if (event.isRecheckRequired()) {
            /* event tells us to recheck the account */
            acc = event.getParameter();
            if (acc != null) AccountChecker.getInstance().check(acc, true);
        }
    }

    @Deprecated
    public Account getValidAccount(final PluginForHost pluginForHost) {
        LinkedList<Account> ret = getValidAccounts(pluginForHost.getHost());
        if (ret != null && ret.size() > 0) return ret.getFirst();
        return null;
    }

    public LinkedList<Account> getValidAccounts(final String host) {
        LinkedList<Account> ret = null;
        synchronized (hosteraccounts) {
            final ArrayList<Account> accounts = hosteraccounts.get(host);
            if (accounts == null || accounts.size() == 0) return null;
            ret = new LinkedList<Account>(accounts);
        }
        Iterator<Account> it = ret.iterator();
        while (it.hasNext()) {
            Account next = it.next();
            if (!next.isEnabled() || !next.isValid() || next.isTempDisabled() || isAccountBlocked(next)) {
                /* we remove every invalid/disabled/tempdisabled/blocked account */
                it.remove();
            }
        }
        return ret;
    }

    public LinkedList<Account> getMultiHostAccounts(final String host) {
        LinkedList<Account> ret = new LinkedList<Account>();
        synchronized (hosteraccounts) {
            Iterator<Entry<String, ArrayList<Account>>> it = hosteraccounts.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, ArrayList<Account>> next = it.next();
                if (next.getKey().equalsIgnoreCase(host)) {
                    /* we dont't want account from same host */
                    continue;
                }
                for (Account acc : next.getValue()) {
                    if (!acc.isEnabled() || !acc.isValid() || acc.isTempDisabled()) {
                        /*
                         * we remove every invalid/disabled/tempdisabled/blocked account
                         */
                        continue;
                    }
                    AccountInfo ai = acc.getAccountInfo();
                    if (ai == null) continue;
                    Object supported = null;
                    synchronized (ai) {
                        /*
                         * synchronized on accountinfo because properties are not threadsafe
                         */
                        supported = ai.getProperty("multiHostSupport", Property.NULL);
                    }
                    if (Property.NULL == supported || supported == null) continue;
                    synchronized (supported) {
                        /*
                         * synchronized on list because plugins can change the list in runtime
                         */
                        if (supported instanceof ArrayList) {
                            for (String sup : (ArrayList<String>) supported) {
                                if (sup.equalsIgnoreCase(host)) {
                                    ret.add(acc);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    @Deprecated
    public ArrayList<Account> getAllAccounts(String string) {
        return (ArrayList<Account>) list(string);
    }

    public static String createFullBuyPremiumUrl(String buyPremiumUrl, String id) {
        return "http://update3.jdownloader.org/jdserv/BuyPremiumInterface/redirect?" + Encoding.urlEncode(buyPremiumUrl) + "&" + Encoding.urlEncode(id);
    }
}