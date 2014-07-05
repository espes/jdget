package org.appwork.utils.swing.dialog;

import org.appwork.uio.Out;
import org.appwork.uio.UserIODefinition;

public interface OKCancelCloseUserIODefinition extends UserIODefinition {

    @Out
    public String getCancelButtonText();

    @Out
    public String getOKButtonText();

}
