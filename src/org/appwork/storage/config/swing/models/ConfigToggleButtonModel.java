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

    public ConfigToggleButtonModel(final BooleanKeyHandler keyHandler) {

        this.keyHandler = keyHandler;
        keyHandler.getEventSender().addListener(this, true);

    }

    public BooleanKeyHandler getKeyHandler() {
        return keyHandler;
    }

    public boolean isSelected() {
        return keyHandler.getValue();
    }

    public void setSelected(final boolean b) {
        keyHandler.setValue(b);

    }

    private void fireItemStateChanged() {
        fireItemStateChanged(new ItemEvent(ConfigToggleButtonModel.this, ItemEvent.ITEM_STATE_CHANGED, ConfigToggleButtonModel.this, ConfigToggleButtonModel.this.isSelected() ? ItemEvent.SELECTED : ItemEvent.DESELECTED));
    }

    public void onConfigValidatorError(final KeyHandler<Boolean> keyHandler, final Boolean invalidValue, final ValidationException validateException) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                fireStateChanged();
                fireItemStateChanged();
            }
        };

    }

    public void onConfigValueModified(final KeyHandler<Boolean> keyHandler, final Boolean newValue) {
        new EDTRunner() {

            @Override
            protected void runInEDT() {
                fireStateChanged();
                fireItemStateChanged();
            }
        };

    }

}
