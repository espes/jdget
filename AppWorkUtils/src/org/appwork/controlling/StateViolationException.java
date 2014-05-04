package org.appwork.controlling;


public class StateViolationException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public StateViolationException(final State downloadingHashlist) {
        super(downloadingHashlist.toString());

    }

}
