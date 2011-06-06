/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config;

import java.util.EventListener;

/**
 * @author thomas
 * 
 */
public interface ConfigEventListener extends EventListener {

    public void onConfigValidatorError(ConfigInterface config, Throwable validateException, KeyHandler methodHandler);

    public void onConfigValueModified(ConfigInterface config, String key, Object newValue);
}
