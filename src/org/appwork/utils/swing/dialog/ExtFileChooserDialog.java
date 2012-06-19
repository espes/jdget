package org.appwork.utils.swing.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
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
    public static final String       LASTSELECTION     = "LASTSELECTION_";

    /**
     * 
     */
    public static final String       FILECHOOSER       = "FILECHOOSER";

    private FileChooserSelectionMode fileSelectionMode = FileChooserSelectionMode.FILES_AND_DIRECTORIES;
    private FileFilter               fileFilter;
    private boolean                  multiSelection    = false;

    private File                     preSelection;

    protected JFileChooser           fc;

    private BasicFileChooserUI       fcUI;

    private ArrayList<String>        quickSelectionList;

    protected boolean                selecting;

    private File                     windowsNetworkFolder;

    private HashMap<String, File>    sambaFolders      = new HashMap<String, File>();

    private SearchComboBox<String>   destination;

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
            if (f == null) {
                String path = getText();
                if (path != null) {
                    f = new File(path);

                } else {
                    return null;
                }
            }
            return new File[] { f };
        }

    }

    public static void main(String[] args) {
        try {

            // FileDialog dd = new FileDialog((java.awt.Dialog) null, "TEST",
            // FileDialog.LOAD);
            // dd.show(true);
            ExtFileChooserDialog d;
            d = new ExtFileChooserDialog(0, "Title", null, null);

            d.setFileSelectionMode(FileChooserSelectionMode.FILES_AND_DIRECTORIES);

            Dialog.getInstance().showDialog(d);
            File[] sel = d.getSelection();
            System.out.println(d.getSelection()[0].getAbsolutePath());
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

    public String getText() {
        try {
            return destination != null ? destination.getText() : fc.getSelectedFile().getAbsolutePath();
        } catch (Throwable e) {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#layoutDialogContent()
     */
    @Override
    public JComponent layoutDialogContent() {

        fc = new JFileChooser(new ExtFileSystemView()) {
            private Insets nullInsets;
            {
                nullInsets = new Insets(0, 0, 0, 0);
            }

            public Icon getIcon(File f) {
                Icon ret = super.getIcon(f);

                return getDirectoryIcon(ret,f);
            }

            public Insets getInsets() {
                return nullInsets;
            }

            @Override
            public void updateUI() {

                putClientProperty("FileChooser.useShellFolder", false);
                putClientProperty("FileChooser.homeFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_HOMEFOLDER());
                putClientProperty("FileChooser.newFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_NEWFOLDER());
                putClientProperty("FileChooser.upFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_UPFOLDER());
                putClientProperty("FileChooser.detailsViewButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_DETAILS());
                putClientProperty("FileChooser.listViewButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_LIST());
                super.updateUI();
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

        // find samba library
        main: for (File r : fc.getFileSystemView().getRoots()) {
            for (File mybenetwork : r.listFiles()) {

                // works a least on windows7
                if (mybenetwork.getName().equalsIgnoreCase(ExtFileSystemView.VIRTUAL_NETWORKFOLDER)) {
                    windowsNetworkFolder = mybenetwork;
                    // File[] list = windowsNetworkFolder.listFiles();
                    for (File f : windowsNetworkFolder.listFiles()) {
                        sambaFolders.put(f.getPath(), f);
                    }
                    // break main;
                }

            }
        }
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
                destination = new SearchComboBox<String>() {

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
                        if (selecting) {
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    File[] ret = createReturnValue();
                                    if (ret == null || ret.length == 0) {
                                        okButton.setEnabled(false);
                                    } else {
                                        okButton.setEnabled(true);
                                    }

                                }

                            });

                            return;
                        }
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    String txt = getText();

                                    File f = getFile(txt);

                                    boolean parent = false;
                                    while (f != null && f.getParentFile() != f) {
                                        if (exists(f)) {
                                            if (f.getParentFile() == null || !f.getParentFile().exists() || parent) {
                                                fc.setCurrentDirectory(f);
                                                fc.setSelectedFile(null);
                                                selecting = true;
                                                setText(txt);
                                                selecting = false;
                                            } else {
                                                if (getText().endsWith("\\") || getText().endsWith("/") && f.isDirectory()) {
                                                    fc.setCurrentDirectory(f);
                                                    fc.setSelectedFile(null);
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
                                } finally {
                                    File[] ret = createReturnValue();
                                    if (ret == null || ret.length == 0) {
                                        okButton.setEnabled(false);
                                    } else {
                                        okButton.setEnabled(true);
                                    }
                                }
                            }

                            private File getFile(String txt) {
                                if (windowsNetworkFolder != null && "\\".equals(txt)) { return windowsNetworkFolder; }
                                File ret = sambaFolders.get(new File(txt).getAbsolutePath());

                                return ret != null ? ret : new File(txt);
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

                // [2][0][0][0][0]
                JComponent c = (JComponent) fc.getComponent(2);
                c = (JComponent) c.getComponent(0);
                c = (JComponent) c.getComponent(0);
                c = (JComponent) c.getComponent(0);
                // sun.swing.FilePane
                final JList list = (JList) c.getComponent(0);
                list.addMouseListener(new MouseAdapter() {

                    // mouselistener sets directory back if we click in empty
                    // list spaces
                    public int loc2IndexFileList(JList jlist, Point point) {
                        int i = jlist.locationToIndex(point);
                        if (i != -1) {

                            if (!pointIsInActualBounds(jlist, i, point)) {
                                i = -1;
                            }
                        }
                        return i;
                    }

                    private boolean pointIsInActualBounds(JList jlist, int i, Point point) {
                        ListCellRenderer listcellrenderer = jlist.getCellRenderer();
                        ListModel listmodel = jlist.getModel();
                        Object obj = listmodel.getElementAt(i);
                        Component component = listcellrenderer.getListCellRendererComponent(jlist, obj, i, false, false);
                        Dimension dimension = component.getPreferredSize();
                        Rectangle rectangle = jlist.getCellBounds(i, i);
                        if (!component.getComponentOrientation().isLeftToRight()) rectangle.x += rectangle.width - dimension.width;
                        rectangle.width = dimension.width;
                        return rectangle.contains(point);
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int index = loc2IndexFileList(list, e.getPoint());
                        if (index < 0) {
                            File dir = fc.getSelectedFile();

                            if (dir != null) {
                                destination.setText(dir.getParent() + File.separator);
                                ListSelectionModel listSelectionModel = list.getSelectionModel();
                                if (listSelectionModel != null) {
                                    listSelectionModel.clearSelection();
                                    ((DefaultListSelectionModel) listSelectionModel).moveLeadSelectionIndex(0);
                                    listSelectionModel.setAnchorSelectionIndex(0);

                                }
                            }
                        }
                    }
                });

            } catch (Throwable e) {
                Log.exception(e);
            }
        }

        return fc;
    }

    /**
     * @param ret
     * @param f
     * @return
     */
    protected Icon getDirectoryIcon(Icon ret, File f) {

        return ret;
    }

    protected boolean exists(File f) {
        if (f.exists()) return true;
        if (isSambaFolder(f)) return true;
        return false;
    }

    /**
     * @param f
     * @return
     */
    protected boolean isSambaFolder(File f) {
        return sambaFolders.containsKey(f.getPath());
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
