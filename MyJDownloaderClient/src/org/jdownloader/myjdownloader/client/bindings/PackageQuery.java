package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class PackageQuery extends AbstractJsonData {
    private long[] packageUUIDs;

    public long[] getPackageUUIDs() {
        return packageUUIDs;
    }

    public void setPackageUUIDs(long[] packageUUIDs) {
        this.packageUUIDs = packageUUIDs;
    }

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(int startAt) {
        this.startAt = startAt;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public boolean isSaveTo() {
        return saveTo;
    }

    public void setSaveTo(boolean saveTo) {
        this.saveTo = saveTo;
    }

    public boolean isBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(boolean size) {
        this.bytesTotal = size;
    }

    public boolean isChildCount() {
        return childCount;
    }

    public void setChildCount(boolean childCount) {
        this.childCount = childCount;
    }

    public boolean isHosts() {
        return hosts;
    }

    public void setHosts(boolean hosts) {
        this.hosts = hosts;
    }

    public boolean isSpeed() {
        return speed;
    }

    public void setSpeed(boolean speed) {
        this.speed = speed;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isEta() {
        return eta;
    }

    public void setEta(boolean eta) {
        this.eta = eta;
    }

    public boolean isBytesLoaded() {
        return bytesLoaded;
    }

    public void setBytesLoaded(boolean done) {
        this.bytesLoaded = done;
    }

    public boolean isComment() {
        return comment;
    }

    public void setComment(boolean comment) {
        this.comment = comment;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    private int     startAt     = 0;
    private int     maxResults  = -1;
    private boolean saveTo      = false;
    private boolean bytesTotal  = false;
    private boolean childCount  = false;
    private boolean hosts       = false;

    private boolean speed       = false;
    private boolean finished    = false;
    private boolean eta         = false;
    private boolean bytesLoaded = false;

    private boolean comment     = false;
    private boolean enabled     = false;
    private boolean running     = false;
}