package org.jdownloader.myjdownloader.client.bindings.downloadlist;

import org.jdownloader.myjdownloader.client.bindings.AbstractLinkStorable;


public class DownloadLinkStorable extends AbstractLinkStorable{

    private long    bytesLoaded      = -1;

    private long    eta              = -1;

    private String  extractionStatus = null;

    private boolean finished         = false;
    private boolean running          = false;

    private boolean skipped          = false;
    private long    speed            = -1;
    public DownloadLinkStorable(/* Storable */) {

    }

    public long getBytesLoaded() {
        return bytesLoaded;
    }

    public long getEta() {
        return eta;
    }

    public String getExtractionStatus() {
        return extractionStatus;
    }

    public long getSpeed() {
        return speed;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setBytesLoaded(final long bytesLoaded) {
        this.bytesLoaded = bytesLoaded;
    }

    public void setEta(final long eta) {
        this.eta = eta;
    }

    public void setExtractionStatus(final String extractionStatus) {
        this.extractionStatus = extractionStatus;
    }

    public void setFinished(final boolean finished) {
        this.finished = finished;
    }

    public void setRunning(final boolean running) {
        this.running = running;
    }

    public void setSkipped(final boolean skipped) {
        this.skipped = skipped;
    }

    public void setSpeed(final long speed) {
        this.speed = speed;
    }

}
