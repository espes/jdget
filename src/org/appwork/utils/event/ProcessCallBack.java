package org.appwork.utils.event;

public interface ProcessCallBack {
    public void setProgress(int percent);

    public void setStatusString(String string);

    /**
     * @param autodetection_success
     */
    public void showMessage(String autodetection_success);

    /**
     * @param autodetection_failed
     */
    public void showWarning(String autodetection_failed);

}
