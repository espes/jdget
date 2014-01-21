package org.jdownloader.myjdownloader.client.bindings;

import org.jdownloader.myjdownloader.client.json.AbstractJsonData;

public class CrawledPackageStorable extends AbstractJsonData {

    public CrawledPackageStorable(/* Storable */) {
    }

    private String name = null;

    public String getName() {
        return name;
    }

    public long getUuid() {
        return uuid;
    }

    public void setUuid(final long uuid) {
        this.uuid = uuid;
    }

    public void setName(final String name) {
        this.name = name;
    }

    private long   uuid   = -1;
    private String saveTo = null;

    public String getSaveTo() {
        return saveTo;
    }

    public void setSaveTo(final String saveTo) {
        this.saveTo = saveTo;
    }

    private long bytesTotal = -1;

    public long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(final long size) {
        bytesTotal = size;
    }

    private int childCount = -1;

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(final int childCount) {
        this.childCount = childCount;
    }

    private String[] hosts = null;

    public String[] getHosts() {
        return hosts;
    }

    public void setHosts(final String[] hosts) {
        this.hosts = hosts;
    }

    private int onlineCount = -1;

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(final int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public int getOfflineCount() {
        return offlineCount;
    }

    public void setOfflineCount(final int offlineCount) {
        this.offlineCount = offlineCount;
    }

    public int getUnknownCount() {
        return unknownCount;
    }

    public void setUnknownCount(final int unknownCount) {
        this.unknownCount = unknownCount;
    }

    private int    offlineCount = -1;
    private int    unknownCount = -1;
    private int    tempUnknownCount = -1;

    public int getTempUnknownCount() {
        return tempUnknownCount;
    }

    public void setTempUnknownCount(final int temnpUnknownCount) {
        this.tempUnknownCount = temnpUnknownCount;
    }

    private String comment      = null;

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

}