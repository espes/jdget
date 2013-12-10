package org.jdownloader.myjdownloader.client.eventsender;


/**
 * @author thomas Callerclass,Parameterclass,Eventclass
 */
public class SimpleEvent<C, P, T> extends DefaultEvent {

    private P[]     parameters;
    private final T type;
    private final C callerImpl;

    /**
     * @param caller
     * @param newState
     * @param currentState
     * @param id
     */
    public SimpleEvent(final C caller, final T type, final P... parameters) {
        super(caller);
        this.callerImpl = caller;
        this.type = type;
        this.parameters = parameters;

    }

    @Override
    public C getCaller() {
        return this.callerImpl;
    }

    public P getParameter() {
        if (this.parameters.length == 0) { return null; }
        return this.parameters[0];
    }

    public P getParameter(final int i) {
        if (i < 0 || this.parameters.length == 0 || i > this.parameters.length - 1) { return null; }
        return this.parameters[i];
    }

    /**
     * @return the parameters
     */
    public P[] getParameters() {
        return this.parameters;
    }

    /**
     * @return the type
     */
    public T getType() {
        return this.type;
    }

    /**
     * @param parameters
     *            the parameters to set
     */
    public void setParameters(final P[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return (this.getCaller() == null ? "null" : this.getCaller().getClass().getSimpleName()) + "'s " + this.getClass().getSimpleName() + " | " + this.type + " (" + this.parameters + ")";
    }

}
