package org.appwork.swing.event;

import java.awt.Component;
import java.util.EventListener;

public interface PropertySetListener extends EventListener {

    void onPropertySet(Component caller, String propertyName, Object oldValue, Object newValue);

}