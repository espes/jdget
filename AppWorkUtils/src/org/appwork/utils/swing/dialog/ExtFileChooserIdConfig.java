/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.annotations.CryptedStorage;

/**
 * @author Thomas
 * 
 */
@CryptedStorage(key = { 0x00, 0x02, 0x11, 0x01, 0x01, 0x54, 0x02, 0x01, 0x01, 0x01, 0x12, 0x01, 0x01, 0x01, 0x12, 0x01 })
public interface ExtFileChooserIdConfig extends ConfigInterface {

    /**
     * @return
     */
    public String getLastSelection();

    public void setLastSelection(String path);

}
