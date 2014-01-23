package org.jdownloader.myjdownloader.client.bindings.linkgrabber;

import org.jdownloader.myjdownloader.client.bindings.AbstractPackageQuery;


public class CrawledPackageQuery extends AbstractPackageQuery {
    private boolean availableOfflineCount=false;
    private boolean availableOnlineCount=false;

    private boolean availableTempUnknownCount=false;

    private boolean availableUnknownCount=false;

    public boolean isAvailableOfflineCount() {
        return availableOfflineCount;
    }

    public boolean isAvailableOnlineCount() {
        return availableOnlineCount;
    }
    public boolean isAvailableTempUnknownCount() {
        return availableTempUnknownCount;
    }

    public boolean isAvailableUnknownCount() {
        return availableUnknownCount;
    }

    public void setAvailableOfflineCount(final boolean availableUnknownCount) {
        availableOfflineCount = availableUnknownCount;
    }

    public void setAvailableOnlineCount(final boolean availableOnlineCount) {
        this.availableOnlineCount = availableOnlineCount;
    }

    public void setAvailableTempUnknownCount(final boolean availableTempUnknownCount) {
        this.availableTempUnknownCount = availableTempUnknownCount;
    }

    public void setAvailableUnknownCount(final boolean availableUnknownCount) {
        this.availableUnknownCount = availableUnknownCount;
    }

}