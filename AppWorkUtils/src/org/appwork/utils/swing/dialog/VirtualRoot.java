/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import java.io.File;

/**
 * @author Thomas
 * 
 */
public class VirtualRoot extends File {

    private String name;

    /**
     * @param f
     * @param name
     */
    public VirtualRoot(final File f, final String name) {
        super(f.getAbsolutePath()); 
        this.name = name;
        

    }
    public String getName() {
       return name;
    }

}
