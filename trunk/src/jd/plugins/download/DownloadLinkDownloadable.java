package jd.plugins.download;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.controlling.downloadcontroller.DownloadWatchDog.DISKSPACECHECK;
import jd.controlling.downloadcontroller.ExceptionRunnable;
import jd.controlling.downloadcontroller.FileIsLockedException;
import jd.controlling.downloadcontroller.ManagedThrottledConnectionHandler;
import jd.controlling.downloadcontroller.SingleDownloadController;
import jd.http.Browser;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.PluginForHost;
import jd.plugins.PluginProgress;
import jd.plugins.download.raf.HashResult;

import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.StringUtils;
import org.appwork.utils.logging2.LogSource;
import org.jdownloader.controlling.FileCreationManager;
import org.jdownloader.downloadcore.v15.Downloadable;
import org.jdownloader.downloadcore.v15.HashInfo;
import org.jdownloader.plugins.FinalLinkState;
import org.jdownloader.statistics.StatsManager;

public class DownloadLinkDownloadable implements Downloadable {
    /**
     * 
     */

    private final DownloadLink downloadLink;
    private PluginForHost      plugin;

    public DownloadLinkDownloadable(DownloadLink downloadLink) {

        this.downloadLink = downloadLink;
        plugin = downloadLink.getLivePlugin();
    }

    @Override
    public void setResumeable(boolean value) {
        downloadLink.setResumeable(value);
    }

    @Override
    public Browser getContextBrowser() {
        return plugin.getBrowser().cloneBrowser();

    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public void setDownloadInterface(DownloadInterface di) {
        plugin.setDownloadInterface(di);
    }

    @Override
    public void setFilesizeCheck(boolean b) {
        downloadLink.setProperty(RAFDownload.PROPERTY_DOFILESIZECHECK, b);
    }

    @Override
    public long getVerifiedFileSize() {
        return downloadLink.getVerifiedFileSize();
    }

    @Override
    public boolean isServerComaptibleForByteRangeRequest() {
        return downloadLink.getBooleanProperty("ServerComaptibleForByteRangeRequest", false);
    }

    @Override
    public String getHost() {
        return downloadLink.getHost();
    }

    @Override
    public boolean isDebug() {
        return this.plugin.getBrowser().isDebug();
    }

    @Override
    public void setDownloadTotalBytes(long l) {

        downloadLink.setDownloadSize(l);
    }

    @Override
    public long[] getChunksProgress() {
        return downloadLink.getChunksProgress();
    }

    @Override
    public void setChunksProgress(long[] ls) {
        downloadLink.setChunksProgress(ls);
    }

    @Override
    public void setLinkStatus(int finished) {
        downloadLink.getDownloadLinkController().getLinkStatus().setStatus(finished);
    }

    @Override
    public void setVerifiedFileSize(long length) {
        downloadLink.setVerifiedFileSize(length);
    }

    @Override
    public void validateLastChallengeResponse() {
        plugin.validateLastChallengeResponse();
    }

    @Override
    public void setConnectionHandler(ManagedThrottledConnectionHandler managedConnetionHandler) {
        downloadLink.getDownloadLinkController().getConnectionHandler().addConnectionHandler(managedConnetionHandler);
    }

    @Override
    public void removeConnectionHandler(ManagedThrottledConnectionHandler managedConnetionHandler) {
        downloadLink.getDownloadLinkController().getConnectionHandler().removeConnectionHandler(managedConnetionHandler);
    }

    @Override
    public void setAvailable(AvailableStatus status) {
        downloadLink.setAvailableStatus(status);
    }

    @Override
    public String getFinalFileName() {
        return downloadLink.getFinalFileName();
    }

    @Override
    public void setFinalFileName(String newfinalFileName) {
        downloadLink.setFinalFileName(newfinalFileName);
    }

    @Override
    public boolean checkIfWeCanWrite() throws Exception {
        final SingleDownloadController dlc = downloadLink.getDownloadLinkController();
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        DownloadWatchDog.getInstance().localFileCheck(dlc, new ExceptionRunnable() {

            @Override
            public void run() throws Exception {
                atomicBoolean.set(true);
            }
        }, null);
        return atomicBoolean.get();
    }

    @Override
    public void lockFiles(File... files) throws FileIsLockedException {
        final SingleDownloadController dlc = downloadLink.getDownloadLinkController();
        for (File f : files) {
            DownloadWatchDog.getInstance().getSession().getFileAccessManager().lock(f, dlc);
        }

    }

    @Override
    public void unlockFiles(File... files) {
        final SingleDownloadController dlc = downloadLink.getDownloadLinkController();
        for (File f : files) {
            DownloadWatchDog.getInstance().getSession().getFileAccessManager().unlock(f, dlc);
        }
    }

    @Override
    public void addDownloadTime(long ms) {
        downloadLink.addDownloadTime(ms);
    }

    @Override
    public void removePluginProgress() {
        downloadLink.setPluginProgress(null);
    }

    @Override
    public void setLinkStatusText(String label) {
        downloadLink.getDownloadLinkController().getLinkStatus().setStatusText(label);
    }

    @Override
    public long getDownloadTotalBytes() {
        return downloadLink.getDownloadSize();
    }

    @Override
    public boolean isDoFilesizeCheckEnabled() {
        return downloadLink.getBooleanProperty(RAFDownload.PROPERTY_DOFILESIZECHECK, true);
    }

    @Override
    public void setDownloadBytesLoaded(long bytes) {
        downloadLink.setDownloadCurrent(bytes);
    }

    @Override
    public boolean isHashCheckEnabled() {
        return downloadLink.getBooleanProperty("ALLOW_HASHCHECK", true);
    }

    @Override
    public String getMD5Hash() {
        return downloadLink.getMD5Hash();
    }

    @Override
    public String getSha1Hash() {
        return downloadLink.getSha1Hash();
    }

    @Override
    public String getName() {
        return downloadLink.getName();
    }

    @Override
    public long getKnownDownloadSize() {
        return downloadLink.getKnownDownloadSize();
    }

    @Override
    public void setPluginProgress(PluginProgress progress) {
        downloadLink.setPluginProgress(progress);
    }

    @Override
    public HashInfo getHashInfo() {
        String hash;
        // StatsManager
        if ((hash = downloadLink.getMD5Hash()) != null && hash.length() == 32) {
            /* MD5 Check */

            return new HashInfo(hash, HashResult.TYPE.MD5);
        } else if (!StringUtils.isEmpty(hash = downloadLink.getSha1Hash()) && hash.length() == 40) {
            /* SHA1 Check */
            return new HashInfo(hash, HashResult.TYPE.SHA1);
        } else if ((hash = new Regex(downloadLink.getName(), ".*?\\[([A-Fa-f0-9]{8})\\]").getMatch(0)) != null) {
            return new HashInfo(hash, HashResult.TYPE.CRC32);
        } else {
            ArrayList<DownloadLink> SFVs = new ArrayList<DownloadLink>();
            boolean readL = downloadLink.getFilePackage().getModifyLock().readLock();
            try {
                for (DownloadLink dl : downloadLink.getFilePackage().getChildren()) {
                    if (dl.getFileOutput().toLowerCase().endsWith(".sfv") && FinalLinkState.CheckFinished(dl.getFinalLinkState())) {
                        SFVs.add(dl);
                    }
                }
            } finally {
                downloadLink.getFilePackage().getModifyLock().readUnlock(readL);
            }
            /* SFV File Available, lets use it */
            for (DownloadLink SFV : SFVs) {
                File file = new File(SFV.getFileOutput());
                if (file.exists()) {
                    String sfvText;
                    try {
                        sfvText = IO.readFileToString(file);

                        if (sfvText != null) {
                            /* Delete comments */
                            sfvText = sfvText.replaceAll(";(.*?)[\r\n]{1,2}", "");
                            if (sfvText != null && sfvText.contains(downloadLink.getName())) {
                                hash = new Regex(sfvText, downloadLink.getName() + "\\s*([A-Fa-f0-9]{8})").getMatch(0);
                                if (hash != null) {

                                return new HashInfo(hash, HashResult.TYPE.CRC32);

                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void setHashResult(HashResult result) {
        downloadLink.getDownloadLinkController().setHashResult(result);
    }

    @Override
    public boolean rename(File outputPartFile, File outputCompleteFile) throws InterruptedException {
        boolean renameOkay = false;
        int retry = 5;
        /* rename part file to final filename */
        while (retry > 0) {
            /* first we try normal rename method */
            if ((renameOkay = outputPartFile.renameTo(outputCompleteFile)) == true) {
                break;
            }
            /* this may fail because something might lock the file */
            Thread.sleep(1000);
            retry--;
        }
        /* Fallback */
        if (renameOkay == false) {
            /* rename failed, lets try fallback */
            getLogger().severe("Could not rename file " + outputPartFile + " to " + outputCompleteFile);
            getLogger().severe("Try copy workaround!");
            try {
                DISKSPACECHECK freeSpace = DownloadWatchDog.getInstance().checkFreeDiskSpace(outputPartFile.getParentFile(), downloadLink.getDownloadLinkController(), outputPartFile.length());
                if (DISKSPACECHECK.FAILED.equals(freeSpace)) throw new Throwable("not enough diskspace free to copy part to complete file");
                IO.copyFile(outputPartFile, outputCompleteFile);
                renameOkay = true;
                outputPartFile.delete();
            } catch (Throwable e) {
                LogSource.exception(getLogger(), e);
                /* error happened, lets delete complete file */
                if (outputCompleteFile.exists() && outputCompleteFile.length() != outputPartFile.length()) {
                    FileCreationManager.getInstance().delete(outputCompleteFile, null);
                }
            }
            if (!renameOkay) {
                getLogger().severe("Copy workaround: :(");

            } else {
                getLogger().severe("Copy workaround: :)");
            }
        }

        return renameOkay;
    }

    @Override
    public void logStats(File outputCompleteFile, int chunksCount, long downloadTimeInMs) {
        if (StatsManager.I().isEnabled()) {
            long speed = 0;
            long startDelay = -1;
            try {
                speed = (outputCompleteFile.length() - Math.max(0, downloadLink.getDownloadLinkController().getSizeBefore())) / ((downloadTimeInMs) / 1000);
            } catch (final Throwable e) {
                // LogSource.exception(logger, e);
            }
            try {
                startDelay = System.currentTimeMillis() - downloadLink.getDownloadLinkController().getStartTimestamp();
            } catch (final Throwable e) {
                // LogSource.exception(logger, e);
            }
            StatsManager.I().onFileDownloaded(outputCompleteFile, downloadLink, speed, startDelay, chunksCount);
        }
    }

    @Override
    public void setFinalFileOutput(String absolutePath) {
        downloadLink.setFinalFileOutput(absolutePath);
    }

    @Override
    public String getFileOutput(boolean ignoreUnsafe, boolean ignoreCustom) {
        return downloadLink.getFileOutput(ignoreUnsafe, ignoreCustom);
    }

    @Override
    public void waitForNextConnectionAllowed() throws InterruptedException {
        plugin.waitForNextConnectionAllowed(downloadLink);
    }

    @Override
    public boolean isInterrupted() {
        SingleDownloadController sdc = downloadLink.getDownloadLinkController();
        return (sdc != null && sdc.isAborting());
    }

    @Override
    public String getFileOutput() {
        return downloadLink.getFileOutput();
    }

    @Override
    public int getLinkStatus() {
        return downloadLink.getDownloadLinkController().getLinkStatus().getStatus();
    }
}