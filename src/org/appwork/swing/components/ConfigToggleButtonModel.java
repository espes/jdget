package org.appwork.swing.components;

import javax.swing.JToggleButton.ToggleButtonModel;

import org.appwork.storage.config.ConfigEventListener;
import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.storage.config.handler.StorageHandler;

public class ConfigToggleButtonModel extends ToggleButtonModel implements ConfigEventListener {

    /**
     * 
     */
    private static final long serialVersionUID = -3517910678740645735L;

    private KeyHandler        keyHandler;

    private StorageHandler<?> sh;

    public ConfigToggleButtonModel(Class<? extends ConfigInterface> class1, String key) {
        ConfigInterface config = JsonConfig.create(class1);
        sh = config.getStorageHandler();
        sh.getEventSender().addListener(this, true);
        keyHandler = config.getStorageHandler().getKeyHandler(key);
        if (keyHandler == null) throw new NullPointerException("Key " + key + " is invalid for " + class1);
    }

    public boolean isSelected() {
        return (Boolean) sh.getValue(keyHandler);
    }

    public void setSelected(boolean b) {
        sh.setValue(keyHandler, b);

    }

    public void onConfigValidatorError(Class<? extends ConfigInterface> config, Throwable validateException, KeyHandler methodHandler) {
        fireStateChanged();
    }

    public void onConfigValueModified(Class<? extends ConfigInterface> config, String key, Object newValue) {
        fireStateChanged();
    }

}
