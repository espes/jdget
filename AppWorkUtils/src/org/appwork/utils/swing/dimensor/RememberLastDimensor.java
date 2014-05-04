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
import org.appwork.utils.Hash;
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
        String storageID = RememberLastDimensor.class.getSimpleName() + "-" + getID(dialog);
        if (storageID.length() > 128) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < storageID.length(); i++) {
                char c = storageID.charAt(i);
                switch (c) {
                case 'U':
                case 'E':
                case 'I':
                case 'O':
                case 'A':
                case 'J':
                case 'u':
                case 'e':
                case 'i':
                case 'o':
                case 'a':
                case 'j':
                    continue;
                default:
                    sb.append(c);

                }
            }
            storageID = sb.toString();

        }
        if (storageID.length() > 128) {
            storageID = RememberLastDimensor.class.getSimpleName() + "-" + Hash.getMD5(storageID);
        }
        return JsonConfig.create(Application.getResource("cfg/" + storageID), LocationStorage.class);
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
            cfg._getStorageHandler().write();
        }

    }

}
