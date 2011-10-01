package org.appwork.swing.components;

import javax.swing.JToggleButton.ToggleButtonModel;

import org.appwork.storage.config.ConfigEventListener;
import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.KeyHandler;
import org.appwork.utils.logging.Log;

public class ConfigToggleButtonModel extends ToggleButtonModel implements ConfigEventListener {

    /**
     * 
     */
    private static final long serialVersionUID = -3517910678740645735L;

    private KeyHandler        keyHandler;

    public ConfigToggleButtonModel(Class<? extends ConfigInterface> class1, String key) {
        ConfigInterface config = JsonConfig.create(class1);

        config.getStorageHandler().getEventSender().addListener(this, true);
        keyHandler = config.getStorageHandler().getKeyHandler(key);
        if(keyHandler==null)throw new NullPointerException("Key "+key+" is invalid for "+class1);
    }

    public boolean isSelected() {
        return (Boolean) keyHandler.getValue();
    }

    public void setSelected(boolean b) {
        try {
            keyHandler.setValue(b);
        } catch (Throwable e) {
            Log.exception(e);
        }
    }

    public void onConfigValidatorError(ConfigInterface config, Throwable validateException, KeyHandler methodHandler) {
        fireStateChanged();
    }

    public void onConfigValueModified(ConfigInterface config, String key, Object newValue) {
        fireStateChanged();
    }

}
