/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.awfc
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.awfc.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.appwork.utils.Hash;
import org.appwork.utils.awfc.AWFCEntry;
import org.appwork.utils.awfc.AWFCInputStream;
import org.appwork.utils.awfc.AWFCOutputStream;

/**
 * @author Daniel Wilhelm
 * 
 */
public class Tester {

    /**
     * @param args
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buffer;
        final byte[] hash = md.digest(buffer = new byte[1024]);
        final long crc32 = Hash.getCRC32(buffer);
        long a = System.currentTimeMillis();
        final AWFCOutputStream cos = new AWFCOutputStream(bos, null);
        AWFCEntry entry = null;
        boolean file = false;
        boolean noPayLoad = false;
        for (int i = 0; i < 1024 * 500; i++) {
            if (file == false) {
                entry = new AWFCEntry("test" + i, buffer.length, hash);
                cos.putNextEntry(entry, noPayLoad);
                if (noPayLoad == false) {
                    cos.write(buffer);
                }
                noPayLoad = !noPayLoad;
                file = true;
            } else {
                entry = new AWFCEntry("test" + i + "/", 0, hash);
                cos.putNextEntry(entry);
                file = false;
            }
        }
        cos.close();
        System.out.println("Size: " + bos.size() + " " + (System.currentTimeMillis() - a));
        final byte[] b = bos.toByteArray();
        // b[31312] = 9;
        // b[75312] = 9;
        final ByteArrayInputStream bis = new ByteArrayInputStream(b);
        a = System.currentTimeMillis();
        System.out.println("next");
        final AWFCInputStream cis = new AWFCInputStream(bis);
        entry = null;
        long i = 0;
        final byte[] buffer2 = new byte[2048];
        while ((entry = cis.getNextEntry()) != null) {
            i++;
            while (cis.read(buffer2) != -1) {
                ;
            }
        }
        System.out.println(System.currentTimeMillis() - a + " = " + i);
        if (true) { return; }
        a = System.currentTimeMillis();
        final ZipOutputStream zos = new ZipOutputStream(bos);
        ZipEntry zentry = null;
        file = false;
        for (i = 0; i < 1024 * 1024; i++) {
            if (file == false) {
                zentry = new ZipEntry("test" + i);
                zentry.setMethod(ZipEntry.STORED);
                zentry.setCompressedSize(buffer.length);
                zentry.setSize(buffer.length);
                zentry.setCrc(crc32);
                zos.putNextEntry(zentry);
                zos.write(buffer);
                file = true;
            } else {
                zentry = new ZipEntry("test" + i + "/");

                zos.putNextEntry(zentry);
                file = false;
            }
        }
        zos.close();
        System.out.println("Size: " + bos.size() + " " + (System.currentTimeMillis() - a));

    }
}
