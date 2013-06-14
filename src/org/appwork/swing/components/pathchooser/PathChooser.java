package org.appwork.swing.components.pathchooser;

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

            this.putValue(Action.NAME, PathChooser.this.getBrowseLabel());
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

            final File file = PathChooser.this.doFileChooser();
            if (file == null) { return; }
            PathChooser.this.setFile(file);

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
        this.setOpaque(false);
        this.txt = new ExtTextField() {

            /**
             * 
             */
            private static final long serialVersionUID = 3243788323043431841L;

            @Override
            public JPopupMenu getPopupMenu(final CutAction cutAction, final CopyAction copyAction, final PasteAction pasteAction, final DeleteAction deleteAction, final SelectAction selectAction) {
                final JPopupMenu self = PathChooser.this.getPopupMenu(PathChooser.this.txt, cutAction, copyAction, pasteAction, deleteAction, selectAction);

                if (self == null) { return super.getPopupMenu(cutAction, copyAction, pasteAction, deleteAction, selectAction); }
                return self;

            }

        };
        this.txt.setHelpText(this.getHelpText());
        this.bt = new ExtButton(new BrowseAction());

        if (useQuickLIst) {
            this.destination = new SearchComboBox<String>() {

                @Override
                public JTextField createTextField() {

                    return PathChooser.this.txt;
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
                    PathChooser.this.onChanged(PathChooser.this.txt);
                }

            };
            // this code makes enter leave the dialog.

            this.destination.getTextField().getInputMap().put(KeyStroke.getKeyStroke("pressed TAB"), "auto");

            this.destination.getTextField().setFocusTraversalKeysEnabled(false);

            this.destination.getTextField().getActionMap().put("auto", new AbstractAction() {

                @Override
                public void actionPerformed(final ActionEvent e) {
                    PathChooser.this.auto(PathChooser.this.txt);
                }
            });
            this.destination.setUnkownTextInputAllowed(true);
            this.destination.setBadColor(null);
            this.destination.setSelectedItem(null);

            this.add(this.destination);
        } else {
            this.add(this.txt);
        }
        this.add(this.bt);

        final String preSelection = JSonStorage.getStorage(Dialog.FILECHOOSER).get(Dialog.LASTSELECTION + id, this.getDefaultPreSelection());
        if (preSelection != null) {
            this.setFile(new File(preSelection));
        }

    }

    @Override
    public synchronized void addMouseListener(final MouseListener l) {
        this.txt.addMouseListener(l);
        this.bt.addMouseListener(l);
        super.addMouseListener(l);

    }

    protected void auto(final JTextField oldTextField) {
        final String txt = oldTextField.getText();

        final int selstart = oldTextField.getSelectionStart();
        final int selend = oldTextField.getSelectionEnd();
        if (selend != txt.length()) { return; }
        final String sel = txt.substring(selstart, selend);
        final String bef = txt.substring(0, selstart);
        final String name = bef.endsWith("/") || bef.endsWith("\\") ? "" : new File(bef).getName();
        final String findName = txt.endsWith("/") || txt.endsWith("\\") ? "" : new File(txt).getName();
        boolean found = sel.length() == 0;
        File root = new File(bef);
        while (!root.exists() && root != null) {
            if (root.getParentFile() == root) { return; }
            root = root.getParentFile();

        }
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

            if (found && this.startsWith(f.getName(), name)) {

                oldTextField.setText(f.getAbsolutePath());
                oldTextField.setSelectionStart(selstart);
                oldTextField.setSelectionEnd(oldTextField.getText().length());

                return;
            }
        }
        oldTextField.setText(bef);

    }

    /**
     * @return
     */
    public File doFileChooser() {

        final ExtFileChooserDialog d = new ExtFileChooserDialog(0, this.getDialogTitle(), null, null);
        d.setStorageID(this.getID());
        d.setFileSelectionMode(this.getSelectionMode());
        d.setFileFilter(this.getFileFilter());
        d.setType(this.getType());
        d.setMultiSelection(false);
        d.setPreSelection(this.getFile());
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
        this.remove(this.bt);
        this.setLayout(new MigLayout("ins 0", "[grow,fill]", "[grow,fill]"));
        return this.bt;
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
        if (StringUtils.isEmpty(this.txt.getText())) { return null; }
        return this.textToFile(this.txt.getText());
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
        return this.id;
    }

    /**
     * @return
     */
    public String getPath() {

        return this.txt.getText();
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
        this.txt.removeMouseListener(l);
        this.bt.removeMouseListener(l);
        super.removeMouseListener(l);
    }

    @Override
    public void setEnabled(final boolean b) {
        this.txt.setEnabled(b);
        this.bt.setEnabled(b);
        if (this.destination != null) {
            this.destination.setEnabled(b);
        }

    }

    public void setFile(final File file) {

        this.txt.setText(this.fileToText(file));
    }

    /**
     * @param packagizerFilterRuleDialog_layoutDialogContent_dest_help
     */
    public void setHelpText(final String helpText) {
        if (this.destination != null) {
            this.destination.setHelpText(helpText);
        }
        this.txt.setHelpText(helpText);

    }

    /**
     * @param downloadDestination
     */
    public void setPath(final String downloadDestination) {
        this.txt.setText(downloadDestination);

    }

    public void setQuickSelectionList(final List<String> quickSelectionList) {

        this.destination.setList(quickSelectionList);
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
