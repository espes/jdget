package org.appwork.utils.swing.dialog;

public interface OKCancelCloseUserIODefinition extends UserIODefinition {
    public static enum CloseReason {
        OK,
        CANCEL,
        CLOSE
    }
public String getTitle();
    public CloseReason getCloseReason();

}
