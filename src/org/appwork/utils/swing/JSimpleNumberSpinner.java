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
    public JSimpleNumberSpinner(final int steps, final int min, final int max) {
        super(new SpinnerNumberModel(min, min, max, steps));

        final DefaultFormatterFactory factory = new DefaultFormatterFactory(new AbstractFormatter() {

            /**
             * 
             */
            private static final long serialVersionUID = 3244976028578192576L;

            @Override
            public Object stringToValue(final String text) throws ParseException {

                return Integer.parseInt(text);

            }

            @Override
            public String valueToString(final Object value) throws ParseException {

                return value + "";

            }

        });
        ((JSpinner.DefaultEditor) this.getEditor()).getTextField().setFormatterFactory(factory);
    }

}
