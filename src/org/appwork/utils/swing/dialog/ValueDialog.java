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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.appwork.utils.BinaryLogic;
import org.appwork.utils.interfaces.ValueConverter;
import org.appwork.utils.logging.Log;

public class ValueDialog extends AbstractDialog<Long> implements KeyListener, MouseListener {

    private JTextArea            converted;
    private final long           defaultValue;
    private JTextField           editable;
    // faktor to downscale long to integervalues
    private int                  faktor           = 1;
    private final long           max;
    private final String         message;
    private JTextPane            messageArea;
    private final long           min;
    private JSlider              slider;
    private final long           step;
    private final ValueConverter valueconverter;

    public ValueDialog(final int flag, final String title, final String message, final ImageIcon icon, final String okOption, final String cancelOption, long defaultValue, long min, long max, long step, ValueConverter valueConverter) {
        super(flag, title, icon, okOption, cancelOption);

        Log.L.fine("Dialog    [" + okOption + "][" + cancelOption + "]\r\nflag:  " + Integer.toBinaryString(flag) + "\r\ntitle: " + title + "\r\nmsg:   \r\n" + message + " \r\n" + min + "<=" + defaultValue + "<=" + max + " [" + step + "]");

        this.message = message;
        while (max > Integer.MAX_VALUE) {
            max /= 2;
            defaultValue /= 2;
            min /= 2;
            step = Math.max(step / 2, 1);
            this.faktor *= 2;

        }
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.step = step;
        if (valueConverter == null) {
            valueConverter = new ValueConverter() {

                public String toString(final long value) {

                    return value * ValueDialog.this.faktor + "";
                }

            };
        }
        this.valueconverter = valueConverter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.utils.swing.dialog.AbstractDialog#getRetValue()
     */
    @Override
    protected Long createReturnValue() {
        if ((this.getReturnmask() & (Dialog.RETURN_OK | Dialog.RETURN_TIMEOUT)) == 0) { return 0l; }
        this.updateSlider();
        return (long) this.slider.getValue() * (long) this.faktor;
    }

    public void keyPressed(final KeyEvent e) {
        this.cancel();
    }

    public void keyReleased(final KeyEvent e) {
    }

    public void keyTyped(final KeyEvent e) {
    }

    @Override
    public JComponent layoutDialogContent() {
        final JPanel contentpane = new JPanel(new MigLayout("ins 0,wrap 1", "[fill,grow]"));
        this.messageArea = new JTextPane();
        this.messageArea.setBorder(null);
        this.messageArea.setBackground(null);
        this.messageArea.setOpaque(false);
        this.messageArea.setText(this.message);
        this.messageArea.setEditable(false);
        this.messageArea.putClientProperty("Synthetica.opaque", Boolean.FALSE);

        contentpane.add(this.messageArea);
        if (BinaryLogic.containsAll(this.flagMask, Dialog.STYLE_LARGE)) {
            this.converted = new JTextArea(this.valueconverter.toString(this.defaultValue));
            this.converted.setEditable(false);
            this.converted.setBackground(null);
            this.slider = new JSlider(SwingConstants.HORIZONTAL, (int) this.min, (int) this.max, (int) this.defaultValue);
            this.slider.setMajorTickSpacing((int) this.step);
            this.slider.setSnapToTicks(true);
            this.slider.addKeyListener(this);
            this.slider.addMouseListener(this);
            this.editable = new JTextField();
            this.editable.addFocusListener(new FocusListener() {

                public void focusGained(final FocusEvent e) {

                }

                public void focusLost(final FocusEvent e) {
                    ValueDialog.this.updateSlider();

                }

            });
            this.editable.addKeyListener(new KeyListener() {

                public void keyPressed(final KeyEvent e) {

                }

                public void keyReleased(final KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        ValueDialog.this.updateSlider();
                    }

                }

                public void keyTyped(final KeyEvent e) {

                }

            });
            this.slider.addChangeListener(new ChangeListener() {

                public void stateChanged(final ChangeEvent arg0) {
                    ValueDialog.this.converted.setText(ValueDialog.this.valueconverter.toString((ValueDialog.this.slider.getValue() * ValueDialog.this.faktor)));
                    ValueDialog.this.editable.setText(ValueDialog.this.slider.getValue() * ValueDialog.this.faktor + "");
                }

            });
            this.editable.setText(this.defaultValue + "");
            contentpane.add(this.slider, "split 2,pushy,growy,w 250");
            contentpane.add(this.editable, "growx,pushx,width 80:n:n");
            contentpane.add(this.converted, "pushy,growy,w 250");
        } else {
            this.converted = new JTextArea(this.valueconverter.toString(this.defaultValue));
            this.slider = new JSlider(SwingConstants.HORIZONTAL, (int) this.min, (int) this.max, (int) this.defaultValue);
            this.slider.setMajorTickSpacing((int) this.step);
            this.slider.setSnapToTicks(true);
            this.slider.setBorder(BorderFactory.createEtchedBorder());
            this.slider.addKeyListener(this);
            this.slider.addMouseListener(this);
            this.slider.addChangeListener(new ChangeListener() {

                public void stateChanged(final ChangeEvent arg0) {
                    ValueDialog.this.converted.setText(ValueDialog.this.valueconverter.toString((ValueDialog.this.slider.getValue() * ValueDialog.this.faktor)));
                    ValueDialog.this.editable.setText(ValueDialog.this.slider.getValue() * ValueDialog.this.faktor + "");
                }

            });

            contentpane.add(this.slider, "pushy,growy,w 250");
            contentpane.add(this.converted, "pushy,growy,w 250");
        }

        return contentpane;
    }

    public void mouseClicked(final MouseEvent e) {
        this.cancel();
    }

    public void mouseEntered(final MouseEvent e) {
    }

    public void mouseExited(final MouseEvent e) {
    }

    public void mousePressed(final MouseEvent e) {
    }

    public void mouseReleased(final MouseEvent e) {
    }

    @Override
    protected void packed() {
        this.requestFocus();
        this.slider.requestFocusInWindow();
    }

    private void updateSlider() {
        // new Thread() {
        // public void run() {
        // new EDTHelper<Object>() {
        //
        // 
        // public Object edtRun() {
        try {
            final long value = Long.parseLong(this.editable.getText());
            this.slider.setValue((int) (value / this.faktor));
        } catch (final Exception e) {
            if (this.editable != null) {
                this.editable.setText(this.slider.getValue() * this.faktor + "");
            }
        }
        // return null;
        // }
        //
        // }.start();
        //
        // }
        // }.start();

    }

}
