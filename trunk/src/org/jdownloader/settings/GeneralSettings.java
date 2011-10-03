package org.jdownloader.settings;

import java.util.ArrayList;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.annotations.AboutConfig;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultEnumValue;
import org.appwork.storage.config.annotations.DefaultIntValue;
import org.appwork.storage.config.annotations.DefaultValue;
import org.appwork.storage.config.annotations.Description;
import org.appwork.storage.config.annotations.SpinnerValidator;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.storage.config.handler.StorageHandler;

public interface GeneralSettings extends ConfigInterface {

    public static final GeneralSettings                 CFG = JsonConfig.create(GeneralSettings.class);
    @SuppressWarnings("unchecked")
    public static final StorageHandler<GeneralSettings> SH  = (StorageHandler<GeneralSettings>) CFG.getStorageHandler();

    @AboutConfig
    void setDefaultDownloadFolder(String ddl);

    @DefaultValue(DefaultDownloadFolder.class)
    String getDefaultDownloadFolder();

    boolean isAutoStartDownloadsOnStartupEnabled();

    void setAutoStartDownloadsOnStartupEnabled(boolean b);

    @AboutConfig
    @DefaultBooleanValue(true)
    boolean isCreatePackageNameSubFolderEnabled();

    void setCreatePackageNameSubFolderEnabled(boolean b);

    @AboutConfig
    boolean isAddNewLinksOnTop();

    void setAddNewLinksOnTop(boolean selected);

    @AboutConfig
    @DefaultBooleanValue(true)
    boolean isAutoDownloadStartAfterAddingEnabled();

    void setAutoDownloadStartAfterAddingEnabled(boolean selected);

    @AboutConfig
    boolean isAutoaddLinksAfterLinkcheck();

    void setAutoaddLinksAfterLinkcheck(boolean selected);

    ArrayList<String[]> getDownloadFolderHistory();

    void setDownloadFolderHistory(ArrayList<String[]> history);

    @AboutConfig
    @DefaultBooleanValue(true)
    boolean isHashCheckEnabled();

    void setHashCheckEnabled(boolean b);

    @AboutConfig
    @DefaultBooleanValue(true)
    boolean isAutoOpenContainerAfterDownload();

    void setAutoOpenContainerAfterDownload(boolean b);

    @AboutConfig
    @DefaultEnumValue("org.jdownloader.settings.CleanAfterDownloadAction.NEVER")
    CleanAfterDownloadAction getCleanupAfterDownloadAction();

    void setCleanupAfterDownloadAction(CleanAfterDownloadAction action);

    @AboutConfig
    @DefaultEnumValue("org.jdownloader.settings.IfFileExistsAction.ASK_FOR_EACH_FILE")
    IfFileExistsAction getIfFileExistsAction();

    void setIfFileExistsAction(IfFileExistsAction action);

    @AboutConfig
    @DefaultIntValue(0)
    @SpinnerValidator(min = 0, max = 100)
    int getMaxSimultaneDownloadsPerHost();

    void setMaxSimultaneDownloadsPerHost(int num);

    @AboutConfig
    @DefaultBooleanValue(true)
    boolean isLinkcheckEnabled();

    void setLinkcheckEnabled(boolean b);

    @AboutConfig
    @DefaultBooleanValue(true)
    boolean isCleanUpFilenames();

    void setCleanUpFilenames(boolean b);

    @AboutConfig
    @DefaultBooleanValue(true)
    boolean isClickNLoadEnabled();

    void setClickNLoadEnabled(boolean b);

    @AboutConfig
    @Description("Force Jdownloader to always keep a certain amount of MB Diskspace free")
    @DefaultIntValue(512)
    int getForcedFreeSpaceOnDisk();

    void setForcedFreeSpaceOnDisk(int mb);

    // JSonWrapper.get("DOWNLOAD").getBooleanProperty("PARAM_DOWNLOAD_AUTORESUME_ON_RECONNECT",
    // true);
    @AboutConfig
    @DefaultBooleanValue(true)
    boolean isInterruptResumeableDownloadsEnable();

    void setInterruptResumeableDownloadsEnable(boolean b);

    // JSonWrapper.get("DOWNLOAD").getIntegerProperty(Configuration.PARAM_DOWNLOAD_MAX_SIMULTAN,
    // 2);
    public static final KeyHandler MAX_SIMULTANE_DOWNLOADS = CFG.getStorageHandler().getKeyHandler("MaxSimultaneDownloads");

    @AboutConfig
    @Description("How many downloads should Jdownloader download at once? Note that most hosters allow only one download at a time in freemode")
    @SpinnerValidator(min = 1, max = 50)
    @DefaultIntValue(3)
    int getMaxSimultaneDownloads();

    void setMaxSimultaneDownloads(int num);

    // JSonWrapper.get("DOWNLOAD").getBooleanProperty("PARAM_DOWNLOAD_PREFER_RECONNECT",
    // true)
    @AboutConfig
    @Description("Do not start further downloads if others are waiting for a reconnect/new ip")
    @DefaultBooleanValue(true)
    boolean isDownloadControllerPrefersReconnectEnabled();

    void setDownloadControllerPrefersReconnectEnabled(boolean b);

    // JSonWrapper.get("DOWNLOAD").getIntegerProperty(Configuration.PARAM_DOWNLOAD_MAX_SPEED,
    // 0)
    public static final KeyHandler DOWNLOAD_SPEED_LIMIT = CFG.getStorageHandler().getKeyHandler("DownloadSpeedLimit");

    @AboutConfig
    @Description("Download Speed limit in bytes.")
    int getDownloadSpeedLimit();

    void setDownloadSpeedLimit(int bytes);

    @AboutConfig
    @Description("Pause Speed. in Pause Mode we limit speed to this value to keep connections open, but use hardly bandwidth")
    @DefaultIntValue(10 * 1024)
    int getPauseSpeed();

    void setPauseSpeed(int kb);

    public static final KeyHandler MAX_CHUNKS_PER_FILE = CFG.getStorageHandler().getKeyHandler("MaxChunksPerFile");

    @AboutConfig
    @Description("http://jdownloader.org/knowledge/wiki/glossary/chunkload")
    @DefaultIntValue(2)
    int getMaxChunksPerFile();

    void setMaxChunksPerFile(int num);

    @AboutConfig
    @Description("max buffer size for each download connection in kb")
    @SpinnerValidator(min = 100, max = 10240)
    @DefaultIntValue(500)
    int getMaxBufferSize();

    void setMaxBufferSize(int num);

    @AboutConfig
    @Description("flush download buffers when filled up to x percent (1-100)")
    @DefaultIntValue(80)
    @SpinnerValidator(min = 1, max = 100)
    int getFlushBufferLevel();

    void setFlushBufferLevel(int level);

    @AboutConfig
    @Description("flush download buffers after x ms")
    @DefaultIntValue(2 * 60 * 1000)
    int getFlushBufferTimeout();

    void setFlushBufferTimeout(int ms);

    @AboutConfig
    @Description("Timeout for connecting to a httpserver")
    @SpinnerValidator(min = 0, max = 300000)
    @DefaultIntValue(10000)
    int getHttpConnectTimeout();

    void setHttpConnectTimeout(int seconds);

    @AboutConfig
    @Description("Timeout for reading to a httpserver")
    @SpinnerValidator(min = 0, max = 300000)
    @DefaultIntValue(10000)
    int getHttpReadTimeout();

    void setHttpReadTimeout(int seconds);

    @AboutConfig
    @Description("How often a Plugin restarts a download if download failed")
    @DefaultIntValue(3)
    int getMaxPluginRetries();

    void setMaxPluginRetries(int nums);

    @AboutConfig
    @Description("Penaltytime before a retry if JDownloader lost connection")
    @DefaultIntValue(5 * 60 * 1000)
    long getWaittimeOnConnectionLoss();

    void setWaittimeOnConnectionLoss(long milliseconds);

    public static final KeyHandler DOWNLOAD_SPEED_LIMIT_ENABLED = CFG.getStorageHandler().getKeyHandler("DownloadSpeedLimitEnabled");

    @AboutConfig
    boolean isDownloadSpeedLimitEnabled();

    void setDownloadSpeedLimitEnabled(boolean b);
}
