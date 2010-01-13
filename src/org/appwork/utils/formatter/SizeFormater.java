/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.formatter
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.formatter;

import java.text.DecimalFormat;

/**
 * @author $Author: unknown$
 * 
 */
public class SizeFormater {

    /**
     * Formats filesize from Bytes to the best readable form
     * 
     * @param fileSize
     *            in Bytes
     * @return
     */
    public static String formatBytes(long fileSize) {

        if (fileSize < 0) fileSize = 0;
        DecimalFormat c = new DecimalFormat("0.00");
        if (fileSize >= (1024 * 1024 * 1024 * 1024l)) return c.format(fileSize / (1024 * 1024 * 1024 * 1024.0)) + " TiB";
        if (fileSize >= (1024 * 1024 * 1024l)) return c.format(fileSize / (1024 * 1024 * 1024.0)) + " GiB";
        if (fileSize >= (1024 * 1024l)) return c.format(fileSize / (1024 * 1024.0)) + " MiB";
        if (fileSize >= 1024l) return c.format(fileSize / 1024.0) + " KiB";
        return fileSize + " B";

    }

}
