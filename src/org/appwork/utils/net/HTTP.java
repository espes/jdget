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
    public static void download(URL url, File file, DownloadProgress progress) throws IOException {
        final File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        file.createNewFile();
        FileOutputStream fos = null;
        BufferedOutputStream output = null;
        BufferedInputStream input = null;
        GZIPInputStream gzi = null;
        boolean deleteInterrupted = false;
        HttpURLConnection con = null;
        try {
            output = new BufferedOutputStream(fos = new FileOutputStream(file, false));
            con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(true);
            con.setConnectTimeout(15000);
            con.setReadTimeout(30000);
            if (url.openConnection().getHeaderField("Content-Encoding") != null && con.getHeaderField("Content-Encoding").equalsIgnoreCase("gzip")) {
                input = new BufferedInputStream(gzi = new GZIPInputStream(con.getInputStream()));
            } else {
                input = new BufferedInputStream(con.getInputStream());
            }
            if (progress != null) progress.setTotal(con.getContentLength());
            final byte[] b = new byte[32767];
            int len;
            while ((len = input.read(b)) != -1) {
                if (Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }
                output.write(b, 0, len);
                if (progress != null) progress.increaseLoaded(len);
            }
        } catch (final InterruptedException e) {
            deleteInterrupted = true;
        } finally {
            try {
                input.close();
            } catch (final Exception e) {
            }
            try {
                gzi.close();
            } catch (final Exception e) {
            }
            try {
                output.close();
            } catch (final Exception e) {
            }
            try {
                fos.close();
            } catch (final Exception e) {
            }
            try {
                con.disconnect();
            } catch (final Throwable e) {
            }
            if (deleteInterrupted) {
                file.delete();
            }
        }
    }
}
