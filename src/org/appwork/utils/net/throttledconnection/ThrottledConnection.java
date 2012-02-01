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
     * get custom set limit
     * 
     * @return
     */
    public int getCustomLimit();

    /**
     * sets custom speed limit -1 : no limit 0 : use managed limit >0: use
     * custom limit
     * 
     * @param kpsLimit
     */
    public void setCustomLimit(int kpsLimit);

    /**
     * sets managed limit 0: no limit >0: use managed limit
     * 
     * @param kpsLimit
     */
    public void setManagedLimit(int kpsLimit);

    /**
     * set a new ThrottledConnectionManager
     * 
     * @param manager
     */
    public void setManager(ThrottledConnectionManager manager);

    /**
     * return how many bytes this ThrottledConnection had transfered
     * 
     * @return
     */
    public long transfered();

    /**
     * return how many bytes got transfered since last call of the function
     * 
     * @return transfered bytes
     */
    public long transferedSinceLastCall();

}
