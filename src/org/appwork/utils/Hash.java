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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.appwork.utils.formatter.HexFormatter;

public class Hash {

    public static String HASH_TYPE_MD5 = "md5";

    public static String HASH_TYPE_SHA1 = "SHA-1";

    public static String getFileHash(File arg, String type) throws NoSuchAlgorithmException, IOException {
        if (arg == null || !arg.exists() || arg.isDirectory()) throw new NullPointerException();

        MessageDigest md = MessageDigest.getInstance(type);
        byte[] b = new byte[1024];
        InputStream in = new FileInputStream(arg);
        for (int n = 0; (n = in.read(b)) > -1;) {
            md.update(b, 0, n);

        }
        in.close();
        byte[] digest = md.digest();
        return HexFormatter.byteArrayToHex(digest);

    }

    public static String getStringHash(String arg, String type) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance(type);
        byte[] digest = md.digest(arg.getBytes());
        return HexFormatter.byteArrayToHex(digest);

    }

    public static String getMD5(String arg) throws NoSuchAlgorithmException {
        return getStringHash(arg, HASH_TYPE_MD5);
    }

    public static String getMD5(File arg) throws NoSuchAlgorithmException, IOException {
        return getFileHash(arg, HASH_TYPE_MD5);
    }

    public static String getSHA1(String arg) throws NoSuchAlgorithmException {
        return getStringHash(arg, HASH_TYPE_SHA1);
    }

    public static String getSHA1(File arg) throws NoSuchAlgorithmException, IOException {
        return getFileHash(arg, HASH_TYPE_SHA1);
    }

    public static long getCRC32(File arg) throws IOException {
        FileInputStream fis = new FileInputStream(arg);
        CheckedInputStream cis = null;
        try {
            cis = new CheckedInputStream(fis, new CRC32());
            byte readBuffer[] = new byte[4096];
            long ret = 0;
            while (cis.read(readBuffer) >= 0) {
                ret = cis.getChecksum().getValue();
            }
            return ret;
        } finally {
            if (cis != null) cis.close();
            fis.close();
        }
    }
}
