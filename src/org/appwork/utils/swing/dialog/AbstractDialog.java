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

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

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

import org.appwork.storage.JSonStorage;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.locale.Tl8;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.LockPanel;
import org.appwork.utils.swing.SwingUtils;

public abstract class AbstractDialog<T> extends TimerDialog implements ActionListener, WindowListener {

    private static final long serialVersionUID = 1831761858087385862L;

    public static void resetDialogInformations() {
        try {
            JSonStorage.getStorage("Dialogs").clear();
        } catch (final Exception e) {
            Log.exception(e);
        }
    }

    protected JButton cancelButton;

    private final String cancelOption;

    private JPanel defaultButtons;

    private JCheckBox dontshowagain;

    protected int flagMask;

    private final ImageIcon icon;

    private boolean initialized = false;

    protected JButton okButton;

    private final String okOption;

    protected JComponent panel;

    private int returnBitMask = 0;

    public AbstractDialog(final int flag, final String title, final ImageIcon icon, final String okOption, final String cancelOption) {
        super(Dialog.getInstance().getParentOwner());

        this.flagMask = flag;
        this.setTitle(title);

        this.icon = (BinaryLogic.containsAll(flag, Dialog.STYLE_HIDE_ICON)) ? null : icon;
        this.okOption = (okOption == null) ? Tl8.ABSTRACTDIALOG_BUTTON_OK.toString() : okOption;
        this.cancelOption = (cancelOption == null) ? Tl8.ABSTRACTDIALOG_BUTTON_CANCEL.toString() : cancelOption;
    }

    /* this function will init and show the dialog */
    private void _init() {
        dont: if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {

            try {
                final int i = JSonStorage.getStorage("Dialogs").get(this.getDontShowAgainKey(), -1);

                if (i >= 0) {
                    // filter saved return value
                    int ret = (i & (Dialog.RETURN_OK | Dialog.RETURN_CANCEL));
                    // add flags
                    ret |= Dialog.RETURN_DONT_SHOW_AGAIN | Dialog.RETURN_SKIPPED_BY_DONT_SHOW;

                    /*
                     * if LOGIC_DONT_SHOW_AGAIN_IGNORES_CANCEL or
                     * LOGIC_DONT_SHOW_AGAIN_IGNORES_OK are used, we check here
                     * if we should handle the dont show again feature
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
        try {
            if (Dialog.getInstance().getParentOwner() != null) {
                LockPanel.create(Dialog.getInstance().getParentOwner()).lock(500);
            }
        } catch (final Exception e) {

        }
        if ((Dialog.getInstance().getParentOwner() == null) || !Dialog.getInstance().getParentOwner().isShowing()) {
            this.setAlwaysOnTop(true);
        }
        // The Dialog Modal
        this.setModal(true);
        // Layout manager
        this.setLayout(new MigLayout("ins 5", "[]", "[fill,grow][]"));
        // Dispose dialog on close
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(this);
        // create panel for the dialog's buttons
        this.defaultButtons = this.getDefaultButtonPanel();

        this.okButton = new JButton(this.okOption);
        /*
         * We set the focus on the ok button. if no ok button is shown, we set
         * the focus on cancel button
         */
        JButton focus = this.okButton;

        this.cancelButton = new JButton(this.cancelOption);
        // add listeners here
        this.okButton.addActionListener(this);
        this.cancelButton.addActionListener(this);
        // add icon if available
        if (this.icon != null) {
            this.add(new JLabel(this.icon), "split 2,alignx left,aligny center,shrinkx,gapright 10");
        }
        // Layout the dialog content and add it to the contentpane
        this.panel = this.layoutDialogContent();
        this.add(this.panel, "pushx,growx,pushy,growy,spanx,aligny center,wrap");
        // add the countdown timer
        this.add(this.timerLbl, "split 3,growx,hidemode 2");
        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {
            this.dontshowagain = new JCheckBox(Tl8.ABSTRACTDIALOG_STYLE_SHOW_DO_NOT_DISPLAY_AGAIN.toString());
            this.dontshowagain.setHorizontalAlignment(SwingConstants.TRAILING);
            this.dontshowagain.setHorizontalTextPosition(SwingConstants.LEADING);

            this.add(this.dontshowagain, "growx,pushx,alignx right,gapleft 20");
        } else {
            this.add(Box.createHorizontalGlue(), "growx,pushx,alignx right,gapleft 20");
        }
        this.add(this.defaultButtons, "alignx right,shrinkx");
        if ((this.flagMask & Dialog.BUTTONS_HIDE_OK) == 0) {

            // Set OK as defaultbutton
            this.getRootPane().setDefaultButton(this.okButton);
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
                this.getRootPane().setDefaultButton(this.cancelButton);
                this.cancelButton.requestFocusInWindow();
                // focus is on cancel if OK is hidden
                focus = this.cancelButton;
            }
        }
        this.addButtons(this.defaultButtons);

        if (BinaryLogic.containsAll(this.flagMask, Dialog.LOGIC_COUNTDOWN)) {
            // show timer
            this.initTimer(Dialog.getInstance().getCoundownTime());
        } else {
            this.timerLbl.setVisible(false);
        }

        // pack dialog
        this.invalidate();
        this.pack();
        this.setResizable(true);

        this.toFront();
        this.setMinimumSize(this.getPreferredSize());
        if (this.getDesiredSize() != null) {
            this.setSize(this.getDesiredSize());
        }
        if ((Dialog.getInstance().getParentOwner() == null) || !Dialog.getInstance().getParentOwner().isDisplayable() || !Dialog.getInstance().getParentOwner().isVisible()) {
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            this.setLocation(new Point((int) (screenSize.getWidth() - this.getWidth()) / 2, (int) (screenSize.getHeight() - this.getHeight()) / 2));

        } else if ((Dialog.getInstance().getParentOwner().getExtendedState() == Frame.ICONIFIED)) {
            // dock dialog at bottom right if mainframe is not visible
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            this.setLocation(new Point((int) (screenSize.getWidth() - this.getWidth() - 20), (int) (screenSize.getHeight() - this.getHeight() - 60)));
        } else {
            this.setLocation(SwingUtils.getCenter(Dialog.getInstance().getParentOwner(), this));
        }
        // register an escape listener to cancel the dialog
        final KeyStroke ks = KeyStroke.getKeyStroke("ESCAPE");
        focus.getInputMap().put(ks, "ESCAPE");
        focus.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "ESCAPE");
        focus.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "ESCAPE");
        focus.getActionMap().put("ESCAPE", new AbstractAction() {

            private static final long serialVersionUID = -6666144330707394562L;

            public void actionPerformed(final ActionEvent e) {
                AbstractDialog.this.dispose();
            }
        });

        focus.requestFocus();
        this.packed();
        this.setVisible(true);

        /*
         * workaround a javabug that forces the parentframe to stay always on
         * top
         */
        if (Dialog.getInstance().getParentOwner() != null) {
            Dialog.getInstance().getParentOwner().setAlwaysOnTop(true);
            Dialog.getInstance().getParentOwner().setAlwaysOnTop(false);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.appwork.utils.event.Event.ActionListener#actionPerformed(com.
     * rapidshare.utils.event.Event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == this.okButton) {
            this.setReturnmask(true);
        } else if (e.getSource() == this.cancelButton) {
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
    public AbstractDialog<T> displayDialog() {
        if (this.initialized) { return this; }
        this.initialized = true;
        this._init();
        return this;
    }

    @Override
    public void dispose() {
        if (!this.initialized) { throw new IllegalStateException("Dialog has not been initialized yet. call displayDialog()"); }
        try {
            if (Dialog.getInstance().getParentOwner() != null) {
                LockPanel.create(Dialog.getInstance().getParentOwner()).unlock(300);
            }
        } catch (final AWTException e1) {
        }
        super.dispose();
    }

    /**
     * @return
     */
    protected JPanel getDefaultButtonPanel() {
        return new JPanel(new MigLayout("ins 0", "[fill,grow]", "[fill,grow]"));
    }

    /**
     * should be overwritten and return a Dimension of the dialog should have a
     * special size
     * 
     * @return
     */
    protected Dimension getDesiredSize() {
        return null;
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

    /**
     * Sets the returnvalue and saves the don't show again states to the
     * database
     * 
     * @param b
     */
    protected void setReturnmask(final boolean b) {
        if (!this.initialized) { throw new IllegalStateException("Dialog has not been initialized yet. call displayDialog()"); }
        this.returnBitMask = b ? Dialog.RETURN_OK : Dialog.RETURN_CANCEL;
        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {
            if (this.dontshowagain.isSelected() && this.dontshowagain.isEnabled()) {
                this.returnBitMask |= Dialog.RETURN_DONT_SHOW_AGAIN;
                try {
                    JSonStorage.getStorage("Dialogs").put(this.getDontShowAgainKey(), this.returnBitMask);
                } catch (final Exception e) {
                    Log.exception(e);
                }
            }
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
            this.returnBitMask |= Dialog.RETURN_CLOSED;
            this.dispose();
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
