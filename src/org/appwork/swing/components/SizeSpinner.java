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

    private SpinnerNumberModel nm;

    public void setModel(SpinnerModel model) {
        throw new IllegalStateException("Not available");
    }

    public SizeSpinner(long min, long max, long steps) {
        this(new SpinnerNumberModel(min, min, max, steps));

    }

    /**
     * @param model
     */
    public SizeSpinner(SpinnerNumberModel model) {
        super(model);

        // this.addFocusListener(this);
        nm = (SpinnerNumberModel) super.getModel();

        final DefaultFormatterFactory factory = new DefaultFormatterFactory(new AbstractFormatter() {

            private static final long serialVersionUID = 7808117078307243989L;

            @Override
            public Object stringToValue(String text) throws ParseException {
                return SizeFormatter.getSize(text,true,true);
            }

            @Override
            public String valueToString(final Object value) throws ParseException {

                return longToText(((Number) value).longValue());
            }

        });
        ((JSpinner.DefaultEditor) getEditor()).getTextField().setFormatterFactory(factory);
        ((JSpinner.DefaultEditor) getEditor()).getTextField().addFocusListener(this);
        ((JSpinner.DefaultEditor) getEditor()).getTextField().addActionListener(this);
    }

    /**
     * @param longValue
     * @return
     */
    protected String longToText(long longValue) {

        return SizeFormatter.formatBytes(longValue);
    }

    public Object getNextValue() {
        Object ret = getValue();
        long num = ((Number) ret).longValue();

        Unit unit = SizeFormatter.getBestUnit(num);

        int c = (int) (num == 0 ? 0 : Math.log10(num / unit.getBytes()));
        c = Math.max(0, c - 1);
        long newV;
        if (nm.getMaximum() != null) {
            newV = (long) Math.min(((Number) nm.getMaximum()).longValue(), num + unit.getBytes() * Math.pow(10, c));
        } else {
            newV = (long) (num + unit.getBytes() * Math.pow(10, c));
        }
        Unit newUnit = SizeFormatter.getBestUnit((long) newV);
        if (newUnit == unit) {
            if (newV == num) {
                beep();
            }
            return newV;
        }

        newV = (int) (newV / newUnit.getBytes()) * newUnit.getBytes();
        if (newV == num) {
            beep();
        }
        return newV;

    }

    public Object getPreviousValue() {
        Object ret = getValue();
        long num = ((Number) ret).longValue();
        Unit unit = SizeFormatter.getBestUnit(num);
        int c = (int) (num == 0 ? 0 : Math.log10(num / unit.getBytes()));
        c = Math.max(0, c - 1);
        long nv;
        if (nm.getMinimum() != null) {
            nv = (long) Math.max(((Number) nm.getMinimum()).longValue(), num - unit.getBytes() * Math.pow(10, c));
        } else {
            nv = (long) (num - unit.getBytes() * Math.pow(10, c));
        }
        Unit nunit = SizeFormatter.getBestUnit(nv);
        if (nunit == unit) {
            if (nv == num) {
                beep();
            }
            return nv;
        }

        nv = Math.max(((Number) nm.getMinimum()).longValue(), num - unit.getBytes() / 1024);

        if (nv == num) {
            beep();
        }
        return nv;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    @Override
    public void focusGained(FocusEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    @Override
    public void focusLost(FocusEvent e) {
        correct();

    }

    /**
     * 
     */
    private void correct() {

        long v = ((Number) getValue()).longValue();
        long newValue = v;
        if (nm.getMinimum() != null) {
            newValue = Math.max(v, ((Number) nm.getMinimum()).longValue());
        }
        if (nm.getMaximum() != null) {
            newValue = Math.min(((Number) nm.getMaximum()).longValue(), newValue);
        }
        if (newValue != v) {
            beep();
            setValue(newValue);
        }
    }

    /**
     * 
     */
    private void beep() {
        Toolkit.getDefaultToolkit().beep();
        final Color bg = ((JSpinner.DefaultEditor) getEditor()).getTextField().getForeground();
        ((JSpinner.DefaultEditor) getEditor()).getTextField().setForeground(Color.RED);

        new Timer(100, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ((JSpinner.DefaultEditor) getEditor()).getTextField().setForeground(bg);

            }
        }).start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        correct();

    }

    /**
     * @return
     */
    public long getBytes() {

        return ((Number) getValue()).longValue();
    }

}
