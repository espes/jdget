package org.jdownloader.myjdownloader.client.bindings.downloadlist;

import org.jdownloader.myjdownloader.client.bindings.AbstractLinkQuery;

public class DownloadLinkQuery extends AbstractLinkQuery {
    public boolean isBytesLoaded() {
        return bytesLoaded;
    }

    public void setBytesLoaded(final boolean done) {
        bytesLoaded = done;
    }

    public boolean isSpeed() {
        return speed;
    }

    public void setSpeed(final boolean speed) {
        this.speed = speed;
    }

    public boolean isEta() {
        return eta;
    }

    public void setEta(final boolean eta) {
        this.eta = eta;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(final boolean finished) {
        this.finished = finished;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(final boolean running) {
        this.running = running;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(final boolean skipped) {
        this.skipped = skipped;
    }

    public boolean isExtractionStatus() {
        return extractionStatus;
    }

    public void setExtractionStatus(final boolean extractionStatus) {
        this.extractionStatus = extractionStatus;
    }

    private boolean bytesLoaded = false;
    private boolean speed       = false;
    private boolean eta         = false;
    private boolean finished    = false;
    private boolean priority    = false;

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    private boolean running          = false;
    private boolean skipped          = false;
    private boolean extractionStatus = false;

}