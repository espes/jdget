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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.appwork.storage.ConfigInterface;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.locale.Tl8;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.LockPanel;
import org.appwork.utils.swing.SwingUtils;

public abstract class AbstractDialog<T> extends TimerDialog implements ActionListener, WindowListener {

    private static final long serialVersionUID = 1831761858087385862L;

    protected JButton cancelButton;

    protected JButton okButton;

    protected JComponent panel;

    protected int flagMask;

    private int returnBitMask = 0;

    private ImageIcon icon;

    private String okOption;

    private String cancelOption;

    private JCheckBox dontshowagain;

    private JPanel defaultButtons;

    public AbstractDialog(int flag, String title, ImageIcon icon, String okOption, String cancelOption) {
        super(Dialog.getInstance().getParentOwner());

        this.flagMask = flag;
        setTitle(title);

        this.icon = (BinaryLogic.containsAll(flag, Dialog.STYLE_HIDE_ICON)) ? null : icon;
        this.okOption = (okOption == null) ? Tl8.ABSTRACTDIALOG_BUTTON_OK.toString() : okOption;
        this.cancelOption = (cancelOption == null) ? Tl8.ABSTRACTDIALOG_BUTTON_CANCEL.toString() : cancelOption;
    }

    /* this function will init and show the dialog */
    protected void init() {
        dont: if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {

            try {
                int i = ConfigInterface.getStorage("Dialogs").get(getDontShowAgainKey(), -1);

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
                    if (BinaryLogic.containsAll(flagMask, Dialog.LOGIC_DONT_SHOW_AGAIN_IGNORES_CANCEL) && BinaryLogic.containsAll(ret, Dialog.RETURN_CANCEL)) {
                        break dont;
                    }
                    if (BinaryLogic.containsAll(flagMask, Dialog.LOGIC_DONT_SHOW_AGAIN_IGNORES_OK) && BinaryLogic.containsAll(ret, Dialog.RETURN_OK)) {
                        break dont;
                    }

                    this.returnBitMask = ret;
                    return;
                }
            } catch (Exception e) {
                Log.exception(e);
            }
        }
        try {
            if (Dialog.getInstance().getParentOwner() != null) LockPanel.create(Dialog.getInstance().getParentOwner()).lock(500);
        } catch (Exception e) {

        }
        if (Dialog.getInstance().getParentOwner() == null || !Dialog.getInstance().getParentOwner().isShowing()) {
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
        this.defaultButtons = getDefaultButtonPanel();

        okButton = new JButton(this.okOption);
        /*
         * We set the focus on the ok button. if no ok button is shown, we set
         * the focus on cancel button
         */
        JButton focus = okButton;

        cancelButton = new JButton(this.cancelOption);
        // add listeners here
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        // add icon if available
        if (icon != null) {
            add(new JLabel(this.icon), "split 2,alignx left,aligny center,shrinkx,gapright 10");
        }
        // Layout the dialog content and add it to the contentpane
        panel = layoutDialogContent();
        add(panel, "pushx,growx,pushy,growy,spanx,aligny center,wrap");
        // add the countdown timer
        add(this.timerLbl, "split 3,growx,hidemode 2");
        if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {
            dontshowagain = new JCheckBox(Tl8.ABSTRACTDIALOG_STYLE_SHOW_DO_NOT_DISPLAY_AGAIN.toString());
            dontshowagain.setHorizontalAlignment(JCheckBox.TRAILING);
            dontshowagain.setHorizontalTextPosition(JCheckBox.LEADING);

            add(dontshowagain, "growx,pushx,alignx right,gapleft 20");
        } else {
            add(Box.createHorizontalGlue(), "growx,pushx,alignx right,gapleft 20");
        }
        add(defaultButtons, "alignx right,shrinkx");
        if ((flagMask & Dialog.BUTTONS_HIDE_OK) == 0) {

            // Set OK as defaultbutton
            getRootPane().setDefaultButton(okButton);
            okButton.addHierarchyListener(new HierarchyListener() {
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
                        JButton defaultButton = (JButton) e.getComponent();
                        JRootPane root = SwingUtilities.getRootPane(defaultButton);
                        if (root != null) {
                            root.setDefaultButton(defaultButton);
                        }
                    }
                }
            });
            focus = okButton;
            defaultButtons.add(okButton, "alignx right,tag ok,sizegroup confirms");
        }
        if (!BinaryLogic.containsAll(flagMask, Dialog.BUTTONS_HIDE_CANCEL)) {

            defaultButtons.add(cancelButton, "alignx right,tag cancel,sizegroup confirms");
            if (BinaryLogic.containsAll(flagMask, Dialog.BUTTONS_HIDE_OK)) {
                getRootPane().setDefaultButton(cancelButton);
                cancelButton.requestFocusInWindow();
                // focus is on cancel if OK is hidden
                focus = cancelButton;
            }
        }
        this.addButtons(defaultButtons);

        if (BinaryLogic.containsAll(flagMask, Dialog.LOGIC_COUNTDOWN)) {
            // show timer
            this.initTimer(Dialog.getInstance().getCoundownTime());
        } else {
            timerLbl.setVisible(false);
        }

        // pack dialog
        this.invalidate();
        this.pack();
        this.setResizable(true);

        this.toFront();
        this.setMinimumSize(this.getPreferredSize());
        if (getDesiredSize() != null) {
            this.setSize(getDesiredSize());
        }
        if (Dialog.getInstance().getParentOwner() == null || !Dialog.getInstance().getParentOwner().isDisplayable() || !Dialog.getInstance().getParentOwner().isVisible()) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            this.setLocation(new Point((int) (screenSize.getWidth() - this.getWidth()) / 2, (int) (screenSize.getHeight() - this.getHeight()) / 2));

        } else if ((Dialog.getInstance().getParentOwner().getExtendedState() == JFrame.ICONIFIED)) {
            // dock dialog at bottom right if mainframe is not visible
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            this.setLocation(new Point((int) (screenSize.getWidth() - this.getWidth() - 20), (int) (screenSize.getHeight() - this.getHeight() - 60)));
        } else {
            this.setLocation(SwingUtils.getCenter(Dialog.getInstance().getParentOwner(), this));
        }
        // register an escape listener to cancel the dialog
        KeyStroke ks = KeyStroke.getKeyStroke("ESCAPE");
        focus.getInputMap().put(ks, "ESCAPE");
        focus.getInputMap(JButton.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "ESCAPE");
        focus.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(ks, "ESCAPE");
        focus.getActionMap().put("ESCAPE", new AbstractAction() {

            private static final long serialVersionUID = -6666144330707394562L;

            public void actionPerformed(ActionEvent e) {
                dispose();
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
     * Overwrite this method to add additional buttons
     */
    protected void addButtons(JPanel buttonBar) {
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
     * may be overwritten to set focus to special components etc.
     */
    protected void packed() {
    }

    /**
     * Returns an id of the dialog based on it's title;
     */
    @Override
    public String toString() {
        return ("dialog-" + this.getTitle()).replaceAll("\\W", "_");
    }

    /**
     * This method has to be overwritten to implement custom content
     * 
     * @return musst return a JComponent
     */
    abstract public JComponent layoutDialogContent();

    /*
     * (non-Javadoc)
     * 
     * @seeorg.appwork.utils.event.Event.ActionListener#actionPerformed(com.
     * rapidshare.utils.event.Event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okButton) {
            setReturnmask(true);
        } else if (e.getSource() == cancelButton) {
            setReturnmask(false);
        }
        dispose();
    }

    public void dispose() {
        try {
            if (Dialog.getInstance().getParentOwner() != null) LockPanel.create(Dialog.getInstance().getParentOwner()).unlock(300);
        } catch (AWTException e1) {
        }
        super.dispose();
    }

    /**
     * Handle timeout
     */
    protected void onTimeout() {
        setReturnmask(false);
        returnBitMask |= Dialog.RETURN_TIMEOUT;

        this.dispose();
    }

    /**
     * Sets the returnvalue and saves the don't show again states to the
     * database
     * 
     * @param b
     */
    protected void setReturnmask(boolean b) {
        returnBitMask = b ? Dialog.RETURN_OK : Dialog.RETURN_CANCEL;
        if (BinaryLogic.containsAll(flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {
            if (dontshowagain.isSelected() && dontshowagain.isEnabled()) {
                returnBitMask |= Dialog.RETURN_DONT_SHOW_AGAIN;
                try {
                    ConfigInterface.getStorage("Dialogs").put(getDontShowAgainKey(), returnBitMask);
                } catch (Exception e) {
                    Log.exception(e);
                }
            }
        }
    }

    /**
     * Return the returnbitmask
     * 
     * @return
     */
    public int getReturnmask() {
        return returnBitMask;
    }

    public static void resetDialogInformations() {
        try {
            ConfigInterface.getStorage("Dialogs").clear();
        } catch (Exception e) {
            Log.exception(e);
        }
    }

    /**
     * called when user closes the window
     * 
     * @return <code>true</code>, if and only if the dialog should be closeable
     **/
    public boolean closeAllowed() {
        return true;
    }

    public void windowClosing(WindowEvent arg0) {
        if (closeAllowed()) {
            returnBitMask |= Dialog.RETURN_CLOSED;
            dispose();
        }
    }

    public void windowDeactivated(WindowEvent arg0) {
    }

    public void windowClosed(WindowEvent arg0) {
    }

    public void windowActivated(WindowEvent arg0) {
    }

    public void windowDeiconified(WindowEvent arg0) {
    }

    public void windowIconified(WindowEvent arg0) {
    }

    public void windowOpened(WindowEvent arg0) {
    }

    abstract public T getRetValue();
}
