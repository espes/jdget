package org.appwork.swing.components.pathchooser;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;
import org.appwork.utils.swing.dialog.ExtFileChooserDialog;
import org.appwork.utils.swing.dialog.FileChooserSelectionMode;
import org.appwork.utils.swing.dialog.FileChooserType;

public class PathChooser extends MigPanel {
    private class BrowseAction extends AbstractAction {
        /**
         * 
         */
        private static final long serialVersionUID = -4350861121298607806L;

        BrowseAction() {

            putValue(Action.NAME, getBrowseLabel());
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        @Override
        public void actionPerformed(final ActionEvent e) {

            final File file = doFileChooser();
            if (file == null) { return; }
            setFile(file);

        }

    }

    /**
     * 
     */
    private static final long        serialVersionUID = -3651657642011425583L;

    protected ExtTextField           txt;
    protected ExtButton              bt;
    private String                   id;
    protected SearchComboBox<String> destination;

    public PathChooser(final String id) {
        this(id, false);
    }

    public PathChooser(final String id, final boolean useQuickLIst) {
        super("ins 0", "[grow,fill][]", "[grow,fill]");
        this.id = id;
        setOpaque(false);
        txt = new ExtTextField() {

            /**
             * 
             */
            private static final long serialVersionUID = 3243788323043431841L;

            @Override
            public JPopupMenu getPopupMenu(final CutAction cutAction, final CopyAction copyAction, final PasteAction pasteAction, final DeleteAction deleteAction, final SelectAction selectAction) {
                final JPopupMenu self = PathChooser.this.getPopupMenu(txt, cutAction, copyAction, pasteAction, deleteAction, selectAction);

                if (self == null) { return super.getPopupMenu(cutAction, copyAction, pasteAction, deleteAction, selectAction); }
                return self;

            }

        };
        txt.setHelpText(getHelpText());
        bt = new ExtButton(new BrowseAction());

        if (useQuickLIst) {
            txt.setHelperEnabled(false);
            destination = new SearchComboBox<String>() {

                @Override
                public JTextField createTextField() {
                    return txt;
                }

                @Override
                protected Icon getIconForValue(final String value) {
                    return null;
                }

                @Override
                protected String getTextForValue(final String value) {
                    return value;
                }

                @Override
                public boolean isAutoCompletionEnabled() {
                    return false;
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
                public void actionPerformed(final ActionEvent e) {
                    if (!PathChooser.this.auto(txt)) {
                        //
                        System.out.println("NExt Fpcus");
                        final KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                        manager.focusNextComponent();
                    }
                }
            });
            destination.setUnkownTextInputAllowed(true);
            destination.setBadColor(null);
            destination.setSelectedItem(null);

            this.add(destination);
        } else {
            txt.setHelperEnabled(true);
            this.add(txt);
        }
        this.add(bt);

        final String preSelection = JSonStorage.getStorage(Dialog.FILECHOOSER).get(Dialog.LASTSELECTION + id, getDefaultPreSelection());
        if (preSelection != null) {
            setFile(new File(preSelection));
        }

    }

    @Override
    public synchronized void addMouseListener(final MouseListener l) {
        txt.addMouseListener(l);
        bt.addMouseListener(l);
        super.addMouseListener(l);

    }

    protected boolean auto(final JTextField oldTextField) {
        final String txt = oldTextField.getText();

        final int selstart = oldTextField.getSelectionStart();
        final int selend = oldTextField.getSelectionEnd();
        if (selend != txt.length()) { return false; }
        final String sel = txt.substring(selstart, selend);
        final String bef = txt.substring(0, selstart);
        final String name = bef.endsWith("/") || bef.endsWith("\\") ? "" : new File(bef).getName();
        final String findName = txt.endsWith("/") || txt.endsWith("\\") ? "" : new File(txt).getName();
        boolean found = sel.length() == 0;
        File root = new File(bef);
        while (root != null && !root.exists()) {
            if (root.getParentFile() == root) { return  false; }
            root = root.getParentFile();

        }
        if (root == null) { return  false; }    
        for (final File f : root.listFiles()) {
            if (f.isFile()) {
                continue;
            }
            if (f.isHidden()) {
                continue;
            }
            if (this.equals(f.getName(), findName)) {
                found = true;
                continue;
            }

            if (found && startsWith(f.getName(), name)) {

                oldTextField.setText(f.getAbsolutePath());
                oldTextField.setSelectionStart(selstart);
                oldTextField.setSelectionEnd(oldTextField.getText().length());

                return true;
            }
        }
        oldTextField.setText(bef);
        return false;

    }

    /**
     * @return
     */
    public File doFileChooser() {

        final ExtFileChooserDialog d = new ExtFileChooserDialog(0, getDialogTitle(), null, null);
        d.setStorageID(getID());
        d.setFileSelectionMode(getSelectionMode());
        d.setFileFilter(getFileFilter());
        d.setType(getType());
        d.setMultiSelection(false);
        d.setPreSelection(getFile());
        try {
            Dialog.I().showDialog(d);
        } catch (final DialogClosedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final DialogCanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return d.getSelectedFile();

    }

    /**
     * @param name
     * @param findName
     * @return
     */
    private boolean equals(final String name, final String findName) {
        if (CrossSystem.isWindows()) { return name.equalsIgnoreCase(findName); }

        return name.equals(findName);
    }

    /**
     * @param file2
     * @return
     */
    protected String fileToText(final File file2) {

        return file2.getAbsolutePath();
    }

    /**
     * @return
     */
    public String getBrowseLabel() {

        return _AWU.T.pathchooser_browselabel();
    }

    /**
     * removes the button from the component to place it externaly
     * 
     * @return
     */
    public JButton getButton() {
        this.remove(bt);
        setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));
        return bt;
    }

    /**
     * @return
     */
    protected String getDefaultPreSelection() {
        // TODO Auto-generated method stub
        return null;
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
    public File getFile() {
        if (StringUtils.isEmpty(txt.getText())) { return null; }
        return textToFile(txt.getText());
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
    protected String getHelpText() {
        return _AWU.T.pathchooser_helptext();
    }

    /**
     * @return
     */
    public String getID() {
        return id;
    }

    /**
     * @return
     */
    public String getPath() {
        return txt.getText();
    }

    public JPopupMenu getPopupMenu(final ExtTextField txt, final CutAction cutAction, final CopyAction copyAction, final PasteAction pasteAction, final DeleteAction deleteAction, final SelectAction selectAction) {
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
    public FileChooserType getType() {
        return FileChooserType.SAVE_DIALOG;
    }

    /**
     * @param txt2
     */
    protected void onChanged(final ExtTextField txt2) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void removeMouseListener(final MouseListener l) {
        txt.removeMouseListener(l);
        bt.removeMouseListener(l);
        super.removeMouseListener(l);
    }

    @Override
    public void setEnabled(final boolean b) {
        txt.setEnabled(b);
        bt.setEnabled(b);
        if (destination != null) {
            destination.setEnabled(b);
        }

    }

    public void setFile(final File file) {
        final String text = fileToText(file);
        if (destination != null) {
            destination.setText(text);
        } else {
            txt.setText(text);
        }
    }

    /**
     * @param packagizerFilterRuleDialog_layoutDialogContent_dest_help
     */
    public void setHelpText(final String helpText) {
        txt.setHelpText(helpText);
        if (destination != null) {
            destination.setHelpText(helpText);
        }
    }

    /**
     * @param downloadDestination
     */
    public void setPath(final String downloadDestination) {
        if (destination != null) {
            destination.setText(downloadDestination);
        } else {
            txt.setText(downloadDestination);
        }

    }

    public void setQuickSelectionList(final List<String> quickSelectionList) {
        destination.setList(quickSelectionList);
    }

    /**
     * @param name
     * @param name2
     * @return
     */
    private boolean startsWith(final String name, final String name2) {
        if (CrossSystem.isWindows()) {//
            return name.toLowerCase(Locale.ENGLISH).startsWith(name2.toLowerCase(Locale.ENGLISH));
        }

        return name.startsWith(name2);
    }

    /**
     * @param text
     * @return
     */
    protected File textToFile(final String text) {
        return new File(text);
    }

}
