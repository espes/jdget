/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dimensor
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dimensor;

import java.awt.Dimension;
import java.awt.Window;

/**
 * @author Thomas
 * 
 */
public abstract class AbstractDimensor implements DimensorInterface {
    /**
     * @param dimension
     * @param dialog
     * @return
     */
    protected Dimension validate(final Dimension dimension, final Window dialog) {

        return dimension;
    }

}
