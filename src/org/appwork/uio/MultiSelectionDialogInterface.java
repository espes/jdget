package org.appwork.uio;

import org.appwork.uio.ConfirmDialogInterface;
import org.appwork.uio.In;
import org.appwork.uio.Out;

public interface MultiSelectionDialogInterface extends ConfirmDialogInterface {
    @Out
    public String[] getLabels();

    @In
    public int[] getSelectedIndices();
}
