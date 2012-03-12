package jd.controlling.linkchecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import jd.controlling.JDLogger;
import jd.controlling.JDPluginLogger;
import jd.controlling.linkcrawler.CheckableLink;
import jd.http.Browser;
import jd.http.BrowserSettingsThread;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.LinkStatus;
import jd.plugins.Plugin;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.logging.Log;
import org.jdownloader.plugins.controller.host.HostPluginController;
import org.jdownloader.plugins.controller.host.LazyHostPlugin;

public class LinkChecker<E extends CheckableLink> {

    private static class InternCheckableLink {
        protected CheckableLink                      link = null;
        protected final int                          linkCheckerGeneration;
        private LinkChecker<? extends CheckableLink> checker;

        public InternCheckableLink(CheckableLink link, LinkChecker<? extends CheckableLink> checker) {
            this.link = link;
            this.linkCheckerGeneration = checker.checkerGeneration.get();
            this.checker = checker;
        }

        public CheckableLink getCheckableLink() {
            return this.link;
        }

        public boolean linkCheckAllowed() {
            return this.linkCheckerGeneration == checker.checkerGeneration.get();
        }

        public LinkChecker<? extends CheckableLink> getLinkChecker() {
            return checker;
        }

    }

    /* static variables */
    private static AtomicInteger                                                    CHECKER                = new AtomicInteger(0);
    private static AtomicInteger                                                    LINKCHECKER_THREAD_NUM = new AtomicInteger(0);
    private final static int                                                        MAX_THREADS;
    private final static int                                                        KEEP_ALIVE;
    private static HashMap<String, Thread>                                          CHECK_THREADS          = new HashMap<String, Thread>();
    private static HashMap<String, ArrayList<LinkChecker<? extends CheckableLink>>> LINKCHECKER            = new HashMap<String, ArrayList<LinkChecker<? extends CheckableLink>>>();
    private static final Object                                                     LOCK                   = new Object();

    /* local variables for this LinkChecker */
    private AtomicLong                                                              linksRequested         = new AtomicLong(0);
    private AtomicLong                                                              linksDone              = new AtomicLong(0);
    private HashMap<String, LinkedList<InternCheckableLink>>                        links2Check            = new HashMap<String, LinkedList<InternCheckableLink>>();
    private boolean                                                                 forceRecheck           = false;
    private LinkCheckerHandler<E>                                                   handler                = null;
    private static int                                                              SPLITSIZE              = 80;
    private static LinkCheckerEventSender                                           EVENTSENDER            = new LinkCheckerEventSender();
    protected AtomicInteger                                                         checkerGeneration      = new AtomicInteger(0);

    public static LinkCheckerEventSender getEventSender() {
        return EVENTSENDER;
    }

    static {
        MAX_THREADS = Math.max(JsonConfig.create(LinkCheckerConfig.class).getMaxThreads(), 1);
        KEEP_ALIVE = Math.max(JsonConfig.create(LinkCheckerConfig.class).getThreadKeepAlive(), 100);
    }

    public LinkChecker() {
        this(false);
    }

    public LinkChecker(boolean forceRecheck) {
        this.forceRecheck = forceRecheck;
    }

    public boolean isForceRecheck() {
        return forceRecheck;
    }

    public void setLinkCheckHandler(LinkCheckerHandler<E> handler) {
        this.handler = handler;
    }

    public LinkCheckerHandler<E> getLinkCheckHandler() {
        return handler;
    }

    public void stopChecking() {
        checkerGeneration.incrementAndGet();
        synchronized (this) {
            for (LinkedList<InternCheckableLink> values : links2Check.values()) {
                linksDone.addAndGet(values.size());
            }
            links2Check.clear();
        }
    }

    @SuppressWarnings("unchecked")
    protected void linkChecked(InternCheckableLink link) {
        if (link == null) return;
        boolean stopped = linksDone.incrementAndGet() == linksRequested.get();
        if (stopped) {
            synchronized (CHECKER) {
                CHECKER.decrementAndGet();
            }
            EVENTSENDER.fireEvent(new LinkCheckerEvent(this, LinkCheckerEvent.Type.STOPPED));
        }
        LinkCheckerHandler<E> h = handler;
        if (h != null && link.linkCheckAllowed()) {
            h.linkCheckDone((E) link.getCheckableLink());
        }
    }

    public void check(List<E> links) {
        if (links == null) throw new IllegalArgumentException("links is null?");
        for (E link : links) {
            check(link);
        }
    }

    public void check(E link) {
        if (link == null || link.getDownloadLink() == null) throw new IllegalArgumentException("links is null?");
        DownloadLink dlLink = link.getDownloadLink();
        /* get Host of the link */
        String host = dlLink.getHost();
        if (Plugin.FTP_HOST.equalsIgnoreCase(host) || Plugin.DIRECT_HTTP_HOST.equalsIgnoreCase(host) || Plugin.HTTP_LINKS_HOST.equalsIgnoreCase(host)) {
            /* direct and ftp links are divided by their hostname */
            String specialHost = Browser.getHost(dlLink.getDownloadURL());
            if (specialHost != null) host = specialHost;
        }
        boolean started = false;
        synchronized (this) {
            /* add link to list of link2Check */
            LinkedList<InternCheckableLink> map = links2Check.get(host);
            if (map == null) {
                map = new LinkedList<InternCheckableLink>();
                links2Check.put(host, map);
            }
            map.add(new InternCheckableLink(link, this));
            if (linksRequested.get() == linksDone.get()) started = true;
            linksRequested.incrementAndGet();
        }
        if (started) {
            synchronized (CHECKER) {
                CHECKER.incrementAndGet();
            }
            EVENTSENDER.fireEvent(new LinkCheckerEvent(this, LinkCheckerEvent.Type.STARTED));
        }
        synchronized (LOCK) {
            ArrayList<LinkChecker<? extends CheckableLink>> checker = LINKCHECKER.get(host);
            if (checker == null) {
                checker = new ArrayList<LinkChecker<? extends CheckableLink>>();
                checker.add(this);
                LINKCHECKER.put(host, checker);
            } else if (!checker.contains(this)) {
                checker.add(this);
            }
            /* notify linkcheckThread or try to start new one */
            Thread thread = CHECK_THREADS.get(host);
            if (thread == null || !thread.isAlive()) {
                startNewThreads();
            }
        }
    }

    /**
     * is the LinkChecker running
     * 
     * @return
     */
    public boolean isRunning() {
        return linksRequested.get() != linksDone.get();
    }

    public long checksRequested() {
        return linksRequested.get();
    }

    public long checksDone() {
        return linksDone.get();
    }

    public static boolean isChecking() {
        return CHECKER.get() > 0;
    }

    /* wait till all requested links are done */
    public boolean waitForChecked() {
        while (isRunning()) {
            synchronized (this) {
                try {
                    this.wait(1000);
                } catch (InterruptedException e) {
                    return false;
                }
            }
        }
        return isRunning() == false;
    }

    /* start a new linkCheckThread for the given host */
    private static void startNewThread(final String threadHost) {
        synchronized (LOCK) {
            if (CHECK_THREADS.size() >= MAX_THREADS) return;
            final BrowserSettingsThread newThread = new BrowserSettingsThread(new Runnable() {

                public void run() {
                    int stopDelay = 1;
                    PluginForHost plg = null;
                    Browser br = new Browser();
                    while (true) {
                        /*
                         * arraylist to hold the current checkable links
                         */
                        ArrayList<InternCheckableLink> roundComplete = new ArrayList<InternCheckableLink>();
                        try {
                            synchronized (LOCK) {
                                ArrayList<LinkChecker<? extends CheckableLink>> map = LINKCHECKER.get(threadHost);
                                if (map != null) {
                                    for (LinkChecker<? extends CheckableLink> lc : map) {
                                        synchronized (lc) {
                                            LinkedList<InternCheckableLink> map2 = lc.links2Check.get(threadHost);
                                            if (map2 != null) {
                                                roundComplete.addAll(map2);
                                                for (InternCheckableLink link : map2) {
                                                    if (lc.isForceRecheck()) {
                                                        /*
                                                         * linkChecker instance
                                                         * is set to
                                                         * forceRecheck
                                                         */
                                                        link.getCheckableLink().getDownloadLink().setAvailableStatus(AvailableStatus.UNCHECKED);
                                                    }
                                                }
                                                /*
                                                 * just clear the map to allow
                                                 * fast adding of new links to
                                                 * the given linkChecker
                                                 * instance
                                                 */
                                                map2.clear();
                                            }
                                        }
                                    }
                                    /*
                                     * remove threadHost from static list to
                                     * remove unwanted references
                                     */
                                    LINKCHECKER.remove(threadHost);
                                }
                            }
                            int n = roundComplete.size();
                            for (int i = 0; i < n; i += SPLITSIZE) {
                                List<InternCheckableLink> roundSplit = roundComplete.subList(i, Math.min(n, i + SPLITSIZE));
                                if (roundSplit.size() > 0) {
                                    stopDelay = 1;
                                    ArrayList<DownloadLink> massLinkCheck = new ArrayList<DownloadLink>(roundSplit.size());
                                    for (InternCheckableLink link : roundSplit) {
                                        if (link.linkCheckAllowed()) massLinkCheck.add(link.getCheckableLink().getDownloadLink());
                                    }
                                    /* now we check the links */
                                    if (plg == null && massLinkCheck.size() > 0) {
                                        /* create plugin if not done yet */
                                        LazyHostPlugin lazyp = HostPluginController.getInstance().get(massLinkCheck.get(0).getDefaultPlugin().getHost());
                                        plg = lazyp.newInstance();
                                        plg.setBrowser(br);
                                        plg.setLogger(new JDPluginLogger(lazyp.getDisplayName() + ":LinkCheck"));
                                        plg.init();
                                    }
                                    /*
                                     * make sure the current Thread uses the
                                     * PluginClassLoaderChild of the Plugin in
                                     * use
                                     */
                                    Thread.currentThread().setContextClassLoader(plg.getLazyP().getClassLoader());
                                    try {
                                        /* try mass link check */
                                        plg.checkLinks(massLinkCheck.toArray(new DownloadLink[massLinkCheck.size()]));
                                    } catch (final Throwable e) {
                                        Log.exception(e);
                                    } finally {
                                        massLinkCheck = null;
                                    }
                                    for (InternCheckableLink link : roundSplit) {
                                        if (link.linkCheckAllowed() && plg != null) {
                                            /*
                                             * this will check the link, if not
                                             * already checked
                                             */
                                            LinkChecker.updateAvailableStatus(plg, link.getCheckableLink().getDownloadLink());
                                        }
                                        link.getLinkChecker().linkChecked(link);
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            Log.exception(e);
                        }
                        try {
                            Thread.sleep(KEEP_ALIVE);
                        } catch (InterruptedException e) {
                            Log.exception(e);
                            synchronized (LOCK) {
                                CHECK_THREADS.remove(threadHost);
                                return;
                            }
                        }
                        synchronized (LOCK) {
                            ArrayList<LinkChecker<? extends CheckableLink>> stopCheck = LINKCHECKER.get(threadHost);
                            if (stopCheck == null || stopCheck.size() == 0) {
                                stopDelay--;
                                if (stopDelay < 0) {
                                    CHECK_THREADS.remove(threadHost);
                                    startNewThreads();
                                    return;
                                }
                            }
                        }
                    }
                }
            });
            newThread.setName("LinkChecker: " + LINKCHECKER_THREAD_NUM.incrementAndGet() + ":" + threadHost);
            newThread.setDaemon(true);
            CHECK_THREADS.put(threadHost, newThread);
            newThread.start();
        }
    }

    /* start new linkCheckThreads until max is reached or no left to start */
    private static void startNewThreads() {
        synchronized (LOCK) {
            Set<Entry<String, ArrayList<LinkChecker<? extends CheckableLink>>>> sets = LINKCHECKER.entrySet();
            for (Entry<String, ArrayList<LinkChecker<? extends CheckableLink>>> set : sets) {
                String host = set.getKey();
                Thread thread = CHECK_THREADS.get(host);
                if (thread == null || !thread.isAlive()) {
                    CHECK_THREADS.remove(host);
                    if (CHECK_THREADS.size() < MAX_THREADS) {
                        startNewThread(host);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private static void updateAvailableStatus(PluginForHost plgToUse, DownloadLink link) {
        if (link.getAvailableStatus() != AvailableStatus.UNCHECKED) return;
        AvailableStatus availableStatus = null;
        try {
            plgToUse.reset();
            availableStatus = plgToUse.requestFileInformation(link);
            try {
                plgToUse.getBrowser().getHttpConnection().disconnect();
            } catch (Throwable e) {
            }
        } catch (PluginException e) {
            e.fillLinkStatus(link.getLinkStatus());
            if (link.getLinkStatus().hasStatus(LinkStatus.ERROR_IP_BLOCKED) || link.getLinkStatus().hasStatus(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE) || link.getLinkStatus().hasStatus(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE)) {
                availableStatus = AvailableStatus.UNCHECKABLE;
            } else {
                availableStatus = AvailableStatus.FALSE;
            }
        } catch (Throwable e) {
            JDLogger.exception(e);
            availableStatus = AvailableStatus.UNCHECKABLE;
        }
        if (availableStatus == null) availableStatus = AvailableStatus.UNCHECKABLE;
        link.setAvailableStatus(availableStatus);
    }
}
