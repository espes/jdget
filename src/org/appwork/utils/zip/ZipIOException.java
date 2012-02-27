/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.zip
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.zip;

import java.util.zip.ZipEntry;

import org.appwork.utils.crypto.SignatureViolation;

/**
 * @author daniel
 * 
 */
public class ZipIOException extends Exception {

    private static final long serialVersionUID = 3395166938053581997L;
    private ZipEntry          entry            = null;

    public ZipIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZipIOException(String message) {
        super(message);
    }

    public ZipIOException(String message, ZipEntry entry) {
        super(message);
        this.entry = entry;
    }

    /**
     * @param signatureViolation
     */
    public ZipIOException(Throwable e) {
        super(e);
    }

    /**
     * @param signatureViolation
     * @param entry2
     */
    public ZipIOException(Throwable e, ZipEntry entry) {
    super(e);
    this.entry=entry;
    }

    public ZipEntry getZipEntry() {
        return entry;
    }
}
