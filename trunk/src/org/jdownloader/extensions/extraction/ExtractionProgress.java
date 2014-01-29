package org.jdownloader.extensions.extraction;

import java.awt.Color;
import java.text.NumberFormat;

import jd.plugins.FilePackageView;
import jd.plugins.PluginProgress;

import org.appwork.swing.exttable.columns.ExtProgressColumn;
import org.appwork.utils.formatter.TimeFormatter;
import org.jdownloader.extensions.extraction.translate.T;
import org.jdownloader.gui.views.downloads.columns.ETAColumn;
import org.jdownloader.gui.views.downloads.columns.ProgressColumn;
import org.jdownloader.images.NewTheme;
import org.jdownloader.plugins.PluginTaskID;

public class ExtractionProgress extends PluginProgress {

    protected long   lastCurrent    = -1;
    protected long   lastTotal      = -1;
    protected long   startTimeStamp = -1;
    protected String message        = null;

    public ExtractionProgress(ExtractionController controller, long current, long total, Color color) {
        super(current, total, color);
        setIcon(NewTheme.I().getIcon(org.jdownloader.gui.IconKey.ICON_COMPRESS, 16));
        message = T._.plugins_optional_extraction_status_extracting2();
        super.setProgressSource(controller);
    }

    @Override
    public PluginTaskID getID() {
        return PluginTaskID.EXTRACTION;
    }

    @Override
    public void setProgressSource(Object progressSource) {
    }

    @Override
    public void updateValues(long current, long total) {
        super.updateValues(current, total);
        if (startTimeStamp == -1 || lastTotal == -1 || lastTotal != total || lastCurrent == -1 || lastCurrent > current) {
            lastTotal = total;
            lastCurrent = current;
            startTimeStamp = System.currentTimeMillis();
            // this.setETA(-1);
            return;
        }
        long currentTimeDifference = System.currentTimeMillis() - startTimeStamp;
        if (currentTimeDifference <= 0) return;
        long speed = (current * 10000) / currentTimeDifference;
        if (speed == 0) return;
        long eta = ((total - current) * 10000) / speed;
        this.setETA(eta);
    }

    private NumberFormat df = NumberFormat.getInstance();

    @Override
    public String getMessage(Object requestor) {
        if (requestor != null && getETA() > 0) {
            if (requestor instanceof ETAColumn) { return TimeFormatter.formatMilliSeconds(getETA(), 0); }
            if (requestor instanceof FilePackageView) {
                //
                return message + " (ETA: " + TimeFormatter.formatMilliSeconds(getETA(), 0) + ")";
            }
            if (requestor instanceof ProgressColumn) { return df.format(ExtProgressColumn.getPercentString(getCurrent(), getTotal())) + "%"; }
        }
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
