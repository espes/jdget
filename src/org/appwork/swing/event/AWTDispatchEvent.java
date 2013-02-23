package org.appwork.swing.event;

import org.appwork.utils.event.SimpleEvent;

public class AWTDispatchEvent extends SimpleEvent<Object, Object, AWTDispatchEvent.Type> {

    public static enum Type {
        PRE_DISPATCH,
        POST_DISPATCH
    }

    public AWTDispatchEvent(Object caller, Type type, Object... parameters) {
        super(caller, type, parameters);
    }
}