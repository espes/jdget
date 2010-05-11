/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;

import org.appwork.utils.formatter.HexFormatter;

public class Files {
    /**
     * Returns the fileextension for a file with the given name
     * 
     * @param name
     * @return
     */
    public static String getExtension(String name) {

        final int index = name.lastIndexOf(".");
        if (index < 0) return null;
        return name.substring(index + 1).toLowerCase();

    }

    /**
     * Returns the mikmetype of the file. If unknown, it returns
     * Unknown/extension
     * 
     * @param name
     * @return
     */
    public static String getMimeType(String name) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String ret = fileNameMap.getContentTypeFor(name);
        if (ret == null) {
            ret = "unknown/" + getExtension(name);
        }
        return ret;
    }

    /**
     * 
     * Returns the hash checksum for the given file.
     * 
     * @param arg
     * @param type
     *            e.g. md5 or sha1
     * @return
     */
    public static String getHash(File arg, String type) {
        if (arg == null || !arg.exists() || arg.isDirectory()) return null;
        try {
            MessageDigest md = MessageDigest.getInstance(type);
            byte[] b = new byte[4096];
            InputStream in = new FileInputStream(arg);
            for (int n = 0; (n = in.read(b)) > -1;) {
                md.update(b, 0, n);
            }
            in.close();
            byte[] digest = md.digest();
            return HexFormatter.byteArrayToHex(digest);
        } catch (Exception e) {
            org.appwork.utils.logging.Log.exception(e);
            return null;
        }
    }

    /**
     * Returns the MD5 Hashsum for the file arg
     * 
     * @param arg
     * @return
     */
    public static String getMD5(File arg) {
        return getHash(arg, "md5");
    }

    /**
     * return all files ( and folders if includeDirectories is true ) for the
     * given files
     * 
     * @param includeDirectories
     * @param files
     * @return
     */
    public static ArrayList<File> getFiles(boolean includeDirectories, boolean includeFiles, File... files) {
        ArrayList<File> ret = new ArrayList<File>();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    if (includeDirectories) ret.add(f);
                    ret.addAll(getFiles(includeDirectories, includeFiles, f.listFiles()));
                } else if (includeFiles) {
                    ret.add(f);
                }
            }
        }
        return ret;
    }

    /**
     * delete all files/folders that are given
     * 
     * @param files
     * @throws IOException
     */
    public static void deleteRecursiv(File... files) throws IOException {
        ArrayList<File> ret = getFiles(true, true, files);
        for (int i = ret.size() - 1; i >= 0; i--) {
            File file = ret.get(i);
            if (!file.exists() || file.isFile()) ret.remove(i);
            if (file.exists() && file.isFile() && !file.delete()) throw new IOException("could not delete " + file);

        }
        for (int i = ret.size() - 1; i >= 0; i--) {
            File file = ret.get(i);
            if (file.isDirectory()) ret.remove(i);
            if (file.exists() && file.isDirectory() && !file.delete()) throw new IOException("could not delete " + file);
        }
    }

}
