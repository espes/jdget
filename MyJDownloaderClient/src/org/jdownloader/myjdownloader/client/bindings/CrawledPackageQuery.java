package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class CrawledPackageQuery extends AbstractJsonData {
    private long[] packageUUIDs;

    private boolean availableOnlineCount=false;
    public boolean isAvailableOnlineCount() {
        return availableOnlineCount;
    }

    public void setAvailableOnlineCount(final boolean availableOnlineCount) {
        this.availableOnlineCount = availableOnlineCount;
    }

    public boolean isAvailableOfflineCount() {
        return availableOfflineCount;
    }

    public void setAvailableOfflineCount(final boolean availableUnknownCount) {
        availableOfflineCount = availableUnknownCount;
    }

    private boolean availableOfflineCount=false;
    private boolean availableUnknownCount=false;

    public boolean isAvailableUnknownCount() {
        return availableUnknownCount;
    }

    public void setAvailableUnknownCount(final boolean availableUnknownCount) {
        this.availableUnknownCount = availableUnknownCount;
    }

    public boolean isAvailableTempUnknownCount() {
        return availableTempUnknownCount;
    }

    public void setAvailableTempUnknownCount(final boolean availableTempUnknownCount) {
        this.availableTempUnknownCount = availableTempUnknownCount;
    }

    private boolean availableTempUnknownCount=false;

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

    public boolean isSaveTo() {
        return saveTo;
    }

    public void setSaveTo(final boolean saveTo) {
        this.saveTo = saveTo;
    }

    public boolean isBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(final boolean size) {
        bytesTotal = size;
    }

    public boolean isChildCount() {
        return childCount;
    }

    public void setChildCount(final boolean childCount) {
        this.childCount = childCount;
    }

    public boolean isHosts() {
        return hosts;
    }

    public void setHosts(final boolean hosts) {
        this.hosts = hosts;
    }



    public boolean isComment() {
        return comment;
    }

    public void setComment(final boolean comment) {
        this.comment = comment;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }



    private int     startAt     = 0;
    private int     maxResults  = -1;
    private boolean saveTo      = false;
    private boolean bytesTotal  = false;
    private boolean childCount  = false;
    private boolean hosts       = false;



    private boolean comment     = false;
    private boolean enabled     = false;

}