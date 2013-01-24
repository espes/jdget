/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.synthetica
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.synthetica;

import org.appwork.txtresource.Default;
import org.appwork.txtresource.Defaults;
import org.appwork.txtresource.DescriptionForTranslationEntry;
import org.appwork.txtresource.TranslateInterface;

/**
 * @author Thomas
 * 
 */

@Defaults(lngs = { "en" })
public interface LanguageFileSetup extends TranslateInterface {
    @DescriptionForTranslationEntry("Always use 'default' except for languages that do not display properly (Like Chinese or Japanese).\r\n In this case, you could change the default font scale faktor (in %) here.\r\nThis Scale Faktor is ignored, if the user set up a fontscale faktor in the advanced config.\r\nA Restart is required to take effect")
    @Default(lngs = { "en" }, values = { "100" })
    String config_fontscale_faktor();

    @DescriptionForTranslationEntry("Always use 'default' except for languages that do not display properly (Like Chinese or Japanese).\r\n In this case, you should use 'Dialog'")
    @Default(lngs = { "en" }, values = { "default" })
    String config_fontname();
}
