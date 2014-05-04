package org.appwork.swing.event;

import org.appwork.utils.event.SimpleEvent;

public class PropertySetEvent extends SimpleEvent<Object, Object, PropertySetEvent.Type> {

public static enum Type{
    SET
}

public PropertySetEvent(final Object caller, final Type type, final Object... parameters) {
super(caller, type, parameters);
}
}