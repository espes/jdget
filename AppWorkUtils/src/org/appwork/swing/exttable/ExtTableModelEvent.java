package org.appwork.swing.exttable;

import org.appwork.utils.event.SimpleEvent;

public abstract class ExtTableModelEvent extends SimpleEvent<Object, Object, ExtTableModelEvent.Type> {

    public static enum Type {
        NATIVE_EVENT
    }

    public ExtTableModelEvent(final Object caller, final Type type, final Object... parameters) {
        super(caller, type, parameters);
    }

    /**
     * @param listener
     */
    abstract public void fire(ExtTableModelListener listener);
}