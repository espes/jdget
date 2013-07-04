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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.appwork.exceptions.WTFException;
import org.appwork.resources.AWUTheme;
import org.appwork.storage.JSonStorage;
import org.appwork.swing.MigPanel;
import org.appwork.uio.UIOManager;
import org.appwork.uio.UserIODefinition;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.images.IconIO;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
import org.appwork.utils.net.Base64OutputStream;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.dialog.dimensor.DialogDimensor;
import org.appwork.utils.swing.dialog.locator.CenterOfScreenDialogLocator;
import org.appwork.utils.swing.dialog.locator.DialogLocator;

public abstract class AbstractDialog<T> extends TimerDialog implements ActionListener, WindowListener, OKCancelCloseUserIODefinition {

    private static final HashMap<String, Integer> SESSION_DONTSHOW_AGAIN  = new HashMap<String, Integer>();

    public static final DialogLocator             LOCATE_CENTER_OF_SCREEN = new CenterOfScreenDialogLocator();
    public static DialogLocator                   DEFAULT_LOCATOR         = null;

    private static int                            BUTTON_HEIGHT           = -1;

    public static int getButtonHeight() {
        return AbstractDialog.BUTTON_HEIGHT;
    }

    public static DialogLocator getDefaultLocator() {
        return AbstractDialog.DEFAULT_LOCATOR;
    }

    public static Integer getSessionDontShowAgainValue(final String key) {
        final Integer ret = AbstractDialog.SESSION_DONTSHOW_AGAIN.get(key);
        if (ret == null) { return -1; }

        return ret;
    }

    public static void resetDialogInformations() {
        try {
            AbstractDialog.SESSION_DONTSHOW_AGAIN.clear();
            JSonStorage.getPlainStorage("Dialogs").clear();
        } catch (final Exception e) {
            Log.exception(e);
        }
    }

    /**
     * @param i
     */
    public static void setButtonHeight(final int height) {
        AbstractDialog.BUTTON_HEIGHT = height;

    }

    public static void setDefaultLocator(final DialogLocator dEFAULT_LOCATOR) {
        AbstractDialog.DEFAULT_LOCATOR = dEFAULT_LOCATOR;
    }

    protected JButton          cancelButton;

    private final String       cancelOption;

    private DefaultButtonPanel defaultButtons;

    protected JCheckBox        dontshowagain;
    protected int              flagMask;

    private ImageIcon          icon;

    private boolean            initialized            = false;

    protected JButton          okButton;

    private final String       okOption;

    protected JComponent       panel;

    protected int              returnBitMask          = 0;

    protected AbstractAction[] actions                = null;

    private String             title;

    private JLabel             iconLabel;

    protected boolean          doNotShowAgainSelected = false;

    private FocusListener      defaultButtonFocusListener;

    private DialogLocator      locator;

    private DialogDimensor     dimensor;

    private boolean            dummyInit              = false;

    private Point              orgLocationOnScreen;

    public AbstractDialog(final int flag, final String title, final ImageIcon icon, final String okOption, final String cancelOption) {
        super();

        this.title = title;
        this.flagMask = flag;

        this.icon = BinaryLogic.containsAll(flag, Dialog.STYLE_HIDE_ICON) ? null : icon;
        this.okOption = okOption == null ? _AWU.T.ABSTRACTDIALOG_BUTTON_OK() : okOption;
        this.cancelOption = cancelOption == null ? _AWU.T.ABSTRACTDIALOG_BUTTON_CANCEL() : cancelOption;
    }

    /**
     * this function will init and show the dialog
     */
    protected void _init() {

        layoutDialog();

        if (BinaryLogic.containsAll(this.flagMask, UIOManager.LOGIC_COUNTDOWN)) {
            timerLbl.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(final MouseEvent e) {
                    AbstractDialog.this.cancel();
                    AbstractDialog.this.timerLbl.removeMouseListener(this);
                }

            });
            timerLbl.setToolTipText(_AWU.T.TIMERDIALOG_TOOLTIP_TIMERLABEL());

            timerLbl.setIcon(AWUTheme.I().getIcon("dialog/cancel", 16));
        }
        /**
         * this is very important so the new shown dialog will become root for
         * all following dialogs! we save old parentWindow, then set current
         * dialogwindow as new root and show dialog. after dialog has been
         * shown, we restore old parentWindow
         */
        final Component parentOwner = Dialog.getInstance().getParentOwner();
        Dialog.getInstance().setParentOwner(getDialog());
        try {

            this.setTitle(this.title);

            if (this.evaluateDontShowAgainFlag()) { return; }
            if (parentOwner == null || !parentOwner.isShowing()) {
                getDialog().setAlwaysOnTop(true);
            }
            // The Dialog Modal
            getDialog().setModal(true);
            // Layout manager

            // Dispose dialog on close
            getDialog().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            getDialog().addWindowListener(this);
            this.defaultButtonFocusListener = new FocusListener() {

                @Override
                public void focusGained(final FocusEvent e) {

                    final JRootPane root = SwingUtilities.getRootPane(e.getComponent());
                    if (root != null && e.getComponent() instanceof JButton) {
                        root.setDefaultButton((JButton) e.getComponent());
                    }
                }

                @Override
                public void focusLost(final FocusEvent e) {

                    final JRootPane root = SwingUtilities.getRootPane(e.getComponent());
                    if (root != null) {
                        root.setDefaultButton(null);
                    }
                }
            };
            // create panel for the dialog's buttons
            this.okButton = new JButton(this.okOption);
            this.cancelButton = new JButton(this.cancelOption);
            this.cancelButton.addFocusListener(this.defaultButtonFocusListener);
            this.okButton.addFocusListener(this.defaultButtonFocusListener);
            this.defaultButtons = this.getDefaultButtonPanel();

            /*
             * We set the focus on the ok button. if no ok button is shown, we
             * set the focus on cancel button
             */
            JButton focus = null;

            // add listeners here
            this.okButton.addActionListener(this);
            this.cancelButton.addActionListener(this);

            // add icon if available
            if (this.icon != null) {
                getDialog().setLayout(new MigLayout("ins 5,wrap 2", "[][grow,fill]", "[grow,fill][]"));
                getDialog().add(this.getIconComponent(), this.getIconConstraints());
            } else {
                getDialog().setLayout(new MigLayout("ins 5,wrap 1", "[grow,fill]", "[grow,fill][]"));
            }
            // Layout the dialog content and add it to the contentpane
            this.panel = this.layoutDialogContent();

            getDialog().add(this.panel, "");

            // add the countdown timer
            final MigPanel bottom = this.createBottomPanel();
            bottom.setOpaque(false);

            bottom.add(timerLbl);
            if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {

                this.dontshowagain = new JCheckBox(this.getDontShowAgainLabelText());
                this.dontshowagain.setHorizontalAlignment(SwingConstants.TRAILING);
                this.dontshowagain.setHorizontalTextPosition(SwingConstants.LEADING);
                this.dontshowagain.setSelected(this.doNotShowAgainSelected);

                bottom.add(this.dontshowagain, "alignx right");
            } else {
                bottom.add(Box.createHorizontalGlue());
            }
            bottom.add(this.defaultButtons);

            if ((this.flagMask & UIOManager.BUTTONS_HIDE_OK) == 0) {

                // Set OK as defaultbutton
                getDialog().getRootPane().setDefaultButton(this.okButton);
                this.okButton.addHierarchyListener(new HierarchyListener() {
                    public void hierarchyChanged(final HierarchyEvent e) {
                        if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
                            final JButton defaultButton = (JButton) e.getComponent();
                            final JRootPane root = SwingUtilities.getRootPane(defaultButton);
                            if (root != null) {
                                root.setDefaultButton(defaultButton);

                            }
                        }
                    }
                });

                focus = this.okButton;
                this.defaultButtons.addOKButton(this.okButton);

            }
            if (!BinaryLogic.containsAll(this.flagMask, UIOManager.BUTTONS_HIDE_CANCEL)) {

                this.defaultButtons.addCancelButton(this.cancelButton);
                if (BinaryLogic.containsAll(this.flagMask, UIOManager.BUTTONS_HIDE_OK)) {
                    getDialog().getRootPane().setDefaultButton(this.cancelButton);

                    // focus is on cancel if OK is hidden
                    focus = this.cancelButton;
                }
            }
            this.addButtons(this.defaultButtons);

            if (BinaryLogic.containsAll(this.flagMask, UIOManager.LOGIC_COUNTDOWN)) {
                // show timer
                initTimer(getCountdown());
            } else {
                timerLbl.setText(null);
            }
            getDialog().add(bottom, "spanx,growx,pushx");
            // pack dialog
            getDialog().invalidate();
            // this.setMinimumSize(this.getPreferredSize());

            getDialog().setResizable(this.isResizable());

            // minimum size foir a dialog

            // // Dimension screenDim =
            // Toolkit.getDefaultToolkit().getScreenSize();

            // this.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
            // getDialog().toFront();

            // if (this.getDesiredSize() != null) {
            // this.setSize(this.getDesiredSize());
            // }

            pack();
            if (this.dimensor != null) {
                final Dimension ret = this.dimensor.getDimension(AbstractDialog.this);
                if (ret != null) {
                    getDialog().setSize(ret);
                }

            }// register an escape listener to cancel the dialog
            this.registerEscape(focus);
            this.packed();

            Point loc = null;

            try {
                loc = this.getLocator().getLocationOnScreen(this);

            } catch (final Exception e) {
                e.printStackTrace();
            }
            if (loc != null) {
                getDialog().setLocation(loc);
            } else {
                try {
                    getDialog().setLocation(AbstractDialog.LOCATE_CENTER_OF_SCREEN.getLocationOnScreen(this));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }

            /*
             * workaround a javabug that forces the parentframe to stay always
             * on top
             */
            // Disabled on 14.06.2013:
            // This peace of code causes the parent to come on top even if we do
            // not want or need it
            // In our case, we do not want the captcha dialogs causing the
            // mainframe to get on top.
            // i think that this piece of code is a workaround for always on top
            // bugs we had years ago.
            //
            // if (getDialog().getParent() != null && !CrossSystem.isMac()) {
            // ((Window) getDialog().getParent()).setAlwaysOnTop(true);
            // ((Window) getDialog().getParent()).setAlwaysOnTop(false);
            // }
            getDialog().addComponentListener(new ComponentListener() {

                @Override
                public void componentHidden(final ComponentEvent e) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void componentMoved(final ComponentEvent e) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void componentResized(final ComponentEvent e) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void componentShown(final ComponentEvent e) {
                    AbstractDialog.this.orgLocationOnScreen = AbstractDialog.this.getDialog().getLocationOnScreen();
                    System.out.println(AbstractDialog.this.orgLocationOnScreen);
                }
            });

            setVisible(true);

            // if the dt has been interrupted,s setVisible will return even for
            // modal dialogs
            // however the dialog will stay open. Make sure to close it here
            this.dispose();
            // dialog gets closed
            // 17.11.2011 I did not comment this - may be debug code while
            // finding the problem with dialogs with closed parent...s

            // this code causes a dialog which gets disposed without setting
            // return mask to appear again.
            // System.out.println("Unlocked " +
            // this.getDialog().isDisplayable());
            //
            // if (this.returnBitMask == 0) {
            // this.setVisible(true);
            // Log.L.fine("Answer: Parent Closed ");
            // this.returnBitMask |= Dialog.RETURN_CLOSED;
            // this.setVisible(false);
            //
            // this.dispose();
            // }
        } finally {
            // System.out.println("SET OLD");
            Dialog.getInstance().setParentOwner(getDialog().getParent());
        }

        /*
         * workaround a javabug that forces the parentframe to stay always on
         * top
         */
        // Disabled on 14.06.2013:
        // This peace of code causes the parent to come on top even if we do not
        // want or need it
        // In our case, we do not want the captcha dialogs causing the mainframe
        // to get on top.
        // i think that this piece of code is a workaround for always on top
        // bugs we had years ago.
        // if (getDialog().getParent() != null && !CrossSystem.isMac()) {
        // ((Window) getDialog().getParent()).setAlwaysOnTop(true);
        // ((Window) getDialog().getParent()).setAlwaysOnTop(false);
        // }
    }

    public void actionPerformed(final ActionEvent e) {

        if (e.getSource() == this.okButton) {
            Log.L.fine("Answer: Button<OK:" + this.okButton.getText() + ">");
            this.setReturnmask(true);
        } else if (e.getSource() == this.cancelButton) {
            Log.L.fine("Answer: Button<CANCEL:" + this.cancelButton.getText() + ">");
            this.setReturnmask(false);
        }

        this.dispose();
    }

    /**
     * Overwrite this method to add additional buttons
     */
    protected void addButtons(final JPanel buttonBar) {
    }

    /**
     * called when user closes the window
     * 
     * @return <code>true</code>, if and only if the dialog should be closeable
     **/
    public boolean closeAllowed() {
        return true;
    }

    /**
     * @return
     */
    protected DefaultButtonPanel createBottomButtonPanel() {
        // TODO Auto-generated method stub
        if (AbstractDialog.BUTTON_HEIGHT <= 0) {
            return new DefaultButtonPanel("ins 0", "[]", "0[grow,fill]0");
        } else {
            return new DefaultButtonPanel("ins 0", "[]", "0[grow,fill," + AbstractDialog.BUTTON_HEIGHT + "!]0");
        }
    }

    /**
     * @return
     */
    protected MigPanel createBottomPanel() {
        // TODO Auto-generated method stub
        return new MigPanel("ins 0", "[]20[grow,fill][]", "[]");
    }

    protected abstract T createReturnValue();

    /**
     * This method has to be called to display the dialog. make sure that all
     * settings have beens et before, becvause this call very likly display a
     * dialog that blocks the rest of the gui until it is closed
     */
    public void displayDialog() {
        if (this.initialized) { return; }
        this.initialized = true;
        this._init();
    }

    @Override
    public void dispose() {

        if (this.dummyInit && dialog == null) { return; }
        if (!this.initialized) { throw new IllegalStateException("Dialog has not been initialized yet. call displayDialog()"); }
        if (isDisposed()) { return; }
        disposed = true;
        new EDTRunner() {

            @Override
            protected void runInEDT() {

                if (AbstractDialog.this.getDialog().isVisible()) {
                    try {
                        AbstractDialog.this.getLocator().onClose(AbstractDialog.this);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        if (AbstractDialog.this.dimensor != null) {
                            AbstractDialog.this.dimensor.onClose(AbstractDialog.this);
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
                AbstractDialog.super.dispose();
                if (AbstractDialog.this.timer != null) {
                    AbstractDialog.this.timer.interrupt();
                    AbstractDialog.this.timer = null;
                }
            }
        };

    }

    /**
     * @return
     */
    public boolean evaluateDontShowAgainFlag() {
        if (this.isDontShowAgainFlagEabled()) {
            final String key = this.getDontShowAgainKey();
            if (key != null) {
                // bypass if key is null. this enables us to show don't show
                // again checkboxes, but handle the result extern.
                try {
                    final int i = BinaryLogic.containsAll(this.flagMask, UIOManager.LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT) ? AbstractDialog.getSessionDontShowAgainValue(key) : JSonStorage.getPlainStorage("Dialogs").get(key, -1);

                    if (i >= 0) {
                        // filter saved return value
                        int ret = i & (Dialog.RETURN_OK | Dialog.RETURN_CANCEL);
                        // add flags
                        ret |= Dialog.RETURN_DONT_SHOW_AGAIN | Dialog.RETURN_SKIPPED_BY_DONT_SHOW;

                        /*
                         * if LOGIC_DONT_SHOW_AGAIN_IGNORES_CANCEL or
                         * LOGIC_DONT_SHOW_AGAIN_IGNORES_OK are used, we check
                         * here if we should handle the dont show again feature
                         */
                        if (BinaryLogic.containsAll(this.flagMask, UIOManager.LOGIC_DONT_SHOW_AGAIN_IGNORES_CANCEL) && BinaryLogic.containsAll(ret, Dialog.RETURN_CANCEL)) { return false; }
                        if (BinaryLogic.containsAll(this.flagMask, UIOManager.LOGIC_DONT_SHOW_AGAIN_IGNORES_OK) && BinaryLogic.containsAll(ret, Dialog.RETURN_OK)) { return false; }
                        if (isDisposed()) { throw new IllegalStateException("Dialog already disposed"); }
                        this.returnBitMask = ret;
                        return true;
                    }
                } catch (final Exception e) {
                    Log.exception(e);
                }
            }
        }
        return false;
    }

    /**
     * Fakes an init of the dialog. we need this if we want to work with the
     * model only.
     */
    public void forceDummyInit() {
        this.initialized = true;
        this.dummyInit = true;
    }

    @Override
    public CloseReason getCloseReason() {
        if (this.getReturnmask() == 0) { throw new IllegalStateException("Dialog has not been closed yet"); }
        if (BinaryLogic.containsSome(this.getReturnmask(), Dialog.RETURN_TIMEOUT)) { return CloseReason.TIMEOUT; }
        if (BinaryLogic.containsSome(this.getReturnmask(), Dialog.RETURN_CLOSED)) { return CloseReason.CLOSE; }
        if (BinaryLogic.containsSome(this.getReturnmask(), Dialog.RETURN_CANCEL)) { return CloseReason.CANCEL; }
        if (BinaryLogic.containsSome(this.getReturnmask(), Dialog.RETURN_OK)) { return CloseReason.OK; }

        throw new WTFException();

    }

    /**
     * @return
     */
    protected DefaultButtonPanel getDefaultButtonPanel() {
        final DefaultButtonPanel ret = this.createBottomButtonPanel();
        if (this.actions != null) {
            for (final AbstractAction a : this.actions) {
                ret.addAction(a).addFocusListener(this.defaultButtonFocusListener);

            }
        }
        return ret;

    }

    public DialogDimensor getDimensor() {
        return this.dimensor;
    }

    /**
     * Create the key to save the don't showmagain state in database. should be
     * overwritten in same dialogs. by default, the dialogs get differed by
     * their title and their classname
     * 
     * @return
     */
    public String getDontShowAgainKey() {
        return "ABSTRACTDIALOG_DONT_SHOW_AGAIN_" + this.getClass().getSimpleName() + "_" + this.toString();
    }

    /**
     * @return
     */
    protected String getDontShowAgainLabelText() {

        return _AWU.T.ABSTRACTDIALOG_STYLE_SHOW_DO_NOT_DISPLAY_AGAIN();
    }

    public int getFlags() {
        return this.flagMask;
    }

    public ImageIcon getIcon() {
        return this.icon;
    }

    /**
     * @return
     */
    protected JComponent getIconComponent() {
        this.iconLabel = new JLabel(this.icon);
        // iconLabel.setVerticalAlignment(JLabel.TOP);
        return this.iconLabel;
    }

    /**
     * @return
     */
    protected String getIconConstraints() {
        // TODO Auto-generated method stub
        return "gapright 10,gaptop 2";
    }

    public String getIconDataUrl() {
        if (this.getIcon() == null) { return null; }

        Base64OutputStream b64os = null;
        ByteArrayOutputStream bos = null;

        try {
            bos = new ByteArrayOutputStream();
            b64os = new Base64OutputStream(bos);
            ImageIO.write(IconIO.toBufferedImage(this.getIcon().getImage()), "png", b64os);
            b64os.flush(true);
            final String ret = "png;base64," + bos.toString("UTF-8");
            return ret;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                b64os.close();
            } catch (final Throwable e) {
            }
            try {
                bos.close();
            } catch (final Throwable e) {
            }

        }

    }

    public DialogLocator getLocator() {
        if (this.locator == null) {
            if (AbstractDialog.DEFAULT_LOCATOR != null) { return AbstractDialog.DEFAULT_LOCATOR; }
            return AbstractDialog.LOCATE_CENTER_OF_SCREEN;
        }
        return this.locator;
    }

    /**
     * Return the returnbitmask
     * 
     * @return
     */
    public int getReturnmask() {
        if (!this.initialized) { throw new IllegalStateException("Dialog has not been initialized yet. call displayDialog()"); }
        return this.returnBitMask;
    }

    public T getReturnValue() {
        if (!this.initialized) { throw new IllegalStateException("Dialog has not been initialized yet. call displayDialog()"); }

        return this.createReturnValue();
    }

    /**
     * @return
     */
    public String getTitle() {
        try {
            return getDialog().getTitle();
        } catch (final NullPointerException e) {
            // not initialized yet
            return this.title;
        }

    }

    /**
     * 
     * @return if the dialog has been moved by the user
     */
    public boolean hasBeenMoved() {

        return this.orgLocationOnScreen != null && !getDialog().getLocationOnScreen().equals(this.orgLocationOnScreen);
    }

    /**
     * @param focus
     */
    protected void initFocus(final JComponent focus) {
        focus.requestFocusInWindow();

    }



    /**
     * Closes the thread. Causes a cancel and setting the interrupted flag
     */
    public void interrupt() {

        if (isDisposed()) {

        throw new IllegalStateException("Dialog already disposed"); }
        new EDTRunner() {

            @Override
            protected void runInEDT() {

                AbstractDialog.this.dispose();
                AbstractDialog.this.returnBitMask = Dialog.RETURN_CLOSED | Dialog.RETURN_INTERRUPT;

            }
        };

    }

    /**
     * @return
     * 
     */
    public boolean isCountdownFlagEnabled() {
        return BinaryLogic.containsAll(this.flagMask, UIOManager.LOGIC_COUNTDOWN);

    }

    /**
     * @return
     */
    public boolean isDontShowAgainFlagEabled() {
        return BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN);
    }

    public boolean isDontShowAgainSelected() {
        if (this.isHiddenByDontShowAgain() || this.dontshowagain.isSelected() && this.dontshowagain.isEnabled()) { return true; }
        return false;

    }

    public boolean isHiddenByDontShowAgain() {
        if (this.dontshowagain != null && this.dontshowagain.isSelected() && this.dontshowagain.isEnabled()) { return false; }
        final String key = this.getDontShowAgainKey();
        if (key == null) { return false; }
        final int i = BinaryLogic.containsAll(this.flagMask, UIOManager.LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT) ? AbstractDialog.getSessionDontShowAgainValue(this.getDontShowAgainKey()) : JSonStorage.getPlainStorage("Dialogs").get(this.getDontShowAgainKey(), -1);
        return i >= 0;
    }

    /**
     * @return the initialized
     */
    public boolean isInitialized() {
        return this.initialized;
    }

    // /**
    // * should be overwritten and return a Dimension of the dialog should have
    // a
    // * special size
    // *
    // * @return
    // */
    // protected Dimension getDesiredSize() {
    //
    // return null;
    // }

    /**
     * override to change default resizable flag
     * 
     * @return
     */
    protected boolean isResizable() {
        // by default dialogs should be resizeble - at least for windows.
        // size calculation is almost impossible for nonresizable dialogs. they
        // are always a bit bigger than getDimension tells us
        return true;
    }

    /**
     * This method has to be overwritten to implement custom content
     * 
     * @return musst return a JComponent
     */
    abstract public JComponent layoutDialogContent();

    @Override
    public void onSetVisible(final boolean b) {

        if (!b && getDialog().isVisible()) {
            try {
                this.getLocator().onClose(AbstractDialog.this);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if (this.dimensor != null) {
                try {
                    this.dimensor.onClose(AbstractDialog.this);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Handle timeout
     */
    @Override
    public void onTimeout() {
        this.setReturnmask(false);
        if (isDisposed()) { throw new IllegalStateException("Dialog is already Disposed"); }
        this.returnBitMask |= Dialog.RETURN_TIMEOUT;

        this.dispose();
    }

    /**
     * may be overwritten to set focus to special components etc.
     */
    protected void packed() {
    }

    protected void registerEscape(final JComponent focus) {
        if (focus != null) {
            final KeyStroke ks = KeyStroke.getKeyStroke("ESCAPE");
            focus.getInputMap().put(ks, "ESCAPE");
            focus.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "ESCAPE");
            focus.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "ESCAPE");
            focus.getActionMap().put("ESCAPE", new AbstractAction() {

                private static final long serialVersionUID = -6666144330707394562L;

                public void actionPerformed(final ActionEvent e) {
                    Log.L.fine("Answer: Key<ESCAPE>");
                    if (isDisposed()) { return; }
                    AbstractDialog.this.setReturnmask(false);
                    AbstractDialog.this.returnBitMask |= Dialog.RETURN_ESC;
                    AbstractDialog.this.dispose();
                }
            });
            this.initFocus(focus);

        }
    }

    /**
     * resets the dummyinit to continue working with the dialog instance after
     * using {@link #forceDummyInit()}
     */

    public void resetDummyInit() {
        this.initialized = false;
        this.dummyInit = false;
    }

    public void setDimensor(final DialogDimensor dimensor) {
        this.dimensor = dimensor;
    }

    /**
     * @param b
     */
    public void setDoNotShowAgainSelected(final boolean b) {
        if (!BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) { throw new IllegalStateException("You have to set the Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN flag to use this method");

        }
        this.doNotShowAgainSelected = b;

    }

    public void setIcon(final ImageIcon icon) {

        this.icon = icon;
        if (this.iconLabel != null) {
            new EDTRunner() {

                @Override
                protected void runInEDT() {
                    AbstractDialog.this.iconLabel.setIcon(AbstractDialog.this.icon);
                }
            };
        }
    }

    /**
     * 
     */
    public void setInterrupted() {
        if (isDisposed()) { throw new IllegalStateException("Dialog already disposed"); }
        this.returnBitMask |= Dialog.RETURN_INTERRUPT;

    }

    /**
     * Add Additional BUttons on the left side of ok and cancel button. You can
     * add a "tag" property to the action in ordner to help the layouter,
     * 
     * <pre>
     * abstractActions[0].putValue(&quot;tag&quot;, &quot;ok&quot;)
     * </pre>
     * 
     * @param abstractActions
     *            list
     */
    public void setLeftActions(final AbstractAction... abstractActions) {
        this.actions = abstractActions;
    }

    /**
     * @param locateCenterOfScreen
     */
    public void setLocator(final DialogLocator locator) {
        this.locator = locator;
    }

    /**
     * Sets the returnvalue and saves the don't show again states to the
     * database
     * 
     * @param b
     */
    protected void setReturnmask(final boolean b) {
        if (isDisposed()) { throw new IllegalStateException("Dialog already disposed"); }
        this.returnBitMask = b ? Dialog.RETURN_OK : Dialog.RETURN_CANCEL;
        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {
            if (this.dontshowagain != null && this.dontshowagain.isSelected() && this.dontshowagain.isEnabled()) {
                this.returnBitMask |= Dialog.RETURN_DONT_SHOW_AGAIN;
                try {
                    final String key = this.getDontShowAgainKey();
                    if (key != null) {
                        if (BinaryLogic.containsAll(this.flagMask, UIOManager.LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT)) {
                            AbstractDialog.SESSION_DONTSHOW_AGAIN.put(this.getDontShowAgainKey(), this.returnBitMask);
                        } else {
                            JSonStorage.getPlainStorage("Dialogs").put(this.getDontShowAgainKey(), this.returnBitMask);
                            JSonStorage.getPlainStorage("Dialogs").save();
                        }
                    }

                } catch (final Exception e) {
                    Log.exception(e);
                }
            }
        }
    }

    /**
     * @param title2
     */
    public void setTitle(final String title2) {
        try {
            getDialog().setTitle(title2);
        } catch (final NullPointerException e) {
            this.title = title2;
        }
    }

    public UserIODefinition show() {
        final UserIODefinition ret = UIOManager.I().show(UserIODefinition.class, this);

        return ret;
    }

    /**
     * @throws DialogClosedException
     * @throws DialogCanceledException
     * 
     */
    public void throwCloseExceptions() throws DialogClosedException, DialogCanceledException {
        final int mask = this.getReturnmask();
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CLOSED)) { throw new DialogClosedException(mask); }
        if (BinaryLogic.containsSome(mask, Dialog.RETURN_CANCEL)) { throw new DialogCanceledException(mask); }

    }

    /**
     * Returns an id of the dialog based on it's title;
     */
    @Override
    public String toString() {
        return ("dialog-" + this.getTitle()).replaceAll("\\W", "_");
    }

    public void windowActivated(final WindowEvent arg0) {
    }

    public void windowClosed(final WindowEvent arg0) {
    }

    public void windowClosing(final WindowEvent arg0) {
        if (this.closeAllowed()) {
            Log.L.fine("Answer: Button<[X]>");
            if (isDisposed()) { throw new IllegalStateException("Dialog already disposed"); }
            this.returnBitMask |= Dialog.RETURN_CLOSED;
            this.dispose();
        } else {
            Log.L.fine("(Answer: Tried [X] bot not allowed)");
        }
    }

    public void windowDeactivated(final WindowEvent arg0) {
    }

    public void windowDeiconified(final WindowEvent arg0) {
    }

    public void windowIconified(final WindowEvent arg0) {
    }

    public void windowOpened(final WindowEvent arg0) {
    }

}
