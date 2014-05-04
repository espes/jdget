package org.appwork.storage.config.swing.models;

import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.annotations.SpinnerValidator;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.ByteKeyHandler;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.swing.EDTRunner;

public class ConfigByteSpinnerModel extends SpinnerNumberModel implements GenericConfigEventListener<Byte> {

    /**
     * 
     */
    private static final long serialVersionUID = 8542048212034642953L;
    private ByteKeyHandler keyHandler;

    public ConfigByteSpinnerModel(ByteKeyHandler keyHandler) {
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
        super.setMinimum(((Number) minimum).byteValue());

    }

    @Override
    public void setMaximum(Comparable maximum) {

        super.setMaximum(((Number) maximum).byteValue());

    }

    @Override
    public void setStepSize(Number stepSize) {

        super.setStepSize(stepSize.byteValue());

    }

    @Override
    public Number getNumber() {

        return (Byte) keyHandler.getValue();

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

        return ((Byte) getValue()).byteValue() + getStepSize().byteValue() * i;

    }

    @Override
    public Object getValue() {

        return keyHandler.getValue();
    }

    @Override
    public void setValue(Object value) {
        try {
        keyHandler.setValue(((Number) value).byteValue());
        }catch(ValidationException e){           
            java.awt.Toolkit.getDefaultToolkit().beep();          
     
        }
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.events.GenericConfigEventListener#onConfigValidatorError(org.appwork.storage.config.handler.KeyHandler, java.lang.Throwable)
     */
    @Override
    public void onConfigValidatorError(KeyHandler<Byte> keyHandler, Byte invalidValue, ValidationException validateException) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                fireStateChanged();
            }
        };
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.events.GenericConfigEventListener#onConfigValueModified(org.appwork.storage.config.handler.KeyHandler, java.lang.Object)
     */
    @Override
    public void onConfigValueModified(KeyHandler<Byte> keyHandler, Byte newValue) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                fireStateChanged();
            }
        };
        
    }


}
