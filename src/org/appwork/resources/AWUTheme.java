/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.resources
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.resources;

/**
 * @author thomas
 * 
 */
public class AWUTheme extends Theme {
    private static final AWUTheme INSTANCE = new AWUTheme();

    /**
     * get the only existing instance of AWUTheme. This is a singleton
     * 
     * @return
     */
    public static AWUTheme getInstance() {
        return AWUTheme.INSTANCE;
    }

    public static AWUTheme I() {
        return AWUTheme.INSTANCE;
    }

    /**
     * Create a new instance of AWUTheme. This is a singleton class. Access the
     * only existing instance by using {@link #getInstance()}.
     */
    private AWUTheme() {
        super("org/appwork/");

    }

}
