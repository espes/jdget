/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.synthetica
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.synthetica;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.annotations.AboutConfig;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultIntValue;
import org.appwork.storage.config.annotations.DefaultStringValue;
import org.appwork.storage.config.annotations.DescriptionForConfigEntry;
import org.appwork.storage.config.annotations.RequiresRestart;

/**
 * @author Thomas
 * 
 */
public interface SyntheticaSettings extends ConfigInterface {
    @RequiresRestart("A JDownloader Restart is Required")
    @AboutConfig
    void setLanguage(String lng);


    String getLanguage();

    @AboutConfig
    @DescriptionForConfigEntry("Font to be used. Default value is default. For foreign chars use e.g. Dialog")
    @DefaultStringValue("default")
    @RequiresRestart("A JDownloader Restart is Required")
    String getFontName();

    @AboutConfig
    @DescriptionForConfigEntry("Font scale factor in percent. Default value is 100 which means no font scaling.")
    @DefaultIntValue(100)
    @RequiresRestart("A JDownloader Restart is Required")
    int getFontScaleFactor();

    @AboutConfig
    @DescriptionForConfigEntry("Disable animation and all animation threads. Optional value. Default value is true.")
    @DefaultBooleanValue(true)
    @RequiresRestart("A JDownloader Restart is Required")
    boolean isAnimationEnabled();

    @AboutConfig
    @DescriptionForConfigEntry("Enable/disable support for system DPI settings. Default value is true.")
    @DefaultBooleanValue(true)
    @RequiresRestart("A JDownloader Restart is Required")
    boolean isFontRespectsSystemDPI();



    @AboutConfig
    @DescriptionForConfigEntry("Enable/disable window opacity on Java 6u10 and above. A value of 'false' disables window opacity which means that the window corner background which is visible for non-rectangular windows disappear. Furthermore the shadow for popupMenus makes use of real translucent window. Some themes like SyntheticaSimple2D support translucent titlePanes if opacity is disabled. The property is ignored on JRE's below 6u10. Note: It is recommended to activate this feature only if your graphics hardware acceleration is supported by the JVM - a value of 'false' can affect application performance. Default value is false which means the translucency feature is enabled")
    @DefaultBooleanValue(false)
    @RequiresRestart("A JDownloader Restart is Required")
    boolean isWindowOpaque();

    void setAnimationEnabled(boolean b);

    void setWindowOpaque(boolean b);

    void setFontName(String name);

    void setFontRespectsSystemDPI(boolean b);

    void setFontScaleFactor(int b);
    @AboutConfig
    @DescriptionForConfigEntry("Paint all labels/text with or without antialias. Default value is false.")
    @DefaultBooleanValue(false)
    @RequiresRestart("A JDownloader Restart is Required")
    boolean isTextAntiAliasEnabled();
    void setTextAntiAliasEnabled(boolean b);

}
