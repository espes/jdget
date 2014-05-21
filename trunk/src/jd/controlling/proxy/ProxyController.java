package jd.controlling.proxy;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import jd.config.SubConfiguration;
import jd.controlling.accountchecker.AccountChecker.AccountCheckJob;
import jd.controlling.accountchecker.AccountCheckerThread;
import jd.controlling.downloadcontroller.AccountCache.CachedAccount;
import jd.controlling.downloadcontroller.DownloadLinkCandidate;
import jd.controlling.downloadcontroller.SingleDownloadController;
import jd.controlling.linkchecker.LinkCheckerThread;
import jd.controlling.linkcrawler.LinkCrawlerThread;
import jd.http.ClonedProxy;
import jd.http.ProxySelectorInterface;
import jd.http.Request;
import jd.nutils.encoding.Encoding;
import jd.plugins.Account;
import jd.plugins.Plugin;
import jd.plugins.PluginForHost;
import jd.plugins.hoster.DirectHTTP;
import jd.plugins.hoster.Ftp;

import org.appwork.scheduler.DelayedRunnable;
import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.shutdown.ShutdownRequest;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.IO;
import org.appwork.utils.StringUtils;
import org.appwork.utils.event.DefaultEventListener;
import org.appwork.utils.event.DefaultEventSender;
import org.appwork.utils.event.queue.Queue;
import org.appwork.utils.event.queue.QueueAction;
import org.appwork.utils.logging2.LogSource;
import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.net.httpconnection.HTTPProxy.TYPE;
import org.appwork.utils.net.httpconnection.ProxyAuthException;
import org.appwork.utils.net.httpconnection.ProxyConnectException;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.appwork.utils.swing.dialog.ProxyDialog;
import org.jdownloader.logging.LogController;
import org.jdownloader.translate._JDT;
import org.jdownloader.updatev2.InternetConnectionSettings;
import org.jdownloader.updatev2.ProxyClone;
import org.jdownloader.updatev2.ProxyData;

import com.btr.proxy.search.ProxySearchStrategy;
import com.btr.proxy.search.browser.firefox.FirefoxProxySearchStrategy;
import com.btr.proxy.search.desktop.DesktopProxySearchStrategy;
import com.btr.proxy.search.env.EnvProxySearchStrategy;
import com.btr.proxy.search.java.JavaProxySearchStrategy;
import com.btr.proxy.selector.pac.PacProxySelector;
import com.btr.proxy.selector.pac.PacScriptParser;
import com.btr.proxy.selector.pac.PacScriptSource;
import com.btr.proxy.selector.pac.UrlPacScriptSource;
import com.btr.proxy.selector.whitelist.ProxyBypassListSelector;
import com.btr.proxy.util.Logger;
import com.btr.proxy.util.Logger.LogBackEnd;
import com.btr.proxy.util.Logger.LogLevel;

public class ProxyController implements ProxySelectorInterface {

    private static final ProxyController INSTANCE = new ProxyController();

    public static final ProxyController getInstance() {
        return ProxyController.INSTANCE;
    }

    private volatile CopyOnWriteArrayList<AbstractProxySelectorImpl>        list  = new CopyOnWriteArrayList<AbstractProxySelectorImpl>();

    private final DefaultEventSender<ProxyEvent<AbstractProxySelectorImpl>> eventSender;

    private final InternetConnectionSettings                                config;

    private final LogSource                                                 logger;

    private final Queue                                                     QUEUE = new Queue(getClass().getName()) {

                                                                                      @Override
                                                                                      public void killQueue() {
                                                                                          LogController.CL().log(new Throwable("YOU CANNOT KILL ME!"));
                                                                                          /*
                                                                                           * this queue can't be killed
                                                                                           */
                                                                                      }

                                                                                  };

    public Queue getQUEUE() {
        return QUEUE;
    }

    private ProxyController() {
        this.eventSender = new DefaultEventSender<ProxyEvent<AbstractProxySelectorImpl>>();
        /* init needed configs */
        this.config = JsonConfig.create(InternetConnectionSettings.PATH, InternetConnectionSettings.class);
        this.logger = LogController.getInstance().getLogger(ProxyController.class.getName());
        if (Logger.getBackend() == null) {
            Logger.setBackend(new LogBackEnd() {

                @Override
                public void log(final Class<?> arg0, final LogLevel arg1, final String arg2, final Object... arg3) {
                    logger.log(Level.ALL, arg2, arg3);
                }

                @Override
                public boolean isLogginEnabled(final LogLevel arg0) {
                    return true;
                }
            });
        }
        list = new CopyOnWriteArrayList<AbstractProxySelectorImpl>(loadProxySettings(true));
        getEventSender().addListener(new DefaultEventListener<ProxyEvent<AbstractProxySelectorImpl>>() {
            final DelayedRunnable asyncSaving = new DelayedRunnable(5000l, 60000l) {
                                                  @Override
                                                  public void delayedrun() {
                                                      ProxyController.this.saveProxySettings();
                                                  }

                                                  @Override
                                                  public String getID() {
                                                      return "ProxyController";
                                                  }
                                              };

            @Override
            public void onEvent(final ProxyEvent<AbstractProxySelectorImpl> event) {
                asyncSaving.resetAndStart();
            }
        });
        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {

            @Override
            public void onShutdown(final ShutdownRequest shutdownRequest) {
                ProxyController.this.saveProxySettings();
            }

            @Override
            public String toString() {
                return "ProxyController: save config";
            }
        });
    }

    public void addProxy(final AbstractProxySelectorImpl proxy) {
        if (proxy != null) {
            QUEUE.add(new QueueAction<Void, RuntimeException>() {
                @Override
                protected Void run() throws RuntimeException {
                    switch (proxy.getType()) {
                    case HTTP:
                    case SOCKS4:
                    case SOCKS5:
                    case DIRECT:
                    case PAC:
                        CopyOnWriteArrayList<AbstractProxySelectorImpl> list = _getList();
                        if (list.addIfAbsent(proxy)) {
                            eventSender.fireEvent(new ProxyEvent<AbstractProxySelectorImpl>(ProxyController.this, ProxyEvent.Types.ADDED, proxy));
                        }
                        break;
                    default:
                        logger.info("Invalid Type " + proxy.getType());
                        break;
                    }
                    return null;
                }
            });
        }
    }

    public void setProxy(final AbstractProxySelectorImpl proxy) {
        if (proxy != null) {
            QUEUE.add(new QueueAction<Void, RuntimeException>() {
                @Override
                protected Void run() throws RuntimeException {
                    switch (proxy.getType()) {
                    case HTTP:
                    case SOCKS4:
                    case SOCKS5:
                    case DIRECT:
                    case PAC:
                        CopyOnWriteArrayList<AbstractProxySelectorImpl> list = _getList();
                        if (list.addIfAbsent(proxy)) {
                            AbstractProxySelectorImpl none = getNone();
                            if (proxy.isEnabled() && none != null && none.isEnabled()) {
                                setEnabled(none, false);
                            }
                            eventSender.fireEvent(new ProxyEvent<AbstractProxySelectorImpl>(ProxyController.this, ProxyEvent.Types.ADDED, proxy));
                        }
                        break;
                    default:
                        logger.info("Invalid Type " + proxy.getType());
                        break;
                    }
                    return null;
                }
            });
        }
    }

    public void move(final List<AbstractProxySelectorImpl> transferData, final int dropRow) {
        if (transferData != null && transferData.size() > 0) {
            QUEUE.add(new QueueAction<Void, RuntimeException>() {
                @Override
                protected Void run() throws RuntimeException {
                    final List<AbstractProxySelectorImpl> list = _getList();
                    int dropRowIndex = dropRow;
                    if (dropRowIndex < 0) {
                        dropRowIndex = 0;
                    } else if (dropRowIndex > list.size()) {
                        dropRowIndex = list.size();
                    }
                    final List<AbstractProxySelectorImpl> newList = new ArrayList<AbstractProxySelectorImpl>();
                    final List<AbstractProxySelectorImpl> before = new ArrayList<AbstractProxySelectorImpl>(list.subList(0, dropRowIndex));
                    final List<AbstractProxySelectorImpl> after = new ArrayList<AbstractProxySelectorImpl>(list.subList(dropRowIndex, list.size()));
                    before.removeAll(transferData);
                    after.removeAll(transferData);
                    newList.addAll(before);
                    newList.addAll(transferData);
                    newList.addAll(after);
                    setList(newList);
                    return null;
                }
            });
        }
    }

    private void setList(final List<AbstractProxySelectorImpl> newList) {
        QUEUE.add(new QueueAction<Void, RuntimeException>() {
            @Override
            protected Void run() throws RuntimeException {
                list = new CopyOnWriteArrayList<AbstractProxySelectorImpl>(newList);
                eventSender.fireEvent(new ProxyEvent<AbstractProxySelectorImpl>(ProxyController.this, ProxyEvent.Types.REFRESH, null));
                return null;
            }
        });
    }

    public List<AbstractProxySelectorImpl> getPossibleProxies(DownloadLinkCandidate downloadLinkCandidate) {
        List<AbstractProxySelectorImpl> ret = getPossibleProxies(downloadLinkCandidate, false, false);
        if (ret == null || ret.size() == 0) {
            ret = getPossibleProxies(downloadLinkCandidate, true, false);
        }
        if (ret == null || ret.size() == 0) {
            ret = getPossibleProxies(downloadLinkCandidate, true, true);
        }
        return ret;
    }

    public List<AbstractProxySelectorImpl> getPossibleProxies(final DownloadLinkCandidate downloadLinkCandidate, final boolean ignoreConnectBans, final boolean ignoreAllBans) {
        final LinkedHashSet<AbstractProxySelectorImpl> ret = new LinkedHashSet<AbstractProxySelectorImpl>();
        try {
            final CachedAccount cachedAccount = downloadLinkCandidate.getCachedAccount();
            final PluginForHost pluginForHost = cachedAccount.getPlugin();
            final int maxActive = pluginForHost.getMaxSimultanDownload(downloadLinkCandidate.getLink(), cachedAccount.getAccount());
            final String pluginHost;
            if ((pluginForHost instanceof DirectHTTP) || (pluginForHost instanceof Ftp)) {
                pluginHost = downloadLinkCandidate.getLink().getDomainInfo().getTld();
            } else {
                pluginHost = pluginForHost.getHost();
            }
            int maxResults;
            if (pluginForHost.isProxyRotationEnabled(cachedAccount.getAccount() != null)) {
                maxResults = Integer.MAX_VALUE;
            } else {
                maxResults = 1;
            }
            for (final AbstractProxySelectorImpl selector : _getList()) {
                try {
                    if (selector.isEnabled() && selector.isAllowedByFilter(pluginHost) && maxResults-- > 0) {
                        if (ignoreAllBans || !selector.isSelectorBannedFor(pluginForHost, ignoreConnectBans)) {
                            final int active = selector.countActive(downloadLinkCandidate);
                            if (active < maxActive) {
                                ret.add(selector);
                            }
                        }
                    }
                } catch (Throwable e) {
                    LogController.getRebirthLogger(logger).log(e);
                }
            }
        } catch (Throwable e) {
            LogController.getRebirthLogger(logger).log(e);
        }
        return new ArrayList<AbstractProxySelectorImpl>(ret);
    }

    private Plugin getPluginFromThread() {
        Thread thread = Thread.currentThread();
        if (thread instanceof AccountCheckerThread) {
            AccountCheckJob job = ((AccountCheckerThread) thread).getJob();
            if (job != null) {
                Account account = job.getAccount();
                return account.getPlugin();
            }
        } else if (thread instanceof LinkCheckerThread) {
            PluginForHost plg = ((LinkCheckerThread) thread).getPlugin();
            if (plg != null) {
                return plg;
            }
        } else if (thread instanceof SingleDownloadController) {
            return ((SingleDownloadController) thread).getDownloadLinkCandidate().getCachedAccount().getPlugin();
        } else if (thread instanceof LinkCrawlerThread) {
            Object owner = ((LinkCrawlerThread) thread).getCurrentOwner();
            if (owner instanceof Plugin) {
                return (Plugin) owner;
            }
        }
        return null;
    }

    public void exportTo(final File saveTo) throws UnsupportedEncodingException, IOException {
        final ProxyExportImport save = new ProxyExportImport();
        final ArrayList<ProxyData> ret = new ArrayList<ProxyData>();
        for (AbstractProxySelectorImpl sel : getList()) {
            switch (sel.getType()) {
            case HTTP:
            case SOCKS4:
            case SOCKS5:
            case NONE:
            case DIRECT:
                ret.add(sel.toProxyData());
                break;
            case PAC:
                if (((PacProxySelectorImpl) sel).getPACUrl().startsWith("pac://")) {
                    ProxyData pd = sel.toProxyData();
                    pd.getProxy().setAddress("pac://" + Encoding.urlEncode(config.getLocalPacScript()));
                    ret.add(pd);
                } else {
                    ret.add(sel.toProxyData());
                }
            }
        }
        save.setCustomProxyList(ret);
        IO.secureWrite(saveTo, JSonStorage.serializeToJson(save).getBytes("UTF-8"));
    }

    private CopyOnWriteArrayList<AbstractProxySelectorImpl> _getList() {
        return list;
    }

    /**
     * returns a copy of current proxy list
     * 
     * @return
     */
    public List<AbstractProxySelectorImpl> getList() {
        return Collections.unmodifiableList(new ArrayList<AbstractProxySelectorImpl>(_getList()));
    }

    private NoProxySelector getNone() {
        final NoProxySelector none = new NoProxySelector();
        for (AbstractProxySelectorImpl proxy : _getList()) {
            if (none.equals(proxy)) {
                return (NoProxySelector) proxy;
            }
        }
        addProxy(none);
        return none;
    }

    public void importFrom(final File selected) throws IOException {
        final ProxyExportImport restore = JSonStorage.restoreFromString(IO.readFileToString(selected), new TypeRef<ProxyExportImport>() {
        });
        QUEUE.add(new QueueAction<Void, RuntimeException>() {

            @Override
            protected Void run() throws RuntimeException {
                config.setLatestProfile(selected.getAbsolutePath());
                config.setCustomProxyList(restore.getCustomProxyList());
                setList(loadProxySettings(false));
                return null;
            }
        });
    }

    public DefaultEventSender<ProxyEvent<AbstractProxySelectorImpl>> getEventSender() {
        return this.eventSender;
    }

    public String getLatestProfilePath() {
        return this.config.getLatestProfile();
    }

    private List<AbstractProxySelectorImpl> loadProxySettings(final boolean allowInit) {
        final LinkedHashSet<AbstractProxySelectorImpl> proxies = new LinkedHashSet<AbstractProxySelectorImpl>();
        PacProxySelectorImpl advancedCondigPac = null;
        logger.info("Load Proxy Settings");
        final List<ProxyData> cfgProxies = config.getCustomProxyList();
        logger.info("Customs: " + (cfgProxies == null ? 0 : cfgProxies.size()));
        boolean noPSFirst = false;
        if (cfgProxies != null) {
            for (final ProxyData proxyData : cfgProxies) {
                try {
                    AbstractProxySelectorImpl proxy = null;
                    if (proxyData.isPac()) {
                        proxy = new PacProxySelectorImpl(proxyData);
                        if (StringUtils.equalsIgnoreCase(proxyData.getProxy().getAddress(), "pac://")) {
                            advancedCondigPac = (PacProxySelectorImpl) proxy;
                        }
                    } else {
                        switch (proxyData.getProxy().getType()) {
                        case DIRECT:
                            proxy = new SingleDirectGatewaySelector(proxyData);
                            break;
                        case HTTP:
                        case SOCKS4:
                        case SOCKS5:
                            proxy = new SingleBasicProxySelectorImpl(proxyData);
                            break;
                        case NONE:
                            proxy = new NoProxySelector(proxyData);
                            break;
                        default:
                            continue;
                        }
                    }
                    if (proxy != null && proxies.add(proxy)) {
                        proxies.add(proxy);
                    }
                } catch (final Throwable e) {
                    logger.log(e);
                }
            }
        } else if (allowInit) {
            noPSFirst = true;
            logger.info("Init Proxy Controller fresh");
            final ArrayList<ProxySearchStrategy> strategies = new ArrayList<ProxySearchStrategy>();
            strategies.add(new DesktopProxySearchStrategy());
            strategies.add(new FirefoxProxySearchStrategy());
            strategies.add(new EnvProxySearchStrategy());
            strategies.add(new JavaProxySearchStrategy());
            for (ProxySearchStrategy s : strategies) {
                logger.info("Selector: " + s);
                try {
                    ProxySelector selector = s.getProxySelector();
                    if (selector == null) {
                        continue;
                    }
                    if (selector instanceof ProxyBypassListSelector) {
                        Field field = ProxyBypassListSelector.class.getDeclaredField("delegate");
                        field.setAccessible(true);
                        selector = (ProxySelector) field.get(selector);
                    }
                    if (selector instanceof PacProxySelector) {
                        Field field = PacProxySelector.class.getDeclaredField("pacScriptParser");
                        field.setAccessible(true);
                        PacScriptParser source = (PacScriptParser) field.get(selector);
                        PacScriptSource pacSource = source.getScriptSource();
                        if (pacSource != null && pacSource instanceof UrlPacScriptSource) {
                            field = UrlPacScriptSource.class.getDeclaredField("scriptUrl");
                            field.setAccessible(true);
                            Object pacURL = field.get(pacSource);
                            if (StringUtils.isNotEmpty((String) pacURL)) {
                                PacProxySelectorImpl pac = new PacProxySelectorImpl((String) pacURL, null, null);
                                if (proxies.add(pac)) {
                                    logger.info("Add pac: " + pacURL);
                                }
                            }
                        }
                    } else {
                        List<Proxy> sproxies = selector.select(new URI("http://google.com"));
                        if (sproxies != null) {
                            for (Proxy p : sproxies) {
                                HTTPProxy httpProxy = null;
                                switch (p.type()) {
                                case DIRECT:
                                    if (p.address() == null) {
                                        if (proxies.add(new NoProxySelector())) {
                                            logger.info("Add None");
                                        }
                                    } else {
                                        httpProxy = new HTTPProxy(((InetSocketAddress) p.address()).getAddress());
                                        SingleDirectGatewaySelector direct = new SingleDirectGatewaySelector(httpProxy);
                                        if (proxies.add(direct)) {
                                            logger.info("Add Direct: " + direct);
                                        }
                                    }
                                    break;
                                case HTTP:
                                    httpProxy = new HTTPProxy(TYPE.HTTP, ((InetSocketAddress) p.address()).getHostString(), ((InetSocketAddress) p.address()).getPort());
                                    final SingleBasicProxySelectorImpl basic = new SingleBasicProxySelectorImpl(httpProxy);
                                    if (proxies.add(basic)) {
                                        logger.info("Add Basic: " + basic);
                                    }
                                    break;
                                case SOCKS:
                                    httpProxy = new HTTPProxy(TYPE.SOCKS5, ((InetSocketAddress) p.address()).getHostString(), ((InetSocketAddress) p.address()).getPort());
                                    final SingleBasicProxySelectorImpl socks = new SingleBasicProxySelectorImpl(httpProxy);
                                    if (proxies.add(socks)) {
                                        logger.info("Add Socks: " + socks);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                } catch (Throwable e) {
                    logger.log(e);
                }
                /* convert from old system */
                final List<HTTPProxy> reto = restoreFromOldConfig();
                for (final HTTPProxy proxyData : reto) {
                    try {
                        switch (proxyData.getType()) {
                        case NONE:
                            final NoProxySelector none = new NoProxySelector(proxyData);
                            if (proxies.add(none)) {
                                logger.info("Restore None: " + none);
                            }
                            break;
                        case DIRECT:
                            final SingleDirectGatewaySelector direct = new SingleDirectGatewaySelector(proxyData);
                            if (proxies.add(direct)) {
                                logger.info("Restore Direct: " + direct);
                            }
                            break;
                        case HTTP:
                            final SingleBasicProxySelectorImpl basic = new SingleBasicProxySelectorImpl(proxyData);
                            if (proxies.add(basic)) {
                                logger.info("Restore Basic: " + basic);
                            }
                            break;
                        case SOCKS4:
                        case SOCKS5:
                            final SingleBasicProxySelectorImpl socks = new SingleBasicProxySelectorImpl(proxyData);
                            if (proxies.add(socks)) {
                                logger.info("Restore Soskcs: " + socks);
                            }
                            break;

                        default:
                            continue;
                        }
                    } catch (final Throwable e) {
                        logger.log(e);
                    }
                }
                /* import proxies from system properties */
                final List<HTTPProxy> sproxy = HTTPProxy.getFromSystemProperties();
                for (final HTTPProxy proxyData : sproxy) {
                    try {
                        SingleBasicProxySelectorImpl proxy = new SingleBasicProxySelectorImpl(proxyData);
                        if (proxies.add(proxy)) {
                            logger.info("Add System Proxy: " + proxy);
                        }
                    } catch (final Throwable e) {
                        logger.log(e);
                    }
                }
            }
        }
        if (StringUtils.isNotEmpty(config.getLocalPacScript()) && advancedCondigPac == null) {
            advancedCondigPac = new PacProxySelectorImpl("pac://", null, null);
            proxies.add(advancedCondigPac);
        }
        ArrayList<AbstractProxySelectorImpl> ret = new ArrayList<AbstractProxySelectorImpl>(proxies);
        /* check for enabled proxySelector */
        boolean enabled = false;
        for (AbstractProxySelectorImpl proxy : ret) {
            if (proxy.isEnabled()) {
                enabled = true;
                break;
            }
        }
        AbstractProxySelectorImpl noPS = new NoProxySelector();
        if (enabled == false || !ret.contains(noPS)) {
            /* no enabled proxySelector or missing NoProxySelector */
            final int indexNoPS = ret.indexOf(noPS);
            if (indexNoPS >= 0) {
                noPS = ret.get(indexNoPS);
                noPSFirst = noPSFirst && indexNoPS > 0;
            } else {
                ret.add(0, noPS);
                noPSFirst = false;
            }
            if (enabled == false) {
                noPS.setEnabled(true);
            }
        }
        if (noPSFirst) {
            ret.remove(noPS);
            ret.add(0, noPS);
        }
        return ret;
    }

    public void remove(final AbstractProxySelectorImpl proxy) {
        if (proxy != null) {
            QUEUE.add(new QueueAction<Void, RuntimeException>() {
                @Override
                protected Void run() throws RuntimeException {
                    if (_getList().remove(proxy)) {
                        validateGateways();
                        eventSender.fireEvent(new ProxyEvent<AbstractProxySelectorImpl>(ProxyController.this, ProxyEvent.Types.REMOVED, proxy));
                    }
                    return null;
                }
            });
        }
    }

    private List<HTTPProxy> restoreFromOldConfig() {
        final java.util.List<HTTPProxy> ret = new ArrayList<HTTPProxy>();
        try {
            final SubConfiguration oldConfig = SubConfiguration.getConfig("DOWNLOAD", true);
            if (oldConfig.getBooleanProperty("USE_PROXY", false)) {
                /* import old http proxy settings */
                final String host = oldConfig.getStringProperty("PROXY_HOST", "");
                final int port = oldConfig.getIntegerProperty("PROXY_PORT", 8080);
                final String user = oldConfig.getStringProperty("PROXY_USER", "");
                final String pass = oldConfig.getStringProperty("PROXY_PASS", "");
                if (!StringUtils.isEmpty(host)) {
                    final HTTPProxy pr = new HTTPProxy(HTTPProxy.TYPE.HTTP, host, port);
                    if (!StringUtils.isEmpty(user)) {
                        pr.setUser(user);
                    }
                    if (!StringUtils.isEmpty(pass)) {
                        pr.setPass(pass);
                    }
                    ret.add(pr);
                }
            }
            if (oldConfig.getBooleanProperty("USE_SOCKS", false)) {
                /* import old socks5 settings */
                final String user = oldConfig.getStringProperty("PROXY_USER_SOCKS", "");
                final String pass = oldConfig.getStringProperty("PROXY_PASS_SOCKS", "");
                final String host = oldConfig.getStringProperty("SOCKS_HOST", "");
                final int port = oldConfig.getIntegerProperty("SOCKS_PORT", 1080);
                if (!StringUtils.isEmpty(host)) {
                    final HTTPProxy pr = new HTTPProxy(HTTPProxy.TYPE.SOCKS5, host, port);
                    if (!StringUtils.isEmpty(user)) {
                        pr.setUser(user);
                    }
                    if (!StringUtils.isEmpty(pass)) {
                        pr.setPass(pass);
                    }
                    ret.add(pr);
                }
            }
        } catch (final Throwable e) {
            logger.log(e);
        }
        return ret;
    }

    private void saveProxySettings() {
        List<AbstractProxySelectorImpl> list = getList();
        if (list.size() > 0) {
            final ArrayList<ProxyData> ret = new ArrayList<ProxyData>();
            for (final AbstractProxySelectorImpl proxy : list) {
                final ProxyData pd = proxy.toProxyData();
                ret.add(pd);
            }
            this.config.setCustomProxyList(ret);
            this.config._getStorageHandler().write();
        }
    }

    public boolean updateProxy(final AbstractProxySelectorImpl selector, final Request request, final int retryCounter) {
        try {
            final SelectedProxy selectedProxy = getSelectedProxy(request.getProxy());
            if (selectedProxy != null && selector == selectedProxy.getSelector()) {
                final List<String> proxyAuths = request.getHttpConnection().getHeaderFields("proxy-authenticate");
                return updateProxy(selectedProxy, request.getProxy(), proxyAuths, new URL(request.getUrl()), retryCounter);
            }
        } catch (Throwable e) {
            LogController.getRebirthLogger(logger).log(e);
        }
        return false;
    }

    private boolean askForProxyAuth(final SelectedProxy selectedProxy, final int flags, final boolean typeEditable, final URL url, final String msg, final String title) {
        final AbstractProxySelectorImpl selector = selectedProxy.selector;
        final Plugin plugin = getPluginFromThread();
        if (selector.isProxyBannedFor(selectedProxy, url, plugin, false) == false) {
            HTTPProxy proxy = null;
            boolean rememberCheckBox = false;
            try {
                final ProxyDialog pd = new ProxyDialog(selectedProxy, msg) {
                    {
                        this.flagMask |= flags;
                    }

                    @Override
                    protected boolean isShowRemember() {
                        return true;
                    }

                    @Override
                    public void pack() {
                        getDialog().setMinimumSize(new Dimension(450, getPreferredSize().height));
                        super.pack();
                    }
                };
                pd.setTimeout(5 * 60 * 1000);
                pd.setAuthRequired(true);
                pd.setTypeEditable(typeEditable);
                pd.setHostEditable(typeEditable);
                pd.setPortEditable(typeEditable);
                pd.setTitle(title);
                proxy = Dialog.getInstance().showDialog(pd);
                rememberCheckBox = pd.isRememberChecked();
            } catch (DialogNoAnswerException e) {
                proxy = null;
            }
            final String userName;
            final String passWord;
            if (proxy != null) {
                userName = proxy.getUser();
                passWord = proxy.getPass();
            } else {
                passWord = null;
                userName = null;
            }
            if (selector instanceof PacProxySelectorImpl) {
                final PacProxySelectorImpl pacProxySelector = (PacProxySelectorImpl) selector;
                if (proxy != null) {
                    if (rememberCheckBox) {
                        pacProxySelector.setUser(userName);
                        pacProxySelector.setPassword(passWord);
                    }
                    pacProxySelector.setTempAuth(selectedProxy, userName, passWord);
                    return true;
                } else {
                    pacProxySelector.setTempAuth(selectedProxy, userName, passWord);
                }
            } else if (selector instanceof SingleBasicProxySelectorImpl) {
                final SingleBasicProxySelectorImpl singleBasic = (SingleBasicProxySelectorImpl) selector;
                if (proxy != null) {
                    if (rememberCheckBox) {
                        singleBasic.setUser(userName);
                        singleBasic.setPassword(passWord);

                    }
                    singleBasic.setTempAuth(userName, passWord);
                    return true;
                } else {
                    singleBasic.setTempAuth(null, null);
                }
            }
            if (proxy == null) {
                if (plugin != null) {
                    selector.addSessionBan(new PluginRelatedConnectionBan(plugin, selector, selectedProxy));
                } else {
                    selector.addSessionBan(new AuthExceptionGenericBan(selector, selectedProxy, url));
                }
            }
        }
        return false;
    }

    private class SelectedProxyLock {
        private final SelectedProxy selectedProxy;
        private volatile int        counter = 0;

        private SelectedProxyLock(SelectedProxy selectedProxy) {
            this.selectedProxy = selectedProxy;
        }

        private int incrementAndGet() {
            counter += 1;
            return counter;
        }

        private int decrementAndGet() {
            counter -= 1;
            return counter;
        }

    }

    private final HashMap<SelectedProxy, SelectedProxyLock> LOCKS = new HashMap<SelectedProxy, SelectedProxyLock>();

    private synchronized SelectedProxyLock requestLock(final SelectedProxy selectedProxy) {
        SelectedProxyLock lockObject = LOCKS.get(selectedProxy);
        if (lockObject == null) {
            lockObject = new SelectedProxyLock(selectedProxy);
            LOCKS.put(selectedProxy, lockObject);
        }
        lockObject.incrementAndGet();
        return lockObject;
    }

    private synchronized void unLock(final SelectedProxyLock lockObject) {
        final SelectedProxyLock selectedProxyLock = LOCKS.get(lockObject.selectedProxy);
        if (selectedProxyLock != null) {
            if (selectedProxyLock.decrementAndGet() == 0) {
                LOCKS.remove(selectedProxyLock.selectedProxy);
            }
        }
    }

    public boolean updateProxy(final SelectedProxy selectedProxy, final HTTPProxy proxy, final List<String> proxyAuths, final URL url, final int retryCounter) {
        try {
            final SelectedProxyLock lockObject = requestLock(selectedProxy);
            synchronized (lockObject) {
                try {
                    if (proxy != null && !proxy.equalsWithSettings(selectedProxy)) {
                        return true;
                    } else if (retryCounter < 10) {
                        if (proxy != null && proxyAuths != null) {
                            for (final String authMethod : proxyAuths) {
                                if ("NTLM".equalsIgnoreCase(authMethod)) {
                                    if (selectedProxy.isPreferNativeImplementation() == false) {
                                        /* enable nativeImplementation for NTLM */
                                        selectedProxy.setPreferNativeImplementation(true);
                                        return true;
                                    }
                                    break;
                                }
                            }
                        }
                        boolean ret = this.askForProxyAuth(selectedProxy, 0, false, url, _JDT._.ProxyController_updateProxy_proxy_auth_required_msg(url.getHost()), _JDT._.ProxyController_updateProxy_proxy_auth_required_title());
                        if (ret && proxy != null && proxy.equalsWithSettings(selectedProxy)) {
                            ret = false;
                        }
                        return ret;
                    }
                } finally {
                    unLock(lockObject);
                }
            }
        } catch (final Throwable e) {
            LogController.getRebirthLogger(logger).log(e);
        }
        return false;
    }

    public HTTPProxy updateProxyAuthForUpdater(int retries, final HTTPProxy proxy, final List<String> proxyAuths, final URL url) {
        try {
            final SelectedProxy selectedProxy = getSelectedProxy(proxy);
            if (retries < 10 && selectedProxy != null) {
                while (this.updateProxy(selectedProxy, proxy, proxyAuths, url, retries)) {
                    if (!selectedProxy.equals(proxy)) {
                        // there has been an update and the orgref changed. return the changed proxy
                        return new ProxyClone(selectedProxy);
                    } else if (retries++ > 10) {
                        return null;
                    }
                }
            }
        } catch (final Throwable e) {
            LogController.getRebirthLogger(logger).log(e);
        }
        return null;

    }

    public List<HTTPProxy> getProxiesForUpdater(final URL url) {
        List<HTTPProxy> ret = getProxiesForUpdater(url, false, false);
        if (ret == null || ret.size() == 0) {
            ret = getProxiesForUpdater(url, true, false);
        }
        if (ret == null || ret.size() == 0) {
            ret = getProxiesForUpdater(url, true, true);
        }
        if (ret == null || ret.size() == 0) {
            final HTTPProxy noneFallBack = getNone().getProxy();
            if (noneFallBack != null) {
                if (ret == null) {
                    ret = new ArrayList<HTTPProxy>();
                }
                ret.add(noneFallBack);
            }
        }
        return ret;
    }

    /**
     * Used by the updatesystem. it returnes all "NOT banned" proxies for the given url.
     * 
     * @param url
     * @return
     */
    private List<HTTPProxy> getProxiesForUpdater(final URL url, final boolean ignoreConnectionBans, final boolean ignoreAllBans) {
        final LinkedHashSet<HTTPProxy> ret = new LinkedHashSet<HTTPProxy>();
        try {
            final String host = url.getHost();
            final String urlString = url.toString();
            final Plugin plugin = getPluginFromThread();
            for (final AbstractProxySelectorImpl selector : _getList()) {
                try {
                    if (selector.isEnabled() && selector.isAllowedByFilter(host)) {
                        final List<HTTPProxy> lst = selector.getProxiesByUrl(urlString);
                        if (lst != null) {
                            for (HTTPProxy p : lst) {
                                if (ignoreAllBans || !selector.isProxyBannedFor(p, url, plugin, ignoreConnectionBans)) {
                                    ret.add(p);
                                }
                            }
                        }
                    }
                } catch (Throwable e) {
                    LogController.getRebirthLogger(logger).log(e);
                }
            }
        } catch (Throwable e) {
            LogController.getRebirthLogger(logger).log(e);
        }
        return new ArrayList<HTTPProxy>(ret);
    }

    /**
     * default proxy selector. This method ignores the banned information. it will always return the same proxy order, including banned
     * proxies.
     * 
     * @param url
     * @return
     */
    @Override
    public List<HTTPProxy> getProxiesByUrl(final String urlString) {
        List<HTTPProxy> ret = getProxiesByUrl(urlString, false, false);
        if (ret == null || ret.size() == 0) {
            ret = getProxiesByUrl(urlString, true, false);
        }
        if (ret == null || ret.size() == 0) {
            ret = getProxiesByUrl(urlString, true, true);
        }
        return ret;
    }

    public List<HTTPProxy> getProxiesByUrl(final String urlString, final boolean ignoreConnectionBans, final boolean ignoreAllBans) {
        final Plugin plugin = getPluginFromThread();
        final boolean proxyRotationEnabled;
        final Thread thread = Thread.currentThread();
        if (plugin != null) {
            if (thread instanceof AccountCheckerThread) {
                proxyRotationEnabled = plugin.isProxyRotationEnabled(true);
            } else if (thread instanceof LinkCheckerThread) {
                proxyRotationEnabled = ((PluginForHost) plugin).isProxyRotationEnabledForLinkChecker();
            } else if (thread instanceof SingleDownloadController && ((SingleDownloadController) thread).getDownloadLinkCandidate().getCachedAccount().getAccount() != null) {
                proxyRotationEnabled = plugin.isProxyRotationEnabled(true);
            } else if (thread instanceof LinkCrawlerThread) {
                proxyRotationEnabled = plugin.isProxyRotationEnabledForLinkCrawler();
            } else {
                proxyRotationEnabled = plugin.isProxyRotationEnabled(false);
            }
        } else {
            proxyRotationEnabled = true;
        }
        final LinkedHashSet<HTTPProxy> ret = new LinkedHashSet<HTTPProxy>();
        try {
            final URL url = new URL(urlString);
            final String host = url.getHost();
            final String plgHost;
            if (plugin == null || (plugin instanceof DirectHTTP) || (plugin instanceof Ftp)) {
                plgHost = host;
            } else {
                plgHost = plugin.getHost();
            }
            for (final AbstractProxySelectorImpl selector : _getList()) {
                try {
                    if (selector.isEnabled() && selector.isAllowedByFilter(plgHost)) {
                        final List<HTTPProxy> lst = selector.getProxiesByUrl(urlString);
                        if (lst != null) {
                            for (HTTPProxy p : lst) {
                                if (ignoreAllBans || !selector.isProxyBannedFor(p, url, plugin, ignoreConnectionBans)) {
                                    ret.add(p);
                                } else if (!proxyRotationEnabled) {
                                    return new ArrayList<HTTPProxy>(ret);
                                }
                            }
                        }
                    }
                } catch (Throwable e) {
                    LogController.getRebirthLogger(logger).log(e);
                }
            }
        } catch (Throwable e) {
            LogController.getRebirthLogger(logger).log(e);
        }
        return new ArrayList<HTTPProxy>(ret);
    }

    @Override
    public boolean updateProxy(final Request request, final int retryCounter) {
        SelectedProxy selectedProxy = getSelectedProxy(request.getProxy());
        return selectedProxy != null && updateProxy(selectedProxy.getSelector(), request, retryCounter);
    }

    public static SelectedProxy getSelectedProxy(final HTTPProxy proxy) {
        if (proxy != null) {
            if (proxy instanceof SelectedProxy) {
                return (SelectedProxy) proxy;
            } else if (proxy instanceof ClonedProxy && ((ClonedProxy) proxy).getParent() instanceof SelectedProxy) {
                return (SelectedProxy) ((ClonedProxy) proxy).getParent();
            } else if (proxy instanceof ProxyClone && ((ProxyClone) proxy).getOrgReference() instanceof SelectedProxy) {
                return (SelectedProxy) ((ProxyClone) proxy).getOrgReference();
            }
        }
        return null;
    }

    public void setEnabled(final AbstractProxySelectorImpl proxy, final boolean value) {
        if (proxy != null) {
            QUEUE.add(new QueueAction<Void, RuntimeException>() {
                @Override
                protected Void run() throws RuntimeException {
                    if (proxy.setEnabled(value) != value) {
                        if (value == false) {
                            validateGateways();
                        }
                        eventSender.fireEvent(new ProxyEvent<AbstractProxySelectorImpl>(ProxyController.this, ProxyEvent.Types.REFRESH, null));
                    }
                    return null;
                }
            });
        }
    }

    private void validateGateways() {
        QUEUE.add(new QueueAction<Void, RuntimeException>() {
            @Override
            protected Void run() throws RuntimeException {
                for (final AbstractProxySelectorImpl pi : _getList()) {
                    if (pi.isEnabled()) {
                        return null;
                    }
                }
                AbstractProxySelectorImpl none = getNone();
                setEnabled(none, true);
                return null;
            }
        });

    }

    @Override
    public boolean reportConnectException(Request request, int retryCounter, IOException e) {
        try {
            if (e instanceof ProxyAuthException) {
                // we handle this
                return false;
            } else if (e instanceof ProxyConnectException) {
                final SelectedProxy selectedProxy = getSelectedProxy(request.getProxy());
                if (selectedProxy != null && selectedProxy.getSelector() != null) {
                    AbstractProxySelectorImpl selector = selectedProxy.getSelector();
                    Plugin plg = getPluginFromThread();
                    if (plg != null) {
                        selector.addSessionBan(new ConnectExceptionInPluginBan(plg, selector, selectedProxy));
                    } else {
                        selector.addSessionBan(new GenericConnectExceptionBan(selector, selectedProxy, new URL(request.getUrl())));
                    }
                }
            }
        } catch (Throwable e1) {
            LogController.getRebirthLogger(logger).log(e1);
        }
        return false;
    }
}
