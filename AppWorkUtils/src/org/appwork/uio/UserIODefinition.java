package org.appwork.uio;

import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

public interface UserIODefinition {

    @In
    public CloseReason getCloseReason();

    @Out
    public String getTitle();

    @In
    public void setCloseReason(CloseReason closeReason);
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
    @Out
    public int getTimeout();

}
