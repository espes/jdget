/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.logging2
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.logging2;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.annotations.AboutConfig;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultIntValue;
import org.appwork.storage.config.annotations.DescriptionForConfigEntry;
import org.appwork.storage.config.annotations.RequiresRestart;
import org.appwork.storage.config.annotations.SpinnerValidator;

/**
 * @author daniel
 * 
 */
public interface LogConfig extends ConfigInterface {

    @AboutConfig
    @DefaultIntValue(2)
    @SpinnerValidator(min = 0, max = Integer.MAX_VALUE)
    @DescriptionForConfigEntry("Automatic remove logs older than x days")
    @RequiresRestart
    int getCleanupLogsOlderThanXDays();

    @AboutConfig
    @DefaultIntValue(60)
    @SpinnerValidator(min = 30, max = Integer.MAX_VALUE)
    @DescriptionForConfigEntry("Timeout in secs after which the logger will be flushed/closed")
    @RequiresRestart
    int getLogFlushTimeout();

    @AboutConfig
    @DefaultIntValue(5)
    @SpinnerValidator(min = 1, max = Integer.MAX_VALUE)
    @DescriptionForConfigEntry("Max number of logfiles for each logger")
    @RequiresRestart
    int getMaxLogFiles();

    @AboutConfig
    @DefaultIntValue(10 * 1024 * 1024)
    @SpinnerValidator(min = 100 * 1024, max = Integer.MAX_VALUE)
    @DescriptionForConfigEntry("Max logfile size in bytes")
    @RequiresRestart
    int getMaxLogFileSize();

    @AboutConfig
    @DefaultBooleanValue(false)
    @DescriptionForConfigEntry("Enable debug mode, nearly everything will be logged!")
    @RequiresRestart
    boolean isDebugModeEnabled();

    void setCleanupLogsOlderThanXDays(int x);

    void setDebugModeEnabled(boolean b);

    void setLogFlushTimeout(int t);

    void setMaxLogFiles(int m);

    void setMaxLogFileSize(int s);
}
