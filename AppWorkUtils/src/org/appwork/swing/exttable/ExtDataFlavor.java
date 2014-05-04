/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.exttable
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable;

import java.awt.datatransfer.DataFlavor;

/**
 * @author Thomas
 * 
 */
public class ExtDataFlavor<T> extends DataFlavor {
    public ExtDataFlavor(Class<? extends Object> class1) {
        super(class1, class1.getName());
    }
    /*
     * we want to reuse the Content and not serialize/deserialize it every time
     */
    @Override
    public boolean isRepresentationClassSerializable() {
        return false;
    }
}
