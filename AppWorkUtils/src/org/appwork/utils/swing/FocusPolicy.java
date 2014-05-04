package org.appwork.utils.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.ArrayList;

import javax.swing.JComponent;

public class FocusPolicy extends FocusTraversalPolicy {
    private java.util.List<JComponent> order;

    public FocusPolicy(JComponent... components) {
        this.order = new ArrayList<JComponent>(components.length);
        for (JComponent c : components) {
            order.add(c);
        }
    }

    public JComponent getComponentAfter(Container focusCycleRoot, Component aJComponent) {
        int idx = (order.indexOf(aJComponent) + 1) % order.size();
        return order.get(idx);
    }

    public JComponent getComponentBefore(Container focusCycleRoot, Component aJComponent) {
        int idx = order.indexOf(aJComponent) - 1;
        if (idx < 0) {
            idx = order.size() - 1;
        }
        return order.get(idx);
    }

    public JComponent getDefaultComponent(Container focusCycleRoot) {
        return order.get(0);
    }

    public JComponent getLastComponent(Container focusCycleRoot) {
        return order.get(order.size() - 1);
    }

    public JComponent getFirstComponent(Container focusCycleRoot) {
        return order.get(0);
    }

}
