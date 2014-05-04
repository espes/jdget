package org.appwork.swing.components;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;

import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.text.DefaultFormatterFactory;

import org.appwork.utils.formatter.SizeFormatter;
import org.appwork.utils.formatter.SizeFormatter.Unit;

public class SizeSpinner extends ExtSpinner implements FocusListener, ActionListener {

    /**
     * 
     */
    private static final long  serialVersionUID = -3983659343629867162L;
    private SpinnerNumberModel nm;

    public SizeSpinner(final long min, final long max, final long steps) {
        this(new SpinnerNumberModel(min, min, max, steps));

    }

    /**
     * @param model
     */
    public SizeSpinner(final SpinnerNumberModel model) {
        super(model);

        // this.addFocusListener(this);
        this.nm = (SpinnerNumberModel) super.getModel();

        final DefaultFormatterFactory factory = new DefaultFormatterFactory(new AbstractFormatter() {

            private static final long serialVersionUID = 7808117078307243989L;

            @Override
            public Object stringToValue(final String text) throws ParseException {
                return SizeSpinner.this.textToObject(text);
            }

            @Override
            public String valueToString(final Object value) throws ParseException {

                return SizeSpinner.this.longToText(((Number) value).longValue());
            }

        });
        ((JSpinner.DefaultEditor) this.getEditor()).getTextField().setFormatterFactory(factory);
        ((JSpinner.DefaultEditor) this.getEditor()).getTextField().addFocusListener(this);
        ((JSpinner.DefaultEditor) this.getEditor()).getTextField().addActionListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        this.correct();

    }

    /**
     * 
     */
    private void beep() {
        Toolkit.getDefaultToolkit().beep();
        final Color bg = ((JSpinner.DefaultEditor) this.getEditor()).getTextField().getForeground();
        ((JSpinner.DefaultEditor) this.getEditor()).getTextField().setForeground(Color.RED);

        new Timer(100, new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                ((JSpinner.DefaultEditor) SizeSpinner.this.getEditor()).getTextField().setForeground(bg);

            }
        }).start();
    }

    /**
     * 
     */
    private void correct() {

        final long v = ((Number) this.getValue()).longValue();
        long newValue = v;
        if (this.nm.getMinimum() != null) {
            newValue = Math.max(v, ((Number) this.nm.getMinimum()).longValue());
        }
        if (this.nm.getMaximum() != null) {
            newValue = Math.min(((Number) this.nm.getMaximum()).longValue(), newValue);
        }
        if (newValue != v) {
            this.beep();
            this.setValue(newValue);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    @Override
    public void focusGained(final FocusEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    @Override
    public void focusLost(final FocusEvent e) {
        this.correct();

    }

    /**
     * @return
     */
    public long getBytes() {

        return ((Number) this.getValue()).longValue();
    }

    @Override
    public Object getNextValue() {
        final Object ret = this.getValue();
        final long num = ((Number) ret).longValue();

        final Unit unit = SizeFormatter.getBestUnit(num);

        int c = (int) (num == 0 ? 0 : Math.log10(num / unit.getBytes()));
        c = Math.max(0, c - 1);
        long newV;
        if (this.nm.getMaximum() != null) {
            newV = (long) Math.min(((Number) this.nm.getMaximum()).longValue(), num + unit.getBytes() * Math.pow(10, c));
        } else {
            newV = (long) (num + unit.getBytes() * Math.pow(10, c));
        }
        final Unit newUnit = SizeFormatter.getBestUnit(newV);
        if (newUnit == unit) {
            if (newV == num) {
                this.beep();
            }
            return newV;
        }

        newV = (int) (newV / newUnit.getBytes()) * newUnit.getBytes();
        if (newV == num) {
            this.beep();
        }
        return newV;

    }

    @Override
    public Object getPreviousValue() {
        final Object ret = this.getValue();
        final long num = ((Number) ret).longValue();
        final Unit unit = SizeFormatter.getBestUnit(num);
        int c = (int) (num == 0 ? 0 : Math.log10(num / unit.getBytes()));
        c = Math.max(0, c - 1);
        long nv;
        if (this.nm.getMinimum() != null) {
            nv = (long) Math.max(((Number) this.nm.getMinimum()).longValue(), num - unit.getBytes() * Math.pow(10, c));
        } else {
            nv = (long) (num - unit.getBytes() * Math.pow(10, c));
        }
        final Unit nunit = SizeFormatter.getBestUnit(nv);
        if (nunit == unit) {
            if (nv == num) {
                this.beep();
            }
            return nv;
        }

        nv = Math.max(((Number) this.nm.getMinimum()).longValue(), num - unit.getBytes() / 1024);

        if (nv == num) {
            this.beep();
        }
        return nv;
    }

    /**
     * @param longValue
     * @return
     */
    protected String longToText(final long longValue) {
        return SizeFormatter.formatBytes(longValue);
    }

    @Override
    public void setModel(final SpinnerModel model) {
        throw new IllegalStateException("Not available");
    }

    protected Object textToObject(final String text) {
        return SizeFormatter.getSize(text, true, true);
    }

}
