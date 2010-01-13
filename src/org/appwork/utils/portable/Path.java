/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.portable
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.portable;

import org.appwork.utils.Application;

/**
 * Path represents a filepath. it is used to keep track of pathes even the
 * install directory of the tool changes.
 * 
 * @author $Author: unknown$
 * 
 */
public class Path {
    /**
     * internal path
     */
    private String path;
    /**
     * Application root when path was stored
     */
    private String root;

    /**
     * @param jdHomeDir
     */
    public Path(String path) {
        this.path = path;
        this.root = Application.getRoot();

    }

}
