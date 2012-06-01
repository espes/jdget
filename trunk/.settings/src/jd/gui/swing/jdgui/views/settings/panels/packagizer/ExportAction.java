package jd.gui.swing.jdgui.views.settings.panels.packagizer;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

import org.appwork.storage.JSonStorage;
import org.appwork.utils.IO;
import org.appwork.utils.StringUtils;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.Dialog.FileChooserSelectionMode;
import org.appwork.utils.swing.dialog.Dialog.FileChooserType;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.jdownloader.actions.AppAction;
import org.jdownloader.controlling.packagizer.PackagizerController;
import org.jdownloader.controlling.packagizer.PackagizerRule;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.translate._JDT;

public class ExportAction extends AppAction {
    /**
     * 
     */
    private static final long         serialVersionUID = 1L;
    private ArrayList<PackagizerRule> rules;

    public ExportAction() {
        setName(_GUI._.LinkgrabberFilter_LinkgrabberFilter_export());
        setIconKey("export");
        setTooltipText(_JDT._.ExportAction_ExportAction_tt());

    }

    public boolean isEnabled() {
        return rules != null && rules.size() > 0;
    }

    public ExportAction(ArrayList<PackagizerRule> selection) {
        this();

        rules = selection;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            final String extension;

            extension = ImportAction.EXT;

            File[] filterFiles = Dialog.getInstance().showFileChooser(ImportAction.EXT, _GUI._.LinkgrabberFilter_export_dialog_title(), FileChooserSelectionMode.FILES_ONLY, new FileFilter() {

                @Override
                public String getDescription() {

                    return "*" + ImportAction.EXT;

                }

                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || StringUtils.endsWithCaseInsensitive(f.getName(), extension);

                }
            }, true, FileChooserType.SAVE_DIALOG, null);

            if (rules == null) {
                rules = PackagizerController.getInstance().list();
            }
            String str = JSonStorage.toString(rules);
            File saveto = filterFiles[0];
            if (!saveto.getName().endsWith(extension)) {
                saveto = new File(saveto.getAbsolutePath() + extension);
            }
            try {
                IO.writeStringToFile(saveto, str);
            } catch (IOException e1) {
                Dialog.getInstance().showExceptionDialog(e1.getMessage(), e1.getMessage(), e1);
            }

        } catch (DialogCanceledException e1) {
            e1.printStackTrace();
        } catch (DialogClosedException e1) {
            e1.printStackTrace();
        }
    }
}