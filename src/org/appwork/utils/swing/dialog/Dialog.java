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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.appwork.storage.JSonStorage;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.interfaces.ValueConverter;
import org.appwork.utils.locale.Loc;
import org.appwork.utils.locale.Tl8;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTHelper;

/**
 * A Dialog Instance which provides extended Dialog features and thus replaces
 * JOptionPane
 * 
 * @author $Author: unknown$
 */
public class Dialog {

    /**
     * Hide the cancel Button
     */
    public static final int BUTTONS_HIDE_CANCEL = 1 << 4;
    /**
     * Hide the OK button
     */
    public static final int BUTTONS_HIDE_OK = 1 << 3;

    /**
     * Icon Key for Error Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int)
     */
    public static final String ICON_ERROR = "error";
    /**
     * Icon Key for Information Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int)
     */
    public static final String ICON_INFO = "info";
    /**
     * Icon Key for Question Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int)
     */
    public static final String ICON_QUESTION = "help";
    /**
     * Icon Key for Warning Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int)
     */
    public static final String ICON_WARNING = "warning";
    /**
     * internal singleton instance to access the instance of this class
     */
    private static final Dialog INSTANCE = new Dialog();
    /**
     * LOGIC_BYPASS all dialogs. Try to fill automatically or return null
     */
    public static final int LOGIC_BYPASS = 1 << 1;
    /**
     * Use this flag to avoid display of the Timer
     */
    public static final int LOGIC_COUNTDOWN = 1 << 2;

    /**
     * Don't show again is only valid for this session, but is not saved for
     * further sessions
     */
    public static final int LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT = 1 << 11;
    /**
     * Often, the {@link #STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} option does not make
     * sense for the cancel option. Use this flag if the option should be
     * ignored if the user selects Cancel
     */
    public static final int LOGIC_DONT_SHOW_AGAIN_IGNORES_CANCEL = 1 << 9;
    /**
     * Often, the {@link #STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} option does not make
     * sense for the ok option. Use this flag if the option should be ignored if
     * the user selects OK
     */
    public static final int LOGIC_DONT_SHOW_AGAIN_IGNORES_OK = 1 << 10;
    /**
     * if the user pressed cancel, the return mask will contain this mask
     */
    public static final int RETURN_CANCEL = 1 << 2;
    /**
     * if user closed the window
     */
    public static final int RETURN_CLOSED = 1 << 6;
    /**
     * this return flag can be set in two situations:<br>
     * a) The user selected the {@link #STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} Option<br>
     * b) The dialog has been skipped because the DO NOT SHOW AGAIN flag has
     * been set previously<br>
     * <br>
     * Check {@link #RETURN_SKIPPED_BY_DONT_SHOW} to know of the dialog has been
     * visible or autoskipped
     * 
     */

    public static final int RETURN_DONT_SHOW_AGAIN = 1 << 3;
    /**
     * If the user pressed OK, the return mask will contain this flag
     */
    public static final int RETURN_OK = 1 << 1;

    /**
     * If the dialog has been skipped due to previously selected
     * {@link #STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} Option, this return flag is set.
     * 
     * @see #RETURN_DONT_SHOW_AGAIN
     */
    public static final int RETURN_SKIPPED_BY_DONT_SHOW = 1 << 4;
    /**
     * If the Timeout ({@link #LOGIC_COUNTDOWN}) has run out, the return mask
     * contains this flag
     */
    public static final int RETURN_TIMEOUT = 1 << 5;
    private static boolean ShellFolderIDWorkaround = false;

    /**
     * Do Not use an Icon. By default dialogs have an Icon
     */
    public static final int STYLE_HIDE_ICON = 1 << 8;
    /**
     * Some dialogs are able to render HTML. Use this switch to enable html
     */
    public static final int STYLE_HTML = 1 << 7;
    /**
     * Some dialogs are able to layout themselves in a large mode. E:g. to
     * display a huge text.
     */
    public static final int STYLE_LARGE = 1 << 6;
    /**
     * Displays a Checkbox with "do not show this again" text. If the user
     * selects this box, the UserInteraktion class will remember the answer and
     * will not disturb the user with the same question (same title)
     */
    public static final int STYLE_SHOW_DO_NOT_DISPLAY_AGAIN = 1 << 5;

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
                return ImageProvider.getImageIcon(Dialog.ICON_QUESTION, 32, 32);
            } else if (text.matches(Loc.getErrorRegex())) {
                return ImageProvider.getImageIcon(Dialog.ICON_ERROR, 32, 32);
            } else if (text.contains("!")) {
                return ImageProvider.getImageIcon(Dialog.ICON_WARNING, 32, 32);
            } else {
                return ImageProvider.getImageIcon(Dialog.ICON_INFO, 32, 32);
            }
        } catch (final IOException e) {
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

    public static boolean isClosed(final Object value) {
        if (!(value instanceof Integer)) { return false; }
        return BinaryLogic.containsSome((Integer) value, Dialog.RETURN_CLOSED);
    }

    public static boolean isOK(final Object value) {
        if (!(value instanceof Integer)) { return false; }
        return BinaryLogic.containsSome((Integer) value, Dialog.RETURN_OK);
    }

    /**
     * The max counter value for a timeout Dialog
     */
    private int coundownTime = 20;

    /**
     * the latest return value is stored in this variable for internal use
     * 
     * @see #isOK()
     */
    private Object latestReturnMask;

    /**
     * Parent window for all dialogs created with abstractdialog
     */
    private JFrame owner = new JFrame();

    /**
     * @return the {@link Dialog#coundownTime}
     * @see Dialog#coundownTime
     */
    protected int getCoundownTime() {
        return this.coundownTime;
    }

    /**
     * @return the {@link Dialog#owner}
     * @see Dialog#owner
     */
    public JFrame getParentOwner() {
        return this.owner;
    }

    /**
     * Returns true, if the latest answer contains the {@link #RETURN_OK} flag
     * WARNING: do not use this in single instance Dialog
     * 
     * @deprecated Use {@link Dialog#isOK(Object)} with returnmask instead.
     */
    @Deprecated
    public boolean isOK() {
        return Dialog.isOK(this.latestReturnMask);
    }

    /**
     * @param coundownTime
     *            the {@link Dialog#coundownTime} to set
     * @see Dialog#coundownTime
     */
    protected void setCoundownTime(final int coundownTime) {
        this.coundownTime = coundownTime;
    }

    /**
     * @param parent
     *            the {@link Dialog#owner} to set
     * @see Dialog#owner
     */
    public void setParentOwner(final JFrame parent) {
        this.owner = parent;
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
     * @param defaultSelection
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
     */
    public int showComboDialog(final int flag, final String title, final String question, final Object[] options, final int defaultSelection, final ImageIcon icon, final String okOption, final String cancelOption, final ListCellRenderer renderer) {
        if ((flag & Dialog.LOGIC_BYPASS) > 0) { return defaultSelection; }
        Integer ret = 0;
        try {
            return ret = new EDTHelper<Integer>() {
                @Override
                public Integer edtRun() {
                    final ComboBoxDialog dialog = new ComboBoxDialog(flag, title, question, options, defaultSelection, icon, okOption, cancelOption, renderer);
                    dialog.displayDialog();
                    return dialog.getReturnIndex();
                }
            }.getReturnValue();
        } finally {
            this.latestReturnMask = ret;
        }
    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param question
     *            The Dialog is able to show a question to the user.
     * @return
     */
    public int showConfirmDialog(final int flag, final String question) {
        return this.showConfirmDialog(flag, Tl8.DIALOG_CONFIRMDIALOG_TITLE.toString(), question, Dialog.getIconByText(question), null, null);
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
     */
    public int showConfirmDialog(final int flag, final String title, final String question) {
        return this.showConfirmDialog(flag, title, question, Dialog.getIconByText(title + question), null, null);
    }

    /**
     * Requests a ConfirmDialog
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * 
     * @param title
     *            The Dialog's Window Title
     * 
     * @param message
     *            The Dialog is able to show a message to the user
     * 
     * @param icon
     *            The dialog is able to display an Icon If this is null, the
     *            dialog might select an Icon derived from the message and title
     *            text
     * 
     * 
     * @param okOption
     *            Text for OK Button [null for default] Text for OK Button [null
     *            for default]
     * @param cancelOption
     *            Text for Cancel Button [null for default] Text for cancel
     *            Button [null for default]
     * @return
     */

    public int showConfirmDialog(final int flag, final String title, final String message, final ImageIcon tmpicon, final String okOption, final String cancelOption) {
        if ((flag & Dialog.LOGIC_BYPASS) > 0) { return 0; }
        final ImageIcon icon;
        if (tmpicon == null) {
            icon = Dialog.getIconByText(title + message);
        } else {
            icon = tmpicon;
        }
        Integer ret = 0;
        try {
            return ret = new EDTHelper<Integer>() {
                @Override
                public Integer edtRun() {
                    final ConfirmDialog dialog = new ConfirmDialog(flag, title, message, icon, okOption, cancelOption);
                    dialog.displayDialog();
                    return dialog.getReturnmask();
                }
            }.getReturnValue();
        } finally {
            this.latestReturnMask = ret;
        }

    }

    /**
     * note: showdialog must not call init itself!!
     * 
     * @param <T>
     * @param dialog
     * @param retType
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T showDialog(final AbstractDialog<T> dialog) {
        if (dialog == null) { return null; }
        return (T) (this.latestReturnMask = new EDTHelper<T>() {
            @Override
            public T edtRun() {
                dialog.displayDialog();
                return dialog.getReturnValue();
            }

        }.getReturnValue());
    }

    public int showErrorDialog(final String s) {
        try {
            return this.showConfirmDialog(Dialog.BUTTONS_HIDE_CANCEL | Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN, Tl8.DIALOG_ERROR_TITLE.s(), s, ImageProvider.getImageIcon(Dialog.ICON_ERROR, 32, 32), null, null);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

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
     *            TODO
     * @return an array of files or null if the user cancel the dialog
     */

    public File[] showFileChooser(final String id, final String title, final Integer fileSelectionMode, final FileFilter fileFilter, final boolean multiSelection, final Integer dialogType, final File preSelection) {

        return new EDTHelper<File[]>() {

            @Override
            public File[] edtRun() {
                for (int tried = 0; tried < 2; tried++) {
                    try {
                        if (Dialog.ShellFolderIDWorkaround) {
                            UIManager.put("FileChooser.useShellFolder", false);
                        } else {
                            UIManager.put("FileChooser.useShellFolder", true);
                        }
                        UIManager.put("FileChooser.homeFolderToolTipText", Tl8.DIALOG_FILECHOOSER_TOOLTIP_HOMEFOLDER.toString());
                        UIManager.put("FileChooser.newFolderToolTipText", Tl8.DIALOG_FILECHOOSER_TOOLTIP_NEWFOLDER.toString());
                        UIManager.put("FileChooser.upFolderToolTipText", Tl8.DIALOG_FILECHOOSER_TOOLTIP_UPFOLDER.toString());
                        UIManager.put("FileChooser.detailsViewButtonToolTipText", Tl8.DIALOG_FILECHOOSER_TOOLTIP_DETAILS.toString());
                        UIManager.put("FileChooser.listViewButtonToolTipText", Tl8.DIALOG_FILECHOOSER_TOOLTIP_LIST.toString());

                        final JFileChooser fc = new JFileChooser();
                        if (Dialog.ShellFolderIDWorkaround) {
                            fc.putClientProperty("FileChooser.useShellFolder", false);
                        } else {
                            fc.putClientProperty("FileChooser.useShellFolder", true);
                        }
                        fc.setAccessory(new FilePreview(fc));
                        if (title != null) {
                            fc.setDialogTitle(title);
                        }
                        if (fileSelectionMode != null) {
                            fc.setFileSelectionMode(fileSelectionMode);
                        }
                        if (fileFilter != null) {
                            fc.setFileFilter(fileFilter);
                        }

                        if (multiSelection && ((dialogType == null) || (dialogType != JFileChooser.SAVE_DIALOG))) {
                            fc.setMultiSelectionEnabled(true);
                        } else {
                            fc.setMultiSelectionEnabled(false);
                        }
                        if (dialogType != null) {
                            fc.setDialogType(dialogType);
                        }
                        if (preSelection != null) {
                            if (preSelection.isDirectory()) {
                                fc.setCurrentDirectory(preSelection);
                            } else {
                                fc.setCurrentDirectory(preSelection.getParentFile());
                                /* only preselect file in savedialog */
                                if ((dialogType != null) && (dialogType == JFileChooser.SAVE_DIALOG)) {
                                    if ((fileSelectionMode != null) && (fileSelectionMode != JFileChooser.DIRECTORIES_ONLY)) {
                                        fc.setSelectedFile(preSelection);
                                    }
                                }
                            }
                        } else {
                            final String latest = JSonStorage.getStorage("FILECHOOSER").get("LASTSELECTION_" + id, (String) null);
                            if (latest != null) {
                                File storeSelection = new File(latest);
                                while (storeSelection != null) {
                                    if (!storeSelection.exists()) {
                                        storeSelection = storeSelection.getParentFile();
                                    } else {
                                        if (storeSelection.isDirectory()) {
                                            fc.setCurrentDirectory(storeSelection);
                                        } else {
                                            fc.setCurrentDirectory(storeSelection.getParentFile());
                                            /*
                                             * only preselect file in savedialog
                                             */
                                            if ((dialogType != null) && (dialogType == JFileChooser.SAVE_DIALOG)) {
                                                if ((fileSelectionMode != null) && (fileSelectionMode != JFileChooser.DIRECTORIES_ONLY)) {
                                                    fc.setSelectedFile(preSelection);
                                                }
                                            }

                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        if ((dialogType == null) || (dialogType == JFileChooser.OPEN_DIALOG)) {
                            if (fc.showOpenDialog(Dialog.this.getParentOwner()) == JFileChooser.APPROVE_OPTION) {
                                if (multiSelection) {
                                    final ArrayList<File> rets = new ArrayList<File>();
                                    for (File ret : fc.getSelectedFiles()) {
                                        ret = Dialog.this.validateFileType(ret, fileSelectionMode, false);
                                        if (ret != null) {
                                            rets.add(ret);
                                        }
                                    }

                                    if (rets.size() > 0) {
                                        Dialog.this.latestReturnMask = rets.toArray(new File[rets.size()]);
                                        final File first = rets.get(0);
                                        if (first != null) {
                                            JSonStorage.getStorage("FILECHOOSER").put("LASTSELECTION_" + id, first.getAbsolutePath());
                                        }
                                        return (File[]) Dialog.this.latestReturnMask;
                                    } else {
                                        return null;
                                    }
                                }

                                File ret = fc.getSelectedFile();
                                /*
                                 * validate selectedFile against
                                 * fileSelectionMode
                                 */
                                ret = Dialog.this.validateFileType(ret, fileSelectionMode, false);
                                if (ret != null) {
                                    Dialog.this.latestReturnMask = ret;
                                    JSonStorage.getStorage("FILECHOOSER").put("LASTSELECTION_" + id, ret.getAbsolutePath());
                                    return new File[] { ret };
                                } else {
                                    return null;
                                }
                            }
                        } else if (dialogType == JFileChooser.SAVE_DIALOG) {
                            if (fc.showSaveDialog(Dialog.this.getParentOwner()) == JFileChooser.APPROVE_OPTION) {
                                File ret = fc.getSelectedFile();
                                /*
                                 * validate selectedFile against
                                 * fileSelectionMode
                                 */
                                ret = Dialog.this.validateFileType(ret, fileSelectionMode, true);
                                if (ret != null) {
                                    Dialog.this.latestReturnMask = ret;
                                    JSonStorage.getStorage("FILECHOOSER").put("LASTSELECTION_" + id, ret.getAbsolutePath());
                                    return new File[] { ret };
                                } else {
                                    return null;
                                }
                            }
                        }
                        return null;
                    } catch (final Exception e) {
                        if ((e != null) && (e.getMessage() != null) && e.getMessage().contains("shell") && !Dialog.ShellFolderIDWorkaround) {
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
     */
    public String showInputDialog(final int flag, final String question, final String defaultvalue) {
        return this.showInputDialog(flag, Tl8.DIALOG_INPUT_TITLE.toString(), question, defaultvalue, Dialog.getIconByText(question), null, null);
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
     */
    public String showInputDialog(final int flag, final String title, final String message, final String defaultMessage, final ImageIcon icon, final String okOption, final String cancelOption) {
        if ((flag & Dialog.LOGIC_BYPASS) > 0) { return defaultMessage; }
        return (String) (this.latestReturnMask = new EDTHelper<String>() {
            @Override
            public String edtRun() {
                final InputDialog dialog = new InputDialog(flag, title, message, defaultMessage, icon, okOption, cancelOption);
                dialog.displayDialog();
                return dialog.getReturnValue();
            }

        }.getReturnValue());
    }

    /**
     * 
     * @param message
     *            The Dialog is able to show a message to the user
     * @return
     */
    public String showInputDialog(final String message) {
        return this.showInputDialog(0, message, null);
    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param message
     *            The Dialog is able to show a message to the user
     */
    public void showMessageDialog(final int flag, final String message) {
        this.showMessageDialog(flag, Tl8.DIALOG_MESSAGE_TITLE.toString(), message);
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
     */
    public void showMessageDialog(final int flag, final String title, final String message) {
        this.showConfirmDialog(Dialog.BUTTONS_HIDE_CANCEL | flag, title, message, Dialog.getIconByText(title + message), null, null);
    }

    /**
     * 
     * @param message
     *            The Dialog is able to show a message to the user
     */
    public void showMessageDialog(final String message) {
        this.showMessageDialog(0, Tl8.DIALOG_MESSAGE_TITLE.toString(), message);
    }

    /**
     * 
     * @param title
     *            The Dialog's Window Title
     * @param message
     *            The Dialog is able to show a message to the user
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
     */
    public String showPasswordDialog(final int flag, final String question, final String defaultvalue) {
        return this.showPasswordDialog(flag, Tl8.DIALOG_PASSWORD_TITLE.toString(), question, defaultvalue, Dialog.getIconByText(question), null, null);
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
     */
    protected String showPasswordDialog(final int flag, final String title, final String message, final String defaultMessage, final ImageIcon icon, final String okOption, final String cancelOption) {
        if ((flag & Dialog.LOGIC_BYPASS) > 0) { return defaultMessage; }
        return (String) (this.latestReturnMask = new EDTHelper<String>() {
            @Override
            public String edtRun() {
                final PasswordDialog dialog = new PasswordDialog(flag, title, message, icon, okOption, cancelOption);
                dialog.displayDialog();
                return dialog.getReturnValue();
            }

        }.getReturnValue());
    }

    /**
     * 
     * @param message
     *            The Dialog is able to show a message to the user
     * @return
     */
    public String showPasswordDialog(final String message) {
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
     */
    protected String showTextAreaDialog(final String title, final String message, final String def) {
        return (String) (this.latestReturnMask = new EDTHelper<String>() {
            @Override
            public String edtRun() {
                try {
                    final TextAreaDialog dialog = new TextAreaDialog(title, message, def);
                    dialog.displayDialog();
                    if (BinaryLogic.containsAll(dialog.getReturnmask(), Dialog.RETURN_OK)) { return dialog.getResult(); }
                } catch (final IOException e) {
                    Log.exception(e);
                }
                return null;
            }

        }.getReturnValue());
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
     */
    public long showValueDialog(final int flag, final String question, final long defaultvalue, final long min, final long max, final long step, final ValueConverter valueConverter) {
        return this.showValueDialog(flag, Tl8.DIALOG_SLIDER_TITLE.toString(), question, defaultvalue, Dialog.getIconByText(question), null, null, min, max, step, valueConverter);
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
     */
    protected long showValueDialog(final int flag, final String title, final String message, final long defaultMessage, final ImageIcon icon, final String okOption, final String cancelOption, final long min, final long max, final long step, final ValueConverter valueConverter) {
        if ((flag & Dialog.LOGIC_BYPASS) > 0) { return defaultMessage; }

        return (Long) (this.latestReturnMask = new EDTHelper<Long>() {
            @Override
            public Long edtRun() {
                final ValueDialog dialog = new ValueDialog(flag, title, message, icon, okOption, cancelOption, defaultMessage, min, max, step, valueConverter);
                dialog.displayDialog();
                return dialog.getReturnValue();
            }
        }.getReturnValue());
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

}
