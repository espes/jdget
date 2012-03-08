package org.appwork.storage.config.swing.models;

import java.awt.event.ItemEvent;

import javax.swing.JToggleButton.ToggleButtonModel;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.BooleanKeyHandler;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.swing.EDTRunner;

public class ConfigToggleButtonModel extends ToggleButtonModel implements GenericConfigEventListener<Boolean> {

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

    private void fireItemStateChanged() {
        fireItemStateChanged(new ItemEvent(ConfigToggleButtonModel.this, ItemEvent.ITEM_STATE_CHANGED, ConfigToggleButtonModel.this, ConfigToggleButtonModel.this.isSelected() ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
    }

    public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                fireStateChanged();
                fireItemStateChanged();
            }
        };

    }

    public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                fireStateChanged();
                fireItemStateChanged();
            }
        };

    }

}
