package org.appwork.swing.components.pathchooser;

import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

import org.appwork.app.gui.copycutpaste.CopyAction;
import org.appwork.app.gui.copycutpaste.CutAction;
import org.appwork.app.gui.copycutpaste.DeleteAction;
import org.appwork.app.gui.copycutpaste.PasteAction;
import org.appwork.app.gui.copycutpaste.SelectAction;
import org.appwork.storage.JSonStorage;
import org.appwork.swing.MigPanel;
import org.appwork.swing.components.ExtButton;
import org.appwork.swing.components.ExtTextField;
import org.appwork.swing.components.searchcombo.SearchComboBox;
import org.appwork.utils.StringUtils;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.Dialog.FileChooserSelectionMode;
import org.appwork.utils.swing.dialog.Dialog.FileChooserType;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

public class PathChooser extends MigPanel {
    /**
     * 
     */
    private static final long serialVersionUID = -3651657642011425583L;

    private class BrowseAction extends AbstractAction {
        /**
         * 
         */
        private static final long serialVersionUID = -4350861121298607806L;

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

            File file = doFileChooser();
            if (file == null) return;
            setFile(file);

        }

    }

    private ExtTextField           txt;
    private ExtButton              bt;
    private String                 id;
    private SearchComboBox<String> destination;

    public JPopupMenu getPopupMenu(ExtTextField txt, CutAction cutAction, CopyAction copyAction, PasteAction pasteAction, DeleteAction deleteAction, SelectAction selectAction) {
        return null;
    }

    /**
     * @return
     */
    public File doFileChooser() {
        try {

            File[] ret = Dialog.getInstance().showFileChooser(getID(), getDialogTitle(), getSelectionMode(), getFileFilter(), false, getType(), getFile());
            if (ret != null && ret.length == 1) return ret[0];
        } catch (DialogCanceledException e1) {
            e1.printStackTrace();
        } catch (DialogClosedException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public PathChooser(String id) {
        this(id, false);
    }

    public PathChooser(String id, boolean useQuickLIst) {
        super("ins 0,debug", "[grow,fill][]", "[grow,fill]");
        this.id = id;

        txt = new ExtTextField() {

            /**
             * 
             */
            private static final long serialVersionUID = 3243788323043431841L;

            @Override
            public JPopupMenu getPopupMenu(CutAction cutAction, CopyAction copyAction, PasteAction pasteAction, DeleteAction deleteAction, SelectAction selectAction) {
                JPopupMenu self = PathChooser.this.getPopupMenu(txt, cutAction, copyAction, pasteAction, deleteAction, selectAction);

                if (self == null) { return super.getPopupMenu(cutAction, copyAction, pasteAction, deleteAction, selectAction); }
                return self;

            }

        };
        txt.setHelpText(getHelpText());
        bt = new ExtButton(new BrowseAction());

        if (useQuickLIst) {
            destination = new SearchComboBox<String>() {

                @Override
                protected Icon getIconForValue(String value) {
                    return null;
                }

                public JTextField createTextField() {

                    return txt;
                }

                @Override
                public boolean isAutoCompletionEnabled() {
                    return false;
                }

                @Override
                protected String getTextForValue(String value) {
                    return value;
                }

                @Override
                public void onChanged() {
                    PathChooser.this.onChanged(txt);
                }

            };
            // this code makes enter leave the dialog.

            destination.getTextField().getInputMap().put(KeyStroke.getKeyStroke("pressed TAB"), "auto");

            destination.getTextField().setFocusTraversalKeysEnabled(false);

            destination.getTextField().getActionMap().put("auto", new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    auto(txt);
                }
            });
            destination.setUnkownTextInputAllowed(true);
            destination.setBadColor(null);
            destination.setSelectedItem(null);

            add(destination);
        } else {
            add(txt);
        }
        add(bt);

        String preSelection = JSonStorage.getStorage(Dialog.FILECHOOSER).get(Dialog.LASTSELECTION + id, getDefaultPreSelection());
        if (preSelection != null) {
            setFile(new File(preSelection));
        }

    }

    /**
     * @param txt2
     */
    protected void onChanged(ExtTextField txt2) {
        // TODO Auto-generated method stub

    }

    protected void auto(JTextField oldTextField) {
        String txt = oldTextField.getText();

        int selstart = oldTextField.getSelectionStart();
        int selend = oldTextField.getSelectionEnd();
        if (selend != txt.length()) return;
        String sel = txt.substring(selstart, selend);
        String bef = txt.substring(0, selstart);
        String name = (bef.endsWith("/") || bef.endsWith("\\")) ? "" : new File(bef).getName();
        String findName = (txt.endsWith("/") || txt.endsWith("\\")) ? "" : new File(txt).getName();
        boolean found = sel.length() == 0;
        File root = new File(bef);
        while (!root.exists() && root != null) {
            if (root.getParentFile() == root) return;
            root = root.getParentFile();

        }
        for (File f : root.listFiles()) {
            if (f.isFile()) continue;
            if (f.isHidden()) continue;
            if (equals(f.getName(), findName)) {
                found = true;
                continue;
            }

            if (found && startsWith(f.getName(), name)) {

                oldTextField.setText(f.getAbsolutePath());
                oldTextField.setSelectionStart(selstart);
                oldTextField.setSelectionEnd(oldTextField.getText().length());

                return;
            }
        }
        oldTextField.setText(bef);

    }

    /**
     * @param name
     * @param name2
     * @return
     */
    private boolean startsWith(String name, String name2) {
        if (CrossSystem.isWindows()) {//
            return name.toLowerCase(Locale.ENGLISH).startsWith(name2.toLowerCase(Locale.ENGLISH));
        }

        return name.startsWith(name2);
    }

    /**
     * @param name
     * @param findName
     * @return
     */
    private boolean equals(String name, String findName) {
        if (CrossSystem.isWindows()) return name.equalsIgnoreCase(findName);

        return name.equals(findName);
    }

    public void setQuickSelectionList(List<String> quickSelectionList) {

        destination.setList(quickSelectionList);
    }

    /**
     * @return
     */
    protected String getDefaultPreSelection() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setEnabled(boolean b) {
        txt.setEnabled(b);
        bt.setEnabled(b);
        if (destination != null) {
            destination.setEnabled(b);
        }

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

    /**
     * removes the button from the component to place it externaly
     * 
     * @return
     */
    public JButton getButton() {
        remove(bt);
        setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));
        return bt;
    }

}
