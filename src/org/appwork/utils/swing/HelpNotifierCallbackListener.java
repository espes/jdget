/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import javax.swing.JComponent;

/**
 * @author daniel
 *
 */
public interface HelpNotifierCallbackListener {

    public void onHelpNotifyShown(JComponent c);
    public void onHelpNotifyHidden(JComponent c);
}
