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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

import org.appwork.storage.ConfigInterface;
import org.appwork.utils.BinaryLogic;
import org.appwork.utils.ImageProvider.ImageProvider;
import org.appwork.utils.locale.Tl8;
import org.appwork.utils.swing.SwingUtils;
import org.appwork.utils.swing.dialog.Dialog;

public abstract class SearchDialog extends JDialog implements KeyListener, WindowListener, ActionListener, FocusListener {

    private static final long serialVersionUID = 9206575398715006581L;

    public static final int NO_REGEX_FLAG = 1 << 0;

    public static final int NO_CASE_FLAG = 1 << 1;

    private JTextComponent input;

    private JCheckBox caseSensitive;

    private JCheckBox regularExpression;

    private JButton okButton;

    private ExtTable<?> owner;

    public SearchDialog(int flag, ExtTable<?> extTable) throws IOException {
        super(Dialog.getInstance().getParentOwner());

        owner = extTable;
        setTitle(Tl8.EXTTABLE_SEARCH_DIALOG_TITLE.toString());

        this.caseSensitive = new JCheckBox(Tl8.SEARCHDIALOG_CHECKBOX_CASESENSITIVE.toString());
        this.regularExpression = new JCheckBox(Tl8.SEARCHDIALOG_CHECKBOX_REGULAREXPRESSION.toString());

        try {
            caseSensitive.setSelected(ConfigInterface.getStorage("SearchDialog_" + owner.getTableID()).get("caseSensitive", false));

            regularExpression.setSelected(ConfigInterface.getStorage("SearchDialog_" + owner.getTableID()).get("regularExpression", false));
            ActionListener saveListener = new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ConfigInterface.getStorage("SearchDialog_" + owner.getTableID()).put("caseSensitive", caseSensitive.isSelected());
                    ConfigInterface.getStorage("SearchDialog_" + owner.getTableID()).put("regularExpression", regularExpression.isSelected());

                }

            };
            caseSensitive.addActionListener(saveListener);
            regularExpression.addActionListener(saveListener);
        } catch (Exception e) {
            
            org.appwork.utils.logging.Log.exception(e);
        }
        caseSensitive.setVisible(BinaryLogic.containsNone(flag, NO_CASE_FLAG));
        regularExpression.setVisible(BinaryLogic.containsNone(flag, NO_REGEX_FLAG));
        this.setLayout(new MigLayout("ins 5", "[fill,grow]", "[fill,grow][][]"));
        // Dispose dialog on close
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(this);
        okButton = new JButton(Tl8.SEARCHDIALOG_BUTTON_FIND.toString());

        okButton.addActionListener(this);

        add(new JLabel(ImageProvider.getImageIcon("find", 32, 32)), "alignx left,aligny center,shrinkx,gapright 10,spany");

        input = new JTextField();
        input.setBorder(BorderFactory.createEtchedBorder());

        input.addKeyListener(this);

        add(input, "pushy,growy,spanx,wrap");

        input.selectAll();

        add(regularExpression, "");
        add(caseSensitive, "");
        add(okButton, "skip 2,alignx right,wrap");

        // pack dialog
        this.invalidate();
        this.pack();
        this.setResizable(false);

        this.toFront();

        // this.setSize(new Dimension(400, 200));
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

        AbstractAction enterAction = new AbstractAction() {

            /**
             * 
             */
            private static final long serialVersionUID = -1331741306700505613L;

            public void actionPerformed(ActionEvent e) {
                okButton.doClick();
            }

        };
        ks = KeyStroke.getKeyStroke("ENTER");
        okButton.getInputMap().put(ks, "ENTER");
        okButton.getInputMap(JButton.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ks, "ENTER");
        okButton.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW).put(ks, "ENTER");
        okButton.getActionMap().put("ENTER", enterAction);

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

    @Override
    public void focusGained(FocusEvent e) {
        

    }

    @Override
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

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent e) {
        

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
        

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped(KeyEvent e) {
        

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    abstract public void actionPerformed(ActionEvent e);

}
