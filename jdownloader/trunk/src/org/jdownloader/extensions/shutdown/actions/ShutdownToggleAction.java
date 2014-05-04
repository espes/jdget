package org.jdownloader.extensions.shutdown.actions;

import java.awt.event.ActionEvent;

import org.appwork.storage.config.ValidationException;
import org.appwork.storage.config.events.GenericConfigEventListener;
import org.appwork.storage.config.handler.KeyHandler;
import org.jdownloader.extensions.AbstractExtensionAction;
import org.jdownloader.extensions.shutdown.CFG_SHUTDOWN;
import org.jdownloader.extensions.shutdown.ShutdownExtension;
import org.jdownloader.extensions.shutdown.translate.T;

public class ShutdownToggleAction extends AbstractExtensionAction<ShutdownExtension> implements GenericConfigEventListener<Boolean> {

    public ShutdownToggleAction() {
        super();
        setIconKey(this._getExtension().getIconKey());
        CFG_SHUTDOWN.SHUTDOWN_ACTIVE.getEventSender().addListener(this, true);
        onConfigValueModified(null, null);
    }

    public void setSelected(final boolean selected) {
        super.setSelected(selected);
        if (selected) {
            setName(T._.shutdown_toggle_action_enabled2());
        } else {
            setName(T._.shutdown_toggle_action_disabled2());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CFG_SHUTDOWN.SHUTDOWN_ACTIVE.toggle();
    }

    @Override
    public void onConfigValidatorError(KeyHandler<Boolean> keyHandler, Boolean invalidValue, ValidationException validateException) {
    }

    @Override
    public void onConfigValueModified(KeyHandler<Boolean> keyHandler, Boolean newValue) {
        setSelected(CFG_SHUTDOWN.SHUTDOWN_ACTIVE.isEnabled());
    }

}
