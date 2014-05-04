package org.appwork.utils.swing;

import javax.swing.SpinnerNumberModel;

/**
 * A spinnermodel for filesizes.
 */
public class SpinnerFilesizeModel extends SpinnerNumberModel {

    private static final long serialVersionUID = 8892296746389046229L;

    public SpinnerFilesizeModel(int value, int minimum, int maximum) {
        super(value, minimum, maximum, 1);
    }

    @Override
    public Object getNextValue() {
        try {
            int step = getStep();
            Integer ret = (Integer) super.getNextValue() / step;
            return ret * step;
        } catch (Exception e) {
            return null;
        }
    }

    private int getStep() {
        Integer value = (Integer) this.getValue();
        int step;
        if (value < 10 * 1000) {
            step = 1000;
        } else {
            int base = (int) Math.pow(1000, (int) (Math.log(value) / Math.log(1000)));
            step = value / base;
            step = (int) (Math.log(step) / Math.log(10));
            step = (int) Math.pow(10, step) / 10;
            step = Math.max(step * base, base / 10);
        }
        this.setStepSize(step);
        return step;
    }

    @Override
    public Object getPreviousValue() {
        try {
            int step = getStep();
            Integer ret = (Integer) super.getPreviousValue() / step;
            return ret * step;
        } catch (Exception e) {
            return null;
        }
    }

}
