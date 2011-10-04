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

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

import org.appwork.storage.config.handler.StorageHandler;
import org.appwork.utils.Application;

/**
 * @author thomas
 * 
 */
public class JsonConfig {

    private static final HashMap<String, ConfigInterface> CACHE = new HashMap<String, ConfigInterface>();

    public static HashMap<String, ConfigInterface> getCache() {
        return CACHE;
    }

    /**
     * @param <T>
     * @param class1
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends ConfigInterface> T create(final Class<T> configInterface) {

        synchronized (JsonConfig.CACHE) { 

            ConfigInterface ret = JsonConfig.CACHE.get(configInterface.getName());
            if (ret == null) {

                // a static referenze in the interface itself would bypass the
                // cache and create to storagehandler. let's create a dummy
                // proxy here. and check again afterwards
                Proxy.newProxyInstance(JsonConfig.class.getClassLoader(), new Class<?>[] { configInterface }, new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // TODO Auto-generated method stub
                        return null;
                    }
                });
                ret = JsonConfig.CACHE.get(configInterface.getName());
                if (ret == null) {
         
                    ret = (T) Proxy.newProxyInstance(JsonConfig.class.getClassLoader(), new Class<?>[] { configInterface }, new StorageHandler<T>(Application.getResource("cfg/" + configInterface.getName()), configInterface));
                    JsonConfig.CACHE.put(configInterface.getName(), ret);
                }
            }
            return (T) ret;
        }

    }

    /**
     * @param name
     * @param class1
     */
    @SuppressWarnings("unchecked")
    public static <T extends ConfigInterface> T create(final File path, final Class<T> configInterface) {
        final String id = path.getAbsolutePath() + configInterface.getName();
        synchronized (JsonConfig.CACHE) {

            ConfigInterface ret = JsonConfig.CACHE.get(id);
            if (ret == null) {
                ret = (T) Proxy.newProxyInstance(JsonConfig.class.getClassLoader(), new Class<?>[] { configInterface }, new StorageHandler<T>(path, configInterface));
                JsonConfig.CACHE.put(id, ret);
            }
            return (T) ret;
        }

    }

}
