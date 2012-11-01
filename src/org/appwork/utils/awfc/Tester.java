/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.awfc
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.awfc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipInputStream;

import org.appwork.utils.net.CountingOutputStream;
import org.appwork.utils.net.NullOutputStream;

/**
 * @author Daniel Wilhelm
 * 
 */
public class Tester {

    /**
     * @param args
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buffer;
        byte[] hash = md.digest(buffer=new byte[65000]);
        long a = System.currentTimeMillis();
        AWFCOutputStream cos = new AWFCOutputStream(bos, md);
        AWFCEntry entry = new AWFCEntry("test", buffer.length, hash);
        cos.putNextEntry(entry);
        cos.write(buffer);
        for (int i = 0; i < 2 * 1024; i++) {
            entry = new AWFCEntry("test2", buffer.length, hash);
            cos.putNextEntry(entry);
            cos.write(buffer);            
        }
        cos.close();
        System.out.println(System.currentTimeMillis() - a);
        byte[] b = bos.toByteArray();
        // b[31312] = 9;
         //b[75312] = 9;
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        a = System.currentTimeMillis();
        AWFCInputStream cis = new AWFCInputStream(bis);
        entry = null;
        long i = 0;
        
        while ((entry = cis.getNextEntry()) != null) {
            while (cis.read(buffer) != -1)
                ;

        }
        System.out.println(System.currentTimeMillis() - a);

    }
}
