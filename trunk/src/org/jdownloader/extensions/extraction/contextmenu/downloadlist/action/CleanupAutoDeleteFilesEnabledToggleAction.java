package org.jdownloader.extensions.extraction.contextmenu.downloadlist.action;

import java.awt.event.ActionEvent;
import java.util.List;

import org.appwork.utils.swing.dialog.Dialog;
import org.jdownloader.extensions.extraction.Archive;
import org.jdownloader.extensions.extraction.ArchiveSettings.BooleanStatus;
import org.jdownloader.extensions.extraction.contextmenu.downloadlist.AbstractExtractionAction;
import org.jdownloader.gui.views.SelectionInfo;

public class CleanupAutoDeleteFilesEnabledToggleAction extends AbstractExtractionAction {

    protected List<Archive> archives;

    public CleanupAutoDeleteFilesEnabledToggleAction(final SelectionInfo<?, ?> selection) {
        super(selection);
        setName(_.contextmenu_autodeletefiles());
        setIconKey("file");
        setSelected(false);
        setEnabled(false);

    }

    @Override
    protected void onAsyncInitDone() {
        super.onAsyncInitDone();
        if (archives!=null&&archives.size() > 0) setSelected(_getExtension().isRemoveFilesAfterExtractEnabled(archives.get(0)));
    }

    public void actionPerformed(ActionEvent e) {

        for (Archive archive : archives) {
            archive.getSettings().setRemoveFilesAfterExtraction(isSelected() ? BooleanStatus.TRUE : BooleanStatus.FALSE);
        }
        Dialog.getInstance().showMessageDialog(Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN, isSelected() ? _.set_autoremovefiles_true() : _.set_autoremovefiles_false());

    }
}
