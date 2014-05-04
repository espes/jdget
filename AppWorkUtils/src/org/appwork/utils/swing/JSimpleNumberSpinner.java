package org.appwork.utils.swing;

import java.text.ParseException;

import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatterFactory;

public class JSimpleNumberSpinner extends JSpinner {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param i
     * @param j
     * @param k
     */
    public JSimpleNumberSpinner(final int steps, final int min, final int max, final int secondmin) {
        super(new SpinnerNumberModel(min, min, max, steps) {

            /**
             * 
             */
            private static final long serialVersionUID = -5666000802809450936L;

            @Override
            public Object getNextValue() {
                Number n = getNumber();
                if (n.intValue() >= min && n.intValue() < secondmin) return secondmin;
                return super.getNextValue();
            }

            @Override
            public Object getPreviousValue() {
                Number n = getNumber();
                if (n.intValue() >= min && n.intValue() <= secondmin) return min;
                return super.getPreviousValue();
            }

        });

        final DefaultFormatterFactory factory = new DefaultFormatterFactory(new AbstractFormatter() {

            /**
             * 
             */
            private static final long serialVersionUID = 3244976028578192576L;

            @Override
            public Object stringToValue(final String text) throws ParseException {
                try {
                    int i = Integer.parseInt(text);
                    if (i > min && i < secondmin) i = min;
                    if (i < min) i = min;
                    if (i > max) i = max;
                    return i;
                } catch (Throwable e) {
                    return null;
                }

            }

            @Override
            public String valueToString(final Object value) throws ParseException {
                return value + "";

            }

        });
        ((JSpinner.DefaultEditor) this.getEditor()).getTextField().setFormatterFactory(factory);
    }

}
