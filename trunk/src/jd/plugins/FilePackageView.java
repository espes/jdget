package jd.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.downloadcontroller.SingleDownloadController;
import jd.controlling.packagecontroller.ChildrenView;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.download.DownloadInterface;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.StringUtils;
import org.jdownloader.DomainInfo;
import org.jdownloader.controlling.Priority;
import org.jdownloader.extensions.extraction.ExtractionProgress;
import org.jdownloader.extensions.extraction.ExtractionStatus;
import org.jdownloader.extensions.extraction.contextmenu.downloadlist.action.ExtractIconVariant;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.downloads.columns.AvailabilityColumn;
import org.jdownloader.images.NewTheme;
import org.jdownloader.plugins.ConditionalSkipReason;
import org.jdownloader.plugins.FinalLinkState;
import org.jdownloader.plugins.MirrorLoading;
import org.jdownloader.plugins.SkipReason;
import org.jdownloader.settings.GraphicalUserInterfaceSettings;

public class FilePackageView extends ChildrenView<DownloadLink> {

    private final FilePackage                     fp;

    protected volatile long                       lastUpdateTimestamp      = -1;

    protected volatile boolean                    lastRunningState         = false;
    protected volatile long                       finishedDate             = -1;
    protected volatile long                       estimatedETA             = -1;

    private volatile int                          offline                  = 0;
    private volatile int                          online                   = 0;
    private final AtomicLong                      updatesRequired          = new AtomicLong(0);
    private volatile long                         updatesDone              = -1;
    private volatile String                       availabilityColumnString = null;
    private volatile ChildrenAvailablility        availability             = ChildrenAvailablility.UNKNOWN;

    private volatile java.util.List<DownloadLink> items                    = new ArrayList<DownloadLink>();

    private final ImageIcon                       falseIcon;

    protected static final long                   GUIUPDATETIMEOUT         = JsonConfig.create(GraphicalUserInterfaceSettings.class).getDownloadViewRefresh();

    public boolean isEnabled() {
        return enabledCount > 0;
    }

    /**
     * This constructor is protected. do not use this class outside the FilePackage
     * 
     * @param fp
     */
    public FilePackageView(FilePackage fp) {
        this.fp = fp;
        this.falseIcon = NewTheme.I().getIcon("false", 16);

    }

    private DomainInfo[]  infos            = new DomainInfo[0];
    private volatile long size             = 0;
    private volatile int  finalCount       = 0;
    private volatile int  unknownFileSizes = 0;
    private volatile int  children         = 0;

    public int getChildren() {
        return children;
    }

    public int getUnknownFileSizes() {
        return unknownFileSizes;
    }

    public int getFinalCount() {
        return finalCount;
    }

    private long                  done = 0;

    private int                   enabledCount;

    private Priority              lowestPriority;

    private Priority              highestPriority;

    private PluginStateCollection pluginStates;

    public PluginStateCollection getPluginStates() {
        return pluginStates;
    }

    public DomainInfo[] getDomainInfos() {
        return infos;
    }

    public long getSize() {
        return size;
    }

    public long getDone() {
        return done;
    }

    public long getETA() {
        return estimatedETA;
    }

    public boolean isFinished() {
        return finishedDate > 0;
    }

    public int getDisabledCount() {
        return Math.max(0, getItems().size() - enabledCount);
    }

    public long getFinishedDate() {
        return finishedDate;
    }

    private String commonSourceUrl;

    @Override
    public void aggregate() {

        long lupdatesRequired = updatesRequired.get();
        lastUpdateTimestamp = System.currentTimeMillis();
        synchronized (this) {
            /* this is called for tablechanged, so update everything for given items */
            Temp tmp = new Temp();
            boolean readL = fp.getModifyLock().readLock();
            try {
                tmp.children = fp.getChildren().size();
                for (DownloadLink link : fp.getChildren()) {
                    addLinkToTemp(tmp, link);
                }
            } finally {
                fp.getModifyLock().readUnlock(readL);
            }
            for (Long size : tmp.downloadSizes.values()) {
                if (size >= 0) {
                    tmp.newSize += size;
                } else {
                    tmp.newUnknownFileSizes++;
                }
            }
            for (Long done : tmp.downloadDone.values()) {
                if (done >= 0) {
                    tmp.newDone += done;
                }
            }
            writeTempToFields(tmp);
            updatesDone = lupdatesRequired;

        }
    }

    public Priority getLowestPriority() {
        return lowestPriority;
    }

    public Priority getHighestPriority() {
        return highestPriority;
    }

    private class Temp {
        private int                          newUnknownFileSizes = 0;
        private int                          newFinalCount       = 0;
        private long                         newSize             = -1;
        private long                         newDone             = -1;
        private long                         newFinishedDate     = -1;
        private int                          newOffline          = 0;
        private int                          newOnline           = 0;
        private int                          newEnabledCount     = 0;
        private int                          children            = 0;
        private long                         fpETA               = -1;
        private long                         fpTODO              = 0;
        private Priority                     priorityLowset      = Priority.HIGHEST;
        private Priority                     priorityHighest     = Priority.LOWER;
        private long                         fpSPEED             = 0;
        private boolean                      sizeKnown           = false;
        private boolean                      fpRunning           = false;
        private HashMap<String, Long>        downloadSizes       = new HashMap<String, Long>();
        private HashMap<String, Long>        downloadDone        = new HashMap<String, Long>();
        private HashSet<String>              eta                 = new HashSet<String>();
        private HashSet<DomainInfo>          newInfos            = new HashSet<DomainInfo>();
        private boolean                      allFinished         = true;
        private String                       sameSource          = null;
        private boolean                      sameSourceFullUrl   = true;

        private HashMap<Object, PluginState> pluginStates        = new HashMap<Object, PluginState>();
    }

    public static class PluginState {

        private String description;
        private Icon   icon;

        public String getDescription() {
            return description;
        }

        public Icon getIcon() {
            return icon;
        }

        private PluginState(String message, Icon icon2) {
            this.description = message;
            this.icon = icon2;
        }

        public static PluginState create(String message, Icon icon) {
            if (StringUtils.isEmpty(message) && icon == null) {
                return null;
            }
            return new PluginState(message, icon);
        }

    }

    @Override
    public void setItems(List<DownloadLink> updatedItems) {
        long lupdatesRequired = updatesRequired.get();
        lastUpdateTimestamp = System.currentTimeMillis();
        synchronized (this) {
            /* this is called for tablechanged, so update everything for given items */
            Temp tmp = new Temp();

            boolean readL = fp.getModifyLock().readLock();

            try {
                tmp.children = fp.getChildren().size();
                for (DownloadLink link : fp.getChildren()) {
                    tmp.newInfos.add(link.getDomainInfo());
                    addLinkToTemp(tmp, link);
                }
            } finally {
                fp.getModifyLock().readUnlock(readL);
            }
            for (Long size : tmp.downloadSizes.values()) {
                if (size >= 0) {
                    tmp.newSize += size;
                } else {
                    tmp.newUnknownFileSizes++;
                }
            }
            for (Long done : tmp.downloadDone.values()) {
                if (done >= 0) {
                    tmp.newDone += done;
                }
            }
            writeTempToFields(tmp);
            items = updatedItems;
            updatesDone = lupdatesRequired;
            ArrayList<DomainInfo> lst = new ArrayList<DomainInfo>(tmp.newInfos);
            Collections.sort(lst, new Comparator<DomainInfo>() {

                @Override
                public int compare(DomainInfo o1, DomainInfo o2) {
                    return o1.getTld().compareTo(o2.getTld());
                }
            });
            infos = lst.toArray(new DomainInfo[tmp.newInfos.size()]);
        }
    }

    protected void writeTempToFields(Temp tmp) {
        size = tmp.newSize;
        done = tmp.newDone;
        this.children = tmp.children;
        this.finalCount = tmp.newFinalCount;
        this.unknownFileSizes = tmp.newUnknownFileSizes;
        if (tmp.newUnknownFileSizes == tmp.children) {
            size = -1;
        }
        this.enabledCount = tmp.newEnabledCount;
        if (tmp.allFinished) {
            /* all links have reached finished state */
            finishedDate = tmp.newFinishedDate;
        } else {
            /* not all have finished */
            finishedDate = -1;
        }
        lastRunningState = tmp.fpRunning;
        if (tmp.fpRunning) {
            if (tmp.sizeKnown && tmp.fpSPEED > 0) {
                /* we could calc an ETA because at least one filesize is known */
                estimatedETA = tmp.fpETA;
            } else {
                /* no filesize is known, we use Integer.Min_value to signal this */
                estimatedETA = Integer.MIN_VALUE;
            }
        } else {
            /* no download running */
            estimatedETA = -1;
        }
        if (!tmp.sameSourceFullUrl) {
            tmp.sameSource += "[...]";
        }
        this.commonSourceUrl = tmp.sameSource;

        this.pluginStates = new PluginStateCollection(tmp.pluginStates.values());

        offline = tmp.newOffline;
        online = tmp.newOnline;
        updateAvailability(tmp);
        this.lowestPriority = tmp.priorityLowset;
        this.highestPriority = tmp.priorityHighest;

        availabilityColumnString = _GUI._.AvailabilityColumn_getStringValue_object_(tmp.newOnline, tmp.children);
    }

    public String getCommonSourceUrl() {
        return commonSourceUrl;
    }

    private ConditionalSkipReason getConditionalSkipReason(DownloadLink link) {
        ConditionalSkipReason conditionalSkipReason = link.getConditionalSkipReason();
        if (conditionalSkipReason == null || conditionalSkipReason.isConditionReached()) {
            return null;
        }
        if (conditionalSkipReason instanceof MirrorLoading) {
            /* we dont have to handle this, as another link is already downloading */
            return null;
        }
        return conditionalSkipReason;
    }

    protected void addLinkToTemp(Temp tmp, DownloadLink link) {
        if (link.getPriorityEnum().getId() < tmp.priorityLowset.getId()) {
            tmp.priorityLowset = link.getPriorityEnum();
        }
        if (link.getPriorityEnum().getId() > tmp.priorityHighest.getId()) {
            tmp.priorityHighest = link.getPriorityEnum();
        }
        String sourceUrl = null;
        if (link.getLinkType() == DownloadLink.LINKTYPE_CONTAINER) {
            if (link.gotBrowserUrl()) {
                sourceUrl = link.getBrowserUrl();
            }
        } else {
            sourceUrl = link.getBrowserUrl();
        }
        if (sourceUrl != null) {
            tmp.sameSource = StringUtils.getCommonalities(tmp.sameSource, sourceUrl);
            tmp.sameSourceFullUrl = tmp.sameSourceFullUrl && tmp.sameSource.equals(sourceUrl);
        }
        if (AvailableStatus.FALSE == link.getAvailableStatus()) {
            // offline
            tmp.newOffline++;
        } else if (AvailableStatus.TRUE == link.getAvailableStatus()) {
            // online
            tmp.newOnline++;
        }
        String id = null;
        PluginState ps = null;
        //
        PluginProgress prog = link.getPluginProgress();
        if (prog != null) {
            if (!(prog instanceof ExtractionProgress)) {
                id = prog.getClass().getName() + link.getHost();
                if (prog.getIcon(this) != null) {
                    if (!tmp.pluginStates.containsKey(id)) {
                        ps = PluginState.create(prog.getMessage(FilePackageView.this) + " (" + link.getDomainInfo().getTld() + ")", new FavitIcon(prog.getIcon(this), link.getDomainInfo()));
                        if (ps != null) {
                            tmp.pluginStates.put(id, ps);
                        }
                    }
                }
            }
        }
        ConditionalSkipReason conditionalSkipReason = getConditionalSkipReason(link);
        if (conditionalSkipReason != null) {
            id = conditionalSkipReason.getClass().getName() + link.getHost();
            if (!tmp.pluginStates.containsKey(id)) {
                ps = PluginState.create(conditionalSkipReason.getMessage(this, link) + " (" + link.getDomainInfo().getTld() + ")", new FavitIcon(conditionalSkipReason.getIcon(this, link), link.getDomainInfo()));
                if (ps != null) {
                    tmp.pluginStates.put(id, ps);
                }
            }
        }
        SkipReason skipReason = link.getSkipReason();
        if (skipReason != null) {
            id = skipReason.name();
            if (!tmp.pluginStates.containsKey(id)) {
                ps = PluginState.create(skipReason.getExplanation(this), skipReason.getIcon(this, 18));
                if (ps != null) {
                    tmp.pluginStates.put(id, ps);
                }
            }
        }
        FinalLinkState finalLinkState = link.getFinalLinkState();

        if (finalLinkState != null) {
            // if (FinalLinkState.CheckFailed(finalLinkState)) {
            switch (finalLinkState) {
            case FAILED:
            case FAILED_CRC32:
            case FAILED_EXISTS:
            case FAILED_FATAL:
            case FAILED_MD5:
            case FAILED_SHA1:
            case OFFLINE:
            case PLUGIN_DEFECT:
                id = "error" + link.getHost();
                if (!tmp.pluginStates.containsKey(id)) {
                    ps = PluginState.create(_GUI._.FilePackageView_addLinkToTemp_downloaderror_() + " (" + link.getDomainInfo().getTld() + ")", new FavitIcon(this.falseIcon, link.getDomainInfo()));
                    if (ps != null) {
                        tmp.pluginStates.put(id, ps);
                    }
                }
                break;
            case FINISHED:
            case FINISHED_SHA1:
            case FINISHED_MD5:
            case FINISHED_CRC32:
            case FINISHED_MIRROR:
            }

            // }
            ExtractionStatus extractionStatus = link.getExtractionStatus();
            if (extractionStatus != null) {
                switch (extractionStatus) {
                case ERROR:
                case ERROR_PW:
                case ERROR_CRC:
                case ERROR_NOT_ENOUGH_SPACE:
                case ERRROR_FILE_NOT_FOUND:

                    // ArchiveSettings as = ArchiveController.getInstance().getArchiveSettings(new DownloadLinkArchiveFactory(link));
                    String archiveID = link.getArchiveID();
                    // extracting the archive name here is probably too slow
                    // ExtractionExtension.getIntance().createArchiveID(new DownloadLinkArchiveFactory(link));
                    if (StringUtils.isNotEmpty(archiveID)) {
                        id = "extractError:" + archiveID;
                        if (!tmp.pluginStates.containsKey(id)) {
                            ps = PluginState.create(extractionStatus.getExplanation() + " (" + link.getName() + ")", new ExtractIconVariant("error", 18, 10));
                            if (ps != null) {
                                tmp.pluginStates.put(id, ps);
                            }
                        }
                    }
                    break;
                case SUCCESSFUL:
                    archiveID = link.getArchiveID();
                    // extracting the archive name here is probably too slow
                    // ExtractionExtension.getIntance().createArchiveID(new DownloadLinkArchiveFactory(link));
                    if (StringUtils.isNotEmpty(archiveID)) {
                        id = "ExtractSuccess:" + archiveID;
                        if (!tmp.pluginStates.containsKey(id)) {
                            ps = PluginState.create(extractionStatus.getExplanation() + " (" + link.getName() + ")", new ExtractIconVariant("ok", 18, 10));
                            if (ps != null) {
                                tmp.pluginStates.put(id, ps);
                            }
                        }
                    }
                    break;
                case RUNNING:
                    archiveID = link.getArchiveID();
                    // extracting the archive name here is probably too slow
                    // ExtractionExtension.getIntance().createArchiveID(new DownloadLinkArchiveFactory(link));

                    if (StringUtils.isNotEmpty(archiveID)) {
                        id = "ExtractionRunning" + archiveID;
                        prog = link.getPluginProgress();
                        ps = null;
                        if (prog != null) {
                            if (prog instanceof ExtractionProgress) {
                                if (!tmp.pluginStates.containsKey(id)) {
                                    ps = PluginState.create(prog.getMessage(FilePackageView.this) + " (" + link.getName() + ")", new ExtractIconVariant(IconKey.ICON_MEDIA_PLAYBACK_START, 18, 16, 3, 3).crop());
                                    if (ps != null) {
                                        tmp.pluginStates.put(id, ps);
                                    }
                                }
                            }
                        }
                        if (ps == null && !tmp.pluginStates.containsKey(id)) {
                            ps = PluginState.create(extractionStatus.getExplanation() + " (" + link.getName() + ")", new ExtractIconVariant(IconKey.ICON_MEDIA_PLAYBACK_START, 18, 16, 3, 3).crop());
                            if (ps != null) {
                                tmp.pluginStates.put(id, ps);
                            }
                        }

                    }
                    break;
                }
            }
        }
        if (skipReason != null || finalLinkState != null) {
            tmp.newFinalCount++;
        }
        if (link.isEnabled()) {
            /*
             * we still have enabled links, so package must be enabled too
             */

            tmp.newEnabledCount++;
        }
        Long downloadSize = tmp.downloadSizes.get(link.getView().getDisplayName());

        if (downloadSize == null) {
            tmp.downloadSizes.put(link.getView().getDisplayName(), link.getView().getBytesTotal());
            tmp.downloadDone.put(link.getView().getDisplayName(), link.getView().getBytesLoaded());
        } else {
            if (!tmp.eta.contains(link.getView().getDisplayName())) {
                if (link.isEnabled()) {
                    tmp.downloadSizes.put(link.getView().getDisplayName(), link.getView().getBytesTotal());
                    tmp.downloadDone.put(link.getView().getDisplayName(), link.getView().getBytesLoaded());
                } else if (downloadSize < link.getView().getBytesTotal()) {
                    tmp.downloadSizes.put(link.getView().getDisplayName(), link.getView().getBytesTotal());
                    tmp.downloadDone.put(link.getView().getDisplayName(), link.getView().getBytesLoaded());
                }
            }
        }

        /* ETA calculation */
        if (link.isEnabled() && link.getFinalLinkState() == null) {
            /* link must be enabled and not finished state */
            boolean linkRunning = link.getDownloadLinkController() != null;
            if (linkRunning || tmp.eta.contains(link.getView().getDisplayName()) == false) {
                if (linkRunning) {
                    tmp.fpRunning = true;
                    tmp.eta.add(link.getView().getDisplayName());
                }

                if (link.getView().getBytesTotal() >= 0) {
                    /* we know at least one filesize */
                    tmp.sizeKnown = true;
                }
                final long linkTodo = Math.max(-1, link.getView().getBytesTotal() - link.getView().getBytesLoaded());
                SingleDownloadController sdc = link.getDownloadLinkController();
                DownloadInterface dli = null;
                if (sdc != null) {
                    dli = sdc.getDownloadInstance();
                }
                long linkSpeed = link.getView().getSpeedBps();
                if (dli == null || (System.currentTimeMillis() - dli.getStartTimeStamp()) < 5000) {
                    /* wait at least 5 secs when download is running, to avoid speed fluctuations in overall ETA */
                    linkSpeed = 0;
                }
                tmp.fpSPEED += linkSpeed;
                if (linkTodo > 0) {
                    tmp.fpTODO += linkTodo;
                }
                if (tmp.fpSPEED > 0) {
                    /* we have ongoing downloads, lets calculate ETA */
                    long newfpETA = tmp.fpTODO / tmp.fpSPEED;
                    if (newfpETA > tmp.fpETA) {
                        tmp.fpETA = newfpETA;
                    }
                }
                if (linkSpeed > 0 && linkTodo >= 0) {
                    /* link is running,lets calc ETA for single link */
                    long currentETA = linkTodo / linkSpeed;
                    if (currentETA > tmp.fpETA) {
                        /*
                         * ETA for single link is bigger than ETA for all, so we use the bigger one
                         */
                        tmp.fpETA = currentETA;
                    }
                }
            }
        }

        if (link.isEnabled() && link.getFinalLinkState() == null) {
            /* we still have an enabled link which is not finished */
            tmp.allFinished = false;
        } else if (tmp.allFinished && link.getFinishedDate() > tmp.newFinishedDate) {
            /*
             * we can set latest finished date because all links till now are finished
             */
            tmp.newFinishedDate = link.getFinishedDate();
        }
    }

    @Override
    public void clear() {
        infos = new DomainInfo[0];
        items = new ArrayList<DownloadLink>();
    }

    @Override
    public List<DownloadLink> getItems() {
        return items;
    }

    public int getOfflineCount() {
        return offline;
    }

    public int getOnlineCount() {
        return online;
    }

    @Override
    public void requestUpdate() {
        updatesRequired.incrementAndGet();
    }

    @Override
    public boolean updateRequired() {
        boolean ret = updatesRequired.get() != updatesDone;
        if (ret == false) {
            ret = fp.isEnabled() && (System.currentTimeMillis() - lastUpdateTimestamp > GUIUPDATETIMEOUT) && DownloadWatchDog.getInstance().hasRunningDownloads(fp);
        }
        return ret;
    }

    private final void updateAvailability(Temp tmp) {

        if (online == tmp.children) {
            availability = ChildrenAvailablility.ONLINE;
            return;
        }
        if (offline == tmp.children) {
            availability = ChildrenAvailablility.OFFLINE;
            return;
        }
        if ((tmp.newOffline == 0 && tmp.newOnline == 0) || (tmp.newOnline == 0 && tmp.newOffline > 0)) {
            availability = ChildrenAvailablility.UNKNOWN;
            return;
        }
        availability = ChildrenAvailablility.MIXED;
        return;
    }

    @Override
    public ChildrenAvailablility getAvailability() {
        return availability;
    }

    @Override
    public String getMessage(Object requestor) {
        if (requestor instanceof AvailabilityColumn) {
            return availabilityColumnString;
        }
        return null;
    }

    public String getDownloadDirectory() {

        return fp.getDownloadDirectory();
    }

    public boolean isRunning() {
        return lastRunningState;
    }

}
