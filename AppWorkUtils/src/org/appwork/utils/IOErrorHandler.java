/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

import java.io.File;
import java.io.InputStream;

/**
 * @author Thomas
 *
 */
public interface IOErrorHandler {

    /**
     * @param e
     * @param file
     * @param data
     */
    void onWriteException(Throwable e, File file, byte[] data);


    /**
     * @param e
     * @param out 
     * @param in 
     */
    void onCopyException(Throwable e, File in, File out);


    /**
     * @param e
     * @param fis
     */
    void onReadStreamException(Throwable e, InputStream fis);


}
