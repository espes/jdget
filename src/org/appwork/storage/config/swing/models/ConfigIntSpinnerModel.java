package org.appwork.storage.config.swing.models;

import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.annotations.SpinnerValidator;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.IntegerKeyHandler;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.swing.EDTRunner;

public class ConfigIntSpinnerModel extends SpinnerNumberModel implements GenericConfigEventListener<Integer> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private IntegerKeyHandler keyHandler;

    public ConfigIntSpinnerModel(final IntegerKeyHandler keyHandler) {
        super();

        this.keyHandler = keyHandler;
        // keyHandler.getEventSender().removeListener(this);
        keyHandler.getEventSender().addListener(this, true);

        final SpinnerValidator spinn = keyHandler.getAnnotation(SpinnerValidator.class);
        if (spinn != null) {
            this.setMinimum(spinn.min());
            this.setMaximum(spinn.max());
            this.setStepSize(spinn.step());
        }
    }

    public IntegerKeyHandler getKeyHandler() {
        return this.keyHandler;
    }

    @Override
    public void setMinimum(final Comparable minimum) {
        super.setMinimum(((Number) minimum).intValue());

    }

    @Override
    public void setMaximum(final Comparable maximum) {

        super.setMaximum(((Number) maximum).intValue());

    }

    @Override
    public void setStepSize(final Number stepSize) {

        super.setStepSize(stepSize.intValue());

    }

    @Override
    public Number getNumber() {

        return this.keyHandler.getValue();

    }

    /**
     * Returns the next number in the sequence.
     * 
     * @return <code>value + stepSize</code> or <code>null</code> if the sum
     *         exceeds <code>maximum</code>.
     * 
     * @see SpinnerModel#getNextValue
     * @see #getPreviousValue
     * @see #setStepSize
     */
    @Override
    public Object getNextValue() {
        return this.incrValue(+1);
    }

    @Override
    public Object getPreviousValue() {
        return this.incrValue(-1);
    }

    protected Number incrValue(final int i) {
        return ((Integer) this.getValue()).intValue() + this.getStepSize().intValue() * i;
    }

    @Override
    public Object getValue() {

        return this.keyHandler.getValue();
    }

    @Override
    public void setValue(final Object value) {
        try {

            this.keyHandler.setValue(((Number) value).intValue());
        } catch (final ValidationException e) {
            java.awt.Toolkit.getDefaultToolkit().beep();

        }
    }

    public void onConfigValidatorError(final KeyHandler<Integer> keyHandler, final Integer invalidValue, final ValidationException validateException) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                ConfigIntSpinnerModel.this.fireStateChanged();
            }
        };
    }

    public void onConfigValueModified(final KeyHandler<Integer> keyHandler, final Integer newValue) {

        new EDTRunner() {
            @Override
            protected void runInEDT() {
                ConfigIntSpinnerModel.this.fireStateChanged();
            }
        };
    }

}
