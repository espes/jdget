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
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;

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

import org.appwork.app.gui.MigPanel;
import org.appwork.resources.AWUTheme;
import org.appwork.storage.JSonStorage;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.locale._AWU;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;
import org.appwork.utils.swing.EDTRunner;
import org.appwork.utils.swing.SwingUtils;

public abstract class AbstractDialog<T> extends TimerDialog implements ActionListener, WindowListener {

    private static final long                     serialVersionUID       = 1831761858087385862L;

    private static final HashMap<String, Integer> SESSION_DONTSHOW_AGAIN = new HashMap<String, Integer>();

    public static Integer getSessionDontShowAgainValue(final String key) {
        final Integer ret = AbstractDialog.SESSION_DONTSHOW_AGAIN.get(key);
        if (ret == null) { return -1; }

        return ret;
    }

    public static void resetDialogInformations() {
        try {
            AbstractDialog.SESSION_DONTSHOW_AGAIN.clear();
            JSonStorage.getStorage("Dialogs").clear();
        } catch (final Exception e) {
            Log.exception(e);
        }
    }

    protected JButton        cancelButton;

    private final String     cancelOption;
    private JPanel           defaultButtons;

    protected JCheckBox      dontshowagain;

    protected int            flagMask;

    private ImageIcon        icon;

    private boolean          initialized            = false;

    protected JButton        okButton;

    private final String     okOption;

    protected JComponent     panel;

    protected int            returnBitMask          = 0;

    private AbstractAction[] actions                = null;

    private String           title;

    private JLabel           iconLabel;

    protected boolean        doNotShowAgainSelected = false;

    public AbstractDialog(final int flag, final String title, final ImageIcon icon, final String okOption, final String cancelOption) {
        super();

        this.title = title;
        this.flagMask = flag;

        this.icon = BinaryLogic.containsAll(flag, Dialog.STYLE_HIDE_ICON) ? null : icon;
        this.okOption = okOption == null ? _AWU.T.ABSTRACTDIALOG_BUTTON_OK() : okOption;
        this.cancelOption = cancelOption == null ? _AWU.T.ABSTRACTDIALOG_BUTTON_CANCEL() : cancelOption;
    }

    public boolean isHiddenByDontShowAgain() {
        final int i = BinaryLogic.containsAll(this.flagMask, Dialog.LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT) ? AbstractDialog.getSessionDontShowAgainValue(this.getDontShowAgainKey()) : JSonStorage.getStorage("Dialogs").get(this.getDontShowAgainKey(), -1);
        return i >= 0;
    }

    /**
     * this function will init and show the dialog
     */
    protected void _init() {

        this.layoutDialog();

        if (BinaryLogic.containsAll(this.flagMask, Dialog.LOGIC_COUNTDOWN)) {
            this.timerLbl.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(final MouseEvent e) {
                    AbstractDialog.this.cancel();
                    AbstractDialog.this.timerLbl.removeMouseListener(this);
                }

            });
            this.timerLbl.setToolTipText(_AWU.T.TIMERDIALOG_TOOLTIP_TIMERLABEL());

            this.timerLbl.setIcon(AWUTheme.I().getIcon("dialog/cancel", 16));
        }
        /**
         * this is very important so the new shown dialog will become root for
         * all following dialogs! we save old parentWindow, then set current
         * dialogwindow as new root and show dialog. after dialog has been
         * shown, we restore old parentWindow
         */
        final Component parentOwner = Dialog.getInstance().getParentOwner();
        Dialog.getInstance().setParentOwner(this.getDialog());
        try {
            this.setTitle(this.title);
            dont: if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {

                try {
                    final int i = BinaryLogic.containsAll(this.flagMask, Dialog.LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT) ? AbstractDialog.getSessionDontShowAgainValue(this.getDontShowAgainKey()) : JSonStorage.getStorage("Dialogs").get(this.getDontShowAgainKey(), -1);

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
                        if (BinaryLogic.containsAll(this.flagMask, Dialog.LOGIC_DONT_SHOW_AGAIN_IGNORES_CANCEL) && BinaryLogic.containsAll(ret, Dialog.RETURN_CANCEL)) {
                            break dont;
                        }
                        if (BinaryLogic.containsAll(this.flagMask, Dialog.LOGIC_DONT_SHOW_AGAIN_IGNORES_OK) && BinaryLogic.containsAll(ret, Dialog.RETURN_OK)) {
                            break dont;
                        }

                        this.returnBitMask = ret;
                        return;
                    }
                } catch (final Exception e) {
                    Log.exception(e);
                }
            }
            if (parentOwner == null || !parentOwner.isShowing()) {
                this.getDialog().setAlwaysOnTop(true);
            }
            // The Dialog Modal
            this.getDialog().setModal(true);
            // Layout manager

            // Dispose dialog on close
            this.getDialog().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.getDialog().addWindowListener(this);
            // create panel for the dialog's buttons
            this.okButton = new JButton(this.okOption);
            this.cancelButton = new JButton(this.cancelOption);
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
                this.getDialog().setLayout(new MigLayout("ins 5,wrap 2", "[][grow,fill]", "[grow,fill][]"));
                this.getDialog().add(getIconComponent(), getIconConstraints());
            } else {
                this.getDialog().setLayout(new MigLayout("ins 5,wrap 1", "[grow,fill]", "[grow,fill][]"));
            }
            // Layout the dialog content and add it to the contentpane
            this.panel = this.layoutDialogContent();

            this.getDialog().add(this.panel, "");

            // add the countdown timer
            final MigPanel bottom = new MigPanel("ins 0", "[]20[grow,fill][]", "[]");
            bottom.setOpaque(false);

            bottom.add(this.timerLbl);
            if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {

                this.dontshowagain = new JCheckBox(_AWU.T.ABSTRACTDIALOG_STYLE_SHOW_DO_NOT_DISPLAY_AGAIN());
                this.dontshowagain.setHorizontalAlignment(SwingConstants.TRAILING);
                this.dontshowagain.setHorizontalTextPosition(SwingConstants.LEADING);
                this.dontshowagain.setSelected(this.doNotShowAgainSelected);

                bottom.add(this.dontshowagain, "alignx right");
            } else {
                bottom.add(Box.createHorizontalGlue());
            }
            bottom.add(this.defaultButtons);

            if ((this.flagMask & Dialog.BUTTONS_HIDE_OK) == 0) {

                // Set OK as defaultbutton
                this.getDialog().getRootPane().setDefaultButton(this.okButton);
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
                this.defaultButtons.add(this.okButton, "alignx right,tag ok,sizegroup confirms");

            }
            if (!BinaryLogic.containsAll(this.flagMask, Dialog.BUTTONS_HIDE_CANCEL)) {

                this.defaultButtons.add(this.cancelButton, "alignx right,tag cancel,sizegroup confirms");
                if (BinaryLogic.containsAll(this.flagMask, Dialog.BUTTONS_HIDE_OK)) {
                    this.getDialog().getRootPane().setDefaultButton(this.cancelButton);
                    this.cancelButton.requestFocusInWindow();
                    // focus is on cancel if OK is hidden
                    focus = this.cancelButton;
                }
            }
            this.addButtons(this.defaultButtons);

            if (BinaryLogic.containsAll(this.flagMask, Dialog.LOGIC_COUNTDOWN)) {
                // show timer
                this.initTimer(this.getCountdown());
            } else {
                this.timerLbl.setText(null);
            }
            this.getDialog().add(bottom, "spanx,growx,pushx");
            // pack dialog
            this.getDialog().invalidate();
            // this.setMinimumSize(this.getPreferredSize());
            if (!this.getDialog().isMinimumSizeSet()) {
                this.getDialog().setMinimumSize(new Dimension(300, 80));
            }
            this.getDialog().setResizable(this.isResizable());

            this.pack();

            // minimum size foir a dialog

            // // Dimension screenDim =
            // Toolkit.getDefaultToolkit().getScreenSize();

            // this.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
            this.getDialog().toFront();

            // if (this.getDesiredSize() != null) {
            // this.setSize(this.getDesiredSize());
            // }

            if (!this.getDialog().getParent().isDisplayable() || !this.getDialog().getParent().isVisible()) {
                final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                this.getDialog().setLocation(new Point((int) (screenSize.getWidth() - this.getDialog().getWidth()) / 2, (int) (screenSize.getHeight() - this.getDialog().getHeight()) / 2));

            } else if (this.getDialog().getParent() instanceof Frame && ((Frame) this.getDialog().getParent()).getExtendedState() == Frame.ICONIFIED) {
                // dock dialog at bottom right if mainframe is not visible
                final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                this.getDialog().setLocation(new Point((int) (screenSize.getWidth() - this.getDialog().getWidth() - 20), (int) (screenSize.getHeight() - this.getDialog().getHeight() - 60)));
            } else {
                this.getDialog().setLocation(SwingUtils.getCenter(this.getDialog().getParent(), this.getDialog()));
            }
            // register an escape listener to cancel the dialog
            this.registerEscape(focus);
            this.packed();

            // System.out.println("NEW ONE "+this.getDialog());
            /*
             * workaround a javabug that forces the parentframe to stay always
             * on top
             */
            if (this.getDialog().getParent() != null && !CrossSystem.isMac()) {
                ((Window) this.getDialog().getParent()).setAlwaysOnTop(true);
                ((Window) this.getDialog().getParent()).setAlwaysOnTop(false);
            }

            this.setVisible(true);
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
            Dialog.getInstance().setParentOwner(this.getDialog().getParent());
        }

        /*
         * workaround a javabug that forces the parentframe to stay always on
         * top
         */
        if (this.getDialog().getParent() != null && !CrossSystem.isMac()) {
            ((Window) this.getDialog().getParent()).setAlwaysOnTop(true);
            ((Window) this.getDialog().getParent()).setAlwaysOnTop(false);
        }
    }

    /**
     * @return
     */
   protected JComponent getIconComponent() {
        this.iconLabel = new JLabel(this.icon);
//        iconLabel.setVerticalAlignment(JLabel.TOP);
        return iconLabel;
    }

    /**
     * @return
     */
    protected String getIconConstraints() {
        // TODO Auto-generated method stub
        return "gapright 10,gaptop 2";
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

        if (!this.initialized) { throw new IllegalStateException("Dialog has not been initialized yet. call displayDialog()"); }

        new EDTRunner() {

            @Override
            protected void runInEDT() {
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
    protected JPanel getDefaultButtonPanel() {
        final JPanel ret = new JPanel(new MigLayout("ins 0", "[]", "0[]0"));
        if (this.actions != null) {
            for (final AbstractAction a : this.actions) {
                String tag = (String) a.getValue("tag");
                if (tag == null) {
                    tag = "help";
                }
                ret.add(new JButton(a), "tag " + tag + ",sizegroup confirms");
            }
        }
        return ret;

    }

    /**
     * Create the key to save the don't showmagain state in database. should be
     * overwritten in same dialogs. by default, the dialogs get differed by
     * their title and their classname
     * 
     * @return
     */
    protected String getDontShowAgainKey() {
        return "ABSTRACTDIALOG_DONT_SHOW_AGAIN_" + this.getClass().getSimpleName() + "_" + this.toString();
    }

    public ImageIcon getIcon() {
        return this.icon;
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
            return this.getDialog().getTitle();
        } catch (final NullPointerException e) {
            // not initialized yet
            return this.title;
        }

    }

    /**
     * @return the initialized
     */
    public boolean isInitialized() {
        return this.initialized;
    }

    /**
     * override to change default resizable flag
     * 
     * @return
     */
    protected boolean isResizable() {
        // TODO Auto-generated method stub
        return false;
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
     * This method has to be overwritten to implement custom content
     * 
     * @return musst return a JComponent
     */
    abstract public JComponent layoutDialogContent();

    /**
     * Handle timeout
     */
    @Override
    protected void onTimeout() {
        this.setReturnmask(false);
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
                    AbstractDialog.this.setReturnmask(false);
                    AbstractDialog.this.dispose();
                }
            });
            focus.requestFocus();
        }
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
     * Sets the returnvalue and saves the don't show again states to the
     * database
     * 
     * @param b
     */
    protected void setReturnmask(final boolean b) {

        this.returnBitMask = b ? Dialog.RETURN_OK : Dialog.RETURN_CANCEL;
        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {
            if (this.dontshowagain.isSelected() && this.dontshowagain.isEnabled()) {
                this.returnBitMask |= Dialog.RETURN_DONT_SHOW_AGAIN;
                try {
                    if (BinaryLogic.containsAll(this.flagMask, Dialog.LOGIC_DONT_SHOW_AGAIN_DELETE_ON_EXIT)) {
                        AbstractDialog.SESSION_DONTSHOW_AGAIN.put(this.getDontShowAgainKey(), this.returnBitMask);
                    } else {
                        JSonStorage.getStorage("Dialogs").put(this.getDontShowAgainKey(), this.returnBitMask);
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
    protected void setTitle(final String title2) {
        try {
            this.getDialog().setTitle(title2);
        } catch (final NullPointerException e) {
            this.title = title2;
        }
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
