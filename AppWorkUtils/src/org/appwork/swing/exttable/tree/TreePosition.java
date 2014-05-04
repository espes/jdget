/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.exttable.tree
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.exttable.tree;

/**
 * @author Thomas
 * 
 */
public class TreePosition<T> {

    final private T parent;

    public T getParent() {
        return parent;
    }

    public int getIndex() {
        return index;
    }

    final private int index;

    /**
     * @param rowObject
     * @param index
     */
    public TreePosition(final T rowObject, final int index) {
        parent = rowObject;
        this.index = index;
    }

}
