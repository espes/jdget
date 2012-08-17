package org.appwork.utils.swing.dialog;

import java.awt.Component;
import java.awt.Cursor;
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
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;

import org.appwork.storage.config.JsonConfig;
import org.appwork.swing.components.searchcombo.SearchComboBox;
import org.appwork.utils.Application;
import org.appwork.utils.StringUtils;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.SwingUtils;

public class ExtFileChooserDialog extends AbstractDialog<File[]> {
    static {

        UIManager.put("FileChooser.lookInLabelText", _AWU.T.DIALOG_FILECHOOSER_lookInLabelText());
        UIManager.put("FileChooser.saveInLabelText", _AWU.T.DIALOG_FILECHOOSER_saveInLabelText());

        UIManager.put("FileChooser.fileNameLabelText", _AWU.T.DIALOG_FILECHOOSER_fileNameLabelText());

        UIManager.put("FileChooser.folderNameLabelText", _AWU.T.DIALOG_FILECHOOSER_folderNameLabelText());

        UIManager.put("FileChooser.filesOfTypeLabelText", _AWU.T.DIALOG_FILECHOOSER_filesOfTypeLabelText());

        UIManager.put("FileChooser.upFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_UPFOLDER());
        UIManager.put("FileChooser.upFolderAccessibleName", _AWU.T.DIALOG_FILECHOOSER_upFolderAccessibleName());

        UIManager.put("FileChooser.homeFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_HOMEFOLDER());
        UIManager.put("FileChooser.homeFolderAccessibleName", _AWU.T.DIALOG_FILECHOOSER_homeFolderAccessibleName());

        UIManager.put("FileChooser.newFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_NEWFOLDER());
        UIManager.put("FileChooser.newFolderAccessibleName", _AWU.T.DIALOG_FILECHOOSER_newFolderAccessibleName());

        UIManager.put("FileChooser.listViewButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_LIST());
        UIManager.put("FileChooser.listViewButtonAccessibleName", _AWU.T.DIALOG_FILECHOOSER_listViewButtonAccessibleName());

        UIManager.put("FileChooser.detailsViewButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_DETAILS());
        UIManager.put("FileChooser.detailsViewButtonAccessibleName", _AWU.T.DIALOG_FILECHOOSER_detailsViewButtonAccessibleName());
        UIManager.put("FileChooser.newFolderErrorText", _AWU.T.DIALOG_FILECHOOSER_newFolderErrorText());
        UIManager.put("FileChooser.newFolderErrorSeparator", _AWU.T.DIALOG_FILECHOOSER_newFolderErrorSeparator());

        UIManager.put("FileChooser.newFolderParentDoesntExistTitleText", _AWU.T.DIALOG_FILECHOOSER_newFolderParentDoesntExistTitleText());
        UIManager.put("FileChooser.newFolderParentDoesntExistText", _AWU.T.DIALOG_FILECHOOSER_newFolderParentDoesntExistText());

        UIManager.put("FileChooser.fileDescriptionText", _AWU.T.DIALOG_FILECHOOSER_fileDescriptionText());
        UIManager.put("FileChooser.directoryDescriptionText", _AWU.T.DIALOG_FILECHOOSER_directoryDescriptionText());

        UIManager.put("FileChooser.saveButtonText", _AWU.T.DIALOG_FILECHOOSER_saveButtonText());
        UIManager.put("FileChooser.openButtonText", _AWU.T.DIALOG_FILECHOOSER_openButtonText());
        UIManager.put("FileChooser.saveDialogTitleText", _AWU.T.DIALOG_FILECHOOSER_saveDialogTitleText());
        UIManager.put("FileChooser.openDialogTitleText", _AWU.T.DIALOG_FILECHOOSER_openDialogTitleText());
        UIManager.put("FileChooser.cancelButtonText", _AWU.T.DIALOG_FILECHOOSER_cancelButtonText());
        UIManager.put("FileChooser.updateButtonText", _AWU.T.DIALOG_FILECHOOSER_updateButtonText());
        UIManager.put("FileChooser.helpButtonText", _AWU.T.DIALOG_FILECHOOSER_helpButtonText());
        UIManager.put("FileChooser.directoryOpenButtonText", _AWU.T.DIALOG_FILECHOOSER_directoryOpenButtonText());

        UIManager.put("FileChooser.saveButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_saveButtonToolTipText());
        UIManager.put("FileChooser.openButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_openButtonToolTipText());
        UIManager.put("FileChooser.cancelButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_cancelButtonToolTipText());
        UIManager.put("FileChooser.updateButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_updateButtonToolTipText());
        UIManager.put("FileChooser.helpButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_helpButtonToolTipText());
        UIManager.put("FileChooser.directoryOpenButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_directoryOpenButtonToolTipText());

    }
    /**
     * 
     */
    public static final String       LASTSELECTION     = "LASTSELECTION_";
    private final static Cursor      BUSY_CURSOR       = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    private final static Cursor      DEFAULT_CURSOR    = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
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

    private java.util.List<String>        quickSelectionList;

    protected boolean                selecting;

    private HashMap<String, File>    sambaFolders      = new HashMap<String, File>();

    private SearchComboBox<String>   destination;

    private ExtFileSystemView        fileSystemView;
    private Component                parentGlassPane;
    protected View                   view              = View.DETAILS;
    private FileChooserType          type              = FileChooserType.SAVE_DIALOG;
    private String                   storageID;

    public void setType(FileChooserType type) {
        this.type = type;
    }

    public java.util.List<String> getQuickSelectionList() {
        return quickSelectionList;
    }

    public void setQuickSelectionList(java.util.List<String> quickSelectionList) {
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
        try {
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
        } finally {
            getIDConfig().setLastSelection(fc.getCurrentDirectory().getAbsolutePath());
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

    protected void _init() {

        super._init();

    }

    @Override
    protected void packed() {
        // TODO Auto-generated method stub
        super.packed();
        if (parentGlassPane != null) {

            parentGlassPane.setCursor(DEFAULT_CURSOR);

            parentGlassPane.setVisible(false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#layoutDialogContent()
     */
    @Override
    public JComponent layoutDialogContent() {
        if (SwingUtilities.getRootPane(getDialog().getParent()) != null) parentGlassPane = SwingUtilities.getRootPane(getDialog().getParent()).getGlassPane();
        if (parentGlassPane != null) {
            parentGlassPane.setCursor(BUSY_CURSOR);
            parentGlassPane.setVisible(true);
        }
        fc = new JFileChooser(fileSystemView = new ExtFileSystemView()) {
            private Insets nullInsets;
            {
                nullInsets = new Insets(0, 0, 0, 0);
            }

            public Icon getIcon(File f) {
                Icon ret = super.getIcon(f);

                return getDirectoryIcon(ret, f);
            }

            public Insets getInsets() {
                return nullInsets;
            }

            @Override
            public void updateUI() {
                // UIManager.put("FileChooser.lookInLabelText",
                // _AWU.T.DIALOG_FILECHOOSER_lookInLabelText());
                // UIManager.put("FileChooser.saveInLabelText",
                // _AWU.T.DIALOG_FILECHOOSER_saveInLabelText());

                putClientProperty("FileChooser.useShellFolder", false);

                super.updateUI();
                System.out.println(getUI().getClass().getName());
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

        if (fileSystemView.getSambaFolders() != null) {

            // File[] list = windowsNetworkFolder.listFiles();
            for (File f : fileSystemView.getSambaFolders()) {
                sambaFolders.put(f.getPath(), f);
            }

        }
        // main: for (File r : fc.getFileSystemView().getRoots()) {
        // System.out.println(r.getPath());
        //
        // for (File mybenetwork : r.listFiles()) {
        //
        // // works a least on windows7
        // switch(CrossSystem.getID()){
        // case CrossSystem.OS_WINDOWS_7:
        // case CrossSystem.OS_WINDOWS_8:
        // case CrossSystem.OS_WINDOWS_VISTA:
        // if
        // (mybenetwork.getName().equalsIgnoreCase(ExtFileSystemView.VIRTUAL_NETWORKFOLDER))
        // {
        //
        // // break main;
        // }
        // break;
        // case CrossSystem.OS_WINDOWS_2000:
        // case CrossSystem.OS_WINDOWS_2003:
        // case CrossSystem.OS_WINDOWS_NT:
        // case CrossSystem.OS_WINDOWS_OTHER:
        // case CrossSystem.OS_WINDOWS_SERVER_2008:
        // case CrossSystem.OS_WINDOWS_XP:
        // }
        //
        //
        // }
        // }
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

        if (isFilePreviewEnabled()) {
            fc.setAccessory(new FilePreview(fc));
        }
        fc.setControlButtonsAreShown(false);
        fc.setDialogType(getType().getId());
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

        Log.L.info("Given presel: " + preSelection);

        File presel = preSelection;
        if (presel == null) {
            String path = getIDConfig().getLastSelection();
            presel = StringUtils.isEmpty(path) ? null : new File(path);

        }
        File orgPresel = presel;
        while (presel != null) {
            if (!presel.exists()) {
                /* file does not exist, try ParentFile */
                presel = presel.getParentFile();
            } else {
                if (presel.isDirectory()) {
                    fc.setCurrentDirectory(presel);
                    /*
                     * we have to setSelectedFile here too, so the folder is
                     * preselected
                     */

                } else {
                    fc.setCurrentDirectory(presel.getParentFile());
                    /* only preselect file in savedialog */

                    if (fileSelectionMode != null) {
                        if (fileSelectionMode.getId() == FileChooserSelectionMode.DIRECTORIES_ONLY.getId()) {
                            fc.setSelectedFile(presel.getParentFile());
                        } else {
                            fc.setSelectedFile(presel);
                        }
                    }

                }
                break;
            }

        }
        if(orgPresel!=null&&!orgPresel.isDirectory()){
            if(fc.getSelectedFile()==null||!fc.getSelectedFile().equals(orgPresel)){
                fc.setSelectedFile(new File(fc.getCurrentDirectory(),orgPresel.getName()));
            }
        }
        updateView();

        try {
            JToggleButton detailsButton = (JToggleButton) SwingUtils.getParent(fc, 0, 0, 7);
            detailsButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    view = View.DETAILS;
                }
            });

            JToggleButton listButton = (JToggleButton) SwingUtils.getParent(fc, 0, 0, 6);
            listButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    view = View.LIST;
                }
            });
        } catch (Throwable t) {

            // might throw exceptions, because the path, and the whole
            // detailsview thingy is part of the ui/LAF
            Log.exception(t);
        }
        // fc.addPropertyChangeListener(new PropertyChangeListener() {
        //
        // @Override
        // public void propertyChange(PropertyChangeEvent evt) {
        // System.out.println(evt);
        // }
        // });
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
                                if (fileSystemView.getNetworkFolder() != null && "\\".equals(txt)) { return fileSystemView.getNetworkFolder(); }
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
                // SwingUtils.printComponentTree(fc);
                // [2][0][0][0][0]
                JComponent c = (JComponent) fc.getComponent(2);
                c = (JComponent) c.getComponent(0);
                c = (JComponent) c.getComponent(0);
                c = (JComponent) c.getComponent(0);
                // sun.swing.FilePane

                // this is only a list in list view. else a jtable or something
                // else
                c = (JComponent) c.getComponent(0);
                if (c instanceof JList) {
                    final JList list = (JList) c;
                    list.addMouseListener(new MouseAdapter() {

                        // mouselistener sets directory back if we click in
                        // empty
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
                }

            } catch (Throwable e) {
                Log.exception(e);
            }
        }

        return fc;
    }

    /**
     * @return
     */
    private ExtFileChooserIdConfig getIDConfig() {

        File path = Application.getResource("cfg/FileChooser/" + getStorageID());
        path.getParentFile().mkdirs();
        return JsonConfig.create(path, ExtFileChooserIdConfig.class);
    }

    /**
     * @return
     */
    protected boolean isFilePreviewEnabled() {

        return getFileSelectionMode() != FileChooserSelectionMode.DIRECTORIES_ONLY;
    }

    /**
     * @return
     */
    public FileChooserType getType() {

        return type;
    }

    /**
     * 
     */
    private void updateView() {
        if (fc == null) return;
        switch (getView()) {
        case DETAILS:
            try {
                JToggleButton detailsButton = (JToggleButton) SwingUtils.getParent(fc, 0, 0, 7);
                detailsButton.doClick();
            } catch (Throwable t) {

                // might throw exceptions, because the path, and the whole
                // detailsview thingy is part of the ui/LAF
                Log.exception(t);
            }
            break;

        case LIST:
            try {
                JToggleButton detailsButton = (JToggleButton) SwingUtils.getParent(fc, 0, 0, 6);
                detailsButton.doClick();
            } catch (Throwable t) {

                // might throw exceptions, because the path, and the whole
                // detailsview thingy is part of the ui/LAF
                Log.exception(t);
            }
            break;

        }
    }

    /**
     * @return
     */
    public View getView() {
        return view;
    }

    public void setView(View view) {
        if (view == null) view = View.DETAILS;
        this.view = view;
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                updateView();
            }
        };
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

        return createReturnValue();
    }

    /**
     * @return
     */
    public File getSelectedFile() {
        if (isMultiSelection()) throw new IllegalStateException("Not available if multiselection is active. use #getSelection() instead");
        File[] sel = getSelection();
        return sel == null || sel.length == 0 ? null : sel[0];
    }

    /**
     * @param id
     */
    public void setStorageID(String id) {
        this.storageID = id;
    }

    public String getStorageID() {
        return storageID;
    }

}
