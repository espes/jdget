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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import jd.config.Property;
import jd.controlling.downloadcontroller.SingleDownloadController;
import jd.controlling.linkcrawler.CheckableLink;
import jd.controlling.packagecontroller.AbstractNodeNotifier;
import jd.controlling.packagecontroller.AbstractPackageChildrenNode;
import jd.plugins.DownloadLinkDatabindingInterface.Key;
import jd.plugins.download.DownloadInterface;

import org.appwork.exceptions.WTFException;
import org.appwork.utils.Application;
import org.appwork.utils.NullsafeAtomicReference;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.reflection.Clazz;
import org.jdownloader.DomainInfo;
import org.jdownloader.controlling.DefaultDownloadLinkViewImpl;
import org.jdownloader.controlling.DownloadLinkView;
import org.jdownloader.controlling.Priority;
import org.jdownloader.controlling.UniqueAlltimeID;
import org.jdownloader.extensions.extraction.ExtractionStatus;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.logging.LogController;
import org.jdownloader.plugins.ConditionalSkipReason;
import org.jdownloader.plugins.FinalLinkState;
import org.jdownloader.plugins.SkipReason;

/**
 * Hier werden alle notwendigen Informationen zu einem einzelnen Download festgehalten. Die Informationen werden dann in einer Tabelle
 * dargestellt
 * 
 * @author astaldo
 */
public class DownloadLink extends Property implements Serializable, AbstractPackageChildrenNode<FilePackage>, CheckableLink {

    private static final String VARIANT_SUPPORT = "VARIANT_SUPPORT";

    public static enum AvailableStatus {
        UNCHECKED(_GUI._.linkgrabber_onlinestatus_unchecked()),
        FALSE(_GUI._.linkgrabber_onlinestatus_offline()),
        UNCHECKABLE(_GUI._.linkgrabber_onlinestatus_uncheckable()),
        TRUE(_GUI._.linkgrabber_onlinestatus_online());

        private final String exp;

        private AvailableStatus(String exp) {
            this.exp = exp;
        }

        public String getExplanation() {
            return exp;
        }
    }

    private static final String                                 PROPERTY_MD5                        = "MD5";
    private static final String                                 PROPERTY_SHA1                       = "SHA1";
    private static final String                                 PROPERTY_PASS                       = "pass";
    private static final String                                 PROPERTY_FINALFILENAME              = "FINAL_FILENAME";
    private static final String                                 PROPERTY_FORCEDFILENAME             = "FORCED_FILENAME";
    private static final String                                 PROPERTY_COMMENT                    = "COMMENT";
    private static final String                                 PROPERTY_PRIORITY                   = "PRIORITY2";
    private static final String                                 PROPERTY_FINISHTIME                 = "FINISHTIME";
    private static final String                                 PROPERTY_ENABLED                    = "ENABLED";
    private static final String                                 PROPERTY_PWLIST                     = "PWLIST";
    private static final String                                 PROPERTY_LINKDUPEID                 = "LINKDUPEID";
    private static final String                                 PROPERTY_SPEEDLIMIT                 = "SPEEDLIMIT";
    private static final String                                 PROPERTY_VERIFIEDFILESIZE           = "VERIFIEDFILESIZE";
    public static final String                                  PROPERTY_RESUMEABLE                 = "PROPERTY_RESUMEABLE";
    public static final String                                  PROPERTY_CUSTOM_LOCALFILENAME       = "CUSTOM_LOCALFILENAME";
    public static final String                                  PROPERTY_CUSTOM_LOCALFILENAMEAPPEND = "CUSTOM_LOCALFILENAMEAPPEND";                    ;
    public static final String                                  PROPERTY_DOWNLOADTIME               = "DOWNLOADTIME";
    public static final String                                  PROPERTY_ARCHIVE_ID                 = "ARCHIVE_ID";
    public static final String                                  PROPERTY_EXTRACTION_STATUS          = "EXTRACTION_STATUS";
    public static final String                                  PROPERTY_CUSTOM_MESSAGE             = "CUSTOM_MESSAGE";

    public static final int                                     LINKTYPE_CONTAINER                  = 1;

    public static final int                                     LINKTYPE_NORMAL                     = 0;

    private static final long                                   serialVersionUID                    = 1981079856214268373L;

    private static final String                                 UNKNOWN_FILE_NAME                   = "unknownFileName";
    private static final String                                 PROPERTY_CHUNKS                     = "CHUNKS";

    private transient volatile AvailableStatus                  availableStatus                     = AvailableStatus.UNCHECKED;

    @Deprecated
    private long[]                                              chunksProgress                      = null;

    /** Aktuell heruntergeladene Bytes der Datei */
    private long                                                downloadCurrent                     = 0;

    private transient volatile SingleDownloadController         downloadLinkController              = null;

    /** Maximum der heruntergeladenen Datei (Dateilaenge) */
    private long                                                downloadMax                         = -1;

    private String                                              browserurl                          = null;

    private FilePackage                                         filePackage;

    /** Hoster des Downloads */
    private String                                              host;

    /* do not remove to keep stable compatibility */
    @SuppressWarnings("unused")
    private boolean                                             isEnabled;

    @Deprecated
    private LinkStatus                                          linkStatus;

    private int                                                 linkType                            = LINKTYPE_NORMAL;

    /** Beschreibung des Downloads */
    /* kann sich noch ändern, NICHT final */
    private String                                              name;

    private transient PluginForHost                             defaultplugin;

    private transient PluginForHost                             liveplugin;

    /*
     * we need to keep this some time to perform conversion from variable to property
     */
    private String                                              finalFileName;

    /**
     * /** Von hier soll der Download stattfinden
     */
    private String                                              urlDownload;

    private transient volatile List<PluginProgress>             pluginProgress                      = null;

    private long                                                created                             = -1l;

    private transient volatile UniqueAlltimeID                  uniqueID                            = null;
    private transient AbstractNodeNotifier                      propertyListener;

    private transient DomainInfo                                domainInfo                          = null;
    private transient Boolean                                   resumeable                          = null;
    private transient volatile SkipReason                       skipReason                          = null;
    private transient volatile ConditionalSkipReason            conditionalSkipReason               = null;
    private transient volatile FinalLinkState                   finalLinkState                      = null;
    private transient AtomicBoolean                             enabled                             = new AtomicBoolean(false);
    private transient UniqueAlltimeID                           previousParent                      = null;
    private transient NullsafeAtomicReference<ExtractionStatus> extractionStatus                    = new NullsafeAtomicReference<ExtractionStatus>();
    private transient volatile LinkStatus                       currentLinkStatus                   = null;
    private transient volatile Property                         tempProperties                      = null;
    private transient volatile DownloadLinkView                 view                                = null;
    private transient NullsafeAtomicReference<LinkInfo>         linkInfo                            = new NullsafeAtomicReference<LinkInfo>();

    private transient volatile long                             lastAvailableStatusChange           = -1;

    private transient volatile FilePackage                      lastValidFilePackage                = null;

    public FilePackage getLastValidFilePackage() {
        return lastValidFilePackage;
    }

    /**
     * these properties will not be saved/restored
     * 
     * @return
     */
    public Property getTempProperties() {
        if (tempProperties != null) {
            return tempProperties;
        }
        synchronized (this) {
            Property ltempProperties = tempProperties;
            if (ltempProperties == null) {
                ltempProperties = new Property();
                tempProperties = ltempProperties;
            }
            return ltempProperties;
        }
    }

    /**
     * Erzeugt einen neuen DownloadLink
     * 
     * @param plugin
     *            Das Plugins, das fuer diesen Download zustaendig ist
     * @param name
     *            Bezeichnung des Downloads
     * @param host
     *            Anbieter, von dem dieser Download gestartet wird
     * @param urlDownload
     *            Die Download URL (Entschluesselt)
     * @param isEnabled
     *            Markiert diesen DownloadLink als aktiviert oder deaktiviert
     */
    public DownloadLink(PluginForHost plugin, String name, String host, String urlDownload, boolean isEnabled) {

        setDefaultPlugin(plugin);
        setView(new DefaultDownloadLinkViewImpl());

        setName(name);
        downloadMax = -1;
        setHost(host);
        this.isEnabled = isEnabled;
        enabled.set(isEnabled);
        created = System.currentTimeMillis();
        this.setUrlDownload(urlDownload);
        if (plugin != null && this.getDownloadURL() != null) {
            try {
                plugin.correctDownloadLink(this);
            } catch (Throwable e) {
                LogController.CL().log(e);
            }
        }
        if (name == null && urlDownload != null) {
            setName(Plugin.extractFileNameFromURL(getDownloadURL()));
        }
    }

    public long getFinishedDate() {
        return this.getLongProperty(PROPERTY_FINISHTIME, -1l);
    }

    public void setFinishedDate(long finishedDate) {
        if (finishedDate <= 0) {
            this.setProperty(PROPERTY_FINISHTIME, Property.NULL);
        } else {
            this.setProperty(PROPERTY_FINISHTIME, finishedDate);
        }
    }

    public void addDownloadTime(long time) {
        if (time < 0) {
            setProperty(PROPERTY_DOWNLOADTIME, Property.NULL);
        } else {
            setProperty(PROPERTY_DOWNLOADTIME, time + getDownloadTime());
        }
    }

    /**
     * @deprecated use {@link #getView()}
     * @return
     */
    public long getDownloadTime() {
        return getLongProperty(PROPERTY_DOWNLOADTIME, 0l);
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        /* deserialize object and then fill other stuff(transient..) */
        stream.defaultReadObject();
        setView(new DefaultDownloadLinkViewImpl());
        extractionStatus = new NullsafeAtomicReference<ExtractionStatus>();
        enabled = new AtomicBoolean(isEnabled);
        linkInfo = new NullsafeAtomicReference<LinkInfo>();
        availableStatus = AvailableStatus.UNCHECKED;
        try {
            if (linkStatus != null) {
                if (linkStatus.getStatus() == LinkStatus.FINISHED || linkStatus.hasStatus(LinkStatus.FINISHED)) {
                    setFinalLinkState(FinalLinkState.FINISHED);
                } else if (linkStatus.getStatus() == LinkStatus.ERROR_FILE_NOT_FOUND || linkStatus.hasStatus(LinkStatus.ERROR_FILE_NOT_FOUND)) {
                    setFinalLinkState(FinalLinkState.OFFLINE);
                } else if (linkStatus.getStatus() == LinkStatus.ERROR_FATAL || linkStatus.hasStatus(LinkStatus.ERROR_FATAL)) {
                    setFinalLinkState(FinalLinkState.FAILED_FATAL);
                }
            }
        } catch (final Throwable e) {
        }
        linkStatus = null;
    }

    public UniqueAlltimeID getUniqueID() {
        if (uniqueID != null) {
            return uniqueID;
        }
        synchronized (this) {
            if (uniqueID != null) {
                return uniqueID;
            }
            uniqueID = new UniqueAlltimeID();
        }
        return uniqueID;
    }

    public Priority getPriorityEnum() {
        try {
            final String priority = getStringProperty(PROPERTY_PRIORITY, null);
            if (priority == null) {
                return Priority.DEFAULT;
            }
            return Priority.valueOf(priority);
        } catch (final Throwable e) {
            return Priority.DEFAULT;
        }
    }

    public int getChunks() {
        return getIntegerProperty(PROPERTY_CHUNKS, 0);
    }

    public void setChunks(int chunks) {
        if (chunks <= 0) {
            setProperty(PROPERTY_CHUNKS, Property.NULL);
        } else {
            setProperty(PROPERTY_CHUNKS, chunks);
        }

    }

    public void setCustomSpeedLimit(int limit) {
        if (limit == 0) {
            setProperty(PROPERTY_SPEEDLIMIT, Property.NULL);
        } else {
            if (limit < 0) {
                limit = 1;
            }
            setProperty(PROPERTY_SPEEDLIMIT, limit);
        }
    }

    public int getCustomSpeedLimit() {
        return this.getIntegerProperty(PROPERTY_SPEEDLIMIT, 0);
    }

    /**
     * 
     * 
     * @return use {@link #getView()} for external usage
     */
    @Deprecated
    public long[] getChunksProgress() {
        return chunksProgress;
    }

    /**
     * returns the approximate(live) amount of downloaded bytes
     * 
     * @return Anzahl der heruntergeladenen Bytes
     * @deprecated use {@link #getView()} instead
     */
    public long getDownloadCurrent() {
        final SingleDownloadController dlc = getDownloadLinkController();
        DownloadInterface dli = null;
        if (dlc != null && (dli = dlc.getDownloadInstance()) != null) {
            if (dli.getTotalLinkBytesLoadedLive() == 0 && getDownloadCurrentRaw() != 0) {
                return getDownloadCurrentRaw();
            } else {
                return dli.getTotalLinkBytesLoadedLive();
            }
        }
        return getDownloadCurrentRaw();

    }

    /**
     * returns the exact amount of downloaded bytes (depends on DownloadInterface if this value is updated during download or at the end)
     * 
     * @return
     */
    public long getDownloadCurrentRaw() {
        return downloadCurrent;
    }

    public SingleDownloadController getDownloadLinkController() {
        return downloadLinkController;
    }

    /**
     * Die Groesse der Datei
     * 
     * @return Die Groesse der Datei
     * @deprecated use {@link #getView()} sintead
     */
    public long getDownloadSize() {
        final long verifiedFileSize = getVerifiedFileSize();
        if (verifiedFileSize >= 0) {
            return verifiedFileSize;
        }
        return Math.max(getDownloadCurrent(), downloadMax);
    }

    /**
     * Gibt die aktuelle Downloadgeschwindigkeit in bytes/sekunde zurueck
     * 
     * @return Downloadgeschwindigkeit in bytes/sekunde
     * @deprecated use {@link #getView()}
     */
    public long getDownloadSpeed() {
        final SingleDownloadController dlc = getDownloadLinkController();
        DownloadInterface dli = null;
        if (dlc != null && (dli = dlc.getDownloadInstance()) != null) {
            return dli.getManagedConnetionHandler().getSpeed();
        }
        return 0;
    }

    public DownloadLinkView getView() {
        return view;
    }

    public DownloadLinkView setView(DownloadLinkView status) {
        if (status == null) {
            throw new NullPointerException();
        }
        status.setLink(this);
        final DownloadLinkView old;
        synchronized (this) {
            old = view;
            view = status;
        }
        return old;
    }

    public String getDownloadURL() {
        return urlDownload;
    }

    public String getBrowserUrl() {
        final String lBrowserUrl = browserurl;
        if (lBrowserUrl != null) {
            return lBrowserUrl;
        }
        return getDownloadURL();
    }

    public void setBrowserUrl(String url) {
        browserurl = url;
    }

    public boolean gotBrowserUrl() {
        return browserurl != null;
    }

    public String getFileOutput() {

        return getFileOutput(false, false);
    }

    public String getFileOutputForPlugin(boolean ignoreUnsafe, boolean ignoreCustom) {
        SingleDownloadController con = getDownloadLinkController();
        if (con == null) {
            return getFileOutput(ignoreUnsafe, ignoreCustom);
        } else {
            return con.getFileOutput(ignoreUnsafe, ignoreCustom).getAbsolutePath();
        }

    }

    public String getFileOutput(boolean ignoreUnsafe, boolean ignoreCustom) {
        String downloadDirectory = getDownloadDirectory();
        String fileName = getInternalTmpFilename();
        if (!StringUtils.isEmpty(fileName) && !ignoreCustom) {
            /* we have a customized fileOutputFilename */
            return new File(downloadDirectory, fileName).getAbsolutePath();
        }
        fileName = getName(ignoreUnsafe, false);
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        String customAppend = getInternalTmpFilenameAppend();
        if (!StringUtils.isEmpty(customAppend) && !ignoreCustom) {
            fileName = fileName + customAppend;
        }
        return new File(downloadDirectory, fileName).getAbsolutePath();
    }

    public String getDownloadDirectory() {
        // workaround to redirect plugin calls via downloadcontroller.
        if (Thread.currentThread() instanceof SingleDownloadController) {
            return ((SingleDownloadController) Thread.currentThread()).getSessionDownloadDirectory();
        }
        final FilePackage fp = getLastValidFilePackage();
        if (fp != null) {
            final String downloadDirectory = fp.getDownloadDirectory();
            if (StringUtils.isEmpty(downloadDirectory)) {
                throw new WTFException("what the fuck just happened here? defaultFilePackage: " + FilePackage.isDefaultFilePackage(fp));
            }
            return downloadDirectory;
        } else {
            throw new WTFException("what the fuck just happened here? no lastValidFilePackage");
        }
    }

    /**
     * Use this if we need a tmp filename for downloading. this tmp is internal! The gui will not display it.
     * 
     * @since JD2
     */
    public String getInternalTmpFilename() {
        final String ret = this.getStringProperty(PROPERTY_CUSTOM_LOCALFILENAME, null);
        if (!StringUtils.isEmpty(ret)) {
            /* we have a customized localfilename, eg xy.tmp */
            return ret;
        }
        return null;
    }

    /**
     * Use this if we need a tmp filename for downloading. this tmp is internal! The gui will not display it.
     * 
     * @since JD2
     */
    public String getInternalTmpFilenameAppend() {
        final String ret = this.getStringProperty(PROPERTY_CUSTOM_LOCALFILENAMEAPPEND, null);
        if (!StringUtils.isEmpty(ret)) {
            /* we have a customized localfilename, eg xy.tmp */
            return ret;
        }
        return null;
    }

    /**
     * Use this if we need a tmp filename for downloading. this tmp is internal! The gui will not display it.
     * 
     * @since JD2
     */
    public void setInternalTmpFilename(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            setProperty(PROPERTY_CUSTOM_LOCALFILENAME, Property.NULL);
        } else {
            fileName = CrossSystem.alleviatePathParts(fileName);
            this.setProperty(PROPERTY_CUSTOM_LOCALFILENAME, fileName);
        }
    }

    /**
     * Use this if we need a tmp filename for downloading. this tmp is internal! The gui will not display it.
     * 
     * @since JD2
     */
    public void setInternalTmpFilenameAppend(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            setProperty(PROPERTY_CUSTOM_LOCALFILENAMEAPPEND, Property.NULL);
        } else {
            fileName = CrossSystem.alleviatePathParts(fileName);
            this.setProperty(PROPERTY_CUSTOM_LOCALFILENAMEAPPEND, fileName);
        }
    }

    /**
     * return the FilePackage that contains this DownloadLink, if none is set it will return defaultFilePackage
     * 
     * @return
     */
    public FilePackage getFilePackage() {
        final FilePackage lFilePackage = filePackage;
        if (lFilePackage == null) {
            return FilePackage.getDefaultFilePackage();
        }
        return lFilePackage;
    }

    /**
     * Gibt den Hoster dieses Links azurueck.
     * 
     * @return Der Hoster, auf dem dieser Link verweist
     */
    public String getHost() {
        return host;
    }

    public void setHost(String newHost) {
        if (newHost == null) {
            return;
        }
        if (Application.getJavaVersion() >= Application.JAVA17) {
            host = newHost.toLowerCase(Locale.ENGLISH).intern();
        } else {
            host = newHost;
        }
    }

    public LinkStatus getLinkStatus() {
        final Thread current = Thread.currentThread();
        if (current instanceof UseSetLinkStatusThread) {
            return currentLinkStatus;
        }
        final SingleDownloadController controller = getDownloadLinkController();
        if (controller != null) {
            return controller.getLinkStatus();
        }
        throw new WTFException("Cannot use getLinkStatus outside UseSetLinkStatusThread/SingleDownloadController");
    }

    public void setLinkStatus(LinkStatus linkStatus) {
        final Thread current = Thread.currentThread();
        if (current instanceof UseSetLinkStatusThread) {
            currentLinkStatus = linkStatus;
        } else {
            throw new WTFException("Cannot setLinkStatus outside UseSetLinkStatusThread");
        }
    }

    public int getLinkType() {
        return linkType;
    }

    public String getName() {
        return getName(false, false);
    }

    /**
     * 
     * 
     * priority of returned fileName 0.) tmpAsynchRenameFilename (e.g. renamed in downloadlist) 1.) forcedFileName (eg manually set)
     * 
     * 2.) finalFileName (eg set by plugin where the final is 100% safe, eg API)
     * 
     * 3.) unsafeFileName (eg set by plugin when no api is available, or no filename provided)
     * 
     * @param ignoreUnsafe
     * @param ignoreForcedFilename
     *            TODO
     * @return
     */
    public String getName(boolean ignoreUnsafe, boolean ignoreForcedFilename) {
        String ret = this.getForcedFileName();
        if (ret != null) {
            return ret;
        }
        ret = this.getFinalFileName();
        if (ret != null) {
            return ret;
        }
        if (ignoreUnsafe) {
            return null;
        }
        try {
            if (name != null) {
                return name;
            }
            if (this.getDownloadURL() != null) {
                String urlName = new File(new URL(this.getDownloadURL()).toURI()).getName();
                if (urlName != null) {
                    return urlName;
                }
            }
            return UNKNOWN_FILE_NAME;
        } catch (Exception e) {
            return UNKNOWN_FILE_NAME;
        }
    }

    public LinkInfo getLinkInfo() {
        LinkInfo ret = linkInfo.get();
        if (ret == null) {
            ret = LinkInfo.getLinkInfo(this);
            linkInfo.set(ret);
        }
        return ret;
    }

    private void setLinkInfo(LinkInfo linkInfo) {
        if (linkInfo != null) {
            this.linkInfo.set(linkInfo);
        } else {
            this.linkInfo.getAndClear();
        }
    }

    /**
     * returns fileName set by plugin (setFinalFileName)
     * 
     * @return
     */
    public String getNameSetbyPlugin() {
        String ret = this.getFinalFileName();
        if (ret != null) {
            return ret;
        }
        return name;
    }

    /**
     * Liefert das Plugin zurueck, dass diesen DownloadLink handhabt
     * 
     * @return Das Plugin
     */
    public PluginForHost getDefaultPlugin() {
        return defaultplugin;
    }

    public PluginForHost getLivePlugin() {
        return liveplugin;
    }

    public String getComment() {
        return this.getStringProperty(PROPERTY_COMMENT, null);
    }

    @Deprecated
    public List<String> getSourcePluginPasswordList() {
        final Object ret = this.getProperty(PROPERTY_PWLIST);
        if (ret != null && ret instanceof List) {
            return (List<String>) ret;
        }
        return null;
    }

    /**
     * Gibt den Finalen Downloadnamen zurueck. Wird null zurueckgegeben, so wird der dateiname von den jeweiligen plugins automatisch
     * ermittelt.
     * 
     * @return Statischer Dateiname
     */
    public String getFinalFileName() {
        if (finalFileName != null) {
            /* convert existing finalFileName into Property System */
            String lfinalFileName = finalFileName;
            finalFileName = null;
            this.setFinalFileName(lfinalFileName);
        }
        return this.getStringProperty(PROPERTY_FINALFILENAME, null);
    }

    public String getForcedFileName() {
        // workaround. all plugin calls should return the forced filename from the singledownloadcontroller - if available
        if (Thread.currentThread() instanceof SingleDownloadController) {
            return ((SingleDownloadController) Thread.currentThread()).getSessionDownloadFilename();
        }
        return this.getStringProperty(PROPERTY_FORCEDFILENAME, null);
    }

    public String getLinkID() {
        final String ret = this.getStringProperty(PROPERTY_LINKDUPEID, null);
        return ret;
    }

    /**
     * Sets DownloadLinks Unquie ID
     * 
     * @param id
     * @since JD2
     */
    public void setLinkID(String id) {
        if (StringUtils.isEmpty(id)) {
            this.setProperty(PROPERTY_LINKDUPEID, Property.NULL);
        } else {
            this.setProperty(PROPERTY_LINKDUPEID, id);
        }
    }

    /*
     * Gibt zurueck ob Dieser Link schon auf verfuegbarkeit getestet wurde.+ Diese FUnktion fuehrt keinen!! Check durch. Sie prueft nur ob
     * schon geprueft worden ist. anschiessend kann mit isAvailable() die verfuegbarkeit ueberprueft werden
     * 
     * @return Link wurde schon getestet (true) nicht getestet(false)
     */
    public boolean isAvailabilityStatusChecked() {
        return availableStatus != AvailableStatus.UNCHECKED;
    }

    /**
     * Returns if the downloadLInk is available
     * 
     * @return true/false
     */
    public boolean isAvailable() {
        return availableStatus != AvailableStatus.FALSE;
    }

    /*
     * WARNING: do not use withing plugins!
     */
    public AvailableStatus getAvailableStatus() {
        return availableStatus;
    }

    public void setAvailableStatus(AvailableStatus availableStatus) {
        if (availableStatus == null) {
            availableStatus = AvailableStatus.UNCHECKED;
        }
        if (AvailableStatus.UNCHECKED.equals(availableStatus)) {
            lastAvailableStatusChange = -1;
        } else {
            lastAvailableStatusChange = System.currentTimeMillis();
        }
        if (this.availableStatus == availableStatus) {
            return;
        }
        this.availableStatus = availableStatus;
        switch (availableStatus) {
        case FALSE:
            if (getFinalLinkState() == null) {
                setFinalLinkState(FinalLinkState.OFFLINE);
            }
            break;
        case TRUE:
            if (FinalLinkState.OFFLINE.equals(getFinalLinkState())) {
                setFinalLinkState(null);
            }
            break;
        }
        if (hasNotificationListener()) {
            notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.AVAILABILITY, availableStatus));
        }
    }

    public long getLastAvailableStatusChange() {
        return lastAvailableStatusChange;
    }

    private void notifyChanges(AbstractNodeNotifier.NOTIFY notify, Object param) {
        final AbstractNodeNotifier pl = propertyListener;
        if (pl != null) {
            pl.nodeUpdated(this, notify, param);
            return;
        }
        final AbstractNodeNotifier pl2 = filePackage;
        if (pl2 != null) {
            pl2.nodeUpdated(this, notify, param);
        }
    }

    public boolean hasNotificationListener() {
        AbstractNodeNotifier pl = propertyListener;
        if (pl != null && pl.hasNotificationListener()) {
            return true;
        }
        pl = filePackage;
        if (pl != null && pl.hasNotificationListener()) {
            return true;
        }
        return false;
    }

    public void reset(List<PluginForHost> resetPlugins) {
        setInternalTmpFilenameAppend(null);
        setInternalTmpFilename(null);
        setFinalFileName(null);
        setFinalLinkState(null);
        long size = getView().getBytesTotal();
        setVerifiedFileSize(-1);
        if (size >= 0) {
            setDownloadSize(size);
        }
        setChunksProgress(null);
        setChunks(0);
        setCustomSpeedLimit(0);
        setDownloadCurrent(0);
        setFinishedDate(-1l);
        addDownloadTime(-1);
        setAvailableStatus(AvailableStatus.UNCHECKED);
        setSkipReason(null);
        setConditionalSkipReason(null);
        setEnabled(true);
        setLinkInfo(null);
        setExtractionStatus(null);
        if (resetPlugins != null) {
            for (PluginForHost resetPlugin : resetPlugins) {
                try {
                    resetPlugin.resetDownloadlink(this);
                } catch (final Throwable e) {
                    LogController.CL().log(e);
                }
            }
        }
        if (hasNotificationListener()) {
            notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.RESET, null));
        }
    }

    public void resume(List<PluginForHost> resetPlugins) {
        setAvailableStatus(AvailableStatus.UNCHECKED);
        setSkipReason(null);
        setConditionalSkipReason(null);
        setEnabled(true);
        if (resetPlugins != null) {
            for (PluginForHost resetPlugin : resetPlugins) {
                try {
                    resetPlugin.resumeDownloadlink(this);
                } catch (final Throwable e) {
                    LogController.CL().log(e);
                }
            }
        }
    }

    public void setAvailable(boolean available) {
        setAvailableStatus(available ? AvailableStatus.TRUE : AvailableStatus.FALSE);
    }

    /**
     * do not use this method, only kept for compatibility reasons and some plugins need it
     * 
     * @param is
     */
    @Deprecated
    public void setChunksProgress(long[] is) {
        chunksProgress = is;
    }

    /**
     * Setzt die Anzahl der heruntergeladenen Bytes fest und aktualisiert die Fortschrittsanzeige
     * 
     * @param downloadedCurrent
     *            Anzahl der heruntergeladenen Bytes
     * 
     */
    public void setDownloadCurrent(long downloadedCurrent) {
        if (getDownloadCurrentRaw() == downloadedCurrent) {
            return;
        }
        downloadCurrent = downloadedCurrent;
        if (hasNotificationListener() && this.getCurrentDownloadInterface() == null) {
            notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, null);
        }
    }

    private DownloadInterface getCurrentDownloadInterface() {
        SingleDownloadController dlc = getDownloadLinkController();
        DownloadInterface dli = null;
        if (dlc != null && (dli = dlc.getDownloadInstance()) != null) {
            return dli;
        }
        return null;
    }

    /**
     * do not call this method. Only The Downloadwatchdog queue is allowed to call this method
     * 
     * @param downloadLinkController
     */
    public void setDownloadLinkController(SingleDownloadController downloadLinkController) {
        final SingleDownloadController old;
        // sync is not required. this method is only called from the downloadwatchdog queue
        old = this.downloadLinkController;
        this.downloadLinkController = downloadLinkController;

        if (old != null && old != downloadLinkController) {
            old.onDetach(this);
        }
        if (old != downloadLinkController && downloadLinkController != null) {
            downloadLinkController.onAttach(this);
        }
    }

    /**
     * Setzt die Groesse der herunterzuladenden Datei
     * 
     * @param downloadMax
     *            Die Groesse der Datei
     */
    public void setDownloadSize(long downloadMax) {
        if (this.downloadMax == downloadMax) {
            return;
        }
        this.downloadMax = Math.max(-1, downloadMax);
        if (hasNotificationListener() && this.getCurrentDownloadInterface() == null) {
            notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, null);
        }
    }

    /**
     * Zeigt, ob dieser Download aktiviert ist
     * 
     * @return wahr, falls dieser DownloadLink aktiviert ist
     */
    public boolean isEnabled() {
        return enabled.get();
    }

    /**
     * changes the enabled status of this DownloadLink, aborts download if its currently running
     */
    public void setEnabled(boolean isEnabled) {
        if (enabled.getAndSet(isEnabled) == isEnabled) {
            return;
        }
        if (isEnabled == false) {
            setProperty(PROPERTY_ENABLED, isEnabled);
        } else {
            setProperty(PROPERTY_ENABLED, Property.NULL);
        }
        if (hasNotificationListener()) {
            notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.ENABLED, isEnabled));
        }
    }

    /**
     * Zeigt, ob dieser Download aktiviert ist
     * 
     * @return wahr, falls dieser DownloadLink aktiviert ist
     */
    public boolean isSkipped() {
        return skipReason != null;
    }

    /**
     * changes the enabled status of this DownloadLink, aborts download if its currently running
     */
    public SkipReason setSkipReason(SkipReason skipReason) {
        final SkipReason old;
        synchronized (this) {
            old = this.skipReason;
            this.skipReason = skipReason;
        }
        if (old != skipReason && hasNotificationListener()) {
            notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.SKIPPED, skipReason));
        }
        return old;
    }

    public FinalLinkState setFinalLinkState(FinalLinkState finalLinkState) {
        final FinalLinkState old;
        synchronized (this) {
            old = this.finalLinkState;
            this.finalLinkState = finalLinkState;
        }
        if (old != finalLinkState) {
            if (FinalLinkState.CheckFinished(finalLinkState)) {
                setResumeable(false);
            }
            if (finalLinkState == null || !FinalLinkState.CheckFinished(finalLinkState)) {
                setFinishedDate(-1);
            }
            if (finalLinkState == FinalLinkState.OFFLINE) {
                setAvailable(false);
            }
            if (finalLinkState != FinalLinkState.FAILED_FATAL) {
                setProperty(PROPERTY_CUSTOM_MESSAGE, Property.NULL);
            }
            if (hasNotificationListener()) {
                notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.FINAL_STATE, finalLinkState));
            }
        }
        return old;
    }

    public FinalLinkState getFinalLinkState() {
        return finalLinkState;
    }

    public ConditionalSkipReason setConditionalSkipReason(ConditionalSkipReason conditionalSkipReason) {
        final ConditionalSkipReason old;
        synchronized (this) {
            old = this.conditionalSkipReason;
            this.conditionalSkipReason = conditionalSkipReason;
        }
        if (old != conditionalSkipReason && hasNotificationListener()) {
            notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.CONDITIONAL_SKIPPED, conditionalSkipReason));
        }
        return old;
    }

    public ConditionalSkipReason getConditionalSkipReason() {
        return conditionalSkipReason;
    }

    public SkipReason getSkipReason() {
        return skipReason;
    }

    public void setLinkType(int linktypeContainer) {
        if (linktypeContainer == linkType) {
            return;
        }
        if (linkType == LINKTYPE_CONTAINER) {
            System.out.println("You are not allowd to Change the Linktype of " + this);
            return;
        }
        linkType = linktypeContainer;
    }

    /**
     * Setzt nachtraeglich das Plugin. Wird nur zum Laden der Liste benoetigt
     * 
     * @param plugin
     *            Das fuer diesen Download zustaendige Plugin
     */
    public void setDefaultPlugin(PluginForHost plugin) {
        this.defaultplugin = plugin;
    }

    public void setLivePlugin(PluginForHost plugin) {
        final PluginForHost oldLivePlugin = liveplugin;
        this.liveplugin = plugin;
        if (plugin != null) {
            plugin.setDownloadLink(this);
        }
        if (oldLivePlugin != null && oldLivePlugin != plugin) {
            oldLivePlugin.setDownloadLink(null);
        }
    }

    /**
     * Setzt den Namen des Downloads neu
     * 
     * @param name
     *            Neuer Name des Downloads
     */
    public void setName(String name) {
        String oldName = getName();
        if (StringUtils.isEmpty(name)) {
            name = Plugin.extractFileNameFromURL(getDownloadURL());
        }
        if (!StringUtils.isEmpty(name)) {
            name = CrossSystem.alleviatePathParts(name);
        }
        if (StringUtils.isEmpty(name)) {
            name = UNKNOWN_FILE_NAME;
        }
        this.name = name;
        setLinkInfo(null);
        final String newName = getName();
        if (!StringUtils.equals(oldName, newName) && hasNotificationListener()) {
            notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.NAME, newName));
        }
    }

    /**
     * use this function to force a name, it has highest priority
     */
    public void forceFileName(String name) {
        String oldName = getName();
        if (StringUtils.isEmpty(name)) {
            this.setProperty(PROPERTY_FORCEDFILENAME, Property.NULL);
            oldName = getName();
        } else {
            name = CrossSystem.alleviatePathParts(name);
            this.setProperty(PROPERTY_FORCEDFILENAME, name);
        }
        setLinkInfo(null);
        final String newName = getName();
        if (!StringUtils.equals(oldName, newName) && hasNotificationListener()) {
            notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.NAME, newName));
        }
    }

    /**
     * WARNING: DO NOT use in 09581 stable!
     * 
     * @since JD2
     */
    public void setComment(String comment) {
        if (comment == null || comment.length() == 0) {
            this.setProperty(PROPERTY_COMMENT, Property.NULL);
        } else {
            this.setProperty(PROPERTY_COMMENT, comment);
        }
    }

    @Deprecated
    public DownloadLink setSourcePluginPasswordList(ArrayList<String> sourcePluginPassword) {
        if (sourcePluginPassword == null || sourcePluginPassword.size() == 0) {
            this.setProperty(PROPERTY_PWLIST, Property.NULL);
        } else {
            this.setProperty(PROPERTY_PWLIST, sourcePluginPassword);
        }
        return this;
    }

    /**
     * Filename Setter for Plugins if the plugin is 100% sure that this is the correct filename
     * 
     * @param newfinalFileName
     */
    public void setFinalFileName(String newfinalFileName) {
        final String oldName = getName();
        finalFileName = null;
        if (!StringUtils.isEmpty(newfinalFileName)) {
            if (new Regex(newfinalFileName, Pattern.compile("r..\\.htm.?$", Pattern.CASE_INSENSITIVE)).matches()) {
                System.out.println("Use Workaround for stupid >>rar.html<< uploaders!");
                newfinalFileName = newfinalFileName.substring(0, newfinalFileName.length() - new Regex(newfinalFileName, Pattern.compile("r..(\\.htm.?)$", Pattern.CASE_INSENSITIVE)).getMatch(0).length());
            }
            this.setProperty(PROPERTY_FINALFILENAME, newfinalFileName = CrossSystem.alleviatePathParts(newfinalFileName));
            setName(newfinalFileName);
        } else {
            this.setProperty(PROPERTY_FINALFILENAME, Property.NULL);
        }
        setLinkInfo(null);
        final String newName = getName();
        if (!StringUtils.equals(oldName, newName) && hasNotificationListener()) {
            notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.NAME, newName));
        }
    }

    /**
     * Setzt die URL, von der heruntergeladen werden soll
     * 
     * @param urlDownload
     *            Die URL von der heruntergeladen werden soll
     */
    public void setUrlDownload(String urlDownload) {
        String previousURLDownload = this.urlDownload;
        if (urlDownload != null) {
            if (previousURLDownload != null && previousURLDownload.equals(urlDownload)) {
                return;
            }
            this.urlDownload = new String(urlDownload.trim());
        } else {
            this.urlDownload = null;
        }
        if (previousURLDownload != null && !previousURLDownload.equals(urlDownload)) {
            if (getLinkID() == null) {
                /* downloadURL changed, so set original one as linkID, so all dupemaps still work */
                setLinkID(previousURLDownload);
            }
        }
    }

    /**
     * Diese Methhode fragt das eigene Plugin welche Informationen ueber die File bereit gestellt werden. Der String eignet Sich zur
     * Darstellung in der UI
     */
    @Override
    public String toString() {
        if (getPreviousParentNodeID() == null) {
            return getName();
        }
        if (getPreviousParentNodeID().equals(getParentNode().getUniqueID())) {
            return getName();
        }
        return getName() + " previousParentNode:" + getPreviousParentNodeID();

    }

    /**
     * returns real downloadMAx Value. use #getDownloadSize if you are not sure
     * 
     * @return use {@link #getView()} for external handling
     */
    public long getKnownDownloadSize() {
        long ret = getVerifiedFileSize();
        if (ret >= 0) {
            return ret;
        }
        return downloadMax;
    }

    /**
     * DO NOT USE in 09581 Stable
     * 
     * @return
     * @since JD2
     */
    public String getDownloadPassword() {
        return getStringProperty(PROPERTY_PASS, null);
    }

    /**
     * DO NOT USE in 09581 Stable
     * 
     * @return
     * @since JD2
     */
    public void setDownloadPassword(String pass) {
        if (StringUtils.isEmpty(pass)) {
            this.setProperty(PROPERTY_PASS, Property.NULL);
        } else {
            this.setProperty(PROPERTY_PASS, pass);
        }
    }

    public void setMD5Hash(String md5) {
        // validate md5 String is a MD5 hash!
        if (StringUtils.isEmpty(md5) || !md5.trim().matches("[a-fA-F0-9]{32}")) {
            this.setProperty(PROPERTY_MD5, Property.NULL);
        } else {
            this.setProperty(PROPERTY_MD5, md5.trim());
            this.setProperty(PROPERTY_SHA1, Property.NULL);
        }
    }

    public void firePropertyChanged(DownloadLinkProperty.Property property, Object param) {
        if (hasNotificationListener()) {
            notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, property, param));
        }

    }

    public String getMD5Hash() {
        return getStringProperty(PROPERTY_MD5, (String) null);
    }

    public void addPluginProgress(final PluginProgress progress) {
        if (progress != null) {
            synchronized (this) {
                List<PluginProgress> lPluginProgress = pluginProgress;
                if (lPluginProgress == null) {
                    /* to avoid concurrentmodificationexception */
                    lPluginProgress = new CopyOnWriteArrayList<PluginProgress>();
                }
                if (!lPluginProgress.contains(progress)) {
                    lPluginProgress.add(0, progress);
                } else if (lPluginProgress.get(0) != progress) {
                    lPluginProgress.add(0, progress);
                    final int index = lPluginProgress.lastIndexOf(progress);
                    lPluginProgress.remove(index);
                } else {
                    return;
                }
                /* pluginProgress must always contain at least 1 item, see getPluginProgress */
                pluginProgress = lPluginProgress;
            }
            if (hasNotificationListener()) {
                notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.PLUGIN_PROGRESS, progress));
            }
        }
    }

    public boolean removePluginProgress(final PluginProgress remove) {
        if (remove != null) {
            final PluginProgress latest;
            synchronized (this) {
                List<PluginProgress> lPluginProgress = pluginProgress;
                if (lPluginProgress == null || lPluginProgress.contains(remove) == false) {
                    return false;
                }
                if (lPluginProgress.size() > 1) {
                    lPluginProgress.remove(remove);
                    latest = lPluginProgress.get(0);
                } else {
                    latest = null;
                    pluginProgress = null;
                }
            }
            if (hasNotificationListener()) {
                notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.PLUGIN_PROGRESS, latest));
            }
            return true;
        }
        return false;
    }

    public PluginProgress getPluginProgress() {
        final List<PluginProgress> lPluginProgress = pluginProgress;
        if (lPluginProgress != null) {
            try {
                return lPluginProgress.get(0);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean hasPluginProgress(PluginProgress contains) {
        final List<PluginProgress> lPluginProgress = pluginProgress;
        return lPluginProgress != null && lPluginProgress.contains(contains);
    }

    public void setSha1Hash(String sha1) {
        // validate sha1 String is a SHA1 hash!
        if (StringUtils.isEmpty(sha1) || !sha1.trim().matches("[a-fA-F0-9]{40}")) {
            this.setProperty(PROPERTY_SHA1, Property.NULL);
        } else {
            this.setProperty(PROPERTY_SHA1, sha1.trim());
            this.setProperty(PROPERTY_MD5, Property.NULL);
        }
    }

    public String getSha1Hash() {
        return getStringProperty(PROPERTY_SHA1, (String) null);
    }

    /**
     * Do not use in Plugins for old Stable, or use try/catch or set property manually
     * 
     * @param size
     */
    public void setVerifiedFileSize(long size) {
        setDownloadSize(size);
        if (size < 0) {
            setProperty(DownloadLink.PROPERTY_VERIFIEDFILESIZE, Property.NULL);
        } else {
            setProperty(DownloadLink.PROPERTY_VERIFIEDFILESIZE, size);
        }
    }

    /**
     * use {@link #getView()} for external handling
     * 
     * @return
     */
    public long getVerifiedFileSize() {
        return getLongProperty(PROPERTY_VERIFIEDFILESIZE, -1);
    }

    /**
     * Do not use in Plugins for old Stable, or use try/catch or set property manually
     * 
     * @param size
     */
    public void setResumeable(boolean b) {
        resumeable = b;
        if (!b) {
            setProperty(PROPERTY_RESUMEABLE, Property.NULL);
        } else {
            setProperty(PROPERTY_RESUMEABLE, true);
        }
    }

    public boolean isResumeable() {
        if (resumeable != null) {
            return resumeable;
        }
        resumeable = getBooleanProperty(PROPERTY_RESUMEABLE, false);
        return resumeable;
    }

    public DomainInfo getDomainInfo() {
        if (domainInfo == null) {
            DomainInfo newDomainInfo = null;
            if (defaultplugin != null) {
                newDomainInfo = defaultplugin.getDomainInfo(this);
            }
            if (newDomainInfo == null) {
                newDomainInfo = DomainInfo.getInstance(getHost());
            }
            domainInfo = newDomainInfo;
        }
        return domainInfo;
    }

    public FilePackage getParentNode() {
        return getFilePackage();
    }

    /**
     * set the FilePackage that contains this DownloadLink, DO NOT USE this if you want to add this DownloadLink to a FilePackage
     * 
     * @param filePackage
     */
    public synchronized void _setFilePackage(FilePackage filePackage) {
        if (filePackage == this.filePackage) {
            previousParent = null;
            return;
        }
        if (FilePackage.isDefaultFilePackage(filePackage)) {
            filePackage = null;
        }
        if (this.filePackage != null && filePackage != null) {
            this.filePackage.remove(this);
        }
        if (this.filePackage != null) {
            this.previousParent = this.filePackage.getUniqueID();
        }
        if (filePackage != null) {
            lastValidFilePackage = filePackage;
        }
        this.filePackage = filePackage;
    }

    public UniqueAlltimeID getPreviousParentNodeID() {
        return previousParent;
    }

    public void setParentNode(FilePackage parent) {
        _setFilePackage(parent);
    }

    public DownloadLink getDownloadLink() {
        return this;
    }

    public void setNodeChangeListener(AbstractNodeNotifier propertyListener) {
        this.propertyListener = propertyListener;
    }

    public void setPriorityEnum(Priority priority) {
        if (priority == null) {
            priority = Priority.DEFAULT;
        }
        if (getPriorityEnum() != priority) {
            if (Priority.DEFAULT.equals(priority)) {
                setProperty(PROPERTY_PRIORITY, Property.NULL);
            } else {
                setProperty(PROPERTY_PRIORITY, priority.name());
            }
            if (hasNotificationListener()) {
                notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.PRIORITY, priority));
            }
        }
    }

    public String getArchiveID() {
        return getStringProperty(DownloadLink.PROPERTY_ARCHIVE_ID);
    }

    public void setArchiveID(String id) {
        if (!StringUtils.isEmpty(id)) {
            setProperty(DownloadLink.PROPERTY_ARCHIVE_ID, id);
        } else {
            setProperty(DownloadLink.PROPERTY_ARCHIVE_ID, Property.NULL);
        }
    }

    public ExtractionStatus getExtractionStatus() {
        if (extractionStatus.isValueSet()) {
            return extractionStatus.get();
        }
        String string = getStringProperty(PROPERTY_EXTRACTION_STATUS, null);
        ExtractionStatus ret = null;
        try {
            if (string != null) {
                ret = ExtractionStatus.valueOf(string);
            }
        } catch (Exception e) {
        }
        extractionStatus.set(ret);
        return ret;
    }

    public void setExtractionStatus(ExtractionStatus newExtractionStatus) {
        ExtractionStatus old = extractionStatus.getAndSet(newExtractionStatus);
        if (old != newExtractionStatus) {
            if (newExtractionStatus == null) {
                setProperty(DownloadLink.PROPERTY_EXTRACTION_STATUS, Property.NULL);
            } else {
                setProperty(DownloadLink.PROPERTY_EXTRACTION_STATUS, newExtractionStatus.name());
            }
            if (hasNotificationListener()) {
                notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, new DownloadLinkProperty(this, DownloadLinkProperty.Property.EXTRACTION_STATUS, newExtractionStatus));
            }
            return;
        }
    }

    public void setVariantSupport(boolean b) {
        if (b) {
            setProperty(VARIANT_SUPPORT, b);
        } else {
            setProperty(VARIANT_SUPPORT, Property.NULL);
        }
    }

    public boolean hasVariantSupport() {
        return getBooleanProperty(VARIANT_SUPPORT, false);
    }

    public static <T extends DownloadLinkDatabindingInterface> T bindData(final Property property, final String ID, final Class<T> clazz) {
        @SuppressWarnings("unchecked")
        final T ret = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] { clazz }, new InvocationHandler() {

            public String getKey(Method method) {
                String key = null;
                if (method.getName().startsWith("set")) {
                    key = method.getName().substring(3).replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ENGLISH);
                } else if (method.getName().startsWith("is")) {
                    key = method.getName().substring(2).replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ENGLISH);
                } else if (method.getName().startsWith("get")) {
                    key = method.getName().substring(3).replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ENGLISH);
                } else {
                    return null;
                }
                Key keyAnnotation = method.getAnnotation(Key.class);
                if (keyAnnotation != null) {
                    key = keyAnnotation.value();
                }
                if (ID != null) {
                    key = ID + key;
                }
                return key;
            }

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("reset")) {
                    if (!Clazz.isVoid(method.getReturnType())) {
                        throw new WTFException("reset must have void as return type.");
                    }
                    HashSet<String> reset = new HashSet<String>();
                    for (Method resetMethod : clazz.getDeclaredMethods()) {
                        String key = getKey(resetMethod);
                        if (key != null && reset.add(key)) {
                            property.removeProperty(key);
                        }
                    }
                    return null;
                }
                String key = getKey(method);
                if (key == null) {
                    throw new WTFException("Only Setter and getter are allowed");
                }
                if (method.getName().startsWith("set")) {

                    if (method.getParameterTypes().length != 1) {
                        throw new WTFException("Setter " + method + " should have 1 parameter. instead: " + Arrays.toString(method.getParameterTypes()));
                    }
                    if (!Clazz.isVoid(method.getReturnType())) {
                        throw new WTFException("Setter " + method + " must not have any return type. Has: " + method.getReturnType());
                    }

                    Class<?> param = method.getParameterTypes()[0];
                    Object arg = args[0];
                    if (Clazz.isPrimitiveWrapper(param) && arg == null) {
                        property.setProperty(key, Property.NULL);
                        return null;
                    }
                    if (Clazz.isEnum(param)) {
                        if (arg == null) {
                            property.setProperty(key, Property.NULL);
                        } else {
                            property.setProperty(key, ((Enum<?>) arg).name());
                        }
                        return null;
                    }
                    property.setProperty(key, arg);
                } else {
                    Type returnType = method.getGenericReturnType();
                    if (method.getParameterTypes().length != 0) {
                        throw new WTFException("Getter " + method + " must not have any parameter. instead: " + Arrays.toString(method.getParameterTypes()));
                    }
                    if (Clazz.isVoid(method.getReturnType())) {
                        throw new WTFException("Getter " + method + " must have a return type. is Void.");
                    }

                    Object value = property.getProperty(key);
                    if (Clazz.isBoolean(returnType)) {
                        if (value == null) {
                            if (Clazz.isPrimitiveWrapper(returnType)) {
                                return null;
                            }
                            return false;
                        }
                        if (value instanceof Boolean) {
                            return value;
                        }
                    } else if (Clazz.isByte(returnType)) {
                        if (value == null) {
                            if (Clazz.isPrimitiveWrapper(returnType)) {
                                return null;
                            }
                            return (byte) 0;
                        }
                        if (value instanceof Number) {
                            return ((Number) value).byteValue();
                        }
                    } else if (Clazz.isDouble(returnType)) {
                        if (value == null) {
                            if (Clazz.isPrimitiveWrapper(returnType)) {
                                return null;
                            }
                            return 0d;
                        }
                        if (value instanceof Number) {
                            return ((Number) value).doubleValue();
                        }
                    } else if (Clazz.isEnum(returnType)) {
                        if (value == null) {
                            return null;
                        }
                        if (value instanceof Enum) {
                            return value;
                        }
                        if (value instanceof String) {
                            return Enum.valueOf(((Enum<?>) returnType).getDeclaringClass(), (String) value);
                        }
                    } else if (Clazz.isFloat(returnType)) {
                        if (value == null) {
                            if (Clazz.isPrimitiveWrapper(returnType)) {
                                return null;
                            }
                            return 0f;
                        }
                        if (value instanceof Number) {
                            return ((Number) value).floatValue();
                        }
                    } else if (Clazz.isInteger(returnType)) {
                        if (value == null) {
                            if (Clazz.isPrimitiveWrapper(returnType)) {
                                return null;
                            }
                            return 0;
                        }
                        if (value instanceof Number) {
                            return ((Number) value).intValue();
                        }
                    } else if (Clazz.isLong(returnType)) {
                        if (value == null) {
                            if (Clazz.isPrimitiveWrapper(returnType)) {
                                return null;
                            }
                            return 0l;
                        }
                        if (value instanceof Number) {
                            return ((Number) value).longValue();
                        }
                    } else if (Clazz.isShort(returnType)) {
                        if (value == null) {
                            if (Clazz.isPrimitiveWrapper(returnType)) {
                                return null;
                            }
                            return (short) 0;
                        }
                        if (value instanceof Number) {
                            return ((Number) value).shortValue();
                        }
                    } else if (Clazz.isString(returnType)) {
                        if (value == null) {
                            return null;
                        }
                        if (value instanceof String) {
                            return value;
                        }
                    } else {
                        return value;
                    }
                    throw new WTFException("Cannot restore " + returnType + " from " + value);
                }
                return null;
            }
        });
        return ret;
    }

    public <T extends DownloadLinkDatabindingInterface> T bindData(Class<T> clazz) {
        return bindData(this, null, clazz);
    }

    public void firePropertyChange(DownloadLinkProperty downloadLinkProperty) {
        notifyChanges(AbstractNodeNotifier.NOTIFY.PROPERTY_CHANCE, downloadLinkProperty);
    }

}