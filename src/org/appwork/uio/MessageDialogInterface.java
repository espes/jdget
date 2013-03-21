package org.appwork.uio;

import org.appwork.utils.swing.dialog.UserIODefinition;

public interface MessageDialogInterface extends UserIODefinition {
    public String getMessage();

    public void setMessage(String str);
}
