/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.throttledconnection
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.throttledconnection;

import java.util.List;

/**
 * @author daniel
 * 
 */
public interface ThrottledConnectionHandler {

    public void addThrottledConnection(ThrottledConnection con);

    public List<ThrottledConnection> getConnections();

    public int getLimit();

    public int getSpeed();

    public long getTraffic();

    public void removeThrottledConnection(ThrottledConnection con);

    public void setLimit(int limit);

    public int size();

}
