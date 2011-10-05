package org.appwork.storage.config.events;

import java.util.EventListener;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.handler.KeyHandler;

public interface GenericConfigEventListener<T> extends EventListener  {
      public void onConfigValidatorError(KeyHandler<T> keyHandler, T invalidValue, ValidationException validateException);

    public void onConfigValueModified(KeyHandler<T> keyHandler, T newValue);
}
