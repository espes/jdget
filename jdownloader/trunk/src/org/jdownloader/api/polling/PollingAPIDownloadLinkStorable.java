package org.jdownloader.api.polling;

import jd.plugins.DownloadLink;

import org.appwork.storage.Storable;
import org.jdownloader.api.downloads.LinkStatusAPIStorable;
import org.jdownloader.plugins.FinalLinkState;

public class PollingAPIDownloadLinkStorable implements Storable {

    private DownloadLink link;

    public PollingAPIDownloadLinkStorable(/* Storable */) {
        this.link = null;
    }

    public PollingAPIDownloadLinkStorable(DownloadLink link) {
        this.link = link;
    }

    public Long getUUID() {
        return link.getUniqueID().getID();
    }

    public Long getDone() {
        return link.getView().getBytesLoaded();
    }

    public Boolean getFinished() {
        return FinalLinkState.CheckFinished(link.getFinalLinkState());
    }

    public Long getSize() {
        return link.getView().getBytesTotalEstimated();
    }

    public Long getSpeed() {
        return link.getView().getSpeedBps();
    }

    public Long getEta() {
        return link.getFinishedDate();
    }

    public LinkStatusAPIStorable getLinkStatus() {
        return new LinkStatusAPIStorable(link);
    }
}