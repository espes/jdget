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

import java.lang.reflect.Proxy;

/**
 * @author thomas
 * 
 */
public class JsonConfig {

    /**
     * @param <T>
     * @param class1
     * @return
     */
    public static <T extends ConfigInterface> T create(final Class<T> configInterface) {

        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { configInterface }, new StorageHandler(configInterface));

    }

}
