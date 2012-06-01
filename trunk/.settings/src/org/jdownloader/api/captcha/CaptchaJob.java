package org.jdownloader.api.captcha;

import org.appwork.storage.Storable;

public class CaptchaJob implements Storable {

    public static enum TYPE {
        NORMAL;
    }

    private long   captchaID;
    private String hosterID;
    private long   linkID;
    private TYPE   type = TYPE.NORMAL;

    /**
     * @return the type
     */
    public TYPE getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(TYPE type) {
        this.type = type;
    }

    public CaptchaJob() {
    }

    public long getID() {
        return captchaID;
    }

    public String getHoster() {
        return hosterID;
    }

    public long getLink() {
        return linkID;
    }

    /**
     * @param captchaID
     *            the captchaID to set
     */
    public void setID(long captchaID) {
        this.captchaID = captchaID;
    }

    /**
     * @param hosterID
     *            the hosterID to set
     */
    public void setHoster(String hosterID) {
        this.hosterID = hosterID;
    }

    /**
     * @param linkID
     *            the linkID to set
     */
    public void setLink(long linkID) {
        this.linkID = linkID;
    }

}
