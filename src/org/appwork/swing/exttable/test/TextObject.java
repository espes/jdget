/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.table.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable.test;

import java.io.File;

import org.appwork.utils.Application;

/**
 * @author thomas
 * 
 */
public class TextObject {
    private String     a;
    private String     b;
    private String     c;
    private final long rand = (long) (Math.random() * 100);
    private File file;

    public TextObject(final String a, final String b, final String c) {
        super();
        this.a = a;
        this.b = b;
        this.c = c;
        file=Application.getResource(a);
    }

    public String getA() {
        return this.a;
    }

    public String getB() {
        return this.b;
    }

    public String getC() {
        return this.c;
    }

    /**
     * @return
     */
    public long getRand() {
        // TODO Auto-generated method stub
        return this.rand;
    }

    public void setA(final String a) {
        this.a = a;
    }

    public void setB(final String b) {
        this.b = b;
    }

    public void setC(final String c) {
        this.c = c;
    }

    /**
     * @param newFile
     */
    public void setFile(File newFile) {
       this.file=newFile;
    }

    public File getFile() {
        return file;
    }

}
