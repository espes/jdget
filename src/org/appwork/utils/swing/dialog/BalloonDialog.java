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
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.appwork.resources.AWUTheme;
import org.appwork.storage.JSonStorage;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.formatter.TimeFormatter;
import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;

import com.sun.awt.AWTUtilities;

public class BalloonDialog extends AbstractDialog<Integer> {

    private static final long serialVersionUID = -7647771640756844691L;
    private final JComponent  component;
    private final Point       desiredLocation;
    private GraphicsDevice    screen;
    private BallonPanel       ballonPanel;

    public BalloonDialog(final int flag, final JComponent comp, final Point point) throws OffScreenException {
        super(flag | Dialog.BUTTONS_HIDE_CANCEL | Dialog.BUTTONS_HIDE_OK | Dialog.STYLE_HIDE_ICON, "Balloon", null, null, null);
        this.component = comp;
        this.desiredLocation = point;

        final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] screens = ge.getScreenDevices();
        // store screen device bounds to find current screen later easily

        main: for (int i = 0; i < ge.getScreenDevices().length; i++) {
            final Rectangle r = screens[i].getDefaultConfiguration().getBounds();
            if (this.desiredLocation.x >= r.x && this.desiredLocation.x <= r.x + r.width) {
                // x correct
                if (this.desiredLocation.y >= r.y && this.desiredLocation.y <= r.y + r.height) {
                    // y correct
                    this.screen = screens[i];
                    break main;
                }

            }
        }
        if (this.screen == null) { throw new OffScreenException("Point not on screen"); }

    }

    /**
     * this function will init and show the dialog
     */
    @Override
    protected void _init() {

        this.layoutDialog();

        // p.add(this.component);

        this.getDialog().setUndecorated(true);

        if (BinaryLogic.containsAll(this.flagMask, Dialog.LOGIC_COUNTDOWN)) {
            this.timerLbl.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(final MouseEvent e) {
                    BalloonDialog.this.cancel();
                    BalloonDialog.this.timerLbl.removeMouseListener(this);
                }

            });
            this.timerLbl.setToolTipText(APPWORKUTILS.T.TIMERDIALOG_TOOLTIP_TIMERLABEL());

            this.timerLbl.setIcon(AWUTheme.I().getIcon("cancel", 16));

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
            this.getDialog().setLayout(new MigLayout("ins 0", "[]", "[]"));
            // Dispose dialog on close
            this.getDialog().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.getDialog().addWindowListener(this);

            // add the countdown timer
            // this.getDialog().add(this.timerLbl, "split 3,growx,hidemode 2");
            if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_SHOW_DO_NOT_DISPLAY_AGAIN)) {
                this.dontshowagain = new JCheckBox(APPWORKUTILS.T.ABSTRACTDIALOG_STYLE_SHOW_DO_NOT_DISPLAY_AGAIN());
                this.dontshowagain.setHorizontalAlignment(SwingConstants.TRAILING);
                this.dontshowagain.setHorizontalTextPosition(SwingConstants.LEADING);

                // this.getDialog().add(this.dontshowagain,
                // "growx,pushx,alignx right,gapleft 20");
            } else {
                // this.getDialog().add(Box.createHorizontalGlue(),
                // "growx,pushx,alignx right,gapleft 20");
            }

            this.ballonPanel = new BallonPanel(this.component, this.timerLbl, this.dontshowagain, this.screen.getDefaultConfiguration().getBounds(), this.desiredLocation);
            this.getDialog().setLayout(new MigLayout("ins 0", "[]", "[]"));
            this.getDialog().getContentPane().add(this.ballonPanel);
            AWTUtilities.setWindowOpaque(BalloonDialog.this.getDialog(), false);

            if (BinaryLogic.containsAll(this.flagMask, Dialog.LOGIC_COUNTDOWN)) {
                // show timer
                this.initTimer(this.getCountdown());
            } else {
                this.timerLbl.setVisible(false);
            }

            // pack dialog
            this.getDialog().invalidate();
            // this.setMinimumSize(this.getPreferredSize());
            // this.getDialog().setMinimumSize(new Dimension(300, 80));

            this.pack();

            // minimum size foir a dialog

            // // Dimension screenDim =
            // Toolkit.getDefaultToolkit().getScreenSize();

            // this.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
            this.getDialog().toFront();

            // if (this.getDesiredSize() != null) {
            // this.setSize(this.getDesiredSize());
            // }
            this.getDialog().setLocation(this.desiredLocation);
            // register an escape listener to cancel the dialog
            final KeyStroke ks = KeyStroke.getKeyStroke("ESCAPE");
            this.ballonPanel.getInputMap().put(ks, "ESCAPE");
            this.ballonPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "ESCAPE");
            this.ballonPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "ESCAPE");
            this.ballonPanel.getActionMap().put("ESCAPE", new AbstractAction() {

                private static final long serialVersionUID = -6666144330707394562L;

                public void actionPerformed(final ActionEvent e) {
                    Log.L.fine("Answer: Key<ESCAPE>");
                    BalloonDialog.this.setReturnmask(false);
                    BalloonDialog.this.dispose();
                }
            });
            this.ballonPanel.requestFocus();
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
     * @param ballonPanel
     * 
     */

    @Override
    protected Integer createReturnValue() {

        return this.getReturnmask();
    }

    @Override
    protected void layoutDialog() {
        this.dialog = new InternDialog() {

            @Override
            public void setLocation(final Point p) {
                p.x += BalloonDialog.this.ballonPanel.getXOffset();
                p.y += BalloonDialog.this.ballonPanel.getYOffset();
                System.out.println("Locaetion " + p);
                super.setLocation(p);
            }

        };

        this.timerLbl = new JLabel(TimeFormatter.formatSeconds(this.getCountdown(), 0));

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#layoutDialogContent()
     */
    @Override
    public JComponent layoutDialogContent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        if (BinaryLogic.containsAll(this.flagMask, Dialog.LOGIC_DONOTSHOW_BASED_ON_TITLE_ONLY)) {
            return ("dialog-" + this.getTitle()).replaceAll("\\W", "_");
        } else {
            return ("dialog-" + this.getTitle() + "_" + this.component.toString()).replaceAll("\\W", "_");
        }

    }

}
