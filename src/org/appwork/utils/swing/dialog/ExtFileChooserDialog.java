package org.appwork.utils.swing.dialog;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;

import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
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

    private JFileChooser             fc;

    private BasicFileChooserUI       fcUI;

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

        return fc;
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
