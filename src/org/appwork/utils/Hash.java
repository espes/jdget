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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.appwork.utils.formatter.HexFormatter;

public class Hash {

    public static String HASH_TYPE_MD5  = "md5";

    public static String HASH_TYPE_SHA1 = "SHA-1";

    public static long getCRC32(final File arg) throws IOException {
        final FileInputStream fis = new FileInputStream(arg);
        CheckedInputStream cis = null;
        try {
            cis = new CheckedInputStream(fis, new CRC32());
            final byte readBuffer[] = new byte[32767];
            while (cis.read(readBuffer) >= 0) {
            }
            return cis.getChecksum().getValue();
        } finally {
            try {
                cis.close();
            } catch (Throwable e) {
            }
            try {
                fis.close();
            } catch (Throwable e) {
            }
        }
    }

    public static String getFileHash(final File arg, final String type) throws NoSuchAlgorithmException, IOException {
        if (arg == null || !arg.exists() || arg.isDirectory()) { throw new NullPointerException(); }
        final MessageDigest md = MessageDigest.getInstance(type);
        // if (true) { throw new IOException("Any IOEXCeption"); }
        final byte[] b = new byte[32767];

        final FileInputStream fis = new FileInputStream(arg);
        try {
            for (int n = 0; (n = fis.read(b)) > -1;) {
                md.update(b, 0, n);
            }
        } finally {
            try {
                fis.close();
            } catch (Throwable e) {
            }
        }
        final byte[] digest = md.digest();
        return HexFormatter.byteArrayToHex(digest);

    }

    public static String getMD5(final File arg) throws NoSuchAlgorithmException, IOException {
        return Hash.getFileHash(arg, Hash.HASH_TYPE_MD5);
    }

    public static String getMD5(final String arg) throws NoSuchAlgorithmException {
        return Hash.getStringHash(arg, Hash.HASH_TYPE_MD5);
    }

    public static String getSHA1(final File arg) throws NoSuchAlgorithmException, IOException {
        return Hash.getFileHash(arg, Hash.HASH_TYPE_SHA1);
    }

    public static String getSHA1(final String arg) throws NoSuchAlgorithmException {
        return Hash.getStringHash(arg, Hash.HASH_TYPE_SHA1);
    }

    public static String getStringHash(final String arg, final String type) throws NoSuchAlgorithmException {

        final MessageDigest md = MessageDigest.getInstance(type);
        final byte[] digest = md.digest(arg.getBytes());
        return HexFormatter.byteArrayToHex(digest);

    }
}
