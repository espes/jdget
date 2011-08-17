/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable;

import java.awt.Dimension;
import java.awt.Frame;
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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import net.miginfocom.swing.MigLayout;

import org.appwork.resources.AWUTheme;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.locale.APPWORKUTILS;
import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.SwingUtils;
import org.appwork.utils.swing.dialog.AbstractDialog;

/**
 * @deprecated port to {@link AbstractDialog}
 * @author thomas
 * 
 */
@Deprecated
public abstract class SearchDialog extends JDialog implements WindowListener, ActionListener, FocusListener {

    private static final long    serialVersionUID = 9206575398715006581L;

    public static final int      NO_REGEX_FLAG    = 1 << 0;
    public static final int      NO_CASE_FLAG     = 1 << 1;

    private final ExtTable<?>    owner;
    private final JTextComponent input;
    private final JCheckBox      caseSensitive;
    private final JCheckBox      regularExpression;
    private final JButton        okButton;

    public SearchDialog(final int flag, final ExtTable<?> owner) throws IOException {
        super(SwingUtils.getWindowForComponent(owner), APPWORKUTILS.T.EXTTABLE_SEARCH_DIALOG_TITLE());

        this.owner = owner;
        this.owner.addFocusListener(this);

        this.caseSensitive = new JCheckBox(APPWORKUTILS.T.SEARCHDIALOG_CHECKBOX_CASESENSITIVE());
        this.regularExpression = new JCheckBox(APPWORKUTILS.T.SEARCHDIALOG_CHECKBOX_REGULAREXPRESSION());
        try {
            this.caseSensitive.setSelected(owner.getStorage().get("caseSensitive", false));
            this.regularExpression.setSelected(owner.getStorage().get("regularExpression", false));

            final ActionListener saveListener = new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    owner.getStorage().put("caseSensitive", SearchDialog.this.caseSensitive.isSelected());
                    owner.getStorage().put("regularExpression", SearchDialog.this.regularExpression.isSelected());
                }

            };

            this.caseSensitive.addActionListener(saveListener);
            this.regularExpression.addActionListener(saveListener);
        } catch (final Exception e) {
            Log.exception(e);
        }
        this.caseSensitive.setVisible(BinaryLogic.containsNone(flag, SearchDialog.NO_CASE_FLAG));
        this.regularExpression.setVisible(BinaryLogic.containsNone(flag, SearchDialog.NO_REGEX_FLAG));
        this.setLayout(new MigLayout("ins 5", "[fill,grow]", "[fill,grow][][]"));
        // Dispose dialog on close
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(this);
        this.okButton = new JButton(APPWORKUTILS.T.SEARCHDIALOG_BUTTON_FIND());
        this.okButton.addActionListener(this);

        this.add(new JLabel(AWUTheme.I().getIcon("dialog/find", 32)), "alignx left,aligny center,shrinkx,gapright 10,spany");

        this.input = new JTextField();
        this.input.setBorder(BorderFactory.createEtchedBorder());

        this.add(this.input, "pushy,growy,spanx,wrap");

        this.input.selectAll();

        this.add(this.regularExpression);
        this.add(this.caseSensitive);
        this.add(this.okButton, "skip 2,alignx right,wrap");

        // pack dialog
        this.invalidate();
        this.pack();
        this.setResizable(false);

        this.toFront();

        if (!this.getParent().isDisplayable() || !this.getParent().isVisible()) {
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            this.setLocation(new Point((int) (screenSize.getWidth() - this.getWidth()) / 2, (int) (screenSize.getHeight() - this.getHeight()) / 2));
        } else if (this.getParent() instanceof Frame && ((Frame) this.getParent()).getExtendedState() == Frame.ICONIFIED) {
            // dock dialog at bottom right if mainframe is not visible
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            this.setLocation(new Point((int) (screenSize.getWidth() - this.getWidth() - 20), (int) (screenSize.getHeight() - this.getHeight() - 60)));
        } else {
            this.setLocation(SwingUtils.getCenter(this.getParent(), this));
        }

        // register an escape listener to cancel the dialog
        KeyStroke ks = KeyStroke.getKeyStroke("ESCAPE");
        this.okButton.getInputMap().put(ks, "ESCAPE");
        this.okButton.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "ESCAPE");
        this.okButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "ESCAPE");
        this.okButton.getActionMap().put("ESCAPE", new AbstractAction() {

            private static final long serialVersionUID = -6666144330707394562L;

            public void actionPerformed(final ActionEvent e) {
                SearchDialog.this.close();
            }

        });

        ks = KeyStroke.getKeyStroke("ENTER");
        this.okButton.getInputMap().put(ks, "ENTER");
        this.okButton.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "ENTER");
        this.okButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "ENTER");
        this.okButton.getActionMap().put("ENTER", new AbstractAction() {

            private static final long serialVersionUID = -1331741306700505613L;

            public void actionPerformed(final ActionEvent e) {
                SearchDialog.this.okButton.doClick();
            }

        });

        this.setVisible(true);

        this.requestFocus();
        this.input.requestFocusInWindow();
        this.input.requestFocus();
    }

    abstract public void actionPerformed(ActionEvent e);

    private void close() {
        this.owner.removeFocusListener(this);
        this.dispose();
    }

    public void focusGained(final FocusEvent e) {
    }

    public void focusLost(final FocusEvent e) {
        if (!e.isTemporary()) {
            this.close();
        }
    }

    public String getReturnID() {
        return this.input.getText();
    }

    public boolean isCaseSensitive() {
        return this.caseSensitive.isSelected();
    }

    public boolean isRegex() {
        return this.regularExpression.isSelected();
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        this.input.requestFocusInWindow();
        this.input.requestFocus();
    }

    public void windowActivated(final WindowEvent arg0) {
    }

    public void windowClosed(final WindowEvent arg0) {
    }

    public void windowClosing(final WindowEvent arg0) {
        this.close();
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
