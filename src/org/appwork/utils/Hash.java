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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.appwork.utils.formatter.HexFormatter;

public class Hash {

    public static final String HASH_TYPE_SHA256 = "SHA-256";

    public static String       HASH_TYPE_MD5    = "md5";

    public static String       HASH_TYPE_SHA1   = "SHA-1";

    /**
     * @param download
     * @param hashType
     * @return
     */
    public static String getBytesHash(final byte[] download, final String type) {
        try {
            final MessageDigest md = MessageDigest.getInstance(type);
            final byte[] digest = md.digest(download);
            return HexFormatter.byteArrayToHex(digest);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getCRC32(final byte[] data) throws IOException {
        CheckedInputStream cis = null;
        ByteArrayInputStream bis = null;
        try {
            bis = new ByteArrayInputStream(data);
            cis = new CheckedInputStream(bis, new CRC32());
            final byte readBuffer[] = new byte[32767];
            while (cis.read(readBuffer) >= 0) {
            }
            return cis.getChecksum().getValue();
        } finally {
            try {
                cis.close();
            } catch (final Throwable e) {
            }
            try {
                bis.close();
            } catch (final Throwable e) {
            }
        }
    }

    public static long getCRC32(final File arg) throws IOException {
        FileInputStream fis = null;
        CheckedInputStream cis = null;
        try {
            fis = new FileInputStream(arg);
            cis = new CheckedInputStream(fis, new CRC32());
            final byte readBuffer[] = new byte[32767];
            while (cis.read(readBuffer) >= 0) {
            }
            return cis.getChecksum().getValue();
        } finally {
            try {
                cis.close();
            } catch (final Throwable e) {
            }
            try {
                fis.close();
            } catch (final Throwable e) {
            }
        }
    }

    public static String getFileHash(final File arg, final String type) {
        if (arg == null || !arg.exists() || arg.isDirectory()) { return null; }
        FileInputStream fis = null;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(type);
            // if (true) { throw new IOException("Any IOEXCeption"); }
            final byte[] b = new byte[32767];

            fis = new FileInputStream(arg);
            int n = 0;
            while ((n = fis.read(b)) >= 0) {
                if (n > 0) {
                    md.update(b, 0, n);
                }
            }
        } catch (final Throwable e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fis.close();
            } catch (final Throwable e) {
            }
        }
        final byte[] digest = md.digest();
        return HexFormatter.byteArrayToHex(digest);
    }

    public static String getFileHash(final File arg, final String type, final long maxHash) {
        if (arg == null || !arg.exists() || arg.isDirectory()) { return null; }
        FileInputStream fis = null;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(type);
            // if (true) { throw new IOException("Any IOEXCeption"); }
            int bufferSize = 32767;
            if (maxHash < bufferSize) {
                bufferSize = (int) maxHash;
            }
            final byte[] b = new byte[bufferSize];
            fis = new FileInputStream(arg);
            int n = 0;
            long todo = maxHash;
            while ((n = fis.read(b, 0, bufferSize)) >= 0) {
                if (n > 0) {
                    md.update(b, 0, n);
                }
                if (maxHash > 0 && n > 0) {
                    todo -= n;
                    if (todo == 0) {
                        break;
                    }
                    if (todo < bufferSize) {
                        bufferSize = (int) todo;
                    }
                }

            }
        } catch (final Throwable e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fis.close();
            } catch (final Throwable e) {
            }
        }
        final byte[] digest = md.digest();
        return HexFormatter.byteArrayToHex(digest);
    }

    public static String getMD5(final File arg) {
        return Hash.getFileHash(arg, Hash.HASH_TYPE_MD5);
    }

    public static String getMD5(final String arg) {
        return Hash.getStringHash(arg, Hash.HASH_TYPE_MD5);
    }

    public static String getSHA1(final File arg) {
        return Hash.getFileHash(arg, Hash.HASH_TYPE_SHA1);
    }

    public static String getSHA1(final String arg) {
        return Hash.getStringHash(arg, Hash.HASH_TYPE_SHA1);
    }

    /**
     * @param download
     * @return
     */
    public static String getSHA256(final byte[] download) {
        return Hash.getBytesHash(download, Hash.HASH_TYPE_SHA256);
    }

    /**
     * @param f
     * @return
     */
    public static String getSHA256(final File f) {
        return Hash.getFileHash(f, Hash.HASH_TYPE_SHA256);
    }

    /**
     * @param createPostData
     * @return
     */
    public static String getSHA256(final String createPostData) {
        return Hash.getStringHash(createPostData, Hash.HASH_TYPE_SHA256);
    }

    public static String getStringHash(final String arg, final String type) {
        try {
            final MessageDigest md = MessageDigest.getInstance(type);
            final byte[] digest = md.digest(arg.getBytes("UTF-8"));
            return HexFormatter.byteArrayToHex(digest);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
