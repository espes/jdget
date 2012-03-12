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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import jd.config.SubConfiguration;
import jd.controlling.accountchecker.AccountChecker;
import jd.http.Browser;
import jd.plugins.Account;
import jd.plugins.AccountInfo;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.JDUtilities;

import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.StringUtils;
import org.appwork.utils.event.Eventsender;
import org.appwork.utils.logging.Log;
import org.jdownloader.settings.AccountData;
import org.jdownloader.settings.AccountSettings;

public class AccountController implements AccountControllerListener {

    private static final long                                                    serialVersionUID          = -7560087582989096645L;

    private static HashMap<String, ArrayList<Account>>                           hosteraccounts            = null;

    private static HashMap<String, ArrayList<Account>>                           blockedAccounts           = new HashMap<String, ArrayList<Account>>();

    private static AccountController                                             INSTANCE                  = new AccountController();

    private final Eventsender<AccountControllerListener, AccountControllerEvent> broadcaster               = new Eventsender<AccountControllerListener, AccountControllerEvent>() {

                                                                                                               @Override
                                                                                                               protected void fireEvent(final AccountControllerListener listener, final AccountControllerEvent event) {
                                                                                                                   listener.onAccountControllerEvent(event);
                                                                                                               }

                                                                                                           };

    private long                                                                 waittimeAccountInfoUpdate = 15 * 60 * 1000l;

    private Logger                                                               logger                    = JDLogger.getLogger();

    private AccountSettings                                                      config;

    public static final Object                                                   ACCOUNT_LOCK              = new Object();

    public long getUpdateTime() {
        return waittimeAccountInfoUpdate;
    }

    public void setUpdateTime(final long time) {
        this.waittimeAccountInfoUpdate = time;
    }

    private static final Comparator<Account> COMPARE_MOST_TRAFFIC_LEFT = new Comparator<Account>() {
                                                                           public int compare(final Account o1, final Account o2) {
                                                                               final AccountInfo ai1 = o1.getAccountInfo();
                                                                               final AccountInfo ai2 = o2.getAccountInfo();
                                                                               long t1 = ai1 == null ? 0 : ai1.getTrafficLeft();
                                                                               long t2 = ai2 == null ? 0 : ai2.getTrafficLeft();
                                                                               if (t1 < 0) t1 = Long.MAX_VALUE;
                                                                               if (t2 < 0) t2 = Long.MAX_VALUE;
                                                                               if (t1 == t2) return 0;
                                                                               /*
                                                                                * reverse
                                                                                * order
                                                                                * ,
                                                                                * we
                                                                                * want
                                                                                * biggest
                                                                                * on
                                                                                * top
                                                                                */
                                                                               if (t1 < t2) return 1;
                                                                               return -1;
                                                                           }
                                                                       };

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

    public AccountInfo updateAccountInfo(final String host, final Account account, final boolean forceupdate) {
        final String hostname = host != null ? host : getHosterName(account);
        if (hostname == null) {
            account.setAccountInfo(null);
            logger.severe("Cannot update AccountInfo, no Hostername available!");
            return null;
        }
        final PluginForHost plugin = JDUtilities.getNewPluginForHostInstance(hostname);
        if (plugin == null) {
            account.setAccountInfo(null);
            logger.severe("Cannot update AccountInfo, no HosterPlugin available!");
            return null;
        }
        plugin.setLogger(new JDPluginLogger("AccountCheck:" + hostname));
        plugin.setBrowser(new Browser());
        AccountInfo ai = account.getAccountInfo();
        if (!forceupdate) {
            if (account.lastUpdateTime() != 0 && ai != null && ai.isExpired()) {
                account.setEnabled(false);
                this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_EXPIRED, hostname, account));
                /* account is expired, no need to update */
                return ai;
            }
            if (!account.isValid() && account.lastUpdateTime() != 0) {
                account.setEnabled(false);
                this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_INVALID, hostname, account));
                /* account is invalid, no need to update */
                return ai;
            }
            if ((System.currentTimeMillis() - account.lastUpdateTime()) < waittimeAccountInfoUpdate) {
                /*
                 * account was checked before, timeout for recheck not reached,
                 * no need to update
                 */
                return ai;
            }
        }
        try {
            /* not every plugin sets this info correct */
            account.setValid(true);
            /* get previous account info and resets info for new update */
            ai = account.getAccountInfo();
            if (ai != null) {
                /* reset expired and setValid */
                ai.setExpired(false);
                ai.setValidUntil(-1);
            }
            try {
                ai = plugin.fetchAccountInfo(account);
            } finally {
                account.setUpdateTime(System.currentTimeMillis());
            }
            if (ai == null) {
                // System.out.println("plugin no update " + hostname);
                /* not every plugin has fetchAccountInfo */
                account.setAccountInfo(null);
                this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_UPDATE, hostname, account));
                return null;
            }
            synchronized (ACCOUNT_LOCK) {
                account.setAccountInfo(ai);
            }
            if (ai.isExpired()) {
                account.setEnabled(false);
                this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_EXPIRED, hostname, account));
            } else if (!account.isValid()) {
                account.setEnabled(false);
                this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_INVALID, hostname, account));
            } else {
                this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_UPDATE, hostname, account));
            }
        } catch (final IOException e) {
            logger.severe("AccountUpdate: " + host + " failed!");
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
                        logger.severe("Premium Account " + account.getUser() + ": Traffic Limit reached");
                        account.setTempDisabled(true);
                        account.getAccountInfo().setTrafficLeft(0);
                        this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_UPDATE, hostname, account));
                        return ai;
                    } else if (pe.getValue() == PluginException.VALUE_ID_PREMIUM_DISABLE) {
                        account.setEnabled(false);
                        account.setValid(false);
                        if (StringUtils.isEmpty(ai.getStatus())) ai.setStatus("Invalid Account!");
                        logger.severe("Premium Account " + account.getUser() + ": expired:");
                        this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_INVALID, hostname, account));
                        return ai;
                    }
                }
            }
            logger.severe("AccountUpdate: " + host + " failed!");
            JDLogger.exception(e);
            account.setAccountInfo(null);
            account.setEnabled(false);
            account.setValid(false);
            this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_INVALID, hostname, account));
        }
        return ai;
    }

    /**
     * return hostername if account is under controll of AccountController
     */
    public String getHosterName(final Account account) {
        if (account == null) return null;
        if (account.getHoster() != null) { return account.getHoster(); }
        synchronized (hosteraccounts) {
            for (final String host : hosteraccounts.keySet()) {
                if (hosteraccounts.get(host).contains(account)) {
                    account.setHoster(host);
                    return host;
                }
            }
        }
        return null;
    }

    public HashMap<String, ArrayList<Account>> list() {
        synchronized (hosteraccounts) {
            return new HashMap<String, ArrayList<Account>>(hosteraccounts);
        }
    }

    public static AccountController getInstance() {
        return INSTANCE;
    }

    public void addListener(final AccountControllerListener l) {
        broadcaster.addListener(l);
    }

    public void removeListener(final AccountControllerListener l) {
        broadcaster.removeListener(l);
    }

    private synchronized HashMap<String, ArrayList<Account>> loadAccounts() {
        HashMap<String, ArrayList<AccountData>> dat = config.getAccounts();
        if (dat == null) {
            try {
                dat = restore();
            } catch (final Throwable e) {
                Log.exception(e);
                dat = new HashMap<String, ArrayList<AccountData>>();
            }
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
                    /* make sure hostername is set */
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
        SubConfiguration sub = SubConfiguration.getConfig("AccountController");
        HashMap<String, ArrayList<HashMap<String, Object>>> tree = sub.getGenericProperty("accountlist", new HashMap<String, ArrayList<HashMap<String, Object>>>());
        HashMap<String, ArrayList<AccountData>> ret = new HashMap<String, ArrayList<AccountData>>();

        for (Iterator<Entry<String, ArrayList<HashMap<String, Object>>>> it = tree.entrySet().iterator(); it.hasNext();) {
            Entry<String, ArrayList<HashMap<String, Object>>> next = it.next();
            if (next.getValue().size() > 0) {
                ArrayList<AccountData> list = new ArrayList<AccountData>();
                ret.put(next.getKey(), list);
                for (HashMap<String, Object> a : next.getValue()) {
                    AccountData ac;
                    list.add(ac = new AccountData());
                    ac.setUser((String) a.get("user"));
                    ac.setPassword((String) a.get("pass"));
                    ac.setEnabled("true".equals(a.containsKey("enabled")));
                }
            }
        }
        config.setAccounts(ret);
        return ret;
    }

    public void addAccount(final PluginForHost pluginForHost, final Account account) {
        addAccount(pluginForHost.getHost(), account);
        account.setAccountController(this);
    }

    public ArrayList<Account> getAllAccounts(final PluginForHost pluginForHost) {
        // if (pluginForHost == null) return new ArrayList<Account>();
        // return this.getAllAccounts(pluginForHost.getHost());
        return (pluginForHost == null) ? new ArrayList<Account>() : getAllAccounts(pluginForHost.getHost());
    }

    public boolean isAccountBlocked(final Account account) {
        synchronized (blockedAccounts) {
            final String host = this.getHosterName(account);
            List<Account> blockedAccountsOfTheSameHoster = blockedAccounts.get(host);
            return blockedAccountsOfTheSameHoster != null && blockedAccountsOfTheSameHoster.contains(account);
        }
    }

    public void addAccountBlocked(final Account account) {
        synchronized (blockedAccounts) {
            if (isAccountBlocked(account)) return;
            final String host = this.getHosterName(account);
            ArrayList<Account> ar = blockedAccounts.get(host);
            if (ar == null) {
                ar = new ArrayList<Account>();
                blockedAccounts.put(host, ar);
            }
            ar.add(account);
        }
    }

    public void removeAccountBlocked(final Account account) {
        synchronized (blockedAccounts) {
            if (!isAccountBlocked(account)) return;
            final String host = this.getHosterName(account);
            final ArrayList<Account> ar = blockedAccounts.get(host);
            if (ar != null) ar.remove(account);
        }
    }

    public void removeAccountBlocked(final String host) {
        synchronized (blockedAccounts) {
            if (host == null) {
                blockedAccounts.clear();
            } else {
                blockedAccounts.remove(host);
            }
        }
    }

    public ArrayList<Account> getAllAccounts(final String host) {
        final ArrayList<Account> ret = new ArrayList<Account>();
        if (host == null) return ret;
        synchronized (hosteraccounts) {
            if (hosteraccounts.containsKey(host)) {
                return hosteraccounts.get(host);
            } else {
                final ArrayList<Account> haccounts = new ArrayList<Account>();
                hosteraccounts.put(host, haccounts);
                return haccounts;
            }
        }
    }

    public boolean hasAccounts(final String host) {
        synchronized (hosteraccounts) {
            final ArrayList<Account> ret = hosteraccounts.get(host);
            if (ret != null && ret.size() > 0) return true;
        }
        return false;
    }

    public int validAccounts() {
        int count = 0;
        synchronized (hosteraccounts) {
            for (final ArrayList<Account> accs : hosteraccounts.values()) {
                for (final Account acc : accs) {
                    if (acc.isEnabled()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private void addAccount(final String host, final Account account) {
        if (host != null && account != null) {
            synchronized (hosteraccounts) {
                if (hosteraccounts.containsKey(host)) {
                    final ArrayList<Account> haccounts = hosteraccounts.get(host);
                    synchronized (haccounts) {
                        boolean b = haccounts.contains(account);
                        if (!b) {
                            boolean b2 = false;
                            final ArrayList<Account> temp = new ArrayList<Account>(haccounts);
                            for (final Account acc : temp) {
                                if (acc.equals(account)) {
                                    b2 = true;
                                    break;
                                }
                            }
                            if (!b2) {
                                haccounts.add(account);
                                b = true;
                            }
                        }
                        if (b) {
                            this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_ADDED, host, account));
                        }
                    }
                } else {
                    final ArrayList<Account> haccounts = new ArrayList<Account>();
                    haccounts.add(account);
                    hosteraccounts.put(host, haccounts);
                    this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_ADDED, host, account));
                }
            }
        }
    }

    public boolean removeAccount(final String hostname, final Account account) {
        if (account == null) { return false; }
        final String host = (hostname == null) ? getHosterName(account) : hostname;
        if (host == null) { return false; }
        synchronized (hosteraccounts) {
            if (!hosteraccounts.containsKey(host)) { return false; }
            final ArrayList<Account> haccounts = hosteraccounts.get(host);
            synchronized (haccounts) {
                boolean b = haccounts.remove(account);
                if (!b) {
                    final ArrayList<Account> temp = new ArrayList<Account>(haccounts);
                    for (final Account acc : temp) {
                        if (acc.equals(account)) {
                            // account = acc;
                            // b = haccounts.remove(account);
                            b = haccounts.remove(acc);
                            break;
                        }
                    }
                }
                if (b) {
                    this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_REMOVED, host, account));
                }
                return b;
            }
        }
    }

    public boolean removeAccount(final PluginForHost pluginForHost, final Account account) {
        if (account == null) { return false; }
        if (pluginForHost == null) { return removeAccount((String) null, account); }
        return removeAccount(pluginForHost.getHost(), account);
    }

    public void onAccountControllerEvent(final AccountControllerEvent event) {
        Account acc = null;
        switch (event.getEventID()) {
        case AccountControllerEvent.ACCOUNT_ADDED:
            acc = event.getAccount();
            /*
             * we do an accountcheck as this account just got added to this
             * controller
             */
            if (acc != null) {
                if (System.currentTimeMillis() - acc.lastUpdateTime() > 20000) {
                    /*
                     * only check account again if last check is more than 20
                     * secs ago, prevents double check on addaccountdialog
                     */
                    AccountChecker.getInstance().check(acc, true);
                }
            }
            org.jdownloader.settings.staticreferences.CFG_GENERAL.USE_AVAILABLE_ACCOUNTS.setValue(true);
            break;
        case AccountControllerEvent.ACCOUNT_UPDATE:
            acc = event.getAccount();
            /* we do a new accountcheck as this account got updated */
            /* WARNING: DO NOT FORCE check here, it might end up in a loop */
            if (acc != null && acc.isEnabled() && this == acc.getAccountController()) AccountChecker.getInstance().check(acc, false);
            break;
        case AccountControllerEvent.ACCOUNT_REMOVED:
        case AccountControllerEvent.ACCOUNT_EXPIRED:
        case AccountControllerEvent.ACCOUNT_INVALID:
            break;
        default:
            break;
        }
    }

    public void throwUpdateEvent(final PluginForHost pluginForHost, final Account account) {
        if (pluginForHost != null) {
            this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_UPDATE, pluginForHost.getHost(), account));
        } else {
            this.broadcaster.fireEvent(new AccountControllerEvent(this, AccountControllerEvent.ACCOUNT_UPDATE, null, account));
        }
    }

    public Account getValidAccount(final PluginForHost pluginForHost) {
        return getValidAccount(pluginForHost.getHost());
    }

    public boolean hasValidAccount(final String host) {
        synchronized (hosteraccounts) {
            final ArrayList<Account> ret = hosteraccounts.get(host);
            if (ret != null) {
                for (final Account next : ret) {
                    if (!next.isTempDisabled() && next.isEnabled() && next.isValid()) { return true; }
                }
            }
        }
        return false;
    }

    public Account getValidAccount(final String host) {
        Account ret = null;
        synchronized (hosteraccounts) {
            final ArrayList<Account> accounts = new ArrayList<Account>(getAllAccounts(host));
            if (config.isUseAccountWithMostTrafficLeft()) {
                Collections.sort(accounts, COMPARE_MOST_TRAFFIC_LEFT);
            }
            // final int accountsSize = accounts.size();
            // for (int i = 0; i < accountsSize; i++) {
            // final Account next = accounts.get(i);
            for (final Account next : accounts) {
                if (!next.isTempDisabled() && next.isEnabled() && next.isValid() && !isAccountBlocked(next)) {
                    ret = next;
                    break;
                }
            }
        }
        return ret;
    }
}