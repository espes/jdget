package org.jdownloader.extensions.extraction.contextmenu.downloadlist.action;

import java.awt.event.ActionEvent;

import org.appwork.utils.swing.dialog.Dialog;
import org.jdownloader.extensions.extraction.Archive;
import org.jdownloader.extensions.extraction.contextmenu.downloadlist.AbstractExtractionContextAction;
import org.jdownloader.gui.IconKey;

public class ExtractArchiveNowAction extends AbstractExtractionContextAction {

    /**
 * 
 */

    public ExtractArchiveNowAction() {
        super();

        setName(org.jdownloader.extensions.extraction.translate.T._.contextmenu_extract());

        setSmallIcon(new ExtractIconVariant(IconKey.ICON_MEDIA_PLAYBACK_START, 18, 20, 3, 3).crop());

    }

    @Override
    protected void onAsyncInitDone() {
        super.onAsyncInitDone();

    }

    public void actionPerformed(ActionEvent e) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                for (Archive archive : archives) {
                    if (_getExtension().isComplete(archive)) {
                        _getExtension().addToQueue(archive, true);
                    } else {
                        Dialog.getInstance().showMessageDialog(org.jdownloader.extensions.extraction.translate.T._.cannot_extract_incopmplete(archive.getName()));
                    }
                }

            }
        };
        thread.setName("Extract Context: extract");
        thread.setDaemon(true);
        thread.start();
    }

}