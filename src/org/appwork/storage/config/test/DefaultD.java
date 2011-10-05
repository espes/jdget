/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.test;

import org.appwork.storage.config.defaults.AbstractDefaultFactory;

/**
 * @author Thomas
 *
 */
public class DefaultD extends AbstractDefaultFactory<Double> {

    /* (non-Javadoc)
     * @see org.appwork.storage.config.defaults.DefaultFactory#getDefaultValue()
     */
    @Override
    public Double getDefaultValue() {
        System.out.println("Create Default");
        return Math.random();
    }

}
