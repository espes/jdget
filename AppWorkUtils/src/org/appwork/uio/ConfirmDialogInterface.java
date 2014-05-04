/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing.dialog
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.uio;

import org.appwork.utils.swing.dialog.OKCancelCloseUserIODefinition;

/**
 * @author Thomas
 * 
 */
public interface ConfirmDialogInterface extends OKCancelCloseUserIODefinition {
    @Out
    public String getMessage();

    @Out
    public String getOKButtonText();
    
    @Out
    public String getCancelButtonText();
}
