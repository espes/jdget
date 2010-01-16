package org.appwork.utils.swing;

import javax.swing.SpinnerNumberModel;

public class SpinnerFilesizeModel extends SpinnerNumberModel {

    /**
     * A spinnermodel for filesizes. The stes
     */
    private static final long serialVersionUID = 8892296746389046229L;

    /**
     * @param i
     * @param j
     * @param k
     */
    public SpinnerFilesizeModel(int value, int minimum, int maximum) {
        super(value, minimum, maximum, 1);
    }

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
        if (value < 10 * 1024) {
            this.setStepSize(step = 1024);
        } else {
            int base = (int) Math.pow(1024, (int) (Math.log(value) / Math.log(1024)));
            step = value / base;
            step = (int) (Math.log(step) / Math.log(10));
            step = ((int) Math.pow(10, step) / 10);
            step = Math.max(step * base, base / 10);
            this.setStepSize(step);
        }
        return step;
    }

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
