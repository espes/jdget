package org.jdownloader.myjdownloader.client.bindings.linkgrabber;

import org.jdownloader.myjdownloader.client.bindings.AbstractPackageStorable;

public class CrawledPackageStorable extends AbstractPackageStorable {

    private int offlineCount     = -1;
    private int onlineCount      = -1;

    private int tempUnknownCount = -1;

    private int unknownCount     = -1;

    public CrawledPackageStorable(/* Storable */) {
    }

    int getOfflineCount() {
        return offlineCount;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public int getTempUnknownCount() {
        return tempUnknownCount;
    }

    public int getUnknownCount() {
        return unknownCount;
    }

    public void setOfflineCount(final int offlineCount) {
        this.offlineCount = offlineCount;
    }

    public void setOnlineCount(final int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public void setTempUnknownCount(final int temnpUnknownCount) {
        tempUnknownCount = temnpUnknownCount;
    }

    public void setUnknownCount(final int unknownCount) {
        this.unknownCount = unknownCount;
    }

}