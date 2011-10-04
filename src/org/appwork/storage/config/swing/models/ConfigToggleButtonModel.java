package org.appwork.storage.config.swing.models;

import javax.swing.JToggleButton.ToggleButtonModel;

import org.appwork.storage.config.events.ConfigEventListener;
import org.appwork.storage.config.handler.BooleanKeyHandler;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.swing.EDTRunner;

public class ConfigToggleButtonModel extends ToggleButtonModel implements ConfigEventListener {

    /**
     * 
     */
    private static final long serialVersionUID = -3517910678740645735L;
    private BooleanKeyHandler keyHandler;

    public ConfigToggleButtonModel(BooleanKeyHandler keyHandler) {

        this.keyHandler = keyHandler;
        keyHandler.getEventSender().addListener(this, true);

    }

    public boolean isSelected() {
        return keyHandler.getValue();
    }

    public void setSelected(boolean b) {
        keyHandler.setValue(b);

    }

    public void onConfigValidatorError(KeyHandler<?> keyHandler, Throwable validateException) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                fireStateChanged();
            }
        };

    }

    public void onConfigValueModified(KeyHandler<?> keyHandler, Object newValue) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                fireStateChanged();
            }
        };

    }

}
