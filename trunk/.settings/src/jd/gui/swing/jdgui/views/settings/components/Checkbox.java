package jd.gui.swing.jdgui.views.settings.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.appwork.storage.config.handler.BooleanKeyHandler;
import org.appwork.swing.components.ExtCheckBox;

public class Checkbox extends ExtCheckBox implements SettingsComponent {

    private static final long                serialVersionUID = -4399955553684242527L;
    private StateUpdateEventSender<Checkbox> eventSender;
    private boolean                          setting;

    public Checkbox(BooleanKeyHandler keyHandler) {
        super(keyHandler);
        eventSender = new StateUpdateEventSender<Checkbox>();
        this.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!setting) eventSender.fireEvent(new StateUpdateEvent<Checkbox>(Checkbox.this));
            }
        });
    }

    public Checkbox(boolean selected) {
        super();
        this.setSelected(selected);
        eventSender = new StateUpdateEventSender<Checkbox>();
        this.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!setting) eventSender.fireEvent(new StateUpdateEvent<Checkbox>(Checkbox.this));
            }
        });
    }

    public void addStateUpdateListener(StateUpdateListener listener) {
        eventSender.addListener(listener);

    }

    @Override
    public void setSelected(boolean b) {
        try {
            setting = true;
            super.setSelected(b);

        } finally {
            setting = false;
        }

    }

    public Checkbox() {
        this(false);
    }

    public String getConstraints() {
        return null;
    }

    public boolean isMultiline() {
        return false;
    }

}
