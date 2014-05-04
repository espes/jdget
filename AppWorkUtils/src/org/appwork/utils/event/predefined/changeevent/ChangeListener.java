/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.circlebar
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.event.predefined.changeevent;

import java.util.EventListener;

/**
 * @author thomas
 * 
 */
public interface ChangeListener extends EventListener {

    /**
     * @param event
     */
    void onChangeEvent(ChangeEvent event);

}
