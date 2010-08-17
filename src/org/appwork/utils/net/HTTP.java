/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * @author thomas
 * 
 */
public class HTTP {

    /**
     * Downloads a file and stores data to a file
     * 
     * @param url
     * @param cache
     * @throws IOException
     */
    public static void download(URL url, File file) throws IOException {

        final File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        file.createNewFile();
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        FileOutputStream fos = null;
        try {
            output = new BufferedOutputStream(fos = new FileOutputStream(file, true));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(true);
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);
            if (url.openConnection().getHeaderField("Content-Encoding") != null && con.getHeaderField("Content-Encoding").equalsIgnoreCase("gzip")) {
                input = new BufferedInputStream(new GZIPInputStream(con.getInputStream()));
            } else {
                input = new BufferedInputStream(con.getInputStream());
            }

            final byte[] b = new byte[1024];
            int len;
            while ((len = input.read(b)) != -1) {
                output.write(b, 0, len);
            }
        } finally {
            try {
                input.close();
            } catch (Throwable e) {
            }
            try {
                output.close();
            } catch (Throwable e) {
            }
            try {
                fos.close();
            } catch (Throwable e) {
            }
        }
    }

}
