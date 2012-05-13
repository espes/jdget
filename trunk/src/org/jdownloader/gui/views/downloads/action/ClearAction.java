package org.jdownloader.gui.views.downloads.action;

import java.awt.event.ActionEvent;

import jd.controlling.IOEQ;

import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogNoAnswerException;
import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.translate._GUI;

public class ClearAction extends AppAction {
    /**
     * 
     */
    private static final long serialVersionUID = 6027982395476716687L;

    public ClearAction() {
        setIconKey("clear");
        putValue(SHORT_DESCRIPTION, _GUI._.ClearAction_tt_());
    }

    public void actionPerformed(ActionEvent e) {
        try {
            Dialog.getInstance().showConfirmDialog(Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN | Dialog.LOGIC_DONT_SHOW_AGAIN_IGNORES_CANCEL, _GUI._.literally_are_you_sure(), _GUI._.ClearAction_actionPerformed_msg(), null, _GUI._.literally_yes(), _GUI._.literall_no());
            IOEQ.add(new Runnable() {

                public void run() {
                    // LinkCollector.getInstance().clear();
                }

            }, true);
        } catch (DialogNoAnswerException e1) {
        }
    }

}
