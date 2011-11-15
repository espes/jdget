package org.appwork.swing.components.pathchooser;

import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileFilter;

import org.appwork.app.gui.MigPanel;
import org.appwork.app.gui.copycutpaste.CopyAction;
import org.appwork.app.gui.copycutpaste.CutAction;
import org.appwork.app.gui.copycutpaste.DeleteAction;
import org.appwork.app.gui.copycutpaste.PasteAction;
import org.appwork.app.gui.copycutpaste.SelectAction;
import org.appwork.storage.JSonStorage;
import org.appwork.swing.components.ExtButton;
import org.appwork.swing.components.ExtTextField;
import org.appwork.utils.StringUtils;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.Dialog.FileChooserSelectionMode;
import org.appwork.utils.swing.dialog.Dialog.FileChooserType;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

public class PathChooser extends MigPanel {
    private class BrowseAction extends AbstractAction {
        BrowseAction() {
            putValue(NAME, getBrowseLabel());
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            try {

                File[] ret = Dialog.getInstance().showFileChooser(getID(), getDialogTitle(), getSelectionMode(), getFileFilter(), false, getType(), getFile());

                if (ret != null && ret.length == 1) setFile(ret[0]);

            } catch (DialogCanceledException e1) {
                e1.printStackTrace();
            } catch (DialogClosedException e1) {
                e1.printStackTrace();
            }
        }

    }

    private ExtTextField txt;
    private ExtButton    bt;
    private String       id;

    public JPopupMenu getPopupMenu(ExtTextField txt, CutAction cutAction, CopyAction copyAction, PasteAction pasteAction, DeleteAction deleteAction, SelectAction selectAction) {
        return null;
    }

    public PathChooser(String id) {
        super("ins 0", "[grow,fill][]", "[grow,fill]");
        this.id = id;
        txt = new ExtTextField() {

            @Override
            public JPopupMenu getPopupMenu(CutAction cutAction, CopyAction copyAction, PasteAction pasteAction, DeleteAction deleteAction, SelectAction selectAction) {
                JPopupMenu self = PathChooser.this.getPopupMenu(txt, cutAction, copyAction, pasteAction, deleteAction, selectAction);

                if (self == null) { return super.getPopupMenu(cutAction, copyAction, pasteAction, deleteAction, selectAction); }
                return self;

            }

        };
        txt.setHelpText(getHelpText());
        bt = new ExtButton(new BrowseAction());
        add(txt);
        add(bt);

        String preSelection = JSonStorage.getStorage(Dialog.FILECHOOSER).get(Dialog.LASTSELECTION + id, (String) null);
        if (preSelection != null) {
            setFile(new File(preSelection));
        }

    }

    public void setEnabled(boolean b) {
        txt.setEnabled(b);
        bt.setEnabled(b);

    }

    public synchronized void addMouseListener(MouseListener l) {
        txt.addMouseListener(l);
        bt.addMouseListener(l);
        super.addMouseListener(l);

    }

    public synchronized void removeMouseListener(MouseListener l) {
        txt.removeMouseListener(l);
        bt.removeMouseListener(l);
        super.removeMouseListener(l);
    }

    /**
     * @return
     */
    public String getBrowseLabel() {

        return _AWU.T.pathchooser_browselabel();
    }

    /**
     * @return
     */
    protected String getHelpText() {

        return _AWU.T.pathchooser_helptext();
    }

    /**
     * @return
     */
    public File getFile() {
        if (StringUtils.isEmpty(txt.getText())) return null;
        return textToFile(txt.getText());
    }

    /**
     * @param text
     * @return
     */
    protected File textToFile(String text) {
        return new File(text);
    }

    /**
     * @return
     */
    public FileChooserType getType() {
        return FileChooserType.SAVE_DIALOG;
    }

    /**
     * @return
     */
    public FileFilter getFileFilter() {
        return null;
    }

    /**
     * @return
     */
    public FileChooserSelectionMode getSelectionMode() {

        return FileChooserSelectionMode.DIRECTORIES_ONLY;
    }

    /**
     * @return
     */
    public String getDialogTitle() {
        // TODO Auto-generated method stub
        return _AWU.T.pathchooser_dialog_title();
    }

    /**
     * @return
     */
    public String getID() {
        return id;
    }

    public void setFile(File file) {

        this.txt.setText(fileToText(file));
    }

    /**
     * @param file2
     * @return
     */
    protected String fileToText(File file2) {

        return file2.getAbsolutePath();
    }

    /**
     * @return
     */
    public String getPath() {

        return txt.getText();
    }

    /**
     * @param downloadDestination
     */
    public void setPath(String downloadDestination) {
        txt.setText(downloadDestination);

    }

    /**
     * @param packagizerFilterRuleDialog_layoutDialogContent_dest_help
     */
    public void setHelpText(String helpText) {
        txt.setHelpText(helpText);

    }

}
