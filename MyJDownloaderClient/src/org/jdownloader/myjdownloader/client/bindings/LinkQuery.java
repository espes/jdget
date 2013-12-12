package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class LinkQuery extends AbstractJsonData {
    private long[]  packageUUIDs;
    private int     startAt=0;
    private int     maxResults=-1;
    private boolean host = false;

    public boolean isHost() {
        return host;
    }

    public void setHost(final boolean host) {
        this.host = host;
    }

    public boolean isBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(final boolean size) {
        bytesTotal = size;
    }

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

    public boolean isUrl() {
        return url;
    }

    public void setUrl(final boolean url) {
        this.url = url;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isExtractionStatus() {
        return extractionStatus;
    }

    public void setExtractionStatus(final boolean extractionStatus) {
        this.extractionStatus = extractionStatus;
    }

    private boolean bytesTotal       = false;
    private boolean bytesLoaded      = false;
    private boolean speed            = false;
    private boolean eta              = false;
    private boolean finished         = false;

    private boolean running          = false;
    private boolean skipped          = false;
    private boolean url              = false;
    private boolean enabled          = false;
    private boolean extractionStatus = false;

    public long[] getPackageUUIDs() {
        return packageUUIDs;
    }

    public void setPackageUUIDs(final long[] packageUUIDs) {
        this.packageUUIDs = packageUUIDs;
    }

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(final int startAt) {
        this.startAt = startAt;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(final int maxResults) {
        this.maxResults = maxResults;
    }

}