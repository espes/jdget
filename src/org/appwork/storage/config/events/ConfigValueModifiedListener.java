package org.appwork.storage.config.events;

import org.appwork.storage.config.handler.KeyHandler;

public abstract class ConfigValueModifiedListener<T> implements ConfigEventListener {

    private KeyHandler<T> handler;

    /**
     * @param maxChunksPerFile
     * @param maxSimultaneDownloads
     */
    public ConfigValueModifiedListener(KeyHandler<T> keyHandler) {
        this.handler = keyHandler;

    }

    public void onConfigValidatorError(KeyHandler<?> keyHandler, Throwable validateException) {

        if (keyHandler == handler) {
            onValidationError(handler, validateException);
        }
    }

    /**
     * @param handler
     * @param validateException
     */
    protected abstract void onValidationError(KeyHandler<T> handler, Throwable validateException);

    @SuppressWarnings("unchecked")
    @Override
    public void onConfigValueModified(KeyHandler<?> keyHandler, Object newValue) {

        if (handler == keyHandler) {
            onChanged(handler, (T) newValue);
        }
    }

    /**
     * @param handler
     * @param newValue
     */
    protected abstract void onChanged(KeyHandler<T> handler, T newValue);
}
