/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remoteapi.events
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remoteapi.events;

import org.appwork.remoteapi.events.json.EventObject;

/**
 * @author daniel
 * 
 */
public interface EventsSender {
    public void publishEvent(final EventObject event, final long[] subscriptionids);
}
