package org.appwork.uio;

import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

public interface UserIODefinition {

    public static enum CloseReason {
        OK,
        CANCEL,
        CLOSE,
        TIMEOUT,
        INTERRUPT
    }

    @In
    public CloseReason getCloseReason();

    /**
     * @throws DialogCanceledException
     * @throws DialogClosedException
     * 
     */
    public void throwCloseExceptions() throws DialogClosedException, DialogCanceledException;

    @In
    public boolean isDontShowAgainSelected();

    @Out
    public int getFlags();

}
