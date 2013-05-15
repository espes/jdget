package org.jdownloader.gui.views.downloads.overviewpanel;

import jd.controlling.downloadcontroller.ManagedThrottledConnectionHandler;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.download.DownloadInterface;

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.TimeFormatter;
import org.jdownloader.gui.views.SelectionInfo;

public class AggregatedNumbers {

    private long totalBytes;

    public String getTotalBytesString() {
        return SizeFormatter.formatBytes(totalBytes);
    }

    public String getLoadedBytesString() {
        return SizeFormatter.formatBytes(loadedBytes);
    }

    public String getDownloadSpeedString() {
        return SizeFormatter.formatBytes(downloadSpeed) + "/s";
    }

    public String getEtaString() {
        return eta > 0 ? TimeFormatter.formatSeconds(eta, 0) : "~";
    }

    public int getLinkCount() {
        return linkCount;
    }

    public int getPackageCount() {
        return packageCount;
    }

    private long loadedBytes;
    private long downloadSpeed;
    private long eta;
    private int  linkCount;
    private int  packageCount;
    private long running;

    public long getRunning() {
        return running;
    }

    public long getConnections() {
        return connections;
    }

    private long connections;

    public AggregatedNumbers(SelectionInfo<FilePackage, DownloadLink> selection) {

        totalBytes = 0l;
        loadedBytes = 0l;
        downloadSpeed = 0l;
        running = 0l;
        connections = 0l;
        packageCount = selection.getAllPackages().size();
        linkCount = selection.getChildren().size();
        for (DownloadLink dl : selection.getChildren()) {
            totalBytes += dl.getDownloadSize();
            loadedBytes += dl.getDownloadCurrent();
            downloadSpeed += dl.getDownloadSpeed();
            if (dl.getDownloadLinkController() != null) {
                running++;
                DownloadInterface conInst = dl.getDownloadInstance();
                if (conInst != null) {
                    ManagedThrottledConnectionHandler handlerP = conInst.getManagedConnetionHandler();
                    if (handlerP != null) {

                        connections += handlerP.size();

                    }

                }
            }

        }

        eta = downloadSpeed == 0 ? 0 : (totalBytes - loadedBytes) / downloadSpeed;

    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getDownloadSpeed() {
        return downloadSpeed;
    }

    public long getEta() {
        return eta;
    }

    public long getLoadedBytes() {
        return loadedBytes;
    }
}
