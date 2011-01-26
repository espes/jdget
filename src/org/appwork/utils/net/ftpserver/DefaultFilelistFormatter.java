/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.ftpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.ftpserver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * @author thomas
 * 
 */
public class DefaultFilelistFormatter implements FilelistFormatter {

    private static final String DEL = " ";

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.utils.net.ftpserver.FilelistFormatter#format(java.util.ArrayList
     * )
     */
   
    public String format(final ArrayList<FtpFile> list) {
        final StringBuilder sb = new StringBuilder();
        final SimpleDateFormat df = new SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH);
        for (final FtpFile f : list) {
            // directory or not
            sb.append(f.isDirectory() ? "d" : "-");
            // rights
            sb.append("rwxrwxrwx");
            sb.append(DefaultFilelistFormatter.DEL);
            sb.append("0");
            sb.append(DefaultFilelistFormatter.DEL);
            // group
            sb.append("test");

            sb.append(DefaultFilelistFormatter.DEL);
            // user
            sb.append("test");
            sb.append(DefaultFilelistFormatter.DEL);
            sb.append(f.getSize());
            sb.append(DefaultFilelistFormatter.DEL);
            sb.append(df.format(new Date(f.getLastModified())));
            sb.append(DefaultFilelistFormatter.DEL);
            sb.append(f.getName());
            sb.append("\r\n");

        }

        return sb.toString();
    }

}
