package org.jdownloader.plugins;

import java.awt.Color;

import jd.nutils.Formatter;
import jd.plugins.FilePackageView;
import jd.plugins.PluginProgress;
import jd.plugins.download.DownloadInterface;
import jd.plugins.download.Downloadable;

import org.jdownloader.api.jdanywhere.api.Helper;
import org.jdownloader.gui.views.downloads.columns.TaskColumn;
import org.jdownloader.images.NewTheme;
import org.jdownloader.plugins.tasks.PluginProgressTask;
import org.jdownloader.translate._JDT;

public class DownloadPluginProgress extends PluginProgress {

    private final DownloadInterface downloadInterface;

    private final String            unknownFileSize = _JDT._.gui_download_filesize_unknown() + " \u221E";
    protected final long            startTimeStamp;
    private final String            normal;

    private Downloadable            downloadable;

    public DownloadPluginProgress(Downloadable downloadable, DownloadInterface downloadInterface, Color color) {
        super(0, 0, color);
        setProgressSource(downloadInterface);
        this.downloadable = downloadable;
        this.downloadInterface = downloadInterface;
        setIcon(NewTheme.I().getIcon("download", 16));
        startTimeStamp = downloadInterface.getStartTimeStamp();
        normal = _JDT._.download_connection_normal();
    }

    @Override
    public long getCurrent() {
        return downloadInterface.getTotalLinkBytesLoadedLive();
    };

    @Override
    public long getTotal() {
        return downloadable.getKnownDownloadSize();
    }

    @Override
    public long getETA() {
        long speed = getSpeed();
        if (speed > 0) {
            long remainingBytes = (getTotal() - getCurrent());
            if (remainingBytes > 0) {
                long eta = remainingBytes / speed;
                return eta;
            }
        }
        return -1;
    }

    @Override
    public String getMessage(Object requestor) {
        if (requestor instanceof TaskColumn || requestor == Helper.REQUESTOR || requestor instanceof FilePackageView || requestor instanceof PluginProgressTask) { return normal; }
        long total = getTotal();

        if (total < 0) { return unknownFileSize; }
        long eta = getETA();
        if (eta > 0) { return Formatter.formatSeconds(eta); }
        return null;
    }

    public long getDuration() {
        return System.currentTimeMillis() - startTimeStamp;
    }

    public long getSpeed() {
        return downloadInterface.getManagedConnetionHandler().getSpeed();
    }

    @Override
    public PluginTaskID getID() {
        return PluginTaskID.DOWNLOAD;
    }

}
