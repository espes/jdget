/**
 * Copyright (c) 2009 - 2014 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

/**
 * @author Thomas
 * 
 */
public interface ProgressFeedback {

    /**
     * @param length
     */
    void setBytesTotal(long length);

    /**
     * @param position
     */
    void setBytesProcessed(long position);

}
