/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.zip
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.zip;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

import org.appwork.utils.IO;

/**
 * @author daniel
 * 
 */
public class ZipTest {

    /**
     * @param args
     * @throws ZipIOException 
     */
    public static void main(String[] args) throws IOException, ZipIOException {
        File test = new File("/home/daniel/fcgi-2.4.0.zip");
        byte[] byteArray = IO.readFile(test);
        ZipIOReader zip = new ZipIOReader(byteArray);
        ZipEntry[] ii = zip.getZipFiles();
        ZipIOFile fs = zip.getZipIOFileSystem();
        int i = 1;

    }
}
