/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.controlling
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.controlling;

import java.util.EventListener;

/**
 * @author thomas
 * 
 */
public interface StateEventListener extends EventListener {

    /**
     * @param event
     */
    void onStateChange(StateEvent event);

    void onStateUpdate(StateEvent event);

}
