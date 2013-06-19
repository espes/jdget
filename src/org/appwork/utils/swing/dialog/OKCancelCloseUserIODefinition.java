package org.appwork.utils.swing.dialog;

public interface OKCancelCloseUserIODefinition extends UserIODefinition {
    public static enum CloseReason {
        OK,
        CANCEL,
        CLOSE, TIMEOUT
    }
public String getTitle();
    public CloseReason getCloseReason();

}
