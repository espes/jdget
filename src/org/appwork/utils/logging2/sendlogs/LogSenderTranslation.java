/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.logging2.sendlogs
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.logging2.sendlogs;

import org.appwork.txtresource.Default;
import org.appwork.txtresource.Defaults;
import org.appwork.txtresource.TranslateInterface;

/**
 * @author Thomas
 * 
 */
@Defaults(lngs = { "en" })
public interface LogSenderTranslation extends TranslateInterface {

    

    

    @Default(lngs = { "en" }, values = { "Send a Bugreport" })
    String SendLogDialog_SendLogDialog_title_();

    @Default(lngs = { "en" }, values = { "When did the Problem occure? Please check all entries that may be worth considering!" })
    String SendLogDialog_layoutDialogContent_desc_();

    @Default(lngs = { "en" }, values = { "Check" })
    String LogModel_initColumns_x_();

    @Default(lngs = { "en" }, values = { "Time" })
    String LogModel_initColumns_time_();

    @Default(lngs = { "en" }, values = { "Between %s1 and %s2" })
    String LogModel_getStringValue_between_(String from, String to);

    @Default(lngs = { "en" }, values = { "Select" })
    String LogTable_onContextMenu_enable_();

    @Default(lngs = { "en" }, values = { "Unselect" })
    String LogTable_onContextMenu_disable_();

    @Default(lngs = { "en" }, values = { "Creating Log Package" })
    String LogAction_actionPerformed_zip_title_();

    @Default(lngs = { "en" }, values = { "Please wait..." })
    String LogAction_actionPerformed_wait_();

    @Default(lngs = { "en" }, values = { "Preparing your logs" })
    String LogAction_getString_uploading_();

    
}
