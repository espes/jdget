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

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ListCellRenderer;
import javax.swing.filechooser.FileFilter;

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.interfaces.ValueConverter;
import org.appwork.utils.locale.Loc;
import org.appwork.utils.logging.Log;
import org.appwork.utils.storage.DatabaseInterface;
import org.appwork.utils.swing.EDTHelper;

/**
 * A Dialog Instance which provides extended Dialog features and thus replaces
 * JOptionPane
 * 
 * @author $Author: unknown$
 * 
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
    private DatabaseInterface database;
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
     *LOGIC_BYPASS all dialogs. Try to fill automatically or return null
     */
    public static final int LOGIC_BYPASS = 1 << 1;
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
     * Use this flag to avoid display of the Timer
     */
    public static final int LOGIC_COUNTDOWN = 1 << 2;
    /**
     * Parent window for all dialogs created with abstractdialog
     */
    private JFrame owner = new JFrame();
    /**
     * if the user pressed cancel, the return mask will contain this mask
     */
    public static final int RETURN_CANCEL = 1 << 2;
    /**
     * If the Timeout ({@link #LOGIC_COUNTDOWN}) has run out, the return mask
     * contains this flag
     */
    public static final int RETURN_TIMEOUT = 1 << 5;
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
     * @return the {@link AbstractDialog#dATABASE}
     * @see AbstractDialog#dATABASE
     */
    public DatabaseInterface getDatabase() {
        return database;
    }

    /**
     * Return the singleton instance of Dialog
     * 
     * @return
     */
    public static Dialog getInstance() {

        return INSTANCE;
    }

    /**
     * @return the {@link AbstractDialog#pARENT}
     * @see AbstractDialog#pARENT
     */
    public JFrame getParentOwner() {
        return owner;
    }

    /**
     * @param pARENT
     *            the {@link AbstractDialog#pARENT} to set
     * @see AbstractDialog#pARENT
     */
    public void setParentOwner(JFrame parent) {
        owner = parent;
    }

    /**
     * @param dATABASE
     *            the {@link AbstractDialog#dATABASE} to set
     * @see AbstractDialog#dATABASE
     */
    public void setDatabase(DatabaseInterface db) {
        database = db;
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
     * @return the {@link Dialog#coundownTime}
     * @see Dialog#coundownTime
     */
    protected int getCoundownTime() {
        return coundownTime;
    }

    /**
     * tries to find some special markers in the text and selects an appropriate
     * icon
     * 
     * @param text
     * @return
     */
    private ImageIcon getIconByText(String text) {
        try {
            if (text.contains("?")) {

                return ImageProvider.getImageIcon(ICON_QUESTION, 32, 32);
            } else if (text.matches(Loc.getErrorRegex())) {
                return ImageProvider.getImageIcon(ICON_ERROR, 32, 32);
            } else if (text.contains("!")) {
                return ImageProvider.getImageIcon(ICON_WARNING, 32, 32);
            } else {
                return ImageProvider.getImageIcon(ICON_INFO, 32, 32);
            }
        } catch (IOException e) {
            Log.exception(e);
            return null;
        }
    }

    /**
     * Returns true, if the latest answer contains the {@link #RETURN_OK} flag
     * WARNING: do not use this in single instance Dialog
     * 
     * @param returnMask
     * @return
     */
    @Deprecated
    public boolean isOK() {
        if (!(latestReturnMask instanceof Integer)) return false;
        return BinaryLogic.containsSome((Integer) latestReturnMask, Dialog.RETURN_OK);
    }

    public static boolean isOK(Object value) {
        if (!(value instanceof Integer)) return false;
        return BinaryLogic.containsSome((Integer) value, Dialog.RETURN_OK);
    }

    /**
     * @param coundownTime
     *            the {@link Dialog#coundownTime} to set
     * @see Dialog#coundownTime
     */
    protected void setCoundownTime(int coundownTime) {
        this.coundownTime = coundownTime;
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
        if ((flag & LOGIC_BYPASS) > 0) return defaultSelection;
        Integer ret = 0;

        try {
            return ret = new EDTHelper<Integer>() {

                @Override
                public Integer edtRun() {
                    return new ComboBoxDialog(flag, title, question, options, defaultSelection, icon, okOption, cancelOption, renderer).getReturnIndex();
                }

            }.getReturnValue();
        } finally {
            latestReturnMask = ret;
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
    public int showConfirmDialog(int flag, String question) {
        return showConfirmDialog(flag, Loc.L("org.appwork.utils.swing.dialog.Dialog.showConfirmDialog(int, String)", "Please confirm!"), question, this.getIconByText(question), null, null);
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
    public int showConfirmDialog(int flag, String title, String question) {
        return showConfirmDialog(flag, title, question, this.getIconByText(title + question), null, null);
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

    protected int showConfirmDialog(final int flag, final String title, final String message, final ImageIcon tmpicon, final String okOption, final String cancelOption) {
        synchronized (this) {
            final ImageIcon icon;
            if ((flag & LOGIC_BYPASS) > 0) return 0;
            if (tmpicon == null) {
                icon = getIconByText(title + message);
            } else {
                icon = tmpicon;
            }
            Integer ret = 0;
            try {

                return ret = new EDTHelper<Integer>() {

                    @Override
                    public Integer edtRun() {
                        return new ConfirmDialog(flag, title, message, icon, okOption, cancelOption).getReturnmask();
                    }

                }.getReturnValue();
            } finally {
                latestReturnMask = ret;
            }
        }
    }

    /**
     * 
     * @param title
     *            The Dialog's Window Title
     * @param message
     *            The Dialog is able to show a message to the user
     */
    public void showMessageDialog(String title, String message) {
        showMessageDialog(0, title, message);
    }

    /**
     * Requests a FileChooserDialog.
     * 
     * @param id
     *            ID of the dialog (used to save and restore the old directory)
     * @param title
     *            The Dialog's Window Title dialog-title or null for default
     * @param fileSelectionMode
     *            mode for selecting files (like JDFileChooser.FILES_ONLY) or
     *            null for default
     * @param fileFilter
     *            filters the choosable files or null for default
     * @param multiSelection
     *            Multiple files choosable? or null for default
     * @return an array of files or null if the user cancel the dialog
     */

    public File[] showFileChooser(final String id, final String title, final Integer fileSelectionMode, final FileFilter fileFilter, final Boolean multiSelection, final Integer dialogType) {
        synchronized (this) {

            return new EDTHelper<File[]>() {

                @Override
                public File[] edtRun() {
                    try {

                        JFileChooser fc = new JFileChooser(id);
                        if (title != null) fc.setDialogTitle(title);
                        if (fileSelectionMode != null) fc.setFileSelectionMode(fileSelectionMode);
                        if (fileFilter != null) fc.setFileFilter(fileFilter);
                        if (multiSelection != null) fc.setMultiSelectionEnabled(multiSelection);
                        if (dialogType != null) fc.setDialogType(dialogType);
                        if (dialogType == null || dialogType == JFileChooser.OPEN_DIALOG) {
                            if (fc.showOpenDialog(getParentOwner()) == JFileChooser.APPROVE_OPTION) {
                                if (multiSelection) return (File[]) (latestReturnMask = fc.getSelectedFiles());
                                final File[] ret = new File[1];
                                ret[0] = fc.getSelectedFile();
                                return ret;
                            }
                        } else if (dialogType == JFileChooser.SAVE_DIALOG) {
                            if (fc.showSaveDialog(getParentOwner()) == JFileChooser.APPROVE_OPTION) {
                                if (multiSelection) return (File[]) (latestReturnMask = fc.getSelectedFiles());
                                final File[] ret = new File[1];
                                ret[0] = fc.getSelectedFile();
                                return ret;
                            }
                        }
                        return null;
                    } catch (Exception e) {
                        Log.exception(e);
                        return null;
                    }
                }

            }.getReturnValue();

        }
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
    public String showInputDialog(int flag, String question, String defaultvalue) {
        return showInputDialog(flag, Loc.L("org.appwork.utils.swing.dialog.Dialog.showInputDialog(int, String, String)", "Please enter!"), question, defaultvalue, this.getIconByText(question), null, null);
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
        synchronized (this) {
            if ((flag & LOGIC_BYPASS) > 0) return defaultMessage;

            return (String) (latestReturnMask = new EDTHelper<String>() {

                @Override
                public String edtRun() {
                    return new InputDialog(flag, title, message, defaultMessage, icon, okOption, cancelOption).getReturnID();
                }

            }.getReturnValue());

        }
    }

    /**
     * 
     * @param message
     *            The Dialog is able to show a message to the user
     * @return
     */
    public String showInputDialog(String message) {
        return showInputDialog(0, message, null);
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
    public String showPasswordDialog(int flag, String question, String defaultvalue) {
        return showPasswordDialog(flag, Loc.L("org.appwork.utils.swing.dialog.Dialog.showPasswordDialog(int, String, String)", "Please enter!"), question, defaultvalue, this.getIconByText(question), null, null);
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
        synchronized (this) {
            if ((flag & LOGIC_BYPASS) > 0) return defaultMessage;

            return (String) (latestReturnMask = new EDTHelper<String>() {

                @Override
                public String edtRun() {
                    return new PasswordDialog(flag, title, message, icon, okOption, cancelOption).getReturnID();
                }

            }.getReturnValue());

        }
    }

    /**
     * 
     * @param message
     *            The Dialog is able to show a message to the user
     * @return
     */
    public String showPasswordDialog(String message) {
        return showPasswordDialog(0, message, null);
    }

    /**
     * 
     * @param flag
     *            see {@link Dialog} - Flags. There are various flags to
     *            customize the dialog
     * @param message
     *            The Dialog is able to show a message to the user
     */
    public void showMessageDialog(int flag, String message) {
        showMessageDialog(flag, Loc.L("org.appwork.utils.swing.dialog.Dialog.showMessageDialog(int, String).title", "Message"), message);
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
    public void showMessageDialog(int flag, String title, String message) {
        synchronized (this) {
            try {
                showConfirmDialog(Dialog.BUTTONS_HIDE_CANCEL | flag, title, message, ImageProvider.getImageIcon(Dialog.ICON_INFO, 32, 32), null, null);
            } catch (IOException e) {
                Log.exception(e);
            }
        }
    }

    /**
     * 
     * @param message
     *            The Dialog is able to show a message to the user
     */
    public void showMessageDialog(String message) {
        showMessageDialog(0, Loc.L("org.appwork.utils.swing.dialog.Dialog.showMessageDialog(String).title", "Message"), message);
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
        synchronized (this) {
            return (String) (latestReturnMask = new EDTHelper<String>() {

                @Override
                public String edtRun() {
                    TextAreaDialog dialog;
                    try {
                        dialog = new TextAreaDialog(title, message, def);

                        if (BinaryLogic.containsAll(dialog.getReturnmask(), RETURN_OK)) return dialog.getResult();
                    } catch (IOException e) {
                        Log.exception(e);
                    }
                    return null;
                }

            }.getReturnValue());
        }
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
    public long showValueDialog(int flag, String question, long defaultvalue, long min, long max, long step, ValueConverter valueConverter) {
        return showValueDialog(flag, Loc.L("org.appwork.utils.swing.dialog.Dialog.showValueDialog.title", "Please enter!"), question, defaultvalue, this.getIconByText(question), null, null, min, max, step, valueConverter);
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
        synchronized (this) {
            if ((flag & LOGIC_BYPASS) > 0) return defaultMessage;

            return (Long) (latestReturnMask = new EDTHelper<Long>() {

                @Override
                public Long edtRun() {
                    return new ValueDialog(flag, title, message, icon, okOption, cancelOption, defaultMessage, min, max, step, valueConverter).getReturnValue();
                }

            }.getReturnValue());

        }
    }

}
