package org.appwork.storage.config.swing.models;

import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.appwork.storage.config.annotations.SpinnerValidator;
import org.appwork.storage.config.events.ConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.storage.config.handler.LongKeyHandler;
import org.appwork.utils.swing.EDTRunner;

public class ConfigLongSpinnerModel extends SpinnerNumberModel implements ConfigEventListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private LongKeyHandler keyHandler;

    public ConfigLongSpinnerModel(LongKeyHandler keyHandler) {
        this.keyHandler = keyHandler;
        keyHandler.getEventSender().addListener(this, true);

        SpinnerValidator spinn = keyHandler.getAnnotation(SpinnerValidator.class);
        if (spinn != null) {
            setMinimum(spinn.min());
            setMaximum(spinn.max());
            setStepSize(spinn.step());
        }
    }

    @Override
    public void setMinimum(Comparable minimum) {
        super.setMinimum(((Number) minimum).longValue());

    }

    @Override
    public void setMaximum(Comparable maximum) {

        super.setMaximum(((Number) maximum).longValue());

    }

    @Override
    public void setStepSize(Number stepSize) {

        super.setStepSize(stepSize.longValue());

    }

    @Override
    public Number getNumber() {

        return (Long) keyHandler.getValue();

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
    public Object getNextValue() {
        return incrValue(+1);
    }

    public Object getPreviousValue() {
        return incrValue(-1);
    }

    protected Number incrValue(int i) {

        return ((Long) getValue()).longValue() + getStepSize().longValue() * i;

    }

    @Override
    public Object getValue() {

        return keyHandler.getValue();
    }

    @Override
    public void setValue(Object value) {
        keyHandler.setValue(((Number) value).longValue());

    }

    public void onConfigValidatorError(KeyHandler<?> keyHandler, Throwable validateException) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                fireStateChanged();
            }
        };
    }

    public void onConfigValueModified(KeyHandler<?> keyHandler, Object newValue) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                fireStateChanged();
            }
        };
    }

}
