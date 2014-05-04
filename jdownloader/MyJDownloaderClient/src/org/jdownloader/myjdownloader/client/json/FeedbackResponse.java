package org.jdownloader.myjdownloader.client.json;

public class FeedbackResponse implements RequestIDValidator {
    private long   rid        = -1;
    private String feedbackID = null;

    public FeedbackResponse(/* Storable */) {
    }

    /**
     * @return the feedbackID
     */
    public String getFeedbackID() {
        return this.feedbackID;
    }

    public long getRid() {
        return this.rid;
    }

    /**
     * @param feedbackID
     *            the feedbackID to set
     */
    public void setFeedbackID(final String feedbackID) {
        this.feedbackID = feedbackID;
    }

    public void setRid(final long timestamp) {
        this.rid = timestamp;
    }
}
