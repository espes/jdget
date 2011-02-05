/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.table;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import org.appwork.storage.JSonStorage;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.SwingUtils;
import org.appwork.utils.swing.dialog.Dialog;

public abstract class SearchDialog extends JDialog implements WindowListener, ActionListener, FocusListener {

    private static final long serialVersionUID = 9206575398715006581L;

    public static final int NO_REGEX_FLAG = 1 << 0;
    public static final int NO_CASE_FLAG = 1 << 1;

    private final ExtTable<?> owner;
    private final JTextComponent input;
    private final JCheckBox caseSensitive;
    private final JCheckBox regularExpression;
    private final JButton okButton;

    public SearchDialog(int flag, final ExtTable<?> owner) throws IOException {
        super(Dialog.getInstance().getParentWindow(), APPWORKUTILS.EXTTABLE_SEARCH_DIALOG_TITLE.s());

        this.owner = owner;
        this.owner.addFocusListener(this);

        this.caseSensitive = new JCheckBox(APPWORKUTILS.SEARCHDIALOG_CHECKBOX_CASESENSITIVE.s());
        this.regularExpression = new JCheckBox(APPWORKUTILS.SEARCHDIALOG_CHECKBOX_REGULAREXPRESSION.s());
        try {
            caseSensitive.setSelected(JSonStorage.getStorage("SearchDialog_" + owner.getTableID()).get("caseSensitive", false));
            regularExpression.setSelected(JSonStorage.getStorage("SearchDialog_" + owner.getTableID()).get("regularExpression", false));

            ActionListener saveListener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    JSonStorage.getStorage("SearchDialog_" + owner.getTableID()).put("caseSensitive", caseSensitive.isSelected());
                    JSonStorage.getStorage("SearchDialog_" + owner.getTableID()).put("regularExpression", regularExpression.isSelected());
                }

            };

            caseSensitive.addActionListener(saveListener);
            regularExpression.addActionListener(saveListener);
        } catch (Exception e) {
            Log.exception(e);
        }
        caseSensitive.setVisible(BinaryLogic.containsNone(flag, NO_CASE_FLAG));
        regularExpression.setVisible(BinaryLogic.containsNone(flag, NO_REGEX_FLAG));
        this.setLayout(new MigLayout("ins 5", "[fill,grow]", "[fill,grow][][]"));
        // Dispose dialog on close
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(this);
        okButton = new JButton(APPWORKUTILS.SEARCHDIALOG_BUTTON_FIND.s());
        okButton.addActionListener(this);

        add(new JLabel(ImageProvider.getImageIcon("find", 32, 32, true)), "alignx left,aligny center,shrinkx,gapright 10,spany");

        input = new JTextField();
        input.setBorder(BorderFactory.createEtchedBorder());

        add(input, "pushy,growy,spanx,wrap");

        input.selectAll();

        add(regularExpression);
        add(caseSensitive);
        add(okButton, "skip 2,alignx right,wrap");

        // pack dialog
        this.invalidate();
        this.pack();
        this.setResizable(false);

        this.toFront();

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
        okButton.getInputMap().put(ks, "ESCAPE");
        okButton.getInputMap(JButton.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "ESCAPE");
        okButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(ks, "ESCAPE");
        okButton.getActionMap().put("ESCAPE", new AbstractAction() {

            private static final long serialVersionUID = -6666144330707394562L;

            public void actionPerformed(ActionEvent e) {
                close();
            }

        });

        ks = KeyStroke.getKeyStroke("ENTER");
        okButton.getInputMap().put(ks, "ENTER");
        okButton.getInputMap(JButton.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "ENTER");
        okButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(ks, "ENTER");
        okButton.getActionMap().put("ENTER", new AbstractAction() {

            private static final long serialVersionUID = -1331741306700505613L;

            public void actionPerformed(ActionEvent e) {
                okButton.doClick();
            }

        });

        this.setVisible(true);

        /*
         * workaround a javabug that forces the parentframe to stay always on
         * top
         */
        if (Dialog.getInstance().getParentOwner() != null) {
            Dialog.getInstance().getParentOwner().setAlwaysOnTop(true);
            Dialog.getInstance().getParentOwner().setAlwaysOnTop(false);
        }

        requestFocus();
        input.requestFocusInWindow();
        input.requestFocus();
    }

    public void requestFocus() {
        super.requestFocus();
        input.requestFocusInWindow();
        input.requestFocus();
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        if (!e.isTemporary()) {
            close();
        }
    }

    private void close() {
        owner.removeFocusListener(this);
        dispose();
    }

    public void windowClosing(WindowEvent arg0) {
        close();
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

    public String getReturnID() {
        return input.getText();
    }

    public boolean isCaseSensitive() {
        return this.caseSensitive.isSelected();
    }

    public boolean isRegex() {
        return this.regularExpression.isSelected();
    }

    abstract public void actionPerformed(ActionEvent e);

}
