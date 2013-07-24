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

import java.awt.Image;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import org.appwork.resources.AWUTheme;
import org.appwork.uio.UIOManager;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.interfaces.ValueConverter;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.EDTRunner;

/**
 * A Dialog Instance which provides extended Dialog features and thus replaces
 * JOptionPane
 */
public class Dialog {
    /**
     * 
     */
    public static final String  LASTSELECTION                       = "LASTSELECTION_";

    /**
     * 
     */
    public static final String  FILECHOOSER                         = "FILECHOOSER";

    /**
     * Icon Key for Error Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int, boolean)
     */
    public static final String  ICON_ERROR                          = "dialog/error";
    /**
     * Icon Key for Information Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int, boolean)
     */
    public static final String  ICON_INFO                           = "dialog/info";
    /**
     * Icon Key for Question Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int, boolean)
     */
    public static final String  ICON_QUESTION                       = "dialog/help";
    /**
     * Icon Key for Warning Icons
     * 
     * @see org.appwork.utils.ImageProvider.ImageProvider#getImageIcon(String,
     *      int, int, boolean)
     */
    public static final String  ICON_WARNING                        = "dialog/warning";
    /**
     * internal singleton instance to access the instance of this class
     */
    private static final Dialog INSTANCE                            = new Dialog();

    /**
     * 
     * @deprecated Use org.appwork.uio.UIOManager Constants instead
     */
    @Deprecated()
    public static final int     LOGIC_DONOTSHOW_BASED_ON_TITLE_ONLY = 1 << 12;

    /**
     * if the user pressed cancel, the return mask will contain this mask
     */
    public static final int     RETURN_CANCEL                       = 1 << 2;
    /**
     * if user closed the window
     */
    public static final int     RETURN_CLOSED                       = 1 << 6;
    public static final int     RETURN_INTERRUPT                    = 1 << 8;
    /**
     * this return flag can be set in two situations:<br>
     * a) The user selected the {@link #STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} Option<br>
     * b) The dialog has been skipped because the DO NOT SHOW AGAIN flag has
     * been set previously<br>
     * <br>
     * Check {@link #RETURN_SKIPPED_BY_DONT_SHOW} to know if the dialog has been
     * visible or autoskipped
     */
    public static final int     RETURN_DONT_SHOW_AGAIN              = 1 << 3;

    /**
     * If the user pressed OK, the return mask will contain this flag
     */
    public static final int     RETURN_OK                           = 1 << 1;
    /**
     * If the dialog has been skipped due to previously selected
     * {@link #STYLE_SHOW_DO_NOT_DISPLAY_AGAIN} Option, this return flag is set.
     * 
     * @see #RETURN_DONT_SHOW_AGAIN
     */
    public static final int     RETURN_SKIPPED_BY_DONT_SHOW         = 1 << 4;
    /**
     * If the Timeout ({@link UIOManager#LOGIC_COUNTDOWN}) has run out, the
     * return mask contains this flag
     */
    public static final int     RETURN_TIMEOUT                      = 1 << 5;

    /**
     * If the dialog has been skiped/closed by ESC key
     */
    public static final int     RETURN_ESC                          = 1 << 7;

    /**
     * @return
     * 
     */
    public static Dialog I() {
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
            if (text != null && text.contains("?")) {
                return AWUTheme.I().getIcon(Dialog.ICON_QUESTION, 32);
            } else if (text != null && (text.contains("error") || text.contains("exception"))) {
                return AWUTheme.I().getIcon(Dialog.ICON_ERROR, 32);
            } else if (text != null && text.contains("!")) {
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

    public static void main(final String[] args) throws InterruptedException {

        final Thread th = new Thread() {
            @Override
            public void run() {
                try {
                    Dialog.getInstance().showConfirmDialog(0, "Blabla?");

                    System.out.println("RETURNED OK");
                } catch (final DialogClosedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (final DialogCanceledException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        th.start();
        Thread.sleep(5000);
        th.interrupt();

    }

    /**
     * The max counter value for a timeout Dialog
     */
    private int                 defaultTimeout = 20000;

    /**
     * Parent window for all dialogs created with abstractdialog
     */

    private LAFManagerInterface lafManager;

    private DialogHandler       handler        = null;

    private DialogHandler       defaultHandler;

    private Dialog() {

        this.defaultHandler = new DialogHandler() {

            @Override
            public <T> T showDialog(final AbstractDialog<T> dialog) throws DialogClosedException, DialogCanceledException {
                return Dialog.this.showDialogRaw(dialog);
            }
        };
    }

    public DialogHandler getDefaultHandler() {
        return this.defaultHandler;
    }

    /**
     * @return the {@link Dialog#defaultTimeout}
     * @see Dialog#defaultTimeout
     */
    protected int getDefaultTimeout() {
        return this.defaultTimeout;
    }

    public DialogHandler getHandler() {
        return this.handler;
    }

    /**
     * @return
     */
    public List<? extends Image> getIconList() {
        return this.iconList;
    }

    public LAFManagerInterface getLafManager() {
        synchronized (this) {
            return this.lafManager;
        }
    }

    /**
     * 
     */
    public void initLaf() {
        synchronized (this) {
            if (this.lafManager != null) {
                this.lafManager.init();
                this.setLafManager(null);
            }
        }
    }

    /**
     * @param countdownTime
     *            the {@link Dialog#defaultTimeout} to set
     * @see Dialog#defaultTimeout
     */
    public void setDefaultTimeout(final int countdownTime) {
        this.defaultTimeout = countdownTime;
    }

    public void setHandler(DialogHandler handler) {
        if (handler == null) {
            handler = this.defaultHandler;
        }
        this.handler = handler;
    }

    @Deprecated
    public void setIconList(final List<? extends Image> iconList) {
        this.iconList = iconList;
    }

    public void setLafManager(final LAFManagerInterface lafManager) {
        synchronized (this) {
            this.lafManager = lafManager;
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
        final DialogHandler lhandler = this.handler;
        if (lhandler != null) { return lhandler.showDialog(dialog); }
        return this.showDialogRaw(dialog);
    }

    /**
     * @param dialog
     * @throws DialogClosedException
     * @throws DialogCanceledException
     */
    protected <T> T showDialogRaw(final AbstractDialog<T> dialog) throws DialogClosedException, DialogCanceledException {
        if (dialog == null) { return null; }

        if (SwingUtilities.isEventDispatchThread()) {
            return this.showDialogRawInEDT(dialog);
        } else {
            return this.showDialogRawOutsideEDT(dialog);
        }

    }

    protected <T> T showDialogRawInEDT(final AbstractDialog<T> dialog) throws DialogClosedException, DialogCanceledException {
        dialog.setCallerIsEDT(true);
        dialog.displayDialog();
        final T ret = dialog.getReturnValue();
        final int mask = dialog.getReturnmask();
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CLOSED)) { throw new DialogClosedException(mask); }
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CANCEL)) { throw new DialogCanceledException(mask); }
        return ret;

    }

    protected <T> T showDialogRawOutsideEDT(final AbstractDialog<T> dialog) throws DialogClosedException, DialogCanceledException {
        dialog.setCallerIsEDT(false);
        final AtomicBoolean waitingLock = new AtomicBoolean(false);
        final EDTRunner edth = new EDTRunner() {
            @Override
            protected void runInEDT() {
                dialog.setDisposedCallback(new DisposeCallBack() {

                    @Override
                    public void dialogDisposed(final AbstractDialog<?> dialog) {
                        synchronized (waitingLock) {
                            waitingLock.set(true);
                            waitingLock.notifyAll();
                        }
                    }
                });
                dialog.displayDialog();
            }

        };
        boolean interrupted = false;
        try {
            synchronized (waitingLock) {
                if (waitingLock.get() == false) {
                    waitingLock.wait();
                }
            }
        } catch (final InterruptedException e) {
            interrupted = true;
        }
        if (edth.isInterrupted() || interrupted) {

            // Use a edtrunner here. AbstractCaptcha.dispose is edt save...
            // however there may be several CaptchaDialog classes with
            // overriddden unsave dispose methods...

            new EDTRunner() {

                @Override
                protected void runInEDT() {
                    try {
                        // close dialog if open
                        dialog.interrupt();
                    } catch (final Exception e) {
                    }
                }
            };

            throw new DialogClosedException(dialog.getReturnmask(), edth.getInterruptException());
        }
        final T ret = dialog.getReturnValue();
        final int mask = dialog.getReturnmask();
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CLOSED)) { throw new DialogClosedException(mask); }
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CANCEL)) { throw new DialogCanceledException(mask); }
        return ret;

    }

    /**
     * @param i
     * @param dialog_error_title
     * @param dialog_error_noconnection
     * @return
     */
    public int showErrorDialog(final int flags, final String title, final String message) {
        try {
            return this.showConfirmDialog(flags, title, message, AWUTheme.I().getIcon(Dialog.ICON_ERROR, 32), null, null);
        } catch (final DialogClosedException e) {
            return Dialog.RETURN_CLOSED;
        } catch (final DialogCanceledException e) {
            return Dialog.RETURN_CANCEL;
        }
    }

    public int showErrorDialog(final String s) {

        try {
            return this.showConfirmDialog(UIOManager.BUTTONS_HIDE_CANCEL | Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN, _AWU.T.DIALOG_ERROR_TITLE(), s, AWUTheme.I().getIcon(Dialog.ICON_ERROR, 32), null, null);
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
            final ExceptionDialog dialog = new ExceptionDialog(UIOManager.LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT | UIOManager.BUTTONS_HIDE_CANCEL, title, message, e, null, null);
            this.showDialog(dialog);
        } catch (final DialogClosedException e1) {
            return Dialog.RETURN_CLOSED;
        } catch (final DialogCanceledException e1) {
            return Dialog.RETURN_CANCEL;
        }

        return 0;
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
            this.showConfirmDialog(UIOManager.BUTTONS_HIDE_CANCEL | flag, title, message, Dialog.getIconByText(title + message), null, null);
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

        return this.showDialog(new ValueDialog(flag, title, message, icon, okOption, cancelOption, defaultMessage, min, max, step, valueConverter));
    }

}
