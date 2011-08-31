/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.tooltips.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.tooltips.config;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.annotations.DefaultIntValue;

/**
 * @author thomas
 * 
 */
public interface ExtTooltipSettings extends ConfigInterface {

    /**
     * @return
     */
    @DefaultIntValue(0xffffff)
    int getForegroundColor();

    void setForegroundColor(int rgb);

}
