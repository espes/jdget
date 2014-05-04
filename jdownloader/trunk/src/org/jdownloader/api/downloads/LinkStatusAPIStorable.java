package org.jdownloader.api.downloads;

import jd.plugins.DownloadLink;

import org.appwork.storage.Storable;
import org.jdownloader.plugins.ConditionalSkipReason;
import org.jdownloader.plugins.FinalLinkState;
import org.jdownloader.plugins.TimeOutCondition;

public class LinkStatusAPIStorable implements Storable {

    public LinkStatusAPIStorable() {
        /* Storable */
    }

    private DownloadLink link;

    public LinkStatusAPIStorable(DownloadLink link) {
        this.link = link;
    }

    public String getErrorMessage() {
        return "DUMMY";
    }

    public Integer getStatus() {
        return 1;
    }

    public String getStatusText() {
        return "DUMMY";
    }

    public Long getValue() {
        return 0l;
    }

    public Integer getRetryCount() {
        return 1;
    }

    public Long getRemainingWaittime() {
        DownloadLink llink = link;
        if (llink != null) {
            ConditionalSkipReason cond = llink.getConditionalSkipReason();
            if (cond instanceof TimeOutCondition) { return Math.max(0, ((TimeOutCondition) cond).getTimeOutLeft()); }
        }
        return 0l;
    }

    public Boolean getFinished() {
        DownloadLink llink = link;
        if (llink != null) FinalLinkState.CheckFinished(llink.getFinalLinkState());
        return false;
    }

    public Boolean getFailed() {
        DownloadLink llink = link;
        if (llink != null) return FinalLinkState.CheckFailed(llink.getFinalLinkState());
        return false;
    }

}
