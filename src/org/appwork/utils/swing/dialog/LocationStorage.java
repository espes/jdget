/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing.dialog;

import org.appwork.storage.config.ConfigInterface;

/**
 * @author Thomas
 * 
 */
public interface LocationStorage extends ConfigInterface {

    public int getX();

    public void setX(int x);

    public int getY();

    public void setY(int y);

    public boolean isValid();

    public void setValid(boolean b);
}
