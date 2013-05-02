/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dimensor
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dimensor;

import java.awt.Dimension;
import java.awt.Window;

import org.appwork.storage.config.JsonConfig;
import org.appwork.utils.Application;
import org.appwork.utils.swing.dialog.LocationStorage;

/**
 * @author Thomas
 * 
 */
public class RememberLastDimensor extends AbstractDimensor {

    private String id;

    /**
     * @param id
     */
    public RememberLastDimensor(final String id) {
        this.id = id;
    }

    /**
     * @param dialog
     * @return
     */
    public Dimension getDimension(final Window dialog) {
        final LocationStorage cfg = createConfig(dialog);
        if (cfg.isValid()) {

        return validate(new Dimension(cfg.getX(), cfg.getY()), dialog); }
        return null;
    }

    /**
     * @param dialog
     * @return
     */
    private LocationStorage createConfig(final Window dialog) {
        return JsonConfig.create(Application.getResource("cfg/" + RememberLastDimensor.class.getName() + "-" + getID(dialog)), LocationStorage.class);
    }

    protected String getID(final Window dialog) {
        if (id == null) { return dialog.toString(); }
        return id;
    }

    /**
     * @param dialog
     */
    public void onClose(final Window frame) {
        if (frame.isShowing()) {
            final LocationStorage cfg = createConfig(frame);
            cfg.setValid(true);
            cfg.setX(frame.getWidth());
            cfg.setY(frame.getHeight());
            cfg.getStorageHandler().write();
        }

    }

}
