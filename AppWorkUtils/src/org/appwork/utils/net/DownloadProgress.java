/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

/**
 * @author daniel
 * 
 */
public class DownloadProgress {

    private long loaded = 0;
    private long total  = 0;

    public void setLoaded(long loaded) {
        this.loaded = loaded;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void increaseLoaded(long increase) {
        this.loaded += increase;
    }

    public long getLoaded() {
        return loaded;
    }

    public long getTotal() {
        return total;
    }

    /**
     * @param b
     * @param len
     */
    public void onBytesLoaded(byte[] b, int len) {
        // TODO Auto-generated method stub
        
    }

}
