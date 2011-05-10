/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.ftpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.ftpserver.test;

import java.io.File;

import org.appwork.utils.net.ftpserver.FtpFile;

/**
 * @author daniel
 * 
 */
public class FtpTestFile extends FtpFile {

    private File file;

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * @param name
     * @param length
     * @param directory
     * @param lastMod
     */
    public FtpTestFile(String name, long length, boolean directory, long lastMod) {
        super(name, length, directory, lastMod);
        // TODO Auto-generated constructor stub
    }

}
