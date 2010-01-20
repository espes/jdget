/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.throttledconnection
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.throttledconnection;

/**
 * @author daniel
 * 
 */
public interface ThrottledConnection {
    /**
     * set a new ThrottledConnectionManager
     * 
     * @param manager
     */
    public void setManager(ThrottledConnectionManager manager);

    /**
     * sets managed limit 0: no limit >0: use managed limit
     * 
     * @param kpsLimit
     */
    public void setManagedLimit(long kpsLimit);

    /**
     * sets custom speed limit -1 : no limit 0 : use managed limit >0: use
     * custom limit
     * 
     * @param kpsLimit
     */
    public void setCustomLimit(long kpsLimit);

    /**
     * get custom set limit
     * 
     * @return
     */
    public long getCustomLimit();

    /**
     * return how many bytes got transfered till last call of the function
     * 
     * @return
     */
    public long transferedSinceLastCall();

}
