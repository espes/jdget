package org.appwork.utils.swing;

import java.awt.CardLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class JSwitchPanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final CardLayout  layout;

    public JSwitchPanel() {
        super();
        this.layout = new CardLayout();
        this.setLayout(this.layout);
    }

    @Override
    public Component add(final Component comp) {
        if (comp.getName() == null || comp.getName().length() == 0) { throw new IllegalArgumentException(comp + " has no name"); }
        for (final Component c : this.getComponents()) {
            if (comp.getName().equals(c.getName())) { throw new IllegalArgumentException("Duplicate component name: " + comp.getName()); }
        }
        super.add(comp, comp.getName());
        return comp;
    }

    /**
     * @param splitPane
     */
    public void setView(final JComponent splitPane) {
        this.setView(splitPane.getName());

    }

    public void setView(final String splitPane) {
        this.layout.show(this, splitPane);

    }
}
