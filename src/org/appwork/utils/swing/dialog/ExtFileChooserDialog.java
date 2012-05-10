package org.appwork.utils.swing.dialog;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;

import org.appwork.swing.components.searchcombo.SearchComboBox;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.dialog.Dialog.FileChooserSelectionMode;

public class ExtFileChooserDialog extends AbstractDialog<File[]> {

    /**
     * 
     */
    public static final String       LASTSELECTION              = "LASTSELECTION_";

    /**
     * 
     */
    public static final String       FILECHOOSER                = "FILECHOOSER";
    private static boolean           SHELL_FOLDER_ID_WORKAROUND = false;
    private FileChooserSelectionMode fileSelectionMode          = FileChooserSelectionMode.FILES_AND_DIRECTORIES;
    private FileFilter               fileFilter;
    private boolean                  multiSelection             = false;

    private File                     preSelection;

    protected JFileChooser             fc;

    private BasicFileChooserUI       fcUI;

    private ArrayList<String>        quickSelectionList;

    protected boolean                selecting;

    public ArrayList<String> getQuickSelectionList() {
        return quickSelectionList;
    }

    public void setQuickSelectionList(ArrayList<String> quickSelectionList) {
        this.quickSelectionList = quickSelectionList;
    }

    public File getPreSelection() {
        return preSelection;
    }

    public void setPreSelection(File preSelection) {
        this.preSelection = preSelection;
    }

    public FileFilter getFileFilter() {
        return fileFilter;
    }

    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    public FileChooserSelectionMode getFileSelectionMode() {
        return fileSelectionMode;
    }

    public void setFileSelectionMode(FileChooserSelectionMode fileSelectionMode) {
        this.fileSelectionMode = fileSelectionMode;
    }

    /**
     * @param flag
     * @param title
     * @param icon
     * @param okOption
     * @param cancelOption
     */
    public ExtFileChooserDialog(int flag, String title, String okOption, String cancelOption) {
        super(flag | Dialog.STYLE_HIDE_ICON, title, null, okOption, cancelOption);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#createReturnValue()
     */
    @Override
    protected File[] createReturnValue() {

        if (isMultiSelection()) {
            File[] files = fc.getSelectedFiles();
            return files;
        } else {
            File f = fc.getSelectedFile();

            return new File[] { f };
        }

    }

    public static void main(String[] args) {
        try {
            System.out.println(Dialog.getInstance().showDialog(new ExtFileChooserDialog(0, "Title", null, null)));
        } catch (DialogClosedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DialogCanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void setReturnmask(boolean b) {
        if (b) {

            // fc.approveSelection();
        } else {
            fc.cancelSelection();
        }
        super.setReturnmask(b);
    }

    public void actionPerformed(final ActionEvent e) {

        if (e.getSource() == this.okButton) {
            Log.L.fine("Answer: Button<OK:" + this.okButton.getText() + ">");
            if (fcUI != null) {
                fcUI.getApproveSelectionAction().actionPerformed(e);
            } else {
                this.setReturnmask(true);
            }
        } else if (e.getSource() == this.cancelButton) {
            Log.L.fine("Answer: Button<CANCEL:" + this.cancelButton.getText() + ">");
            this.setReturnmask(false);
        }
        this.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#layoutDialogContent()
     */
    @Override
    public JComponent layoutDialogContent() {
        // TODO Auto-generated method stub
        if (SHELL_FOLDER_ID_WORKAROUND) {
            UIManager.put("FileChooser.useShellFolder", false);
        } else {
            UIManager.put("FileChooser.useShellFolder", true);
        }
        UIManager.put("FileChooser.homeFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_HOMEFOLDER());
        UIManager.put("FileChooser.newFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_NEWFOLDER());
        UIManager.put("FileChooser.upFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_UPFOLDER());
        UIManager.put("FileChooser.detailsViewButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_DETAILS());
        UIManager.put("FileChooser.listViewButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_LIST());

        fc = new JFileChooser() {
            private Insets nullInsets;
            {
                nullInsets = new Insets(0, 0, 0, 0);
            }

            public Insets getInsets() {
                return nullInsets;
            }

            @Override
            public void setCurrentDirectory(File dir) {
                selecting = true;
                try {

                    super.setCurrentDirectory(dir);
                } finally {
                    selecting = false;
                }
            }

            @Override
            public void setSelectedFile(File file) {
                selecting = true;
                try {

                    super.setSelectedFile(file);
                } finally {
                    selecting = false;
                }
            }

        };
        fc.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                Log.L.fine("Answer: PRESS:ENTER>");

                setReturnmask(true);

                dispose();
            }
        });

        try {
            fcUI = (BasicFileChooserUI) fc.getUI();
        } catch (Throwable e) {
            Log.exception(e);
        }
        fc.setControlButtonsAreShown(false);
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        if (fileSelectionMode != null) {
            fc.setFileSelectionMode(fileSelectionMode.getId());
        }

        if (fileFilter != null) {
            fc.setFileFilter(fileFilter);
        }

        if (multiSelection) {
            fc.setMultiSelectionEnabled(true);
        } else {
            fc.setMultiSelectionEnabled(false);
        }

        /* preSelection */

        Log.L.info("Given Preselection: " + preSelection);

        while (preSelection != null) {
            if (!preSelection.exists()) {
                /* file does not exist, try ParentFile */
                preSelection = preSelection.getParentFile();
            } else {
                if (preSelection.isDirectory()) {
                    fc.setCurrentDirectory(preSelection);
                    /*
                     * we have to setSelectedFile here too, so the folder is
                     * preselected
                     */

                } else {
                    fc.setCurrentDirectory(preSelection.getParentFile());
                    /* only preselect file in savedialog */

                    if (fileSelectionMode != null) {
                        if (fileSelectionMode.getId() == FileChooserSelectionMode.DIRECTORIES_ONLY.getId()) {
                            fc.setSelectedFile(preSelection.getParentFile());
                        } else {
                            fc.setSelectedFile(preSelection);
                        }
                    }

                }
                break;
            }

        }

        if (quickSelectionList != null && multiSelection == false) {
            try {
                // wraps the textfield to enter a path in a SearchCombobox
                // FilePane filepane = (sun.swing.FilePane)fc.getComponent(2);

                JPanel namePanel = (JPanel) ((JComponent) fc.getComponent(3)).getComponent(0);
                if (fileSelectionMode.getId() == FileChooserSelectionMode.DIRECTORIES_ONLY.getId()) {
                    ((JComponent) fc.getComponent(3)).getComponent(1).setVisible(false);
                    ((JComponent) fc.getComponent(3)).getComponent(2).setVisible(false);
                }
                final JTextField oldTextField = (JTextField) namePanel.getComponent(1);
                namePanel.remove(1);

                final String text = oldTextField.getText();
                SearchComboBox<String> destination = new SearchComboBox<String>() {

                    @Override
                    protected Icon getIconForValue(String value) {
                        return null;
                    }

                    public JTextField createTextField() {

                        return oldTextField;
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
                        if (selecting) return;
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                String txt = getText();
                                File f = new File(txt);
                                boolean parent = false;
                                while (f != null && f.getParentFile() != f) {
                                    if (f.exists()) {
                                        if (f.getParentFile() == null || parent) {
                                            fc.setCurrentDirectory(f);
                                            selecting = true;
                                            setText(txt);
                                            selecting = false;
                                        } else {
                                            if (getText().endsWith("\\") || getText().endsWith("/") && f.isDirectory()) {
                                                fc.setCurrentDirectory(f);
                                                selecting = true;
                                                setText(txt);
                                                selecting = false;
                                            } else {

                                                fc.setSelectedFile(f);
                                                selecting = true;
                                                setText(txt);
                                                selecting = false;
                                            }
                                        }
                                        return;
                                    } else {
                                        parent = true;
                                        f = f.getParentFile();
                                    }
                                }
                            }
                        });

                    }

                };
                // this code makes enter leave the dialog.
                destination.getTextField().getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "approveSelection");
                destination.getTextField().getInputMap().put(KeyStroke.getKeyStroke("pressed TAB"), "auto");

                destination.getTextField().setFocusTraversalKeysEnabled(false);
                destination.setActionMap(fc.getActionMap());
                destination.getTextField().getActionMap().put("auto", new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        auto(oldTextField);
                    }
                });
                destination.setUnkownTextInputAllowed(true);
                destination.setBadColor(null);
                destination.setSelectedItem(null);

                destination.setList(quickSelectionList);
                destination.setText(text);
                namePanel.add(destination);
                modifiyNamePanel(namePanel);
            } catch (Throwable e) {
                Log.exception(e);
            }

        }
        // [3][0][1]

        return fc;
    }

    /**
     * @param namePanel
     */
    protected void modifiyNamePanel(JPanel namePanel) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @param oldTextField
     * 
     */
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
        for (File f : fc.getCurrentDirectory().listFiles()) {

            if (fc.getFileFilter() != null && !fc.getFileFilter().accept(f)) continue;
            if (fc.getFileSelectionMode() == JFileChooser.FILES_ONLY && f.isDirectory()) continue;
            if (fc.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY && !f.isDirectory()) continue;

            if (f.isHidden() && fc.isFileHidingEnabled()) continue;
            if (equals(f.getName(), findName)) {
                found = true;
                continue;
            }

            if (found && startsWith(f.getName(), name)) {
                selecting = true;
                oldTextField.setText(f.getAbsolutePath());
                oldTextField.setSelectionStart(selstart);
                oldTextField.setSelectionEnd(oldTextField.getText().length());
                selecting = false;
                return;
            }
        }
        selecting = true;

        oldTextField.setText(bef);

        selecting = false;

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

    public boolean isMultiSelection() {
        return multiSelection;
    }

    public void setMultiSelection(boolean multiSelection) {
        this.multiSelection = multiSelection;
    }

    /**
     * @return
     */
    public File[] getSelection() {
        // TODO Auto-generated method stub
        return createReturnValue();
    }

}
