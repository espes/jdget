package org.appwork.storage.config.listeners;

import java.util.HashMap;
import java.util.Locale;

import org.appwork.storage.config.ConfigEventListener;
import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.KeyHandler;

public abstract class ModifiedListener implements ConfigEventListener {

    private HashMap<String, KeyHandler<?>> handlerMap;
    private Object                         clazz;

    /**
     * @param maxChunksPerFile
     * @param maxSimultaneDownloads
     */
    public ModifiedListener(KeyHandler<?>... keyHandler) {

        handlerMap = new HashMap<String, KeyHandler<?>>();
        clazz = null;
        for (KeyHandler<?> h : keyHandler) {
            if (clazz == null) {
                clazz = h.getDeclaringClass();
            } else {
                if (clazz != h.getDeclaringClass()) { throw new IllegalStateException(h + " is not in " + clazz); }
            }
            handlerMap.put(h.getKey().toLowerCase(Locale.ENGLISH), h);
        }
    }

    public void onConfigValidatorError(Class<? extends ConfigInterface> config, Throwable validateException, KeyHandler methodHandler) {
    }


    @Override
    public void onConfigValueModified(Class<? extends ConfigInterface> config, String key, Object newValue) {
        if (config != clazz) { return; }
        KeyHandler<?> handler = handlerMap.get(key);

        if (handler != null) {
            onChanged(handler, newValue);
        }
    }

    /**
     * @param handler
     * @param newValue
     */
    protected abstract void onChanged(KeyHandler<?> handler, Object newValue);
}
