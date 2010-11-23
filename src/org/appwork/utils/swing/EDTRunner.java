package org.appwork.utils.swing;

/**
 * A non-generic and asynch brother of EDTHelper. Simpflifies usage if no return
 * value is required
 * 
 * @author thomas
 * 
 */
public abstract class EDTRunner extends EDTHelper<Object>{

    public EDTRunner() {
        this.start();
    }

    @Override
    public Object edtRun() {
        this.runInEDT();
        return null;
    }

    /**
     * 
     */
    abstract protected void runInEDT();

}
