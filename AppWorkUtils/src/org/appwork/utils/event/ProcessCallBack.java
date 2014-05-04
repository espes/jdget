package org.appwork.utils.event;

import javax.swing.ImageIcon;

public interface ProcessCallBack {
    public void setProgress(Object caller, int percent);

    public void setStatusString(Object caller, String string);

    /**
     * @param plugin
     * @param literally_warning
     * @param liveHeaderDetectionWizard_runOnlineScan_notalive
     * @param icon
     */
    public void showDialog(Object caller, String title, String message, ImageIcon icon);

    /**
     * @param liveHeaderDetectionWizard
     * @param ret
     */
    public void setStatus(Object caller, Object statusObject);

}
