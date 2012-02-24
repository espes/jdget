package jd.controlling.proxy;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import jd.controlling.IOEQ;
import jd.controlling.JSonWrapper;
import jd.plugins.Account;
import jd.plugins.DownloadLink;
import jd.plugins.PluginForHost;

import org.appwork.scheduler.DelayedRunnable;
import org.appwork.shutdown.ShutdownController;
import org.appwork.shutdown.ShutdownEvent;
import org.appwork.storage.config.JsonConfig;
import org.appwork.update.updateclient.UpdateHttpClientOptions;
import org.appwork.update.updateclient.UpdaterConstants;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.event.DefaultEventListener;
import org.appwork.utils.event.DefaultEventSender;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.appwork.utils.net.httpconnection.HTTPProxyStorable;
import org.appwork.utils.net.httpconnection.HTTPProxyUtils;
import org.appwork.utils.os.CrossSystem;
import org.jdownloader.settings.GeneralSettings;
import org.jdownloader.settings.InternetConnectionSettings;

public class ProxyController {

    private static final ProxyController              INSTANCE      = new ProxyController();

    private ArrayList<ProxyInfo>                      proxies       = new ArrayList<ProxyInfo>();
    private ArrayList<ProxyInfo>                      directs       = new ArrayList<ProxyInfo>();
    private ProxyInfo                                 defaultproxy  = null;
    private ProxyInfo                                 none          = null;

    private DefaultEventSender<ProxyEvent<ProxyInfo>> eventSender   = null;

    private InternetConnectionSettings                config;

    private UpdateHttpClientOptions                   updaterConfig = null;

    private GeneralSettings                           generalConfig = null;

    private final Object                              LOCK          = new Object();

    public static final ProxyController getInstance() {
        return INSTANCE;
    }

    public DefaultEventSender<ProxyEvent<ProxyInfo>> getEventSender() {
        return eventSender;
    }

    private ProxyController() {
        eventSender = new DefaultEventSender<ProxyEvent<ProxyInfo>>();
        /* init needed configs */
        config = JsonConfig.create(InternetConnectionSettings.class);
        generalConfig = JsonConfig.create(GeneralSettings.class);
        updaterConfig = JsonConfig.create(UpdateHttpClientOptions.class);
        /* init our NONE proxy */
        none = new ProxyInfo(HTTPProxy.NONE);
        loadProxySettings();
        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {

            @Override
            public void run() {
                saveProxySettings();
            }

            @Override
            public String toString() {
                return "ProxyController: save config";
            }
        });

        ShutdownController.getInstance().addShutdownEvent(new ShutdownEvent() {

            @Override
            public void run() {
                exportUpdaterConfig();
            }

            @Override
            public String toString() {
                return "ProxyController: export important settings to updaterConfig";
            }
        });
        eventSender.addListener(new DefaultEventListener<ProxyEvent<ProxyInfo>>() {
            DelayedRunnable asyncSaving = new DelayedRunnable(IOEQ.TIMINGQUEUE, 5000l, 60000l) {

                                            @Override
                                            public void delayedrun() {
                                                saveProxySettings();
                                            }

                                        };

            public void onEvent(ProxyEvent<ProxyInfo> event) {
                asyncSaving.resetAndStart();
            }
        });
    }

    private void exportUpdaterConfig() {
        updaterConfig.setConnectTimeout(generalConfig.getHttpConnectTimeout());
        updaterConfig.setReadTimeout(generalConfig.getHttpReadTimeout());
        exportUpdaterProxy();
    }

    private void exportUpdaterProxy() {
        ProxyInfo ldefaultproxy = defaultproxy;
        if (ldefaultproxy != null && !ldefaultproxy.isNone()) {
            HTTPProxyStorable storable = HTTPProxy.getStorable(ldefaultproxy);
            updaterConfig.setProxy(storable);
        } else {
            updaterConfig.setProxy(null);
        }
    }

    public static List<HTTPProxy> autoConfig() {
        ArrayList<HTTPProxy> ret = new ArrayList<HTTPProxy>();
        try {
            if (CrossSystem.isWindows()) { return ProxyController.checkReg(); }
            /* we enable systemproxies to query them for a test getPage */
            System.setProperty("java.net.useSystemProxies", "true");
            List<Proxy> l = null;
            try {
                l = ProxySelector.getDefault().select(new URI("http://www.appwork.org"));
            } finally {
                System.setProperty("java.net.useSystemProxies", "false");
            }
            for (final Proxy p : l) {
                final SocketAddress ad = p.address();
                if (ad != null && ad instanceof InetSocketAddress) {
                    final InetSocketAddress isa = (InetSocketAddress) ad;
                    if (StringUtils.isEmpty(isa.getHostName())) {
                        continue;
                    }
                    switch (p.type()) {
                    case HTTP: {
                        HTTPProxy pd = new HTTPProxy(HTTPProxy.TYPE.HTTP);
                        pd.setHost(isa.getHostName());
                        pd.setPort(isa.getPort());
                        ret.add(pd);
                    }
                        break;
                    case SOCKS: {
                        HTTPProxy pd = new HTTPProxy(HTTPProxy.TYPE.SOCKS5);
                        pd.setHost(isa.getHostName());
                        pd.setPort(isa.getPort());
                        ret.add(pd);
                    }
                        break;
                    }
                }
            }
        } catch (final Throwable e1) {
            Log.exception(e1);
        }
        return ret;
    }

    private static byte[] toCstr(final String str) {
        final byte[] result = new byte[str.length() + 1];
        for (int i = 0; i < str.length(); i++) {
            result[i] = (byte) str.charAt(i);
        }
        result[str.length()] = 0;
        return result;
    }

    /**
     * Checks windows registry for proxy settings
     */
    private static List<HTTPProxy> checkReg() {
        ArrayList<HTTPProxy> ret = new ArrayList<HTTPProxy>();
        try {
            final Preferences userRoot = Preferences.userRoot();
            final Class<?> clz = userRoot.getClass();
            final Method openKey = clz.getDeclaredMethod("openKey", byte[].class, int.class, int.class);
            openKey.setAccessible(true);

            final Method closeKey = clz.getDeclaredMethod("closeKey", int.class);
            closeKey.setAccessible(true);
            final Method winRegQueryValue = clz.getDeclaredMethod("WindowsRegQueryValueEx", int.class, byte[].class);
            winRegQueryValue.setAccessible(true);

            byte[] valb = null;
            String val = null;
            String key = null;
            Integer handle = -1;

            // Query Internet Settings for Proxy
            key = "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
            try {
                handle = (Integer) openKey.invoke(userRoot, ProxyController.toCstr(key), 0x20019, 0x20019);
                valb = (byte[]) winRegQueryValue.invoke(userRoot, handle.intValue(), ProxyController.toCstr("ProxyServer"));
                val = valb != null ? new String(valb).trim() : null;
            } finally {
                closeKey.invoke(Preferences.userRoot(), handle);
            }
            if (val != null) {
                for (String vals : val.split(";")) {
                    /* parse ip */
                    String proxyurl = new Regex(vals, "(\\d+\\.\\d+\\.\\d+\\.\\d+)").getMatch(0);
                    if (proxyurl == null) {
                        /* parse domain name */
                        proxyurl = new Regex(vals, "=(.*?)($|:)").getMatch(0);
                    }
                    final String port = new Regex(vals, ":(\\d+)").getMatch(0);
                    if (proxyurl != null) {
                        if (vals.trim().contains("socks")) {
                            final int rPOrt = port != null ? Integer.parseInt(port) : 1080;
                            HTTPProxy pd = new HTTPProxy(HTTPProxy.TYPE.SOCKS5);
                            pd.setHost(proxyurl);
                            pd.setPort(rPOrt);
                            ret.add(pd);
                        } else {
                            final int rPOrt = port != null ? Integer.parseInt(port) : 8080;
                            HTTPProxy pd = new HTTPProxy(HTTPProxy.TYPE.HTTP);
                            pd.setHost(proxyurl);
                            pd.setPort(rPOrt);
                            ret.add(pd);
                        }
                    }
                }
            }
        } catch (final Throwable e) {
            Log.exception(e);
        }
        return ret;
    }

    private List<HTTPProxy> getAvailableDirects() {
        List<InetAddress> ips = HTTPProxyUtils.getLocalIPs();
        ArrayList<HTTPProxy> directs = new ArrayList<HTTPProxy>(ips.size());
        if (ips.size() > 1) {
            // we can use non if we have only one WAN ips anyway
            for (InetAddress ip : ips) {
                directs.add(new HTTPProxy(ip));
            }
        }
        return directs;
    }

    private void saveProxySettings() {
        ProxyInfo ldefaultproxy = defaultproxy;
        {
            /* use own scope */
            ArrayList<ProxyData> ret = new ArrayList<ProxyData>(proxies.size());
            ArrayList<ProxyInfo> lproxies = proxies;
            for (ProxyInfo proxy : lproxies) {
                ProxyData pd = proxy.toProxyData();
                pd.setDefaultProxy(proxy == ldefaultproxy);
                ret.add(pd);
            }
            config.setCustomProxyList(ret);
        }
        {
            /* use own scope */
            ArrayList<ProxyData> ret = new ArrayList<ProxyData>();
            ArrayList<ProxyInfo> ldirects = directs;
            for (ProxyInfo proxy : ldirects) {
                ProxyData pd = proxy.toProxyData();
                pd.setDefaultProxy(proxy == ldefaultproxy);
                ret.add(pd);
            }
            config.setDirectGatewayList(ret);
        }
        config.setNoneDefault(none == ldefaultproxy);
        config.setNoneRotationEnabled(none.isProxyRotationEnabled());
    }

    private List<HTTPProxy> restoreFromOldConfig() {
        ArrayList<HTTPProxy> ret = new ArrayList<HTTPProxy>();
        if (JSonWrapper.get("DOWNLOAD").getBooleanProperty(UpdaterConstants.USE_PROXY, false)) {
            /* import old http proxy settings */
            final String host = JSonWrapper.get("DOWNLOAD").getStringProperty(UpdaterConstants.PROXY_HOST, "");
            final int port = JSonWrapper.get("DOWNLOAD").getIntegerProperty(UpdaterConstants.PROXY_PORT, 8080);
            final String user = JSonWrapper.get("DOWNLOAD").getStringProperty(UpdaterConstants.PROXY_USER, "");
            final String pass = JSonWrapper.get("DOWNLOAD").getStringProperty(UpdaterConstants.PROXY_PASS, "");
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
        if (JSonWrapper.get("DOWNLOAD").getBooleanProperty(UpdaterConstants.USE_SOCKS, false)) {
            /* import old socks5 settings */
            final String user = JSonWrapper.get("DOWNLOAD").getStringProperty(UpdaterConstants.PROXY_USER_SOCKS, "");
            final String pass = JSonWrapper.get("DOWNLOAD").getStringProperty(UpdaterConstants.PROXY_PASS_SOCKS, "");
            final String host = JSonWrapper.get("DOWNLOAD").getStringProperty(UpdaterConstants.SOCKS_HOST, "");
            final int port = JSonWrapper.get("DOWNLOAD").getIntegerProperty(UpdaterConstants.SOCKS_PORT, 1080);
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
        return ret;
    }

    private void loadProxySettings() {
        ProxyInfo newDefaultProxy = null;
        boolean rotCheck = false;
        ArrayList<ProxyInfo> proxies = new ArrayList<ProxyInfo>();
        ArrayList<ProxyInfo> directs = new ArrayList<ProxyInfo>();
        ArrayList<HTTPProxy> dupeCheck = new ArrayList<HTTPProxy>();
        ProxyInfo proxy = null;
        {
            /* restore customs proxies */
            /* use own scope */
            ArrayList<ProxyData> ret = config.getCustomProxyList();
            if (ret != null) {
                /* config available */
                restore: for (ProxyData proxyData : ret) {
                    try {
                        proxy = new ProxyInfo(proxyData);
                        for (HTTPProxy p : dupeCheck) {
                            if (p.sameProxy(proxy)) {
                                /* proxy already got restored */
                                continue restore;
                            }
                        }
                        dupeCheck.add(proxy);
                        proxies.add(proxy);
                        if (proxyData.isDefaultProxy()) {
                            newDefaultProxy = proxy;
                        }
                        if (proxy.isProxyRotationEnabled()) rotCheck = true;
                    } catch (final Throwable e) {
                        Log.exception(e);
                    }
                }
            } else {
                /* convert from old system */
                List<HTTPProxy> reto = restoreFromOldConfig();
                restore: for (HTTPProxy proxyData : reto) {
                    try {
                        proxy = new ProxyInfo(proxyData);
                        for (HTTPProxy p : dupeCheck) {
                            if (p.sameProxy(proxy)) {
                                /* proxy already got restored */
                                continue restore;
                            }
                        }
                        dupeCheck.add(proxy);
                        proxies.add(proxy);
                        /* in old system we only had one possible proxy */
                        newDefaultProxy = proxy;
                        if (proxy.isProxyRotationEnabled()) rotCheck = true;
                    } catch (final Throwable e) {
                        Log.exception(e);
                    }
                }
            }
            /* import proxies from system properties */
            List<HTTPProxy> sproxy = HTTPProxy.getFromSystemProperties();
            restore: for (HTTPProxy proxyData : sproxy) {
                try {
                    proxy = new ProxyInfo(proxyData);
                    for (HTTPProxy p : dupeCheck) {
                        if (p.sameProxy(proxy)) {
                            /* proxy already got restored */
                            continue restore;
                        }
                    }
                    dupeCheck.add(proxy);
                    proxies.add(proxy);
                    /* in old system we only had one possible proxy */
                    newDefaultProxy = proxy;
                    if (proxy.isProxyRotationEnabled()) rotCheck = true;
                } catch (final Throwable e) {
                    Log.exception(e);
                }
            }
        }
        {
            /* use own scope */
            List<HTTPProxy> availableDirects = getAvailableDirects();
            ArrayList<ProxyData> ret = config.getDirectGatewayList();
            if (ret != null) {
                // restore directs
                restore: for (ProxyData proxyData : ret) {
                    /* check if the local IP is still avilable */
                    try {
                        proxy = new ProxyInfo(proxyData);
                        for (HTTPProxy p : dupeCheck) {
                            if (p.sameProxy(proxy)) {
                                /* proxy already got restored */
                                continue restore;
                            }
                        }
                        boolean localIPAvailable = false;
                        for (HTTPProxy p : availableDirects) {
                            if (p.sameProxy(proxy)) {
                                localIPAvailable = true;
                                break;
                            }
                        }
                        if (localIPAvailable == false) {
                            /* local ip no longer available */
                            continue restore;
                        }
                        dupeCheck.add(proxy);
                        directs.add(proxy);
                        if (proxyData.isDefaultProxy()) {
                            newDefaultProxy = proxy;
                        }
                        if (proxy.isProxyRotationEnabled()) rotCheck = true;
                    } catch (final Throwable e) {
                        Log.exception(e);
                    }
                }
            }
        }

        if (config.isNoneDefault()) {
            /* check if the NONE Proxy is our default proxy */
            if (newDefaultProxy != null) {
                Log.L.severe("NONE default but already got different default?!");
            }
            newDefaultProxy = none;
        }
        /* is NONE Proxy included in rotation */
        none.setProxyRotationEnabled(config.isNoneRotationEnabled());
        if (none.isProxyRotationEnabled()) rotCheck = true;
        if (!rotCheck) {
            // we need at least one rotation
            none.setProxyRotationEnabled(true);
            config.setNoneRotationEnabled(true);
        }
        if (newDefaultProxy == null || newDefaultProxy == none) config.setNoneDefault(true);
        setDefaultProxy(newDefaultProxy);
        /* set new proxies live */
        this.directs = directs;
        this.proxies = proxies;
        eventSender.fireEvent(new ProxyEvent<ProxyInfo>(ProxyController.this, ProxyEvent.Types.REFRESH, null));
    }

    /**
     * returns a copy of current proxy list
     * 
     * @return
     */
    public ArrayList<ProxyInfo> getList() {
        ArrayList<ProxyInfo> ret = new ArrayList<ProxyInfo>(directs.size() + proxies.size() + 1);
        ret.add(none);
        ret.addAll(directs);
        ret.addAll(proxies);
        return ret;
    }

    /**
     * returns the default proxy for all normal browser activity as well as for
     * premium usage
     * 
     * @return
     */
    public ProxyInfo getDefaultProxy() {
        return defaultproxy;
    }

    /**
     * sets current default proxy
     * 
     * @param def
     */
    public void setDefaultProxy(ProxyInfo def) {
        if (def != null && defaultproxy == def) return;
        if (def == null) {
            defaultproxy = none;
        } else {
            defaultproxy = def;
        }
        eventSender.fireEvent(new ProxyEvent<ProxyInfo>(this, ProxyEvent.Types.REFRESH, null));
        exportUpdaterProxy();
    }

    /**
     * add proxy to proxylist in case its not in it yet
     * 
     * @param proxy
     */
    public void addProxy(HTTPProxy proxy) {
        if (proxy == null) return;
        ProxyInfo ret = null;
        synchronized (LOCK) {
            ArrayList<ProxyInfo> nproxies = new ArrayList<ProxyInfo>(proxies);
            for (ProxyInfo info : nproxies) {
                /* duplicate check */
                if (info.sameProxy(proxy)) return;
            }
            nproxies.add(ret = new ProxyInfo(proxy));
            proxies = nproxies;
        }
        eventSender.fireEvent(new ProxyEvent<ProxyInfo>(this, ProxyEvent.Types.ADDED, ret));
    }

    public void addProxy(List<HTTPProxy> proxy) {
        if (proxy == null || proxy.size() == 0) return;
        int changes = 0;
        synchronized (LOCK) {
            ArrayList<ProxyInfo> nproxies = new ArrayList<ProxyInfo>(proxies);
            changes = nproxies.size();
            main: for (HTTPProxy newP : proxy) {
                for (ProxyInfo info : nproxies) {
                    /* duplicate check */
                    if (info.sameProxy(newP)) continue main;
                }
                nproxies.add(new ProxyInfo(newP));
            }
            proxies = nproxies;
            if (changes != nproxies.size()) changes = -1;
        }
        if (changes == -1) eventSender.fireEvent(new ProxyEvent<ProxyInfo>(this, ProxyEvent.Types.REFRESH, null));
    }

    /**
     * enable/disable given proxy, enables none-proxy in case no proxy would be
     * enabled anymore
     * 
     * @param proxy
     * @param enabled
     */
    public void setproxyRotationEnabled(ProxyInfo proxy, boolean enabled) {
        if (proxy == null) return;
        if (proxy.isProxyRotationEnabled() == enabled) return;
        proxy.setProxyRotationEnabled(enabled);
        eventSender.fireEvent(new ProxyEvent<ProxyInfo>(this, ProxyEvent.Types.REFRESH, null));
    }

    /** removes given proxy from proxylist */
    public void remove(ProxyInfo proxy) {
        if (proxy == null) return;
        boolean removed = false;
        synchronized (LOCK) {
            ArrayList<ProxyInfo> nproxies = new ArrayList<ProxyInfo>(proxies);
            if (nproxies.remove(proxy)) {
                removed = true;
                if (proxy == defaultproxy) {
                    setDefaultProxy(none);
                }
            }
            proxies = nproxies;
        }
        if (removed) eventSender.fireEvent(new ProxyEvent<ProxyInfo>(this, ProxyEvent.Types.REMOVED, proxy));
    }

    public ProxyInfo getProxyForDownload(DownloadLink link, Account acc, boolean byPassMaxSimultanDownload) {
        PluginForHost plugin = link.getDefaultPlugin();
        final String host = plugin.getHost();
        final int maxactive = plugin.getMaxSimultanDownload(acc);
        if (acc != null) {
            /* an account must be used or waittime must be over */
            /*
             * only the default proxy may use accounts, to prevent accountblocks
             * because of simultan ip's using it
             */
            ProxyInfo ldefaultProxy = defaultproxy;
            int active = ldefaultProxy.activeDownloadsbyHosts(host);
            if (byPassMaxSimultanDownload || active < maxactive) return ldefaultProxy;
            return null;
        }
        if (none.isProxyRotationEnabled()) {
            /* only use enabled proxies */
            if (none.getHostBlockedTimeout(host) == null && none.getHostIPBlockTimeout(host) == null) {
                /* active downloads must be less than allowed download */
                int active = none.activeDownloadsbyHosts(host);
                if (byPassMaxSimultanDownload || active < maxactive) return none;
            }
        }
        ArrayList<ProxyInfo> ldirects = directs;
        for (ProxyInfo info : ldirects) {
            if (info.isProxyRotationEnabled()) {
                /* only use enabled proxies */
                if (info.getHostBlockedTimeout(host) == null && info.getHostIPBlockTimeout(host) == null) {
                    /* active downloads must be less than allowed download */
                    int active = info.activeDownloadsbyHosts(host);
                    if (byPassMaxSimultanDownload || active < maxactive) return info;
                }
            }
        }
        ArrayList<ProxyInfo> lproxies = proxies;
        for (ProxyInfo info : lproxies) {
            if (info.isProxyRotationEnabled()) {
                /* only use enabled proxies */
                if (info.getHostBlockedTimeout(host) == null && info.getHostIPBlockTimeout(host) == null) {
                    /* active downloads must be less than allowed download */
                    int active = info.activeDownloadsbyHosts(host);
                    if (byPassMaxSimultanDownload || active < maxactive) return info;
                }
            }
        }
        return null;
    }

    public ProxyBlock getHostIPBlockTimeout(final String host) {
        ProxyBlock ret = null;
        if (none.isProxyRotationEnabled()) {
            ret = none.getHostIPBlockTimeout(host);
        }
        if (ret == null) return null;
        ArrayList<ProxyInfo> lproxies = proxies;
        for (ProxyInfo info : lproxies) {
            if (info.isProxyRotationEnabled()) {
                /* only use enabled proxies */
                ProxyBlock ret2 = info.getHostIPBlockTimeout(host);
                if (ret2 == null) {
                    return null;
                } else if (ret == null || ret2.getBlockedUntil() < ret.getBlockedUntil()) {
                    ret = ret2;
                }

            }
        }
        ArrayList<ProxyInfo> ldirects = directs;
        for (ProxyInfo info : ldirects) {
            if (info.isProxyRotationEnabled()) {
                /* only use enabled proxies */
                ProxyBlock ret2 = info.getHostIPBlockTimeout(host);
                if (ret2 == null) {
                    return null;
                } else if (ret == null || ret2.getBlockedUntil() < ret.getBlockedUntil()) {
                    ret = ret2;
                }
            }
        }
        return ret;
    }

    /* optimize for speed */
    public ProxyBlock getHostBlockedTimeout(final String host) {
        ProxyBlock ret = null;
        if (none.isProxyRotationEnabled()) {
            ret = none.getHostBlockedTimeout(host);
        }
        if (ret == null) return null;
        ArrayList<ProxyInfo> lproxies = proxies;
        for (ProxyInfo info : lproxies) {
            if (info.isProxyRotationEnabled()) {
                /* only use enabled proxies */
                ProxyBlock ret2 = info.getHostBlockedTimeout(host);
                if (ret2 == null) {
                    return null;
                } else if (ret == null || ret2.getBlockedUntil() < ret.getBlockedUntil()) {
                    ret = ret2;
                }

            }
        }
        ArrayList<ProxyInfo> ldirects = directs;
        for (ProxyInfo info : ldirects) {
            if (info.isProxyRotationEnabled()) {
                /* only use enabled proxies */
                ProxyBlock ret2 = info.getHostBlockedTimeout(host);
                if (ret2 == null) {
                    return null;
                } else if (ret == null || ret2.getBlockedUntil() < ret.getBlockedUntil()) {
                    ret = ret2;
                }
            }
        }
        return ret;
    }

    public boolean hasIPBlock(final String host) {
        if (none.isProxyRotationEnabled()) {
            if (none.getHostIPBlockTimeout(host) != null) return true;
        }
        ArrayList<ProxyInfo> lproxies = proxies;
        for (ProxyInfo info : lproxies) {
            if (info.isProxyRotationEnabled()) {
                /* only use enabled proxies */
                if (info.getHostIPBlockTimeout(host) != null) return true;
            }
        }
        ArrayList<ProxyInfo> ldirects = directs;
        for (ProxyInfo info : ldirects) {
            if (info.isProxyRotationEnabled()) {
                /* only use enabled proxies */
                if (info.getHostIPBlockTimeout(host) != null) return true;
            }
        }
        return false;
    }

    public boolean hasHostBlocked(final String host) {
        if (none.isProxyRotationEnabled()) {
            if (none.getHostBlockedTimeout(host) != null) return true;
        }
        ArrayList<ProxyInfo> lproxies = proxies;
        for (ProxyInfo info : lproxies) {
            if (info.isProxyRotationEnabled()) {
                /* only use enabled proxies */
                if (info.getHostBlockedTimeout(host) != null) return true;
            }
        }
        ArrayList<ProxyInfo> ldirects = directs;
        for (ProxyInfo info : ldirects) {
            if (info.isProxyRotationEnabled()) {
                /* only use enabled proxies */
                if (info.getHostBlockedTimeout(host) != null) return true;
            }
        }
        return false;
    }

    public void removeHostBlockedTimeout(final String host, boolean onlyLocal) {
        none.removeHostBlockedWaittime(host);
        ArrayList<ProxyInfo> ldirects = directs;
        for (ProxyInfo info : ldirects) {
            info.removeHostBlockedWaittime(host);
        }
        if (!onlyLocal) {
            ArrayList<ProxyInfo> lproxies = proxies;
            for (ProxyInfo info : lproxies) {
                // if (onlyLocal && info.getProxy().isRemote()) continue;
                info.removeHostBlockedWaittime(host);
            }
        }
    }

    public void removeIPBlockTimeout(final String host, boolean onlyLocal) {
        none.removeHostIPBlockTimeout(host);
        ArrayList<ProxyInfo> ldirects = directs;
        for (ProxyInfo info : ldirects) {
            info.removeHostIPBlockTimeout(host);
        }
        if (!onlyLocal) {
            ArrayList<ProxyInfo> lproxies = proxies;
            for (ProxyInfo info : lproxies) {
                info.removeHostIPBlockTimeout(host);
            }
        }
    }

    public boolean hasRotation() {
        if (none.isProxyRotationEnabled()) return true;
        ArrayList<ProxyInfo> ldirects = directs;
        for (ProxyInfo pi : ldirects) {
            if (pi.isProxyRotationEnabled()) return true;
        }
        ArrayList<ProxyInfo> lproxies = proxies;
        for (ProxyInfo pi : lproxies) {
            if (pi.isProxyRotationEnabled()) return true;
        }
        return false;
    }

    public ProxyInfo getNone() {
        return none;
    }

}
