/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config.defaults
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.test;

import java.util.ArrayList;

import org.appwork.storage.config.defaults.AbstractDefaultFactory;

/**
 * @author Thomas
 * 
 */
public class MyDefaultCreator extends AbstractDefaultFactory<ArrayList<Integer>> {

    /* (non-Javadoc)
     * @see org.appwork.storage.config.defaults.DefaultFactory#getDefaultValue()
     */
    @Override
    public ArrayList<Integer> getDefaultValue() {
     
        ArrayList<Integer> ret = new ArrayList<Integer>();
        ret.add(1);
        ret.add(2);
        ret.add(3);
        return ret;
    }

}
