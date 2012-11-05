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

import java.io.IOException;
import java.util.zip.ZipEntry;

/**
 * @author daniel
 * 
 */
public class ZipIOException extends IOException {

    private static final long serialVersionUID = 3395166938053581997L;
    private ZipEntry          entry            = null;

    public ZipIOException(final String message) {
        super(message);
    }

    public ZipIOException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ZipIOException(final String message, final ZipEntry entry) {
        super(message);
        this.entry = entry;
    }

    /**
     * @param signatureViolation
     */
    public ZipIOException(final Throwable e) {
        super(e);
    }

    /**
     * @param signatureViolation
     * @param entry2
     */
    public ZipIOException(final Throwable e, final ZipEntry entry) {
        super(e);
        this.entry = entry;
    }

    public ZipEntry getZipEntry() {
        return this.entry;
    }
}
