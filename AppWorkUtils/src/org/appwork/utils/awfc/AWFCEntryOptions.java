/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.awfc
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.awfc;

/**
 * @author daniel
 */
public class AWFCEntryOptions {
    /**
     * 
     */
    private final AWFCEntry entry;
    /**
     * 
     */
    private final boolean   noPayLoad;

    /**
     * 
     */

    public AWFCEntryOptions(final AWFCEntry entry, final boolean noPayLoad) {
        this.entry = entry;
        this.noPayLoad = noPayLoad;
    }

    public AWFCEntry getEntry() {
        return this.entry;
    }

    public boolean hasPayLoad() {
        return !this.noPayLoad;
    }

}