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

package jd.controlling.downloadcontroller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import jd.controlling.downloadcontroller.DiskSpaceManager.DISKSPACERESERVATIONRESULT;
import jd.controlling.packagecontroller.AbstractNode;
import jd.controlling.reconnect.ipcheck.BalancedWebIPCheck;
import jd.controlling.reconnect.ipcheck.IPCheckException;
import jd.controlling.reconnect.ipcheck.OfflineException;
import jd.http.Browser;
import jd.http.Browser.BrowserException;
import jd.http.BrowserSettingsThread;
import jd.plugins.Account;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.DownloadLinkProperty;
import jd.plugins.FilePackage;
import jd.plugins.FilePackageProperty;
import jd.plugins.FilePackageView;
import jd.plugins.LinkStatus;
import jd.plugins.LinkStatusProperty;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.plugins.PluginProgress;
import jd.plugins.download.DownloadInterface;
import jd.plugins.download.raf.HashResult;

import org.appwork.utils.NullsafeAtomicReference;
import org.appwork.utils.logging2.LogSource;
import org.appwork.utils.net.httpconnection.HTTPProxy;
import org.jdownloader.controlling.download.DownloadControllerListener;
import org.jdownloader.logging.LogController;
import org.jdownloader.plugins.SkipReason;
import org.jdownloader.plugins.SkipReasonException;
import org.jdownloader.plugins.controller.PluginClassLoader;
import org.jdownloader.plugins.controller.PluginClassLoader.PluginClassLoaderChild;
import org.jdownloader.plugins.tasks.PluginProgressTask;
import org.jdownloader.plugins.tasks.PluginSubTask;

public class SingleDownloadController extends BrowserSettingsThread implements DownloadControllerListener {

    /**
     * signals that abort request has been received
     */
    private final AtomicBoolean                          abortFlag        = new AtomicBoolean(false);

    /**
     * signals the activity of the plugin in use
     */
    private final NullsafeAtomicReference<PluginForHost> processingPlugin = new NullsafeAtomicReference<PluginForHost>(null);

    public static class WaitingQueueItem {
        public final AtomicLong                          lastStartTimestamp      = new AtomicLong(System.currentTimeMillis());
        public final AtomicLong                          lastConnectionTimestamp = new AtomicLong(System.currentTimeMillis());
        private final CopyOnWriteArrayList<DownloadLink> queueLinks              = new CopyOnWriteArrayList<DownloadLink>();

        public int indexOf(DownloadLink link) {
            return queueLinks.indexOf(link);
        }
    }

    private static final HashMap<String, WaitingQueueItem>  LAST_DOWNLOAD_START_TIMESTAMPS = new HashMap<String, WaitingQueueItem>();

    private final DownloadLink                              downloadLink;
    private final Account                                   account;

    private long                                            startTimestamp                 = -1;
    private final DownloadLinkCandidate                     candidate;
    private final DownloadWatchDog                          watchDog;
    private final LinkStatus                                linkStatus;

    private HashResult                                      hashResult                     = null;
    private final CopyOnWriteArrayList<DownloadWatchDogJob> jobsAfterDetach                = new CopyOnWriteArrayList<DownloadWatchDogJob>();
    private final WaitingQueueItem                          queueItem;
    private final long                                      sizeBefore;
    private final ArrayList<PluginSubTask>                  tasks;
    private volatile HTTPProxy                              usedProxy;
    private boolean                                         resumed;

    public WaitingQueueItem getQueueItem() {
        return queueItem;
    }

    public CopyOnWriteArrayList<DownloadWatchDogJob> getJobsAfterDetach() {
        return jobsAfterDetach;
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    public HashResult getHashResult() {
        return hashResult;
    }

    public void setHashResult(HashResult hashResult) {
        this.hashResult = hashResult;
    }

    public LinkStatus getLinkStatus() {
        return linkStatus;
    }

    public DownloadInterface getDownloadInstance() {
        PluginForHost plugin = processingPlugin.get();
        if (plugin != null) {
            return plugin.getDownloadInterface();
        }
        return null;
    }

    public DownloadLinkCandidate getDownloadLinkCandidate() {
        return candidate;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void lockFile(File file) throws FileIsLockedException {
        DownloadWatchDog.getInstance().getSession().getFileAccessManager().lock(file, this);
    }

    public boolean unlockFile(File file) {
        return DownloadWatchDog.getInstance().getSession().getFileAccessManager().unlock(file, this);
    }

    public DownloadSpeedManager getConnectionHandler() {
        return watchDog.getDownloadSpeedManager();
    }

    protected SingleDownloadController(DownloadLinkCandidate candidate, DownloadWatchDog watchDog) {
        super("Download");
        tasks = new ArrayList<PluginSubTask>();
        setPriority(Thread.MIN_PRIORITY);
        this.watchDog = watchDog;
        this.candidate = candidate;
        super.setProxySelector(candidate.getProxySelector());
        this.downloadLink = candidate.getLink();
        this.sizeBefore = Math.max(0, downloadLink.getView().getBytesLoaded());
        this.account = candidate.getCachedAccount().getAccount();
        String host = candidate.getCachedAccount().getPlugin().getHost();
        WaitingQueueItem queueItem = LAST_DOWNLOAD_START_TIMESTAMPS.get(host);
        if (queueItem == null) {
            queueItem = new WaitingQueueItem();
            LAST_DOWNLOAD_START_TIMESTAMPS.put(host, queueItem);
        }
        queueItem.queueLinks.add(downloadLink);
        this.queueItem = queueItem;
        linkStatus = new LinkStatus(downloadLink);
        setName("Download: " + downloadLink.getView().getDisplayName() + "_" + downloadLink.getHost());
    }

    public boolean isDebug() {
        return true;
    }

    @Override
    public boolean isVerbose() {
        return true;
    }

    public Account getAccount() {
        return account;
    }

    public boolean isAborting() {
        return abortFlag.get();
    }

    public boolean isActive() {
        return processingPlugin.isValueSet();
    }

    protected void abort() {
        if (!isActive()) {
            /* this singleDownloadController is no longer active */
            return;
        }
        if (abortFlag.compareAndSet(false, true)) {
            /* this is our initial abort request */
            Thread abortThread = new Thread() {

                @Override
                public void run() {
                    while (isActive() && SingleDownloadController.this.isAlive()) {
                        try {
                            DownloadInterface dli = getDownloadInstance();
                            if (dli != null) {
                                dli.stopDownload();
                            }
                        } catch (final Throwable e) {
                            LogSource.exception(logger, e);
                        }
                        synchronized (processingPlugin) {
                            if (!isActive() || SingleDownloadController.this.isAlive() == false) {
                                return;
                            }
                            try {
                                processingPlugin.wait(1000);
                            } catch (final InterruptedException e) {
                            }
                        }
                    }
                }

            };
            abortThread.setDaemon(true);
            abortThread.setName("Abort: " + downloadLink.getView().getDisplayName() + "_" + downloadLink.getUniqueID());
            abortThread.start();
        }
    }

    public DownloadLink getDownloadLink() {
        return candidate.getLink();
    }

    private PluginForHost finalizeProcessingPlugin() {
        final PluginForHost plugin;
        synchronized (processingPlugin) {
            plugin = processingPlugin.getAndClear();
            processingPlugin.notifyAll();
        }
        return plugin;
    }

    private SingleDownloadReturnState download(LogSource downloadLogger) {
        PluginForHost livePlugin = null;
        PluginForHost originalPlugin = null;
        boolean validateChallenge = true;
        try {
            logger.info("DownloadCandidate: " + candidate);
            final PluginClassLoaderChild cl;
            this.setContextClassLoader(cl = PluginClassLoader.getInstance().getChild());
            livePlugin = candidate.getCachedAccount().getPlugin().getLazyP().newInstance(cl);

            livePlugin.setBrowser(new Browser() {

                @Override
                protected List<HTTPProxy> selectProxies(String url) throws IOException {
                    List<HTTPProxy> ret = super.selectProxies(url);

                    usedProxy = ret.get(0);
                    return ret;
                }

            });
            livePlugin.setLogger(downloadLogger);
            livePlugin.setDownloadLink(downloadLink);
            originalPlugin = livePlugin;
            switch (candidate.getCachedAccount().getType()) {
            case MULTI:
                originalPlugin = downloadLink.getDefaultPlugin().getLazyP().newInstance(cl);
                originalPlugin.setBrowser(new Browser());
                originalPlugin.setLogger(downloadLogger);
                originalPlugin.setDownloadLink(downloadLink);
                break;
            }
            downloadLink.setLivePlugin(livePlugin);
            watchDog.localFileCheck(this, new ExceptionRunnable() {

                @Override
                public void run() throws Exception {
                    final File partFile = new File(getDownloadLink().getFileOutput() + ".part");
                    long doneSize = Math.max((partFile.exists() ? partFile.length() : 0l), getDownloadLink().getView().getBytesLoaded());
                    final long remainingSize = downloadLink.getView().getBytesTotal() - Math.max(0, doneSize);
                    DISKSPACERESERVATIONRESULT result = watchDog.getSession().getDiskSpaceManager().check(new DiskSpaceReservation() {

                        @Override
                        public File getDestination() {
                            return partFile;
                        }

                        @Override
                        public long getSize() {
                            return remainingSize;
                        }

                    });
                    switch (result) {
                    case FAILED:
                        throw new SkipReasonException(SkipReason.DISK_FULL);
                    case INVALIDDESTINATION:
                        throw new SkipReasonException(SkipReason.INVALID_DESTINATION);
                    }
                }
            }, null);
            try {
                startTimestamp = System.currentTimeMillis();
                switch (candidate.getCachedAccount().getType()) {
                case MULTI:
                    processingPlugin.set(originalPlugin);
                    originalPlugin.init();
                    if (AvailableStatus.FALSE == originalPlugin.requestFileInformation(downloadLink)) {
                        throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
                    }
                    break;
                }
                processingPlugin.set(livePlugin);
                livePlugin.init();

                livePlugin.handle(downloadLink, account);
                SingleDownloadReturnState ret = new SingleDownloadReturnState(this, null, finalizeProcessingPlugin());
                return ret;
            } catch (final BrowserException browserException) {
                downloadLogger.log(browserException);
                try {
                    if (browserException.getRequest() != null) {
                        browserException.getRequest().disconnect();
                    }
                } catch (final Throwable ignore) {
                }
                if (browserException.getCause() != null) {
                    throw browserException.getCause();
                } else {
                    throw browserException;
                }
            }
        } catch (Throwable e) {
            final PluginForHost plugin = finalizeProcessingPlugin();
            if (e instanceof PluginException && ((PluginException) e).getLinkStatus() == LinkStatus.ERROR_CAPTCHA) {
                validateChallenge = false;
            } else if (e instanceof SkipReasonException && ((SkipReasonException) e).getSkipReason() == SkipReason.CAPTCHA) {
                validateChallenge = false;
            } else if (e instanceof PluginException) {
                switch (((PluginException) e).getLinkStatus()) {
                case LinkStatus.ERROR_RETRY:
                case LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE:
                    // we might be offline
                    BalancedWebIPCheck onlineCheck = new BalancedWebIPCheck(true);
                    try {
                        onlineCheck.getExternalIP();
                    } catch (final OfflineException e2) {
                        e = new NoInternetConnection(e);
                    } catch (final IPCheckException e2) {
                    }
                }

            }
            downloadLogger.info("Exception: ");
            downloadLogger.log(e);
            SingleDownloadReturnState ret = new SingleDownloadReturnState(this, e, plugin);
            return ret;
        } finally {
            queueItem.queueLinks.remove(downloadLink);
            DownloadInterface di = livePlugin.getDownloadInterface();
            resumed = di != null && di.isResumedDownload();
            downloadLink.setLivePlugin(null);
            finalizePlugins(downloadLogger, originalPlugin, livePlugin, validateChallenge);
            if (downloadLink.getFilePackage() != null) {
                // if we remove link without stopping them.. the filepackage may be the default package already here.
                FilePackageView view = downloadLink.getFilePackage().getView();
                if (view != null) {
                    view.requestUpdate();
                }
            }

        }
    }

    public PluginForHost getProcessingPlugin() {
        return processingPlugin.get();
    }

    public boolean isResumed() {
        return resumed;
    }

    public HTTPProxy getUsedProxy() {
        return usedProxy;
    }

    private void finalizePlugins(LogSource logger, PluginForHost originalPlugin, PluginForHost livePlugin, boolean valid) {
        switch (candidate.getCachedAccount().getType()) {
        case MULTI:
            if (originalPlugin != null) {
                try {
                    if (valid) {
                        originalPlugin.validateLastChallengeResponse();
                    } else {
                        originalPlugin.invalidateLastChallengeResponse();
                    }
                } catch (final Throwable ignore) {
                    logger.log(ignore);
                }
                try {
                    originalPlugin.clean();
                } catch (final Throwable ignore) {
                    logger.log(ignore);
                }
            }
        case ORIGINAL:
        case NONE:
            if (livePlugin != null) {
                try {
                    if (valid) {
                        livePlugin.validateLastChallengeResponse();
                    } else {
                        livePlugin.invalidateLastChallengeResponse();
                    }
                } catch (final Throwable ignore) {
                    logger.log(ignore);
                }
                try {
                    livePlugin.clean();
                } catch (final Throwable ignore) {
                    logger.log(ignore);
                }
            }
        }
    }

    @Override
    public void run() {
        LogSource downloadLogger = null;
        PluginProgressTask task = new PluginProgressTask(null);
        try {
            String logID = downloadLink.getDefaultPlugin().getHost();
            if (AccountCache.ACCOUNTTYPE.MULTI.equals(candidate.getCachedAccount().getType())) {
                logID = logID + "_" + candidate.getCachedAccount().getPlugin().getHost();
            }
            downloadLogger = LogController.getFastPluginLogger(logID);
            downloadLogger.info("Start Download of " + downloadLink.getDownloadURL());
            super.setLogger(downloadLogger);

            task.open();
            addTask(task);
            SingleDownloadReturnState returnState = download(downloadLogger);
            if (isAborting()) {
                /* clear interrupted flag */
                interrupted();
            }
            watchDog.detach(this, returnState);
        } finally {
            task.reopen();
            task.close();
            finalizeProcessingPlugin();
        }
    }

    @Override
    public void setLogger(Logger logger) {
        /* we dont allow external changes */
    }

    /**
     * @return the sizeBefore
     */
    public long getSizeBefore() {
        return sizeBefore;
    }

    public void addTask(PluginSubTask subTask) {
        synchronized (tasks) {
            tasks.add(subTask);
        }
    }

    public void onDetach(DownloadLink downloadLink) {
        DownloadController.getInstance().getEventSender().removeListener(this);
        synchronized (tasks) {
            for (PluginSubTask t : tasks) {
                t.close();
            }
        }
    }

    public void onAttach(DownloadLink downloadLink) {
        DownloadController.getInstance().getEventSender().addListener(this, true);
    }

    @Override
    public void onDownloadControllerAddedPackage(FilePackage pkg) {
    }

    @Override
    public void onDownloadControllerStructureRefresh(FilePackage pkg) {
    }

    @Override
    public void onDownloadControllerStructureRefresh() {
    }

    @Override
    public void onDownloadControllerStructureRefresh(AbstractNode node, Object param) {
    }

    @Override
    public void onDownloadControllerRemovedPackage(FilePackage pkg) {
    }

    @Override
    public void onDownloadControllerRemovedLinklist(List<DownloadLink> list) {
    }

    @Override
    public void onDownloadControllerUpdatedData(DownloadLink downloadlink, DownloadLinkProperty property) {
        try {
            if (downloadlink != this.downloadLink) {
                return;
            }
            if (property.getProperty() == DownloadLinkProperty.Property.PLUGIN_PROGRESS) {
                PluginProgress newProgress = (PluginProgress) property.getValue();
                PluginProgressTask task = null;
                synchronized (tasks) {
                    for (PluginSubTask t : tasks) {
                        if (t instanceof PluginProgressTask) {
                            if (((PluginProgressTask) t).getProgress() != newProgress) {
                                t.close();
                            } else {
                                task = (PluginProgressTask) t;
                            }
                        }
                    }
                    if (task == null) {
                        task = new PluginProgressTask(newProgress);
                        task.open();
                        addTask(task);
                    } else {
                        task.reopen();
                    }
                }
            }
        } catch (Throwable e) {
            // TODO: handle exception
        }
    }

    @Override
    public void onDownloadControllerUpdatedData(FilePackage pkg, FilePackageProperty property) {
        System.out.println(property);
    }

    @Override
    public void onDownloadControllerUpdatedData(DownloadLink downloadlink, LinkStatusProperty property) {
    }

    @Override
    public void onDownloadControllerUpdatedData(DownloadLink downloadlink) {
    }

    @Override
    public void onDownloadControllerUpdatedData(FilePackage pkg) {
    }

    public List<PluginSubTask> getTasks() {
        synchronized (tasks) {
            return new ArrayList<PluginSubTask>(tasks);
        }

    }

}