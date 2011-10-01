/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.awt.Component;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.appwork.resources.AWUTheme;
import org.appwork.storage.JSonStorage;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.interfaces.ValueConverter;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTHelper;

/**
 * A Dialog Instance which provides extended Dialog features and thus replaces
 * JOptionPane
 */
public class Dialog implements WindowFocusListener {
    /**
     * 
     */
    public static final String LASTSELECTION = "LASTSELECTION_";

    /**
     * 
     */
    public static final String FILECHOOSER = "FILECHOOSER";

    /**
     * Requests a FileChooserDialog.
     * 
     * @param id
     *            ID of the dialog (used to save and restore the old directory)
     * @param title
     *            The Dialog's Window Title dialog-title or null for default
     * @param fileSelectionMode
     *            mode for selecting files (like JFileChooser.FILES_ONLY) or
     *            null for default
     * @param fileFilter
     *            filters the choosable files or null for default
     * @param multiSelection
     *            Multiple files choosable? or null for default
     * @param preSelection
     *            File which will be selected by default. leave null for
     *            automode
     * @return an array of files or null if the user cancel the dialog
     */
    public static enum FileChooserSelectionMode {
        FILES_ONLY(JFileChooser.FILES_ONLY),
        DIRECTORIES_ONLY(JFileChooser.DIRECTORIES_ONLY),
        FILES_AND_DIRECTORIES(JFileChooser.FILES_AND_DIRECTORIES);
        private final int id;

        private FileChooserSelectionMode(final int num) {
            this.id = num;
        }

        /**
         * @return the id
         */
        public int getId() {
            return this.id;
        }
    }

    public static enum FileChooserType {
        OPEN_DIALOG(JFileChooser.OPEN_DIALOG),
        SAVE_DIALOG(JFileChooser.SAVE_DIALOG),
        CUSTOM_DIALOG(JFileChooser.CUSTOM_DIALOG),
        OPEN_DIALOG_WITH_PRESELECTION(JFileChooser.OPEN_DIALOG);
        private final int id;

        private FileChooserType(final int id) {
            this.id = id;
        }

        /**
         * @return the id
         */
        public int getId() {
            return this.id;
        }
    }

    static {

        // AWU has their own filechooser extensions
        UIManager.put("Synthetica.extendedFileChooser.rememberPreferences", Boolean.FALSE);
        UIManager.put("Synthetica.extendedFileChooser.rememberLastDirectory", Boolean.FALSE);
    }

    /**
     * Hide the cancel Button
     */
    public static final int     BUTTONS_HIDE_CANCEL                  = 1 << 4;

    /**
     * Hide the OK button
     */
    public static final int     BUTTONS_HIDE_OK                      = 1 << 3;
    /**
     * Icon Key for Error Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int, boolean)
     */
    public static final String  ICON_ERROR                           = "dialog/error";
    /**
     * Icon Key for Information Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int, boolean)
     */
    public static final String  ICON_INFO                            = "dialog/info";
    /**
     * Icon Key for Question Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int, boolean)
     */
    public static final String  ICON_QUESTION                        = "dialog/help";
    /**
     * Icon Key for Warning Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int, boolean)
     */
    public static final String  ICON_WARNING                         = "dialog/warning";
    /**
     * internal singleton instance to access the instance of this class
     */
    private static final Dialog INSTANCE                             = new Dialog();
    /**
     * LOGIC_BYPASS all dialogs. Try to fill automatically or return null
     */
    public static final int     LOGIC_BYPASS                         = 1 << 1;

    /**
     * Use this flag to show display of the Timer
     */
    public static final int     LOGIC_COUNTDOWN                      = 1 << 2;
    /**
     * Don't show again is only valid for this session, but is not saved for
     * further sessions
     */
    public static final int     LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT = 1 << 11;
    public static final int     LOGIC_DONOTSHOW_BASED_ON_TITLE_ONLY  = 1 << 12;

    /**
     * Often, the {@link #STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} option does not make
     * sense for the cancel option. Use this flag if the option should be
     * ignored if the user selects Cancel
     */
    public static final int     LOGIC_DONT_SHOW_AGAIN_IGNORES_CANCEL = 1 << 9;
    /**
     * Often, the {@link #STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} option does not make
     * sense for the ok option. Use this flag if the option should be ignored if
     * the user selects OK
     */
    public static final int     LOGIC_DONT_SHOW_AGAIN_IGNORES_OK     = 1 << 10;
    /**
     * if the user pressed cancel, the return mask will contain this mask
     */
    public static final int     RETURN_CANCEL                        = 1 << 2;
    /**
     * if user closed the window
     */
    public static final int     RETURN_CLOSED                        = 1 << 6;
    /**
     * this return flag can be set in two situations:<br>
     * a) The user selected the {@link #STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} Option<br>
     * b) The dialog has been skipped because the DO NOT SHOW AGAIN flag has
     * been set previously<br>
     * <br>
     * Check {@link #RETURN_SKIPPED_BY_DONT_SHOW} to know if the dialog has been
     * visible or autoskipped
     */
    public static final int     RETURN_DONT_SHOW_AGAIN               = 1 << 3;

    /**
     * If the user pressed OK, the return mask will contain this flag
     */
    public static final int     RETURN_OK                            = 1 << 1;
    /**
     * If the dialog has been skipped due to previously selected
     * {@link #STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} Option, this return flag is set.
     * 
     * @see #RETURN_DONT_SHOW_AGAIN
     */
    public static final int     RETURN_SKIPPED_BY_DONT_SHOW          = 1 << 4;
    /**
     * If the Timeout ({@link #LOGIC_COUNTDOWN}) has run out, the return mask
     * contains this flag
     */
    public static final int     RETURN_TIMEOUT                       = 1 << 5;

    private static boolean      ShellFolderIDWorkaround              = false;

    public static boolean isClosed(final Object value) {
        if (!(value instanceof Integer)) { return false; }
        return BinaryLogic.containsSome((Integer) value, Dialog.RETURN_CLOSED);
    }

    public static boolean isOK(final Object value) {
        if (!(value instanceof Integer)) { return false; }
        return BinaryLogic.containsSome((Integer) value, Dialog.RETURN_OK);
    }

    private List<? extends Image> iconList                        = null;

    /**
     * Do Not use an Icon. By default dialogs have an Icon
     */
    public static final int       STYLE_HIDE_ICON                 = 1 << 8;
    /**
     * Some dialogs are able to render HTML. Use this switch to enable html
     */
    public static final int       STYLE_HTML                      = 1 << 7;
    /**
     * Some dialogs are able to layout themselves in a large mode. E:g. to
     * display a huge text.
     */
    public static final int       STYLE_LARGE                     = 1 << 6;

    /**
     * Displays a Checkbox with "do not show this again" text. If the user
     * selects this box, the UserInteraktion class will remember the answer and
     * will not disturb the user with the same question (same title)
     */
    public static final int       STYLE_SHOW_DO_NOT_DISPLAY_AGAIN = 1 << 5;

    /**
     * Inputdialogs will use passwordfields instead of textfields
     */
    public static final int       STYLE_PASSWORD                  = 1 << 9;

    /**
     * tries to find some special markers in the text and selects an appropriate
     * icon
     * 
     * @param text
     * @return
     */
    public static ImageIcon getIconByText(final String text) {
        try {
            if (text.contains("?")) {
                return AWUTheme.I().getIcon(Dialog.ICON_QUESTION, 32);
            } else if (text.contains("error") || text.contains("exception")) {
                return AWUTheme.I().getIcon(Dialog.ICON_ERROR, 32);
            } else if (text.contains("!")) {
                return AWUTheme.I().getIcon(Dialog.ICON_WARNING, 32);
            } else {
                return AWUTheme.I().getIcon(Dialog.ICON_INFO, 32);
            }
        } catch (final Throwable e) {
            Log.exception(e);
            return null;
        }
    }

    /**
     * Return the singleton instance of Dialog
     * 
     * @return
     */
    public static Dialog getInstance() {
        return Dialog.INSTANCE;
    }

    /**
     * The max counter value for a timeout Dialog
     */
    private int                     countdownTime = 20;

    /**
     * Parent window for all dialogs created with abstractdialog
     */
    private Component               owner         = null;

    private final ArrayList<Window> parents;

    private Dialog() {
        this.parents = new ArrayList<Window>();
    }

    /**
     * @return the {@link Dialog#countdownTime}
     * @see Dialog#countdownTime
     */
    protected int getCountdownTime() {
        return this.countdownTime;
    }

    /**
     * @return
     */
    public List<? extends Image> getIconList() {
        return this.iconList;
    }

    /**
     * @return the {@link Dialog#owner}
     * @see Dialog#owner
     */
    public Component getParentOwner() {

        return this.owner;
    }

    /**
     * returns all windows which have been registerted by
     * {@link #registerFrame(Window)}
     * 
     * @return
     */
    public ArrayList<Window> getRegisteredParents() {
        return this.parents;
    }

    /**
     * Register all windows here that might become parent for dialogs.
     * Dialogsystem will set them as parent if they gain focus
     * 
     * @param frame
     */
    public void registerFrame(final Window frame) {
        frame.addWindowFocusListener(this);

        this.parents.add(frame);
    }

    /**
     * @param countdownTime
     *            the {@link Dialog#countdownTime} to set
     * @see Dialog#countdownTime
     */
    public void setCountdownTime(final int countdownTime) {
        this.countdownTime = countdownTime;
    }

    public void setIconList(final List<? extends Image> iconList) {
        this.iconList = iconList;
    }

    /**
     * @param parent
     *            this is needed for correct parentWindow handling for correct
     *            dialog show order the {@link Dialog#owner} to set
     * @see Dialog#owner
     */
    public void setParentOwner(final Component parent) {
        this.owner = parent;

        if (parent == null) {
            Log.exception(new NullPointerException("parent == null"));
        }

    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param title
     *            The Dialog's Window Title
     * @param question
     *            The Dialog is able to show a question to the user.
     * @param options
     *            A list of various options to select
     * @param defaultSelectedIndex
     *            The option which is selected by default
     * @param icon
     *            The dialog is able to display an Icon If this is null, the
     *            dialog might select an Icon derived from the message and title
     *            text
     * @param okOption
     *            Text for OK Button [null for default]
     * @param cancelOption
     *            Text for Cancel Button [null for default]
     * @param renderer
     *            A renderer to customize the Dialog. Might be null
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public int showComboDialog(final int flag, final String title, final String question, final Object[] options, final int defaultSelectedIndex, final ImageIcon icon, final String okOption, final String cancelOption, final ListCellRenderer renderer) throws DialogClosedException, DialogCanceledException {
        if ((flag & Dialog.LOGIC_BYPASS) > 0) { return defaultSelectedIndex; }
        return this.showDialog(new ComboBoxDialog(flag, title, question, options, defaultSelectedIndex, icon, okOption, cancelOption, renderer));
    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param title
     *            The Dialog's Window Title
     * @param question
     *            The Dialog is able to show a question to the user.
     * @param options
     *            A list of various options to select
     * @param defaultSelectedItem
     *            The option which is selected by default
     * @param icon
     *            The dialog is able to display an Icon If this is null, the
     *            dialog might select an Icon derived from the message and title
     *            text
     * @param okOption
     *            Text for OK Button [null for default]
     * @param cancelOption
     *            Text for Cancel Button [null for default]
     * @param renderer
     *            A renderer to customize the Dialog. Might be null
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public Object showComboDialog(final int flag, final String title, final String question, final Object[] options, final Object defaultSelectedItem, final ImageIcon icon, final String okOption, final String cancelOption, final ListCellRenderer renderer) throws DialogClosedException, DialogCanceledException {
        if ((flag & Dialog.LOGIC_BYPASS) > 0) { return defaultSelectedItem; }
        int def = 0;
        for (int i = 0; i < options.length; i++) {
            if (options[i] == defaultSelectedItem) {
                def = i;
                break;
            }

        }
        final Integer returnIndex = this.showDialog(new ComboBoxDialog(flag, title, question, options, def, icon, okOption, cancelOption, renderer));
        return options[returnIndex];

    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param question
     *            The Dialog is able to show a question to the user.
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public int showConfirmDialog(final int flag, final String question) throws DialogClosedException, DialogCanceledException {
        return this.showConfirmDialog(flag, _AWU.T.DIALOG_CONFIRMDIALOG_TITLE(), question, Dialog.getIconByText(question), null, null);
    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param title
     *            The Dialog's Window Title
     * @param question
     *            The Dialog is able to show a question to the user.
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public int showConfirmDialog(final int flag, final String title, final String question) throws DialogClosedException, DialogCanceledException {
        return this.showConfirmDialog(flag, title, question, Dialog.getIconByText(title + question), null, null);
    }

    /**
     * Requests a ConfirmDialog
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param title
     *            The Dialog's Window Title
     * @param message
     *            The Dialog is able to show a message to the user
     * @param icon
     *            The dialog is able to display an Icon If this is null, the
     *            dialog might select an Icon derived from the message and title
     *            text
     * @param okOption
     *            Text for OK Button [null for default] Text for OK Button [null
     *            for default]
     * @param cancelOption
     *            Text for Cancel Button [null for default] Text for cancel
     *            Button [null for default]
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public int showConfirmDialog(final int flag, final String title, final String message, final ImageIcon tmpicon, final String okOption, final String cancelOption) throws DialogClosedException, DialogCanceledException {
        if ((flag & Dialog.LOGIC_BYPASS) > 0) { return 0; }
        final ImageIcon icon;
        if (tmpicon == null) {
            icon = Dialog.getIconByText(title + message);
        } else {
            icon = tmpicon;
        }
        return this.showDialog(new ConfirmDialog(flag, title, message, icon, okOption, cancelOption));
    }

    /**
     * note: showdialog must not call init itself!!
     * 
     * @param <T>
     * @param dialog
     * @return
     * @throws DialogClosedException
     * @throws DialogCanceledException
     */
    public <T> T showDialog(final AbstractDialog<T> dialog) throws DialogClosedException, DialogCanceledException {
        if (dialog == null) { return null; }
        final T ret = new EDTHelper<T>() {
            @Override
            public T edtRun() {
                dialog.displayDialog();
                return dialog.getReturnValue();
            }

        }.getReturnValue();
        final int mask = dialog.getReturnmask();
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CLOSED)) { throw new DialogClosedException(mask); }
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CANCEL)) { throw new DialogCanceledException(mask); }
        return ret;
    }

    public int showErrorDialog(final String s) {

        try {
            return this.showConfirmDialog(Dialog.BUTTONS_HIDE_CANCEL | Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN, _AWU.T.DIALOG_ERROR_TITLE(), s, AWUTheme.I().getIcon(Dialog.ICON_ERROR, 32), null, null);
        } catch (final DialogClosedException e) {
            return Dialog.RETURN_CLOSED;
        } catch (final DialogCanceledException e) {
            return Dialog.RETURN_CANCEL;
        }

    }

    /**
     * @param string
     * @param message
     * @param e
     */
    public int showExceptionDialog(final String title, final String message, final Throwable e) {

        try {
            final ExceptionDialog dialog = new ExceptionDialog(Dialog.LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT | Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN | Dialog.BUTTONS_HIDE_CANCEL, title, message, e, null, null);
            this.showDialog(dialog);
        } catch (final DialogClosedException e1) {
            return Dialog.RETURN_CLOSED;
        } catch (final DialogCanceledException e1) {
            return Dialog.RETURN_CANCEL;
        }

        return 0;
    }

    public File[] showFileChooser(final String id, final String title, final FileChooserSelectionMode fileSelectionMode, final FileFilter fileFilter, final boolean multiSelection, final FileChooserType dialogType, final File preSelect) throws DialogCanceledException, DialogClosedException {
        final int[] maskWrapper = new int[1];
        final File[] ret = new EDTHelper<File[]>() {

            @Override
            public File[] edtRun() {
                for (int tried = 0; tried < 2; tried++) {
                    try {
                        if (Dialog.ShellFolderIDWorkaround) {
                            UIManager.put("FileChooser.useShellFolder", false);
                        } else {
                            UIManager.put("FileChooser.useShellFolder", true);
                        }
                        UIManager.put("FileChooser.homeFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_HOMEFOLDER());
                        UIManager.put("FileChooser.newFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_NEWFOLDER());
                        UIManager.put("FileChooser.upFolderToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_UPFOLDER());
                        UIManager.put("FileChooser.detailsViewButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_DETAILS());
                        UIManager.put("FileChooser.listViewButtonToolTipText", _AWU.T.DIALOG_FILECHOOSER_TOOLTIP_LIST());

                        final JFileChooser fc = new JFileChooser();
                        if (Dialog.ShellFolderIDWorkaround) {
                            fc.putClientProperty("FileChooser.useShellFolder", false);
                        } else {
                            fc.putClientProperty("FileChooser.useShellFolder", true);
                        }
                        if (title != null) {
                            fc.setDialogTitle(title);
                        }
                        boolean allowFilePreview = true;
                        if (fileSelectionMode != null) {
                            fc.setFileSelectionMode(fileSelectionMode.getId());
                            if (fileSelectionMode.getId() == FileChooserSelectionMode.DIRECTORIES_ONLY.getId()) {
                                allowFilePreview = false;
                            }
                        }

                        if (fileFilter != null) {
                            fc.setFileFilter(fileFilter);
                        }

                        if (multiSelection) {
                            fc.setMultiSelectionEnabled(true);
                        } else {
                            fc.setMultiSelectionEnabled(false);
                        }
                        if (dialogType != null) {
                            fc.setDialogType(dialogType.getId());
                            if (dialogType.getId() != FileChooserType.OPEN_DIALOG.getId()) {
                                allowFilePreview = false;
                            }
                        }

                        if (allowFilePreview) {
                            fc.setAccessory(new FilePreview(fc));
                        }

                        /* preSelection */
                        File preSelection = preSelect;
                        if (preSelection == null && JSonStorage.getStorage(FILECHOOSER).get(LASTSELECTION + id, (String) null) != null) {
                            preSelection = new File(JSonStorage.getStorage(FILECHOOSER).get(LASTSELECTION + id, (String) null));
                            Log.L.info("Preselection: " + id + ": " + JSonStorage.getStorage(FILECHOOSER).get(LASTSELECTION + id, (String) null));
                        } else {
                            Log.L.info("Given Preselection: " + preSelection);
                        }
                        while (preSelection != null) {
                            if (!preSelection.exists() && dialogType != null && dialogType.getId() == FileChooserType.OPEN_DIALOG.getId()) {
                                /* file does not exist, try ParentFile */
                                preSelection = preSelection.getParentFile();
                            } else {
                                if (preSelection.isDirectory()) {
                                    fc.setCurrentDirectory(preSelection);
                                    /*
                                     * we have to setSelectedFile here too, so
                                     * the folder is preselected
                                     */
                                    /*
                                     * only preselect folder in case of
                                     * savedialog
                                     */
                                    if (dialogType != null && (dialogType.getId() == FileChooserType.SAVE_DIALOG.getId() || dialogType == FileChooserType.OPEN_DIALOG_WITH_PRESELECTION)) {
                                        fc.setSelectedFile(preSelection);
                                    }
                                } else {
                                    fc.setCurrentDirectory(preSelection.getParentFile());
                                    /* only preselect file in savedialog */
                                    if (dialogType != null && (dialogType.getId() == FileChooserType.SAVE_DIALOG.getId() || dialogType == FileChooserType.OPEN_DIALOG_WITH_PRESELECTION)) {
                                        if (fileSelectionMode != null) {
                                            if (fileSelectionMode.getId() == FileChooserSelectionMode.DIRECTORIES_ONLY.getId()) {
                                                fc.setSelectedFile(preSelection.getParentFile());
                                            } else {
                                                fc.setSelectedFile(preSelection);
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                        if (dialogType == null || dialogType.getId() == FileChooserType.OPEN_DIALOG.getId()) {
                            switch (maskWrapper[0] = fc.showOpenDialog(Dialog.this.getParentOwner())) {
                            case JFileChooser.APPROVE_OPTION:
                                if (multiSelection) {
                                    final ArrayList<File> rets = new ArrayList<File>();
                                    File[] sfiles = fc.getSelectedFiles();
                                    File sfile = fc.getSelectedFile();
                                    for (File ret : sfiles) {
                                        ret = Dialog.this.validateFileType(ret, fileSelectionMode.getId(), false);
                                        if (ret != null) {
                                            rets.add(ret);
                                        }
                                    }
                                    
                                    if(rets.size()==0){
                                        sfile = Dialog.this.validateFileType(sfile, fileSelectionMode.getId(), false);
                                        if (sfile != null) {
                                            rets.add(sfile);
                                        }
                                    }

                                    if (rets.size() > 0) {
                                        final File[] files = rets.toArray(new File[rets.size()]);
                                        final File first = files[0];
                                        if (first != null) {
                                            JSonStorage.getStorage(FILECHOOSER).put(LASTSELECTION + id, first.getAbsolutePath());
                                        }
                                        return files;
                                    } else {
                                        return null;
                                    }
                                }

                                File ret = fc.getSelectedFile();
                                /*
                                 * validate selectedFile against
                                 * fileSelectionMode
                                 */
                                ret = Dialog.this.validateFileType(ret, fileSelectionMode.getId(), false);
                                if (ret != null) {
                                    JSonStorage.getStorage(FILECHOOSER).put(LASTSELECTION + id, ret.getAbsolutePath());
                                    return new File[] { ret };
                                } else {
                                    return null;
                                }

                            }
                        } else if (dialogType.getId() == FileChooserType.SAVE_DIALOG.getId()) {
                            if ((maskWrapper[0] = fc.showSaveDialog(Dialog.this.getParentOwner())) == JFileChooser.APPROVE_OPTION) {
                                File ret = fc.getSelectedFile();
                                /*
                                 * validate selectedFile against
                                 * fileSelectionMode
                                 */
                                ret = Dialog.this.validateFileType(ret, fileSelectionMode.getId(), true);
                                if (ret != null) {
                                    JSonStorage.getStorage(FILECHOOSER).put(LASTSELECTION + id, ret.getAbsolutePath());
                                    return new File[] { ret };
                                } else {
                                    return null;
                                }
                            }
                        }
                        return null;
                    } catch (final Exception e) {
                        if (e != null && e.getMessage() != null && e.getMessage().contains("shell") && !Dialog.ShellFolderIDWorkaround) {
                            Log.L.info("Enabling Workaround for \"Could not get shell folder ID list\"");
                            Dialog.ShellFolderIDWorkaround = true;
                        } else {
                            Log.exception(e);
                            return null;
                        }
                    }
                }
                return null;
            }

        }.getReturnValue();

        if (maskWrapper[0] == JFileChooser.CANCEL_OPTION) { throw new DialogCanceledException(Dialog.RETURN_CANCEL); }
        if (maskWrapper[0] == JFileChooser.ERROR_OPTION) { throw new DialogClosedException(Dialog.RETURN_CLOSED); }
        return ret;
    }

    /**
     * @Deprecated Use
     *             {@link #showFileChooser(String, String, FileChooserSelectionMode, FileFilter, boolean, FileChooserType, File)}
     *             instead
     * @param id
     * @param title
     * @param fileSelectionMode
     * @param fileFilter
     * @param multiSelection
     * @param dialogType
     * @param preSelect
     * @return
     * @throws DialogClosedException
     * @throws DialogCanceledException
     */
    @Deprecated
    public File[] showFileChooser(final String id, final String title, int fileSelectionMode, final FileFilter fileFilter, final boolean multiSelection, final int dialogType, final File preSelect) throws DialogCanceledException, DialogClosedException {

        FileChooserSelectionMode fsm = null;
        if (fileSelectionMode < 0) {
            fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES;
        }
        for (final FileChooserSelectionMode f : FileChooserSelectionMode.values()) {
            if (f.getId() == fileSelectionMode) {
                fsm = f;
                break;
            }
        }
        FileChooserType fct = null;
        for (final FileChooserType f : FileChooserType.values()) {
            if (f.getId() == dialogType) {
                fct = f;
                break;
            }
        }
        return this.showFileChooser(id, title, fsm, fileFilter, multiSelection, fct, preSelect);
    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog flag
     * @param question
     *            The Dialog is able to show a question to the user. question
     * @param defaultvalue
     *            defaultvalue
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public String showInputDialog(final int flag, final String question, final String defaultvalue) throws DialogClosedException, DialogCanceledException {
        return this.showInputDialog(flag, _AWU.T.DIALOG_INPUT_TITLE(), question, defaultvalue, Dialog.getIconByText(question), null, null);
    }

    /**
     * Requests in Inputdialog.
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param title
     *            The Dialog's Window Title
     * @param message
     *            The Dialog is able to show a message to the user
     * @param defaultMessage
     * @param icon
     *            The dialog is able to display an Icon If this is null, the
     *            dialog might select an Icon derived from the message and title
     *            text
     * @param okOption
     *            Text for OK Button [null for default]
     * @param cancelOption
     *            Text for Cancel Button [null for default]
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public String showInputDialog(final int flag, final String title, final String message, final String defaultMessage, final ImageIcon icon, final String okOption, final String cancelOption) throws DialogClosedException, DialogCanceledException {
        if ((flag & Dialog.LOGIC_BYPASS) > 0) { return defaultMessage; }
        return this.showDialog(new InputDialog(flag, title, message, defaultMessage, icon, okOption, cancelOption));
    }

    /**
     * 
     * @param message
     *            The Dialog is able to show a message to the user
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public String showInputDialog(final String message) throws DialogClosedException, DialogCanceledException {
        return this.showInputDialog(0, message, null);
    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param message
     *            The Dialog is able to show a message to the user
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public void showMessageDialog(final int flag, final String message) {

        this.showMessageDialog(flag, _AWU.T.DIALOG_MESSAGE_TITLE(), message);

    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param title
     *            The Dialog's Window Title
     * @param message
     *            The Dialog is able to show a message to the user
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public void showMessageDialog(final int flag, final String title, final String message) {
        try {
            this.showConfirmDialog(Dialog.BUTTONS_HIDE_CANCEL | flag, title, message, Dialog.getIconByText(title + message), null, null);
        } catch (final DialogClosedException e) {

        } catch (final DialogCanceledException e) {

        }
    }

    /**
     * 
     * @param message
     *            The Dialog is able to show a message to the user
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public void showMessageDialog(final String message) {
        this.showMessageDialog(0, _AWU.T.DIALOG_MESSAGE_TITLE(), message);
    }

    /**
     * 
     * @param title
     *            The Dialog's Window Title
     * @param message
     *            The Dialog is able to show a message to the user
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public void showMessageDialog(final String title, final String message) {
        this.showMessageDialog(0, title, message);
    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog flag
     * @param question
     *            The Dialog is able to show 3 Passwordfields to the user.
     *            question
     * @param defaultvalue
     *            defaultvalue
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public String showPasswordDialog(final int flag, final String question, final String defaultvalue) throws DialogClosedException, DialogCanceledException {
        return this.showPasswordDialog(flag, _AWU.T.DIALOG_PASSWORD_TITLE(), question, defaultvalue, Dialog.getIconByText(question), null, null);
    }

    /**
     * Requests in MultiInputdialog.
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param title
     *            The Dialog's Window Title
     * @param message
     *            The Dialog is able to show a message to the user
     * @param defaultMessage
     * @param icon
     *            The dialog is able to display an Icon If this is null, the
     *            dialog might select an Icon derived from the message and title
     *            text
     * @param okOption
     *            Text for OK Button [null for default]
     * @param cancelOption
     *            Text for Cancel Button [null for default]
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    protected String showPasswordDialog(final int flag, final String title, final String message, final String defaultMessage, final ImageIcon icon, final String okOption, final String cancelOption) throws DialogClosedException, DialogCanceledException {
        if ((flag & Dialog.LOGIC_BYPASS) > 0) { return defaultMessage; }
        return this.showDialog(new PasswordDialog(flag, title, message, icon, okOption, cancelOption));
    }

    /**
     * 
     * @param message
     *            The Dialog is able to show a message to the user
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public String showPasswordDialog(final String message) throws DialogClosedException, DialogCanceledException {
        return this.showPasswordDialog(0, message, null);
    }

    /**
     * 
     * @param title
     *            The Dialog's Window Title
     * @param message
     *            The Dialog is able to show a message to the user
     * @param def
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public String showTextAreaDialog(final String title, final String message, final String def) throws DialogClosedException, DialogCanceledException {
        return this.showDialog(new TextAreaDialog(title, message, def));
    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog flag
     * @param question
     *            The Dialog is able to show a slider to the user. question
     * @param defaultvalue
     *            defaultvalue
     * @param min
     *            Min slider value
     * @param max
     *            Max slider value
     * @param step
     *            slider step
     * @param valueConverter
     *            TODO
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    public long showValueDialog(final int flag, final String question, final long defaultvalue, final long min, final long max, final long step, final ValueConverter valueConverter) throws DialogClosedException, DialogCanceledException {
        return this.showValueDialog(flag, _AWU.T.DIALOG_SLIDER_TITLE(), question, defaultvalue, Dialog.getIconByText(question), null, null, min, max, step, valueConverter);
    }

    /**
     * Requests in ValueDialog.
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param title
     *            The Dialog's Window Title
     * @param message
     *            The Dialog is able to show a message to the user
     * @param defaultMessage
     * @param icon
     *            The dialog is able to display an Icon If this is null, the
     *            dialog might select an Icon derived from the message and title
     *            text
     * @param okOption
     *            Text for OK Button [null for default]
     * @param cancelOption
     *            Text for Cancel Button [null for default]
     * @param min
     *            Min slider value
     * @param max
     *            Max slider value
     * @param step
     *            slider step
     * @param valueConverter
     * @return
     * @throws DialogCanceledException
     * @throws DialogClosedException
     */
    protected long showValueDialog(final int flag, final String title, final String message, final long defaultMessage, final ImageIcon icon, final String okOption, final String cancelOption, final long min, final long max, final long step, final ValueConverter valueConverter) throws DialogClosedException, DialogCanceledException {
        if ((flag & Dialog.LOGIC_BYPASS) > 0) { return defaultMessage; }
        return this.showDialog(new ValueDialog(flag, title, message, icon, okOption, cancelOption, defaultMessage, min, max, step, valueConverter));
    }

    /**
     * @param chatFrame
     */
    public void unregisterFrame(final Window win) {
        win.removeWindowFocusListener(this);
        this.parents.remove(win);

    }

    private File validateFileType(final File ret, final Integer fileSelectionMode, final boolean mkdir) {
        if (ret == null) { return null; }
        if (fileSelectionMode != null) {
            if (fileSelectionMode == JFileChooser.DIRECTORIES_ONLY) {
                if (ret.isFile()) {
                    /*
                     * is file, we need parent folder here
                     */
                    return ret.getParentFile();
                } else if (!ret.exists() && mkdir) {
                    /*
                     * folder but it does not exist yet, we create it
                     */
                    ret.mkdirs();
                    ret.mkdir();
                } else if (!ret.exists()) { return null; }
            } else if (fileSelectionMode == JFileChooser.FILES_ONLY) {
                if (ret.isDirectory()) {
                    /*
                     * we return null cause directory is not a file
                     */
                    return null;
                }
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.WindowFocusListener#windowGainedFocus(java.awt.event.
     * WindowEvent)
     */
    @Override
    public void windowGainedFocus(final WindowEvent e) {

        if (e.getSource() instanceof Window) {

            this.setParentOwner((Component) e.getSource());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.WindowFocusListener#windowLostFocus(java.awt.event.WindowEvent
     * )
     */
    @Override
    public void windowLostFocus(final WindowEvent e) {
        // TODO Auto-generated method stub

    }

    /**
     * @return 
     * 
     */
    public static Dialog I() {
      return INSTANCE;
        
    }

}
